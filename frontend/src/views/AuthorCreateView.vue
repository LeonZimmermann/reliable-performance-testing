<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authorsApi } from '../api/index'
import AuthorForm from '../components/AuthorForm.vue'
import type { NewAuthor } from '../types/author'

const router = useRouter()

const form = ref<NewAuthor>({ name: '' })
const loading = ref(false)
const error = ref<string | null>(null)

async function handleSubmit() {
  loading.value = true
  error.value = null
  try {
    const author = await authorsApi.createAuthor({ newAuthor: form.value })
    router.push({ name: 'author-detail', params: { id: author.id } })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to create author'
    loading.value = false
  }
}
</script>

<template>
  <div class="back-link">
    <RouterLink to="/authors">← Back to authors</RouterLink>
  </div>

  <div class="create-card">
    <h1>New Author</h1>

    <div v-if="error" class="error-banner">{{ error }}</div>

    <AuthorForm
      v-model="form"
      :loading="loading"
      submit-label="Create Author"
      @submit="handleSubmit"
    >
      <template #actions>
        <RouterLink to="/authors" class="btn btn-secondary">Cancel</RouterLink>
      </template>
    </AuthorForm>
  </div>
</template>

<style scoped>
.back-link {
  margin-bottom: 1.25rem;
}

.back-link a {
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.875rem;
}

.back-link a:hover {
  color: var(--color-primary);
}

.create-card {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  padding: 2rem;
  max-width: 560px;
}

h1 {
  margin: 0 0 1.5rem;
  font-size: 1.375rem;
  font-weight: 700;
}
</style>
