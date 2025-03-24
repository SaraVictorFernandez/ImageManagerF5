import { create } from 'zustand'
import { persist } from 'zustand/middleware'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

interface Image {
  id: number
  filename: string
  originalFilename: string
  contentType: string
  fileSize: number
  width: number
  height: number
  title: string
  description: string
  uploadDate: string
  lastModifiedDate: string
  url: string
}

interface ImageState {
  images: Image[]
  isLoading: boolean
  error: string | null
  selectedImage: Image | null
  fetchImages: (token: string) => Promise<void>
  uploadImage: (file: File, title: string, token: string) => Promise<void>
  deleteImage: (id: number, token: string) => Promise<void>
  updateImage: (id: number, file: File | null, title: string | null, token: string) => Promise<void>
  setSelectedImage: (image: Image | null) => void
  clearError: () => void
}

export const useImageStore = create<ImageState>()(
  persist(
    (set, get) => ({
      images: [],
      isLoading: false,
      error: null,
      selectedImage: null,

      fetchImages: async (token: string) => {
        set({ isLoading: true, error: null })
        try {
          const response = await fetch(`${API_BASE_URL}/api/images`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })
          if (!response.ok) throw new Error('Failed to fetch images')
          const data = await response.json()
          set({ images: data, isLoading: false })
        } catch (err) {
          set({
            error: err instanceof Error ? err.message : 'Failed to fetch images',
            isLoading: false
          })
        }
      },

      uploadImage: async (file: File, title: string, token: string) => {
        set({ isLoading: true, error: null })
        try {
          const formData = new FormData()
          formData.append('image', file)
          if (title) formData.append('title', title)

          const response = await fetch(`${API_BASE_URL}/api/images`, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`
            },
            body: formData
          })
          if (!response.ok) throw new Error('Failed to upload image')
          console.log('Perfectly uploaded')
          // Refresh images after upload
          await get().fetchImages(token)
        } catch (err) {
          set({
            error: err instanceof Error ? err.message : 'Failed to upload image',
            isLoading: false
          })
        }
      },

      deleteImage: async (id: number, token: string) => {
        set({ isLoading: true, error: null })
        try {
          const response = await fetch(`${API_BASE_URL}/api/images/${id}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })
          if (!response.ok) throw new Error('Failed to delete image')
          
          // Refresh images after delete
          await get().fetchImages(token)
        } catch (err) {
          set({
            error: err instanceof Error ? err.message : 'Failed to delete image',
            isLoading: false
          })
        }
      },

      updateImage: async (id: number, file: File | null, title: string | null, token: string) => {
        set({ isLoading: true, error: null })
        try {
          const formData = new FormData()
          if (file) formData.append('image', file)
          if (title) formData.append('title', title)
          
          const response = await fetch(`${API_BASE_URL}/api/images/${id}`, {
            method: 'PATCH',
            headers: {
              'Authorization': `Bearer ${token}`
            },
            body: formData
          })
          if (!response.ok) throw new Error('Failed to update image')
          
          // Refresh images after update
          await get().fetchImages(token)
        } catch (err) {
          set({
            error: err instanceof Error ? err.message : 'Failed to update image',
            isLoading: false
          })
        }
      },

      setSelectedImage: (image: Image | null) => {
        set({ selectedImage: image })
      },

      clearError: () => {
        set({ error: null })
      }
    }),
    {
      name: 'image-storage',
      partialize: (state) => ({ images: state.images }) // Only persist the images array
    }
  )
) 