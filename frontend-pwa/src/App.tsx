import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "@/lib/auth";
import Layout from "@/components/Layout";
import Login from "@/pages/Login";
import Patients from "@/pages/Patients";
import PatientNew from "@/pages/PatientNew";
import PatientDetail from "@/pages/PatientDetail";

export default function App() {
  const { status } = useAuth();

  if (status === "loading") return <p className="p-8 text-slate-500">Loading…</p>;
  if (status === "anonymous") {
    return (
      <Routes>
        <Route path="*" element={<Login />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to="/patients" replace />} />
        <Route path="patients" element={<Patients />} />
        <Route path="patients/new" element={<PatientNew />} />
        <Route path="patients/:id" element={<PatientDetail />} />
        <Route path="*" element={<Navigate to="/patients" replace />} />
      </Route>
    </Routes>
  );
}
