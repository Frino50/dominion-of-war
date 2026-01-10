<template>
    <div class="home">
        <div class="hero">
            <h1>Bienvenue</h1>
            <p v-if="!pseudo" class="hero-subtitle">
                Vous n'êtes pas connecté.
            </p>
            <p v-else class="hero-subtitle">
                Connecté en tant que
                <strong class="user-name">{{ pseudo }}</strong>
            </p>
        </div>

        <section class="actions">
            <RouterLink v-if="!pseudo" class="btn btn-primary" to="/login">
                Se connecter
            </RouterLink>
            <RouterLink v-if="!pseudo" class="btn btn-secondary" to="/register">
                Créer un compte
            </RouterLink>
            <button v-if="pseudo" class="btn btn-danger" @click="logout">
                Se déconnecter
            </button>
        </section>

        <div v-if="pseudo" class="routes-section">
            <div class="card">
                <div class="card-header">
                    <h2>Routes :</h2>
                </div>
                <div class="card-body">
                    <ul v-if="routes.length" class="routes-list">
                        <li v-for="r in routes" :key="r.id!" class="route-item">
                            <RouterLink
                                :to="normalizePath(r.name)"
                                class="route-link"
                            >
                                <div class="route-info">
                                    <span class="route-name">{{
                                        normalizePath(r.name)
                                    }}</span>
                                </div>
                                <span class="route-path"
                                    >→ {{ r.componentPath }}</span
                                >
                            </RouterLink>
                        </li>
                    </ul>
                    <p v-else class="empty-state">
                        Aucune route disponible pour le moment.
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import routeService from "@/services/routeService";
import { localStore } from "@/store/local";
import RouteDto from "@/models/dtos/RouteDto.ts";

const routes = ref<RouteDto[]>([]);
const pseudo = computed(() => localStore.pseudo);

function normalizePath(name: string) {
    return name.startsWith("/") ? name : `/${name}`;
}

async function loadRoutes() {
    routes.value = await routeService.getAvailable();
}

function logout() {
    localStore.pseudo = "";
    localStore.token = "";
}

onMounted(loadRoutes);
</script>

<style scoped>
.home {
    max-width: 900px;
    margin: 0 auto;
    padding: 2rem 1.5rem;
}

.hero {
    text-align: center;
    margin-bottom: 3rem;
    padding: 3rem 0;
}

.hero h1 {
    font-size: 3rem;
    margin: 0 0 1rem 0;
    background: linear-gradient(
        135deg,
        var(--primary) 0%,
        var(--secondary) 100%
    );
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.hero-subtitle {
    font-size: 1.1rem;
    color: var(--text-secondary);
    margin: 0;
}

.user-name {
    color: var(--primary-light);
    font-weight: 600;
}

.actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    margin-bottom: 3rem;
}

.routes-section {
    margin-top: 2rem;
}

.routes-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.route-item {
    border-bottom: 1px solid var(--border-base);
}

.route-item:last-child {
    border-bottom: none;
}

.route-link {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-radius: 8px;
    transition: all var(--transition-base);
    text-decoration: none;
    color: var(--text-primary);
}

.route-link:hover {
    background-color: var(--bg-hover);
    text-decoration: none;
    transform: translateX(4px);
}

.route-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.route-name {
    font-weight: 600;
    color: var(--text-bright);
    font-size: 1.05rem;
}

.route-path {
    color: var(--text-muted);
    font-size: 0.85rem;
    font-family: "Courier New", monospace;
}

@media (max-width: 768px) {
    .hero h1 {
        font-size: 2rem;
    }

    .actions {
        flex-direction: column;
    }

    .route-link {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }
}
</style>
