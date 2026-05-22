import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/ai-assistant',
      name: 'aiAssistant',
      component: () => import('../views/AiAssistant.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/publish',
      name: 'publish',
      component: () => import('../views/Publish.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/SearchResult.vue')
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('../views/Chat.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/item/:id',
      name: 'itemDetail',
      component: () => import('../views/ItemDetail.vue')
    },
    {
      path: '/user/:id',
      name: 'sellerDetail',
      component: () => import('../views/SellerDetail.vue')
    },
    {
      path: '/order/detail/:id',
      name: 'orderDetail',
      component: () => import('../views/OrderDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/order/list',
      name: 'orderList',
      component: () => import('../views/OrderList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/announcement/list',
      name: 'announcementList',
      component: () => import('../views/AnnouncementList.vue')
    },
    {
      path: '/notification',
      name: 'notification',
      component: () => import('../views/NotificationList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'adminDashboard',
          component: () => import('../views/admin/Dashboard.vue')
        },
        {
          path: 'user',
          name: 'adminUser',
          component: () => import('../views/admin/UserManage.vue')
        },
        {
          path: 'item',
          name: 'adminItem',
          component: () => import('../views/admin/ItemManage.vue')
        },
        {
          path: 'report',
          name: 'adminReport',
          component: () => import('../views/admin/ReportManage.vue')
        },
        {
          path: 'category',
          name: 'adminCategory',
          component: () => import('../views/admin/CategoryManage.vue')
        },
        {
          path: 'announcement',
          name: 'adminAnnouncement',
          component: () => import('../views/admin/AnnouncementManage.vue')
        }
      ]
    }
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  
  if (to.meta.requiresAuth && !user.token) {
    next('/login')
    return
  }
  
  if (to.meta.requiresAdmin && user.role !== 1) {
    next('/') // 非管理员跳转首页
    return
  }
  
  // 已登录状态下访问登录或注册页，跳转回各自的首页
  if ((to.path === '/login' || to.path === '/register') && user.token) {
    next(user.role === 1 ? '/admin' : '/')
    return
  }
  
  // 如果是管理员且访问首页，自动导向管理员页面
  if (to.path === '/' && user.role === 1) {
    next('/admin')
    return
  }
  
  next()
})

export default router
