import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { VitePWA } from "vite-plugin-pwa";

// The Spring backend (the facility's local node) is proxied so the browser
// sees one origin: no CORS, and session + CSRF cookies just work.
const BACKEND = process.env.MATIBABU_BACKEND ?? "http://localhost:8080";
const proxied = ["/api", "/csrf", "/login", "/logout", "/auth"];
const proxy = Object.fromEntries(
  proxied.map((p) => [p, { target: BACKEND, changeOrigin: true }]),
);

export default defineConfig({
  server: { port: 3000, proxy },
  preview: { port: 3000, proxy },
  resolve: { alias: { "@": new URL("./src", import.meta.url).pathname } },
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      registerType: "autoUpdate",
      manifest: {
        name: "Matibabu EMR",
        short_name: "Matibabu",
        description: "Offline-first electronic medical records",
        theme_color: "#0f766e",
        background_color: "#f8fafc",
        display: "standalone",
        start_url: "/",
        icons: [
          { src: "icon.svg", sizes: "any", type: "image/svg+xml", purpose: "any maskable" },
        ],
      },
      workbox: {
        // Cache the app shell only. API responses (patient data) are never
        // cached by the service worker; offline data is a deliberate,
        // separate layer (see README: Offline roadmap).
        navigateFallback: "/index.html",
        navigateFallbackDenylist: [/^\/api/, /^\/csrf/, /^\/login/, /^\/logout/, /^\/auth/],
      },
    }),
  ],
});
