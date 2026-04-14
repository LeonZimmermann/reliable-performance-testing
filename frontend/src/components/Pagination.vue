<script setup lang="ts">
const props = defineProps<{
  currentPage: number
  totalPages: number
  totalElements: number
}>()

const emit = defineEmits<{
  'page-change': [page: number]
}>()
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
      class="btn btn-secondary btn-sm"
      :disabled="currentPage === 0"
      @click="emit('page-change', currentPage - 1)"
    >
      ← Previous
    </button>
    <span class="page-info">
      Page {{ currentPage + 1 }} of {{ totalPages }}
      <span class="total">({{ totalElements }} total)</span>
    </span>
    <button
      class="btn btn-secondary btn-sm"
      :disabled="currentPage >= totalPages - 1"
      @click="emit('page-change', currentPage + 1)"
    >
      Next →
    </button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
  justify-content: center;
}

.page-info {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  min-width: 160px;
  text-align: center;
}

.total {
  color: var(--color-text-muted);
}
</style>
