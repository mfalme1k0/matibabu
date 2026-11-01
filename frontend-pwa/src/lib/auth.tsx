import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { ApiError, request } from "./api";

// Auth is isolated behind this context on purpose: ADR-02 plans a move from
// session+CSRF to did:web/JWT. Only this file should need to change.
//
// The backend has no "who am I" endpoint yet, so we probe:
//   /api/user  -> 200 means authenticated
//   /api/admin -> 200 means admin
// Ask backend for a real /api/me (email, roles, facility, node) when convenient.

type Status = "loading" | "anonymous" | "authenticated";
interface AuthState {
  status: Status;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const Ctx = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<Status>("loading");
  const [isAdmin, setIsAdmin] = useState(false);

  const refresh = useCallback(async () => {
    try {
      await request("/api/user");
      setStatus("authenticated");
      request("/api/admin").then(() => setIsAdmin(true)).catch(() => setIsAdmin(false));
    } catch (e) {
      // Network failure (server down): keep the current state rather than log out.
      if (e instanceof ApiError) {
        setStatus("anonymous");
        setIsAdmin(false);
      } else {
        setStatus((s) => (s === "loading" ? "anonymous" : s));
      }
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const login = async (email: string, password: string) => {
    try {
      await request("/login", { method: "POST", form: { email, password } });
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) throw new Error("Incorrect email or password.");
      throw e;
    }
    await refresh();
  };

  const logout = async () => {
    await request("/logout", { method: "POST" }).catch(() => {});
    setStatus("anonymous");
    setIsAdmin(false);
  };

  return <Ctx.Provider value={{ status, isAdmin, login, logout }}>{children}</Ctx.Provider>;
}

export function useAuth() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
