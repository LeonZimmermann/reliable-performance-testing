import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '../main'
import BookListView from '../views/BookListView.vue'
import BookListV1View from '../views/BookListV1View.vue'
import BookCreateView from '../views/BookCreateView.vue'
import BookDetailView from '../views/BookDetailView.vue'
import AuthorListView from '../views/AuthorListView.vue'
import AuthorCreateView from '../views/AuthorCreateView.vue'
import AuthorDetailView from '../views/AuthorDetailView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/books/v1' },
    { path: '/books/v1', name: 'book-list-v1', component: BookListV1View },
    { path: '/books', name: 'book-list', component: BookListView },
    { path: '/books/create', name: 'book-create', component: BookCreateView, meta: { requiresAdmin: true } },
    { path: '/books/:id', name: 'book-detail', component: BookDetailView },
    { path: '/authors', name: 'author-list', component: AuthorListView },
    { path: '/authors/create', name: 'author-create', component: AuthorCreateView, meta: { requiresAdmin: true } },
    { path: '/authors/:id', name: 'author-detail', component: AuthorDetailView },
  ],
})

router.beforeEach((to) => {
  if (to.meta.requiresAdmin && !auth.isAdmin) return { path: '/' }
})

export default router
