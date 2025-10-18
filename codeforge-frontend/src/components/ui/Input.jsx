export default function Input({ label, error, ...props }) {
  return (
    <div>
      <label className="block text-sm font-medium text-text mb-2">
        {label}
      </label>
      <input
        {...props}
        className={`w-full px-3 py-2 bg-surface border rounded-md focus:outline-none focus:ring-2 focus:ring-primary ${
          error ? 'border-error' : 'border-border'
        } text-text placeholder-text-muted`}
      />
      {error && <p className="text-error text-sm mt-1">{error}</p>}
    </div>
  )
}