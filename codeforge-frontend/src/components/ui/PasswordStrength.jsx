export default function PasswordStrength({ password }) {
  const calculateStrength = (pwd) => {
    let strength = 0
    if (pwd.length >= 8) strength++
    if (pwd.length >= 12) strength++
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++
    if (/\d/.test(pwd)) strength++
    if (/[^a-zA-Z\d]/.test(pwd)) strength++
    return strength
  }

  const strength = calculateStrength(password)
  const strengthLabels = ["Very Weak", "Weak", "Fair", "Good", "Strong", "Very Strong"]
  const strengthColors = ["bg-error", "bg-warning", "bg-warning", "bg-primary", "bg-success", "bg-success"]

  return (
    <div className="mt-2">
      <div className="flex gap-1 mb-1">
        {[...Array(5)].map((_, i) => (
          <div
            key={i}
            className={`h-1 flex-1 rounded-full ${i < strength ? strengthColors[strength - 1] : "bg-surface-light"}`}
          />
        ))}
      </div>
      <p className={`text-xs font-medium ${strengthColors[strength - 1].replace("bg-", "text-")}`}>
        {strengthLabels[strength - 1]}
      </p>
    </div>
  )
}