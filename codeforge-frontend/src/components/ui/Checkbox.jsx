"use client"

export default function Checkbox({ label, checked, onChange, name, className = "" }) {
  return (
    <label className="flex items-center cursor-pointer">
      <input
        type="checkbox"
        name={name}
        checked={checked}
        onChange={onChange}
        className="w-4 h-4 rounded border-border bg-surface-light border cursor-pointer accent-primary"
      />
      <span className="ml-2 text-sm text-text-muted hover:text-text">{label}</span>
    </label>
  )
}