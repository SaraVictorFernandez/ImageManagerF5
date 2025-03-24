import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/useAuthStore'

const Dashboard = () => {
  const { isAuthenticated, logout } = useAuthStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    }
  }, [isAuthenticated, navigate])

  const handleLogout = () => {
    logout()
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <div className="dashboard">
      <div style={{ 
        position: 'absolute', 
        top: '20px', 
        right: '20px' 
      }}>
        <button 
          onClick={handleLogout}
          style={{
            padding: '8px 16px',
            backgroundColor: '#dc3545',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Logout
        </button>
      </div>
      <h1>Dashboard</h1>
      <p>Welcome to your dashboard!</p>
    </div>
  )
}

export default Dashboard 