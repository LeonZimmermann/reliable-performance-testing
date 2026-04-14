<script setup lang="ts">
import type { NewBook } from '../types/book'

const props = defineProps<{
  modelValue: NewBook
  loading: boolean
  submitLabel: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: NewBook]
  'submit': []
}>()

function update(field: keyof NewBook, value: string | number) {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
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
      <label for="author">Author *</label>
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

input {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.9375rem;
  color: var(--color-text);
  background: var(--color-bg);
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 100%;
}

input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  padding-top: 0.5rem;
}
</style>
