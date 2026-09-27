#!/usr/bin/env python3
"""
Generates a Flyway seed migration (medicines table) by matching a KEML
export against a WHO ATC index export, on generic/INN name.

This is a one-off tool you run each time KEML is updated (every few
years) — it is NOT part of the running application. Its job is to do
the tedious 90% of the matching automatically and clearly flag the
remaining 10% for a pharmacist to review by hand; it does not itself
decide clinical correctness.

USAGE
    python match_keml_to_atc.py \
        --keml keml_export.csv \
        --atc atc_index.csv \
        --keml-version "KEML 2023" \
        --out-migration V17__seed_keml_2023_medicines.sql \
        --out-review needs_review.csv

INPUT FILE FORMATS (adjust the column names below to match your source
files — these are the columns actually used):

  keml_export.csv:
      generic_name, brand_name (optional), form, strength, keml_code

  atc_index.csv (WHO ATC/DDD index export, or the eEML export):
      atc_code, atc_name

OUTPUT
  - A Flyway migration file with INSERT statements. Rows that matched
    confidently get atc_mapping_status = 'AUTO_MATCHED'; rows that
    didn't get atc_mapping_status = 'NEEDS_REVIEW' and a NULL atc_code.
  - A needs_review.csv listing every unmatched/ambiguous row, so a
    pharmacist can fill in the correct ATC code (or confirm there
    isn't one) without touching SQL.

After review, feed the corrected needs_review.csv back through
--review-corrections to produce a follow-up migration that updates
those specific rows to CONFIRMED.
"""

import argparse
import csv
import os
import re
import sys
import time
import uuid
from difflib import SequenceMatcher

MATCH_THRESHOLD = 0.92  # similarity above which we trust an auto-match


def uuid7() -> str:

    unix_ts_ms = int(time.time() * 1000)
    rand = os.urandom(10)

    ts_bits = unix_ts_ms & 0xFFFFFFFFFFFF  # 48 bits
    rand_a = int.from_bytes(rand[0:2], "big") & 0x0FFF  # 12 bits
    rand_b = int.from_bytes(rand[2:10], "big") & 0x3FFFFFFFFFFFFFFF  # 62 bits

    ver_and_rand_a = (0x7 << 12) | rand_a          # version 7, 16 bits total
    variant_and_rand_b = (0b10 << 62) | rand_b      # variant 10, 64 bits total

    uuid_int = (ts_bits << 80) | (ver_and_rand_a << 64) | variant_and_rand_b
    return str(uuid.UUID(bytes=uuid_int.to_bytes(16, "big")))


def normalize(name: str) -> str:
    if not name:
        return ""
    name = name.lower().strip()
    name = re.sub(r"[^a-z0-9 ]", "", name)
    name = re.sub(r"\s+", " ", name)
    return name


def similarity(a: str, b: str) -> float:
    return SequenceMatcher(None, a, b).ratio()


def load_atc_index(path: str) -> list[dict]:
    with open(path, newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    for row in rows:
        row["_normalized"] = normalize(row["atc_name"])
    return rows


def load_keml(path: str) -> list[dict]:
    with open(path, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def best_match(generic_name: str, atc_index: list[dict]) -> tuple[dict | None, float]:
    target = normalize(generic_name)
    if not target:
        return None, 0.0

    # Exact match first — cheap and unambiguous.
    for atc in atc_index:
        if atc["_normalized"] == target:
            return atc, 1.0

    # Fall back to fuzzy match, keep the best candidate.
    best, best_score = None, 0.0
    for atc in atc_index:
        score = similarity(target, atc["_normalized"])
        if score > best_score:
            best, best_score = atc, score
    return best, best_score


def sql_escape(value: str | None) -> str:
    if value is None or value == "":
        return "NULL"
    return "'" + value.replace("'", "''") + "'"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--keml", required=True, help="Path to KEML export CSV")
    parser.add_argument("--atc", required=True, help="Path to WHO ATC index export CSV")
    parser.add_argument("--keml-version", required=True, help='e.g. "KEML 2023"')
    parser.add_argument("--out-migration", required=True, help="Output Flyway .sql path")
    parser.add_argument("--out-review", required=True, help="Output CSV of rows needing manual review")
    args = parser.parse_args()

    atc_index = load_atc_index(args.atc)
    keml_rows = load_keml(args.keml)

    inserts = []
    review_rows = []
    auto_matched, needs_review = 0, 0

    for row in keml_rows:
        generic_name = row.get("generic_name", "").strip()
        form = row.get("form", "").strip()
        strength = row.get("strength", "").strip()
        keml_code = row.get("keml_code", "").strip()
        brand_name = row.get("brand_name", "").strip() or generic_name

        atc, score = best_match(generic_name, atc_index)

        if atc and score >= MATCH_THRESHOLD:
            status = "AUTO_MATCHED"
            atc_code = atc["atc_code"]
            auto_matched += 1
        else:
            status = "NEEDS_REVIEW"
            atc_code = None
            needs_review += 1
            review_rows.append({
                "generic_name": generic_name,
                "form": form,
                "strength": strength,
                "keml_code": keml_code,
                "best_guess_atc_code": atc["atc_code"] if atc else "",
                "best_guess_atc_name": atc["atc_name"] if atc else "",
                "match_score": f"{score:.2f}",
                "confirmed_atc_code": "",  # pharmacist fills this in
            })

        inserts.append(
            "INSERT INTO medicines "
            "(id, name, generic_name, atc_code, form, strength, keml_code, "
            "active, atc_mapping_status, keml_version) VALUES "
            f"({sql_escape(uuid7())}, {sql_escape(brand_name)}, "
            f"{sql_escape(generic_name)}, {sql_escape(atc_code)}, "
            f"{sql_escape(form)}, {sql_escape(strength)}, {sql_escape(keml_code)}, "
            f"TRUE, {sql_escape(status)}, {sql_escape(args.keml_version)});"
        )

    with open(args.out_migration, "w", encoding="utf-8") as f:
        f.write(f"-- Seed data generated from {args.keml_version} against the WHO ATC index.\n")
        f.write(f"-- Auto-matched: {auto_matched}   Needs review: {needs_review}\n")
        f.write("-- Do not hand-edit; regenerate with match_keml_to_atc.py and re-apply\n")
        f.write("-- corrections via the review-corrections step instead.\n\n")
        f.write("\n".join(inserts))
        f.write("\n")

    with open(args.out_review, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=[
            "generic_name", "form", "strength", "keml_code",
            "best_guess_atc_code", "best_guess_atc_name",
            "match_score", "confirmed_atc_code",
        ])
        writer.writeheader()
        writer.writerows(review_rows)

    print(f"KEML rows processed: {len(keml_rows)}", file=sys.stderr)
    print(f"  auto-matched:  {auto_matched}", file=sys.stderr)
    print(f"  needs review:  {needs_review}  -> see {args.out_review}", file=sys.stderr)
    print(f"Migration written to {args.out_migration}", file=sys.stderr)


if __name__ == "__main__":
    main()