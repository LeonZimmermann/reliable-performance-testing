import { createRouter, createWebHistory } from 'vue-router'
import BookListView from '../views/BookListView.vue'
import BookCreateView from '../views/BookCreateView.vue'
import BookDetailView from '../views/BookDetailView.vue'
import AuthorListView from '../views/AuthorListView.vue'
import AuthorCreateView from '../views/AuthorCreateView.vue'
import AuthorDetailView from '../views/AuthorDetailView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/books' },
    { path: '/books', name: 'book-list', component: BookListView },
    { path: '/books/create', name: 'book-create', component: BookCreateView },
    { path: '/books/:id', name: 'book-detail', component: BookDetailView },
    { path: '/authors', name: 'author-list', component: AuthorListView },
    { path: '/authors/create', name: 'author-create', component: AuthorCreateView },
    { path: '/authors/:id', name: 'author-detail', component: AuthorDetailView },
  ],
})

export default router
