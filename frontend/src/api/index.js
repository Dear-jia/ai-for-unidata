import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  me: () => request.get('/auth/me'),
  vipStatus: () => request.get('/auth/vip-status')
}

export const userApi = {
  profile: () => request.get('/user/me'),
  updateProfile: (data) => request.put('/user/profile', data),
  changePassword: (data) => request.put('/user/password', data)
}

export const publicApi = {
  home: () => request.get('/public/home'),
  schools: (params) => request.get('/public/schools', { params }),
  schoolDetail: (id) => request.get(`/public/schools/${id}`),
  scoreLines: (params) => request.get('/public/scorelines', { params }),
  articles: (params) => request.get('/public/articles', { params }),
  articleDetail: (id) => request.get(`/public/articles/${id}`),
  activities: (params) => request.get('/public/activities', { params })
}

export const memberApi = {
  createOrder: (data) => request.post('/member/orders', data),
  activateOrder: (orderNo) => request.post(`/member/orders/${orderNo}/activate`),
  myOrders: () => request.get('/member/orders')
}

export const adminApi = {
  stats: () => request.get('/admin/stats'),
  overview: () => request.get('/admin/overview'),
  users: (params) => request.get('/admin/users', { params }),
  updateUser: (id, data) => request.put(`/admin/users/${id}`, data),
  resetPassword: (id, password) => request.put(`/admin/users/${id}/password`, { password }),
  deleteUser: (id) => request.delete(`/admin/users/${id}`),
  schools: (params) => request.get('/admin/schools', { params }),
  createSchool: (data) => request.post('/admin/schools', data),
  updateSchool: (id, data) => request.put(`/admin/schools/${id}`, data),
  deleteSchool: (id) => request.delete(`/admin/schools/${id}`),
  scoreLines: (params) => request.get('/admin/scorelines', { params }),
  createScoreLine: (data) => request.post('/admin/scorelines', data),
  updateScoreLine: (id, data) => request.put(`/admin/scorelines/${id}`, data),
  deleteScoreLine: (id) => request.delete(`/admin/scorelines/${id}`),
  articles: (params) => request.get('/admin/articles', { params }),
  createArticle: (data) => request.post('/admin/articles', data),
  updateArticle: (id, data) => request.put(`/admin/articles/${id}`, data),
  deleteArticle: (id) => request.delete(`/admin/articles/${id}`),
  activities: (params) => request.get('/admin/activities', { params }),
  createActivity: (data) => request.post('/admin/activities', data),
  updateActivity: (id, data) => request.put(`/admin/activities/${id}`, data),
  deleteActivity: (id) => request.delete(`/admin/activities/${id}`)
}
