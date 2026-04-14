import { createRouter, createWebHistory } from 'vue-router'
import BookListView from '../views/BookListView.vue'
import BookCreateView from '../views/BookCreateView.vue'
import BookDetailView from '../views/BookDetailView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/books' },
    { path: '/books', name: 'book-list', component: BookListView },
    { path: '/books/create', name: 'book-create', component: BookCreateView },
    { path: '/books/:id', name: 'book-detail', component: BookDetailView },
  ],
})

export default router
