import Link from "next/link";
import type {
  ButtonHTMLAttributes,
  HTMLAttributes,
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes
} from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost";
};

export function Button({
  className = "",
  variant = "primary",
  ...props
}: ButtonProps) {
  return (
    <button
      className={`button button--${variant} ${className}`.trim()}
      {...props}
    />
  );
}

export function LinkButton({
  href,
  children,
  variant = "primary"
}: {
  href: string;
  children: ReactNode;
  variant?: "primary" | "secondary" | "ghost";
}) {
  return (
    <Link className={`button button--${variant}`} href={href}>
      {children}
    </Link>
  );
}

export function Surface({
  className = "",
  ...props
}: HTMLAttributes<HTMLElement>) {
  return <section className={`surface ${className}`.trim()} {...props} />;
}

export function StatusChip({
  children,
  tone = "neutral"
}: {
  children: ReactNode;
  tone?: "neutral" | "positive" | "warm" | "attention";
}) {
  return <span className={`statusChip statusChip--${tone}`}>{children}</span>;
}

export function ProgressBar({
  value,
  label
}: {
  value: number;
  label: string;
}) {
  const safeValue = Math.max(0, Math.min(100, value));

  return (
    <div className="progressBar" aria-label={label}>
      <div className="progressBar__track">
        <span
          className="progressBar__fill"
          style={{ width: `${safeValue}%` }}
        />
      </div>
    </div>
  );
}

export function FormField({
  label,
  hint,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  hint?: string;
}) {
  return (
    <label className="formField">
      <span className="formField__label">{label}</span>
      <input className="formField__control" {...props} />
      {hint ? <span className="formField__hint">{hint}</span> : null}
    </label>
  );
}

export function SelectField({
  label,
  hint,
  children,
  ...props
}: SelectHTMLAttributes<HTMLSelectElement> & {
  label: string;
  hint?: string;
  children: ReactNode;
}) {
  return (
    <label className="formField">
      <span className="formField__label">{label}</span>
      <select className="formField__control" {...props}>
        {children}
      </select>
      {hint ? <span className="formField__hint">{hint}</span> : null}
    </label>
  );
}

export function EmptyState({
  eyebrow,
  title,
  description,
  action
}: {
  eyebrow: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <div className="emptyState">
      <span className="emptyState__eyebrow">{eyebrow}</span>
      <h3>{title}</h3>
      <p>{description}</p>
      {action ? <div className="emptyState__action">{action}</div> : null}
    </div>
  );
}

export function Skeleton({ width = "100%" }: { width?: string }) {
  return <span className="skeleton" style={{ width }} aria-hidden="true" />;
}
