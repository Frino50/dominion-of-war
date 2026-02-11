<template>
    <div class="empty-state">
        <p>Aucun unité</p>
    </div>
</template>
<style scoped>
.empty-state {
    text-align: center;
    padding: 4rem 2rem;
    color: var(--text-secondary);
}

.empty-state p {
    color: var(--text-primary);
    font-size: 1.1rem;
    margin-bottom: 0.5rem;
}

.empty-state small {
    color: var(--text-muted);
    font-size: 0.9rem;
}

@media (max-width: 768px) {
    .empty-state {
        padding: 3rem 1rem;
    }
}
</style>
