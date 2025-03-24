import './App.css'
import { AuthForm } from './components/AuthForm'
import Dashboard from './components/Dashboard'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'

function App() {
  return (
    <Router>
      <div >
        <Routes>
          <Route path="/login" element={<AuthForm />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/" element={<Navigate to="/login" replace />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App
