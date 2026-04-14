<script setup lang="ts">
import type { NewBook } from '../types/book'
import type { AuthorSummary } from '../types/author'

const props = defineProps<{
  modelValue: NewBook
  loading: boolean
  submitLabel: string
  availableAuthors?: AuthorSummary[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: NewBook]
  'submit': []
}>()

function update(field: keyof NewBook, value: string | number | number[]) {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
}

function toggleAuthor(authorId: number) {
  const current = props.modelValue.authorIds ?? []
  const next = current.includes(authorId)
    ? current.filter((id) => id !== authorId)
    : [...current, authorId]
  emit('update:modelValue', { ...props.modelValue, authorIds: next })
}

function isChecked(authorId: number): boolean {
  return (props.modelValue.authorIds ?? []).includes(authorId)
}
</script>

<template>
  <form @submit.prevent="emit('submit')" novalidate>
    <div class="field">
      <label for="title">Title *</label>
      <input
        id="title"
        type="text"
        :value="modelValue.title"
        @input="update('title', ($event.target as HTMLInputElement).value)"
        required
        placeholder="Book title"
      />
    </div>

    <div class="field">
      <label for="author">Author (text) *</label>
      <input
        id="author"
        type="text"
        :value="modelValue.author"
        @input="update('author', ($event.target as HTMLInputElement).value)"
        required
        placeholder="Author name"
      />
    </div>

    <div class="field">
      <label for="isbn">ISBN *</label>
      <input
        id="isbn"
        type="text"
        :value="modelValue.isbn"
        @input="update('isbn', ($event.target as HTMLInputElement).value)"
        required
        placeholder="e.g. 978-0-13-235088-4"
      />
    </div>

    <div class="field">
      <label for="price">Price *</label>
      <input
        id="price"
        type="number"
        step="0.01"
        min="0"
        :value="modelValue.price"
        @input="update('price', parseFloat(($event.target as HTMLInputElement).value) || 0)"
        required
        placeholder="0.00"
      />
    </div>

    <div class="field">
      <label for="publisher">Publisher</label>
      <input
        id="publisher"
        type="text"
        :value="modelValue.publisher ?? ''"
        @input="update('publisher', ($event.target as HTMLInputElement).value)"
        placeholder="Optional"
      />
    </div>

    <div v-if="availableAuthors && availableAuthors.length > 0" class="field">
      <label>Linked Authors</label>
      <div class="author-list">
        <label
          v-for="a in availableAuthors"
          :key="a.id"
          class="author-check"
        >
          <input
            type="checkbox"
            :checked="isChecked(a.id)"
            @change="toggleAuthor(a.id)"
          />
          {{ a.name }}
        </label>
      </div>
    </div>

    <div class="form-actions">
      <slot name="actions" />
      <button type="submit" class="btn btn-primary" :disabled="loading">
        {{ loading ? 'Saving…' : submitLabel }}
      </button>
    </div>
  </form>
</template>

<style scoped>
form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 480px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text);
}

input[type="text"],
input[type="number"] {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.9375rem;
  color: var(--color-text);
  background: var(--color-bg);
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 100%;
}

input[type="text"]:focus,
input[type="number"]:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.author-list {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 0.625rem 0.75rem;
  background: var(--color-bg);
  max-height: 180px;
  overflow-y: auto;
}

.author-check {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 400;
  cursor: pointer;
}

.author-check input[type="checkbox"] {
  width: auto;
  cursor: pointer;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  padding-top: 0.5rem;
}
</style>
