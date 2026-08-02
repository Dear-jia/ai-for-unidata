import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('../views/user/UserLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/user/HomeView.vue') },
      { path: 'schools', name: 'Schools', component: () => import('../views/user/SchoolListView.vue') },
      { path: 'schools/:id', name: 'SchoolDetail', component: () => import('../views/user/SchoolDetailView.vue') },
      { path: 'scores', name: 'Scores', component: () => import('../views/user/ScoreListView.vue') },
      { path: 'articles', name: 'Articles', component: () => import('../views/user/ArticleListView.vue') },
      { path: 'articles/:id', name: 'ArticleDetail', component: () => import('../views/user/ArticleDetailView.vue') },
      { path: 'activities', name: 'Activities', component: () => import('../views/user/ActivityListView.vue') },
      { path: 'legal/:type', name: 'Legal', component: () => import('../views/user/LegalView.vue') },
      { path: 'member', name: 'Member', component: () => import('../views/user/MemberView.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/user/ProfileView.vue'), meta: { requiresAuth: true } }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/user/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/user/RegisterView.vue') },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/DashboardView.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UsersView.vue') },
      { path: 'schools', name: 'AdminSchools', component: () => import('../views/admin/SchoolsView.vue') },
      { path: 'scorelines', name: 'AdminScoreLines', component: () => import('../views/admin/ScoreLinesView.vue') },
      { path: 'articles', name: 'AdminArticles', component: () => import('../views/admin/ArticlesView.vue') },
      { path: 'activities', name: 'AdminActivities', component: () => import('../views/admin/ActivitiesView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLogin) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'Home' }
  }
  return true
})

export default router
