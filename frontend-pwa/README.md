# Matibabu Web (PWA)

React + TypeScript + Vite + Tailwind 4, installable as a PWA. Talks to the
facility's local Spring Boot node.

## Run

```bash
npm install
npm run dev        # http://localhost:3000, proxies to the backend
```

Backend defaults to `http://localhost:8080`; override with
`MATIBABU_BACKEND=http://192.168.1.10:8080 npm run dev`.

In dev the proxy makes the browser see a single origin, so no CORS setup and
the session + CSRF cookies work. For production, serve `dist/` from the same
origin as the API (e.g. Spring static resources or a reverse proxy).

## Structure

- `src/lib/api.ts`      one `fetch` wrapper: cookies, CSRF (`X-XSRF-TOKEN`), `{error}` handling
- `src/lib/auth.tsx`    session auth behind a context (swap for did:web/JWT per ADR-02 here only)
- `src/lib/patients.ts` patient endpoints; add one file per backend area
- `src/pages/`          screens; `src/components/ui.tsx` shared primitives

## Offline notes

The service worker caches the **app shell only**. API responses (patient data)
are deliberately not cached. Installing a PWA needs HTTPS or `localhost`, so a
facility LAN deployment needs a local certificate.

## Roadmap

1. Encounters (start/discharge/cancel, department picker)
2. Medical record (vitals, observations, diagnoses, treatments, medicine search)
3. Referrals, facility/department admin
4. Offline write queue once ADR-08 sync design settles
