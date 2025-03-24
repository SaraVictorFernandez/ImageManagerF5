import { create } from 'zustand'
import { persist } from 'zustand/middleware'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

interface User {
  id: string
  name: string
}

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  register: (email: string, password: string, name: string) => Promise<void>
  clearError: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (username: string, password: string) => {
        set({ isLoading: true, error: null })
        try {
          const response = await fetch(`${API_BASE_URL}/api/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
          })

          if (!response.ok) {
            throw new Error('Login failed')
          }

          const data = await response.json()
          set({
            user: {
              id: data.userId,
              name: username,
            },
            token: data.token,
            isAuthenticated: true,
            isLoading: false,
          })
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : 'An error occurred',
            isLoading: false,
          })
        }
      },

      logout: () => {
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          error: null,
        })
      },

      register: async (email: string, password: string, name: string) => {
        set({ isLoading: true, error: null })
        try {
          const response = await fetch(`${API_BASE_URL}/api/users/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email:email, password:password, username:name }),
          })

          if (!response.ok) {
            throw new Error('Registration failed')
          }

          const data = await response.json()
          set({
            user: data.user,
            token: data.token,
            isAuthenticated: true,
            isLoading: false,
          })
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : 'An error occurred',
            isLoading: false,
          })
        }
      },

      clearError: () => {
        set({ error: null })
      },
    }),
    {
      name: 'auth-storage', // unique name for localStorage key
    }
  )
) 