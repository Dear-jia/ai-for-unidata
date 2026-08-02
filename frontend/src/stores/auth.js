import { defineStore } from 'pinia'
import { authApi } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('uni_token') || '',
    user: JSON.parse(localStorage.getItem('uni_user') || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isVip: (state) => state.user?.vip === true
  },
  actions: {
    setAuth(data) {
      this.token = data.token
      this.user = data.user
      localStorage.setItem('uni_token', data.token)
      localStorage.setItem('uni_user', JSON.stringify(data.user))
    },
    setUser(user) {
      this.user = user
      localStorage.setItem('uni_user', JSON.stringify(user))
    },
    async login(form) {
      const data = await authApi.login(form)
      this.setAuth(data)
      return data.user
    },
    async register(form) {
      const data = await authApi.register(form)
      this.setAuth(data)
      return data.user
    },
    async refreshUser() {
      const user = await authApi.me()
      this.setUser(user)
      return user
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('uni_token')
      localStorage.removeItem('uni_user')
    }
  }
})
