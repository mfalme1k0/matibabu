// Single HTTP entry point for the Spring backend.
// - Sends the session cookie (same-origin via the dev/preview proxy).
// - Adds the CSRF header on state-changing requests (CookieCsrfTokenRepository).
// - Normalises errors from GlobalExceptionHandler ({ "error": "..." }).

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

function readCookie(name: string): string | null {
  const m = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return m ? decodeURIComponent(m[1]) : null;
}

async function csrfToken(forceRefresh = false): Promise<string> {
  if (!forceRefresh) {
    const existing = readCookie("XSRF-TOKEN");
    if (existing) return existing;
  }
  const res = await fetch("/csrf", { credentials: "include" });
  const body = await res.json();
  return body.token ?? readCookie("XSRF-TOKEN") ?? "";
}

type Options = { method?: string; json?: unknown; form?: Record<string, string> };

export async function request<T>(path: string, opts: Options = {}, retried = false): Promise<T> {
  const method = opts.method ?? "GET";
  const headers: Record<string, string> = { Accept: "application/json" };
  let body: BodyInit | undefined;

  if (opts.json !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(opts.json);
  } else if (opts.form) {
    headers["Content-Type"] = "application/x-www-form-urlencoded";
    body = new URLSearchParams(opts.form).toString();
  }
  if (method !== "GET") headers["X-XSRF-TOKEN"] = await csrfToken(retried);

  const res = await fetch(path, { method, headers, body, credentials: "include" });

  if (res.status === 403 && method !== "GET" && !retried) {
    return request<T>(path, opts, true); // stale CSRF token: refresh once
  }
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      message = data.error ?? data.message ?? message;
    } catch {
      /* empty or non-JSON body */
    }
    throw new ApiError(res.status, message);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  try {
    return JSON.parse(text) as T;
  } catch {
    return text as unknown as T; // e.g. /api/user returns plain text
  }
}
