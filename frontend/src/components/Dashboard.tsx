import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/useAuthStore'
import { useImageStore } from '../store/useImageStore'

const Dashboard = () => {
  const { isAuthenticated, logout, token } = useAuthStore()
  const { images, isLoading, error, fetchImages, uploadImage, deleteImage } = useImageStore()
  const navigate = useNavigate()
  const [showUploadForm, setShowUploadForm] = useState(false)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [title, setTitle] = useState('')

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    }
  }, [isAuthenticated, navigate])

  useEffect(() => {
    if (token) {
      fetchImages(token)
    }
  }, [token, fetchImages]);

  const handleLogout = () => {
    logout()
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0])
    }
  }

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedFile || !token) return

    await uploadImage(selectedFile, title, token)
    setSelectedFile(null)
    setTitle('')
    setShowUploadForm(false)
  }

  const handleDelete = async (id: number) => {
    if (!token) return
    await deleteImage(id, token)
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <div className="dashboard" style={{
      minHeight: '100%',
      width: '100%',
      position: 'relative',
      paddingTop: '40px'
    }}>
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

      <div className='dashboard-content' style={{ 
        padding: '20px'
      }}>
        <h1>Dashboard</h1>
        
        <button
          onClick={() => setShowUploadForm(!showUploadForm)}
          style={{
            padding: '8px 16px',
            backgroundColor: '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            marginBottom: '20px'
          }}
        >
          {showUploadForm ? 'Cancel Upload' : 'Upload New Image'}
        </button>

        {showUploadForm && (
          <form onSubmit={handleUpload} style={{ marginBottom: '20px', padding: '20px', border: '1px solid #ddd', borderRadius: '4px' }}>
            <div style={{ marginBottom: '10px' }}>
              <input
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                required
                style={{ marginBottom: '10px' }}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <input
                type="text"
                placeholder="Title (optional)"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                style={{ width: '100%', padding: '8px' }}
              />
            </div>
            <button 
              type="submit" 
              disabled={!selectedFile}
              style={{
                padding: '8px 16px',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Upload Image
            </button>
          </form>
        )}

        {isLoading ? (
          <div>Loading images...</div>
        ) : error ? (
          <div style={{ color: 'red' }}>Error: {error}</div>
        ) : (
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', 
            gap: '20px' 
          }}>
            {images.map((image) => (
              <div 
                key={image.id} 
                style={{ 
                  border: '1px solid #ddd', 
                  borderRadius: '4px', 
                  padding: '10px',
                  position: 'relative'
                }}
              >
                <img 
                  src={image.url} 
                  alt={image.title || image.originalFilename}
                  style={{ 
                    width: '100%', 
                    height: '200px', 
                    objectFit: 'cover',
                    borderRadius: '4px'
                  }}
                />
                <div style={{ marginTop: '10px' }}>
                  <h3>{image.title || 'Untitled'}</h3>
                </div>
                <button 
                  onClick={() => handleDelete(image.id)}
                  style={{
                    position: 'absolute',
                    top: '10px',
                    right: '10px',
                    padding: '4px 8px',
                    backgroundColor: '#dc3545',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Delete
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default Dashboard 