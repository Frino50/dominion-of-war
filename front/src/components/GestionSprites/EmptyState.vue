<template>
    <div class="empty-state">
        <div class="empty-icon">🐉</div>
        <p>Aucun monstre détecté dans la base de données.</p>
        <small>Importez un fichier pour commencer votre collection.</small>
    </div>
</template>
<style scoped>
.empty-state {
    text-align: center;
    padding: 4rem 2rem;
    color: var(--text-secondary);
}

.empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.5;
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

    .empty-icon {
        font-size: 3rem;
    }
}
</style>
