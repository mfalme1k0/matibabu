import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { ApiError } from "@/lib/api";
import { patientsApi } from "@/lib/patients";
import { Button, Card, Input } from "@/components/ui";

export default function Patients() {
  const [page, setPage] = useState(0);
  const [phone, setPhone] = useState("");
  const [searched, setSearched] = useState("");

  const list = useQuery({ queryKey: ["patients", page], queryFn: () => patientsApi.list(page) });
  const hit = useQuery({
    queryKey: ["patients", "phone", searched],
    queryFn: () => patientsApi.byPhone(searched),
    enabled: !!searched,
    retry: false,
  });

  const onSearch = (e: FormEvent) => {
    e.preventDefault();
    setSearched(phone.trim());
  };
  const notFound = hit.error instanceof ApiError && hit.error.status === 404;

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Patients</h1>
        <Link to="/patients/new">
          <Button>Register patient</Button>
        </Link>
      </div>

      <form onSubmit={onSearch} className="flex gap-2">
        <Input
          inputMode="tel"
          placeholder="Find by phone, e.g. +254712345678"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
        />
        <Button type="submit">Search</Button>
        {searched && (
          <Button
            type="button"
            variant="ghost"
            onClick={() => {
              setSearched("");
              setPhone("");
            }}
          >
            Clear
          </Button>
        )}
      </form>

      {searched ? (
        <Card>
          {hit.isLoading && <p className="text-slate-500">Searching…</p>}
          {notFound && <p className="text-slate-600">No patient with that phone number.</p>}
          {hit.isError && !notFound && <p className="text-red-600">{(hit.error as Error).message}</p>}
          {hit.data && <Row p={hit.data} />}
        </Card>
      ) : (
        <Card className="divide-y divide-slate-100 p-0">
          {list.isLoading && <p className="p-5 text-slate-500">Loading…</p>}
          {list.isError && <p className="p-5 text-red-600">{(list.error as Error).message}</p>}
          {list.data?.content.length === 0 && <p className="p-5 text-slate-500">No patients registered yet.</p>}
          {list.data?.content.map((p) => <Row key={p.id} p={p} />)}
        </Card>
      )}

      {!searched && list.data && list.data.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm text-slate-600">
          <Button variant="ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </Button>
          <span>
            Page {page + 1} of {list.data.totalPages}
          </span>
          <Button variant="ghost" disabled={list.data.last} onClick={() => setPage(page + 1)}>
            Next
          </Button>
        </div>
      )}
    </div>
  );
}

function Row({ p }: { p: import("@/lib/types").Patient }) {
  return (
    <Link to={`/patients/${p.id}`} className="flex items-center justify-between px-5 py-3 hover:bg-slate-50">
      <div>
        <div className="font-medium">
          {p.firstName} {p.lastName}
        </div>
        <div className="text-sm text-slate-500">
          {p.phoneNumber} · {p.dateOfBirth}
        </div>
      </div>
      <span className="text-sm text-teal-700">Open →</span>
    </Link>
  );
}
