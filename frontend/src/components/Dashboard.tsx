import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/useAuthStore'
import { useImageStore } from '../store/useImageStore'

const Dashboard = () => {
  const { isAuthenticated, logout, token } = useAuthStore()
  const { images, isLoading, error, fetchImages, uploadImage, deleteImage, updateImage } = useImageStore()
  const navigate = useNavigate()
  const [showUploadForm, setShowUploadForm] = useState(false)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [title, setTitle] = useState('')
  const [editingImage, setEditingImage] = useState<{ id: number; title: string;  } | null>(null)
  const [editFile, setEditFile] = useState<File | null>(null)
  const [editTitle, setEditTitle] = useState('')

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

  const handleEdit = (image: { id: number; title: string; }) => {
    setEditingImage(image)
    setEditTitle(image.title || '')
    setEditFile(null)
  }

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!editingImage || !token) return

    await updateImage(editingImage.id, editFile, editTitle, token)
    setEditingImage(null)
    setEditFile(null)
    setEditTitle('')
  }

  const handleEditFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setEditFile(e.target.files[0])
    }
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
                <div style={{
                  position: 'absolute',
                  top: '10px',
                  right: '10px',
                  display: 'flex',
                  gap: '8px'
                }}>
                  <button 
                    onClick={() => handleEdit(image)}
                    style={{
                      padding: '4px 8px',
                      backgroundColor: '#007bff',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer'
                    }}
                  >
                    Edit
                  </button>
                  <button 
                    onClick={() => handleDelete(image.id)}
                    style={{
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
              </div>
            ))}
          </div>
        )}
      </div>

      {editingImage && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000,
          animation: 'fadeIn 0.2s ease-in-out'
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '12px',
            width: '90%',
            maxWidth: '500px',
            boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)',
            animation: 'slideIn 0.3s ease-out',
            position: 'relative'
          }}>
            <button 
              onClick={() => setEditingImage(null)}
              style={{
                position: 'absolute',
                top: '15px',
                right: '15px',
                background: 'none',
                border: 'none',
                fontSize: '24px',
                cursor: 'pointer',
                color: '#666',
                padding: '0',
                width: '30px',
                height: '30px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                borderRadius: '50%',
                transition: 'all 0.2s ease'
              }}
              onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'}
              onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
            >
              ×
            </button>

            <h2 style={{
              margin: '0 0 20px 0',
              color: '#333',
              fontSize: '24px',
              fontWeight: '600'
            }}>
              Edit Image
            </h2>

            <form onSubmit={handleEditSubmit}>
              <div style={{ 
                marginBottom: '20px',
                padding: '20px',
                border: '2px dashed #ddd',
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleEditFileChange}
                  style={{ display: 'none' }}
                  id="edit-image-input"
                />
                <label 
                  htmlFor="edit-image-input"
                  style={{
                    display: 'block',
                    cursor: 'pointer',
                    padding: '15px',
                    backgroundColor: '#f8f9fa',
                    borderRadius: '6px',
                    transition: 'all 0.2s ease'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#e9ecef'}
                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                >
                  <div style={{ marginBottom: '10px' }}>
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginBottom: '8px' }}>
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                      <polyline points="17 8 12 3 7 8" />
                      <line x1="12" y1="3" x2="12" y2="15" />
                    </svg>
                  </div>
                  <div style={{ color: '#666', fontSize: '14px' }}>
                    {editFile ? editFile.name : 'Click to change image'}
                  </div>
                  <div style={{ color: '#999', fontSize: '12px', marginTop: '5px' }}>
                    Leave empty to keep current image
                  </div>
                </label>
              </div>

              <div style={{ marginBottom: '20px' }}>
                <label style={{
                  display: 'block',
                  marginBottom: '8px',
                  color: '#555',
                  fontSize: '14px',
                  fontWeight: '500'
                }}>
                  Title
                </label>
                <input
                  type="text"
                  placeholder="Enter image title"
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '14px',
                    transition: 'border-color 0.2s ease',
                    boxSizing: 'border-box'
                  }}
                  onFocus={(e) => e.target.style.borderColor = '#007bff'}
                  onBlur={(e) => e.target.style.borderColor = '#ddd'}
                />
              </div>

              <div style={{ 
                display: 'flex', 
                gap: '12px', 
                justifyContent: 'flex-end',
                marginTop: '30px'
              }}>
                <button 
                  type="button"
                  onClick={() => setEditingImage(null)}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#f8f9fa',
                    color: '#495057',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: '500',
                    transition: 'all 0.2s ease'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#e9ecef'}
                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#007bff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: '500',
                    transition: 'all 0.2s ease'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#0056b3'}
                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#007bff'}
                >
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default Dashboard 