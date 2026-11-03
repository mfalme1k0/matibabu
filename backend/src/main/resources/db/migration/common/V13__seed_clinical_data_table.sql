-- Starter set of commonly prescribed medicines from the Kenya
-- Essential Medicines List (KEML), coded against the WHO ATC
-- classification. This is a small seed to unblock development and
-- testing — the full KEML should be imported as a follow-up (see
-- docs/decisions).

INSERT INTO medicines (id, name, generic_name, atc_code, form, strength, keml_code, active) VALUES
                                                                                                ('01973bce-0001-73c0-8000-000000000001', 'Paracetamol', 'Paracetamol', 'N02BE01', 'tablet', '500mg', 'KEML-N02BE01', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000002', 'Amoxicillin', 'Amoxicillin', 'J01CA04', 'capsule', '500mg', 'KEML-J01CA04', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000003', 'Ibuprofen', 'Ibuprofen', 'M01AE01', 'tablet', '400mg', 'KEML-M01AE01', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000004', 'Coartem', 'Artemether/Lumefantrine', 'P01BF01', 'tablet', '20mg/120mg', 'KEML-P01BF01', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000005', 'Metformin', 'Metformin', 'A10BA02', 'tablet', '500mg', 'KEML-A10BA02', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000006', 'Amlodipine', 'Amlodipine', 'C08CA01', 'tablet', '5mg', 'KEML-C08CA01', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000007', 'Ceftriaxone', 'Ceftriaxone', 'J01DD04', 'injection', '1g', 'KEML-J01DD04', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000008', 'Salbutamol', 'Salbutamol', 'R03AC02', 'inhaler', '100mcg/dose', 'KEML-R03AC02', TRUE),
                                                                                                ('01973bce-0001-73c0-8000-000000000009', 'ORS', 'Oral Rehydration Salts', 'A07CA', 'oral solution', 'standard sachet', 'KEML-A07CA', TRUE);
