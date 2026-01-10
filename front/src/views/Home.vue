<template>
    <div class="home">
        <h1>Bienvenue</h1>
        <p v-if="!pseudo">
            Vous n'êtes pas connecté. Certaines routes peuvent nécessiter une
            authentification.
        </p>
        <p v-else>
            Connecté en tant que <strong>{{ pseudo }}</strong>
        </p>

        <section class="actions">
            <RouterLink v-if="!pseudo" class="btn" to="/login"
                >Se connecter</RouterLink
            >
            <RouterLink v-if="!pseudo" class="btn" to="/register"
                >Créer un compte</RouterLink
            >
            <button v-if="pseudo" class="btn" @click="logout">
                Se déconnecter
            </button>
        </section>
        <div v-if="pseudo">
            <h2>Routes disponibles</h2>
            <ul v-if="routes.length">
                <li v-for="r in routes" :key="r.id!">
                    <RouterLink :to="normalizePath(r.name)">
                        {{ normalizePath(r.name) }}
                        <small v-if="r.needAuth">(auth requise)</small>
                    </RouterLink>
                    <small class="path"> → {{ r.componentPath }}</small>
                </li>
            </ul>
            <p v-else>Aucune route disponible pour le moment.</p>
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
    max-width: 720px;
    margin: 2rem auto;
    padding: 0 1rem;
}

.actions {
    display: flex;
    gap: 0.5rem;
    margin: 1rem 0 2rem;
}

.path {
    color: #888;
    margin-left: 0.25rem;
}
</style>
