import { createRouter, createWebHistory } from 'vue-router'

// Lazy loaded view components with named chunks for better debugging
const Dashboard = () => import(/* webpackChunkName: "dashboard" */ '@/views/Dashboard.vue')
const DataSources = () => import(/* webpackChunkName: "datasources" */ '@/views/DataSources.vue')
const Collections = () => import(/* webpackChunkName: "collections" */ '@/views/Collections.vue')
const Records = () => import(/* webpackChunkName: "records" */ '@/views/Records.vue')
const EtlTransformations = () => import(/* webpackChunkName: "etl" */ '@/views/EtlTransformations.vue')
const Alerts = () => import(/* webpackChunkName: "alerts" */ '@/views/Alerts.vue')
const Settings = () => import(/* webpackChunkName: "settings" */ '@/views/Settings.vue')

// Loading component for lazy loaded routes
const LoadingComponent = {
  template: '<div class="loading-container"><div class="spinner"></div><p>Loading...</p></div>',
  style: `
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 200px;
    }
    .spinner {
      width: 40px;
      height: 40px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid #3498db;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    p {
      margin-top: 16px;
      color: #666;
    }
  `
}

// Error component for failed lazy loads
const ErrorComponent = {
  template: `
    <div class="error-container">
      <p>Failed to load page. Please try again.</p>
      <button @click="retry">Retry</button>
    </div>
  `,
  methods: {
    retry() {
      window.location.reload()
    }
  },
  style: `
    .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 200px;
      padding: 20px;
      text-align: center;
    }
    button {
      margin-top: 16px;
      padding: 8px 16px;
      background: #3498db;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    button:hover {
      background: #2980b9;
    }
  `
}

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { title: 'Dashboard' }
  },
  {
    path: '/datasources',
    name: 'DataSources',
    component: DataSources,
    meta: { title: 'Data Sources' }
  },
  {
    path: '/collections',
    name: 'Collections',
    component: Collections,
    meta: { title: 'Collections' }
  },
  {
    path: '/collections/:id/records',
    name: 'Records',
    component: Records,
    meta: { title: 'Records' },
    props: true
  },
  {
    path: '/etl',
    name: 'EtlTransformations',
    component: EtlTransformations,
    meta: { title: 'ETL Transformations' }
  },
  {
    path: '/alerts',
    name: 'Alerts',
    component: Alerts,
    meta: { title: 'Alerts' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { title: 'Settings' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import(/* webpackChunkName: "notfound" */ '@/views/NotFound.vue'),
    meta: { title: '404 - Not Found' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // Scroll behavior for navigation
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0, behavior: 'smooth' }
    }
  }
})

// Navigation guard for page titles
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - BigData Admin` : 'BigData Admin'
  next()
})

// Error handling for lazy loading
router.onError((error) => {
  console.error('Router error:', error)
  // Could redirect to error page or show notification
})

export default router
