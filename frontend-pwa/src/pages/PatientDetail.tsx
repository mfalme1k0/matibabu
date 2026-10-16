import { Link, useNavigate, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { patientsApi } from "@/lib/patients";
import { useAuth } from "@/lib/auth";
import { Button, Card, ErrorText } from "@/components/ui";

export default function PatientDetail() {
  const { id = "" } = useParams();
  const nav = useNavigate();
  const qc = useQueryClient();
  const { isAdmin } = useAuth();
  const q = useQuery({ queryKey: ["patients", id], queryFn: () => patientsApi.get(id) });
  const del = useMutation({
    mutationFn: () => patientsApi.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["patients"] });
      nav("/patients");
    },
  });

  if (q.isLoading) return <p className="text-slate-500">Loading…</p>;
  if (q.isError) return <ErrorText>{(q.error as Error).message}</ErrorText>;
  const p = q.data!;

  const rows: [string, string][] = [
    ["Date of birth", p.dateOfBirth],
    ["Gender", p.gender],
    ["Phone", p.phoneNumber],
    ["Address", p.address],
    ["Registered", new Date(p.createdAt).toLocaleString()],
  ];

  return (
    <div className="space-y-4">
      <Link to="/patients" className="text-sm text-teal-700">
        ← Patients
      </Link>
      <Card>
        <h1 className="mb-4 text-2xl font-bold">
          {p.firstName} {p.lastName}
        </h1>
        <dl className="grid gap-x-6 gap-y-3 sm:grid-cols-2">
          {rows.map(([k, v]) => (
            <div key={k}>
              <dt className="text-xs uppercase tracking-wide text-slate-500">{k}</dt>
              <dd>{v}</dd>
            </div>
          ))}
        </dl>
      </Card>
      <Card>
        <h2 className="font-semibold">Encounters</h2>
        <p className="text-sm text-slate-500">Coming next: start, discharge and cancel encounters, then the medical record.</p>
      </Card>
      {isAdmin && (
        <Button
          variant="danger"
          disabled={del.isPending}
          onClick={() => confirm("Delete this patient? This cannot be undone.") && del.mutate()}
        >
          Delete patient
        </Button>
      )}
      {del.isError && <ErrorText>{(del.error as Error).message}</ErrorText>}
    </div>
  );
}
