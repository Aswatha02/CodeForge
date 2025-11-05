import React from "react"
import ReactDOM from "react-dom/client"
import App from "./App.jsx"
import "./index.css"

console.log('🚀 main.jsx loaded')
console.log('📦 React version:', React.version)
console.log('🎯 Root element:', document.getElementById("root"))

try {
  const rootElement = document.getElementById("root")
  if (!rootElement) {
    console.error('❌ Root element not found!')
  } else {
    console.log('✅ Root element found, creating React root...')
    const root = ReactDOM.createRoot(rootElement)
    console.log('✅ React root created, rendering App...')
    root.render(
      <React.StrictMode>
        <App />
      </React.StrictMode>
    )
    console.log('✅ App render called')
  }
} catch (error) {
  console.error('❌ Error during React initialization:', error)
}