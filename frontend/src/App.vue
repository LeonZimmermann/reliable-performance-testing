<template>
  <nav>
    <span class="nav-brand">OAS Demo</span>
    <div class="nav-links">
      <RouterLink to="/books">Books</RouterLink>
      <RouterLink to="/books/create" class="btn btn-primary btn-sm">+ New Book</RouterLink>
    </div>
  </nav>
  <main>
    <RouterView />
  </main>
</template>

<style>
*, *::before, *::after {
  box-sizing: border-box;
}

:root {
  --color-primary: #2563eb;
  --color-primary-hover: #1d4ed8;
  --color-danger: #dc2626;
  --color-danger-hover: #b91c1c;
  --color-text: #111827;
  --color-text-muted: #6b7280;
  --color-border: #e5e7eb;
  --color-bg: #ffffff;
  --color-bg-alt: #f9fafb;
  --color-success-bg: #ecfdf5;
  --color-error-bg: #fef2f2;
  --color-error-text: #b91c1c;
  --radius: 6px;
  --shadow-sm: 0 1px 3px rgba(0,0,0,.08);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 15px;
  color: var(--color-text);
  background: var(--color-bg-alt);
}

body {
  margin: 0;
  min-height: 100vh;
}

nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  height: 56px;
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.nav-brand {
  font-weight: 700;
  font-size: 1.1rem;
  letter-spacing: -0.02em;
  color: var(--color-primary);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.nav-links a {
  text-decoration: none;
  color: var(--color-text-muted);
  font-weight: 500;
  transition: color 0.15s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: var(--color-primary);
}

main {
  max-width: 960px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 1rem;
  border-radius: var(--radius);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
  text-decoration: none;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.btn-secondary {
  background: var(--color-bg);
  color: var(--color-text);
  border-color: var(--color-border);
}

.btn-secondary:hover:not(:disabled) {
  background: var(--color-bg-alt);
}

.btn-danger {
  background: var(--color-bg);
  color: var(--color-danger);
  border-color: var(--color-border);
}

.btn-danger:hover:not(:disabled) {
  background: var(--color-error-bg);
  border-color: var(--color-danger);
}

.btn-sm {
  padding: 0.35rem 0.75rem;
  font-size: 0.8125rem;
}

.error-banner {
  background: var(--color-error-bg);
  color: var(--color-error-text);
  border: 1px solid #fecaca;
  border-radius: var(--radius);
  padding: 0.75rem 1rem;
  margin-bottom: 1.25rem;
  font-size: 0.875rem;
}
</style>
