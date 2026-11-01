import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from "react";
import { forwardRef } from "react";

const cx = (...c: (string | false | undefined)[]) => c.filter(Boolean).join(" ");

export function Button({
  variant = "primary",
  className,
  ...p
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: "primary" | "ghost" | "danger" }) {
  const styles = {
    primary: "bg-teal-700 text-white hover:bg-teal-800",
    ghost: "bg-white text-slate-700 border border-slate-300 hover:bg-slate-50",
    danger: "bg-red-600 text-white hover:bg-red-700",
  }[variant];
  return (
    <button
      {...p}
      className={cx(
        "inline-flex min-h-11 items-center justify-center rounded-lg px-4 text-sm font-medium transition disabled:opacity-50",
        styles,
        className,
      )}
    />
  );
}

export function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-slate-700">{label}</span>
      {children}
      {error && <span className="mt-1 block text-sm text-red-600">{error}</span>}
    </label>
  );
}

const inputCls =
  "min-h-11 w-full rounded-lg border border-slate-300 bg-white px-3 text-base focus:border-teal-600 focus:outline-none focus:ring-2 focus:ring-teal-600/20";

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>((p, ref) => (
  <input ref={ref} {...p} className={cx(inputCls, p.className)} />
));

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>((p, ref) => (
  <select ref={ref} {...p} className={cx(inputCls, p.className)} />
));

export const Card = ({ children, className }: { children: ReactNode; className?: string }) => (
  <div className={cx("rounded-xl border border-slate-200 bg-white p-5 shadow-sm", className)}>{children}</div>
);

export const ErrorText = ({ children }: { children: ReactNode }) => (
  <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
    {children}
  </p>
);
