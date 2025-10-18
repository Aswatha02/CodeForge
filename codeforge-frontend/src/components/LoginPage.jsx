import { useState } from "react"
import LoginForm from "./LoginForm"
import RegisterForm from "./RegisterForm"

export default function LoginPage({ onLogin }) {
  const [isLogin, setIsLogin] = useState(true)

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 p-4">
      <div className="w-full max-w-md">
        {isLogin ? (
          <LoginForm 
            onSwitchToRegister={() => setIsLogin(false)} 
            onLogin={onLogin}
          />
        ) : (
          <RegisterForm 
            onSwitchToLogin={() => setIsLogin(true)}
            onLogin={onLogin}
          />
        )}
      </div>
    </div>
  )
}