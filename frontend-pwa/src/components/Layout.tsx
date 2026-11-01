import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "@/lib/auth";
import { useOnline } from "@/lib/useOnline";
import { Button } from "./ui";

export default function Layout() {
  const { logout, isAdmin } = useAuth();
  const online = useOnline();

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center gap-6 px-4 py-3">
          <span className="text-lg font-bold text-teal-700">Matibabu</span>
          <nav className="flex gap-4 text-sm">
            <NavLink
              to="/patients"
              className={({ isActive }) => (isActive ? "font-semibold text-teal-700" : "text-slate-600")}
            >
              Patients
            </NavLink>
          </nav>
          <div className="ml-auto flex items-center gap-3">
            <span
              className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                online ? "bg-emerald-50 text-emerald-700" : "bg-amber-100 text-amber-800"
              }`}
              title="Whether this device has a network connection"
            >
              {online ? "Online" : "Offline"}
            </span>
            {isAdmin && <span className="text-xs text-slate-500">Admin</span>}
            <Button variant="ghost" onClick={logout}>
              Sign out
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
