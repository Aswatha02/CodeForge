export default function Button({ children, className = "", ...props }) {
  return (
    <button
      className={`bg-primary hover:bg-primary-dark text-white py-2 px-4 rounded-md font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-surface ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}