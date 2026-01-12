<template>
    <div class="route-manager-container">
        <div class="header">
            <h1>{{ editing ? "Modifier la route" : "Gestion des routes" }}</h1>
        </div>

        <div class="card form-card">
            <div class="card-header">
                <h2>
                    {{
                        editing
                            ? "Modifier la route"
                            : "Créer une nouvelle route"
                    }}
                </h2>
            </div>

            <form @submit.prevent="submit" class="route-form">
                <div class="form-row">
                    <div class="form-group">
                        <label for="routeName">Nom</label>
                        <input
                            id="routeName"
                            v-model="form.name"
                            required
                            placeholder="Entrez le nom de la route"
                        />
                    </div>

                    <div class="form-group">
                        <label for="compPath">Page</label>
                        <select
                            id="compPath"
                            v-model="form.componentPath"
                            required
                        >
                            <option disabled value="">Choisir une page</option>
                            <option
                                v-for="opt in viewOptions"
                                :key="opt.value"
                                :value="opt.value"
                            >
                                {{ opt.label }}
                            </option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group checkbox-group">
                        <label class="switch">
                            <input type="checkbox" v-model="form.needAuth" />
                            <span class="slider round"></span>
                        </label>
                        <span class="label-text">Authentification requise</span>
                    </div>

                    <div class="form-group">
                        <label for="roleName">Rôle</label>
                        <select
                            id="roleName"
                            v-model="form.roleName"
                            :required="form.needAuth"
                            :disabled="!form.needAuth"
                            :class="{ 'disabled-field': !form.needAuth }"
                        >
                            <option disabled value="">
                                {{
                                    form.needAuth
                                        ? "Sélectionner un rôle"
                                        : "Auth. requise pour sélectionner"
                                }}
                            </option>
                            <option
                                v-for="(r, id) in roles"
                                :key="id"
                                :value="r"
                            >
                                {{ r }}
                            </option>
                        </select>
                    </div>
                </div>

                <div class="form-actions">
                    <button
                        v-if="editing"
                        class="btn btn-secondary"
                        type="button"
                        @click="cancelEdit"
                    >
                        Annuler
                    </button>
                    <button class="btn btn-primary" type="submit">
                        {{
                            editing
                                ? "Enregistrer les modifications"
                                : "Créer la route"
                        }}
                    </button>
                </div>
            </form>
        </div>

        <div class="card list-card">
            <div class="card-header">
                <h3>Routes existantes</h3>
            </div>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th class="col-id">ID</th>
                            <th>Nom</th>
                            <th>Page</th>
                            <th>Rôle</th>
                            <th class="col-center">Auth</th>
                            <th class="col-actions">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="r in routes" :key="r.id!">
                            <td class="col-id">#{{ r.id }}</td>
                            <td class="font-bold">{{ r.name }}</td>
                            <td class="text-small component-path">
                                {{ r.componentPath }}
                            </td>
                            <td>
                                <span
                                    class="badge badge-primary"
                                    v-if="r.roleName"
                                >
                                    {{ r.roleName }}
                                </span>
                                <span class="text-muted" v-else>-</span>
                            </td>
                            <td class="col-center">
                                <span
                                    :class="[
                                        'status-dot',
                                        r.needAuth ? 'active' : '',
                                    ]"
                                ></span>
                            </td>
                            <td class="col-actions">
                                <div class="action-buttons">
                                    <button
                                        class="btn-icon"
                                        @click="startEdit(r)"
                                        title="Modifier"
                                    >
                                        ✏️
                                    </button>
                                    <button
                                        class="btn-icon danger"
                                        @click="remove(r)"
                                        title="Supprimer"
                                    >
                                        🗑️
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="routes.length === 0">
                            <td colspan="6" class="empty-state">
                                Aucune route configurée.
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import roleService from "@/services/roleService";
import routeService from "@/services/routeService";
import { useToast } from "@/services/toast";
import RouteDto from "@/models/dtos/RouteDto.ts";

const toast = useToast();

const modules = import.meta.glob(["/src/views/**/*.vue", "@/views/**/*.vue"]);
const allViewPaths = Object.keys(modules);
const roles = ref<string[]>([]);

const form = ref<RouteDto>({
    id: null,
    name: "",
    componentPath: "",
    needAuth: false,
    roleName: "",
});
const routes = ref<RouteDto[]>([]);
const editing = ref(false);
const editingId = ref<number | null>(null);

const viewOptions = computed(() => {
    const options = allViewPaths
        .filter((p) => !p.endsWith("/Home.vue"))
        .map((p) => {
            const file = p.split("/").pop() || p;
            const label = p.replace(/^\/src\//, "");
            return { label, value: file };
        });

    const hasGestionRoutes = options.some(
        (opt) => opt.value === "GestionRoutes.vue"
    );

    if (!hasGestionRoutes) {
        options.push({
            label: "views/GestionRoutes.vue",
            value: "GestionRoutes.vue",
        });
    }

    return options.sort((a, b) => a.label.localeCompare(b.label));
});

async function loadData() {
    roles.value = await roleService.getAll();
    routes.value = await routeService.getAll();
}

async function submit() {
    if (!form.value.name.trim() || !form.value.componentPath) {
        toast.show("Le nom et le composant sont obligatoires.", "error");
        return;
    }

    if (editing.value && editingId.value) {
        await routeService.update(form.value);
        toast.show("Route mise à jour", "success");
    } else {
        await routeService.create(form.value);
        toast.show("Route créée avec succès", "success");
    }

    cancelEdit();
    await loadRoutes();
}

async function loadRoutes() {
    routes.value = await routeService.getAll();
}

function startEdit(r: RouteDto) {
    editing.value = true;
    editingId.value = r.id;
    form.value.id = r.id;
    form.value.name = r.name;
    form.value.componentPath = r.componentPath;
    form.value.needAuth = r.needAuth;
    form.value.roleName = r.roleName || "";
}

function cancelEdit() {
    editing.value = false;
    editingId.value = null;
    form.value = {
        id: null,
        name: "",
        componentPath: "",
        needAuth: false,
        roleName: "",
    };
}

async function remove(r: RouteDto) {
    await routeService.remove(r.id!);
    toast.show("Route supprimée", "success");
    await loadRoutes();
}

onMounted(loadData);
</script>

<style scoped>
.route-manager-container {
    max-width: 1200px;
    margin: 2rem auto;
    padding: 0 1.5rem;
}

.header {
    margin-bottom: 2rem;
}

.header h1 {
    margin: 0 0 0.5rem 0;
    color: var(--text-bright);
}

.form-card,
.list-card {
    margin-bottom: 2rem;
}

.card-header h2,
.card-header h3 {
    margin: 0;
    color: var(--text-bright);
}

.route-form {
    padding: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
}

@media (max-width: 768px) {
    .form-row {
        grid-template-columns: 1fr;
    }
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.form-group label {
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--text-secondary);
}

.disabled-field {
    opacity: 0.5;
    cursor: not-allowed;
}

.checkbox-group {
    flex-direction: row;
    align-items: center;
    gap: 1rem;
    height: 100%;
    padding-top: 1.8rem;
}

.switch {
    position: relative;
    display: inline-block;
    width: 48px;
    height: 26px;
}

.switch input {
    opacity: 0;
    width: 0;
    height: 0;
}

.slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: var(--border-base);
    transition: 0.3s;
}

.slider.round {
    border-radius: 34px;
}

.slider.round:before {
    border-radius: 50%;
}

.slider:before {
    position: absolute;
    content: "";
    height: 20px;
    width: 20px;
    left: 3px;
    bottom: 3px;
    background-color: white;
    transition: 0.3s;
}

input:checked + .slider {
    background-color: var(--primary);
}

input:checked + .slider:before {
    transform: translateX(22px);
}

.label-text {
    font-size: 0.9rem;
    font-weight: 500;
    color: var(--text-primary);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1rem;
}

.table-wrapper {
    overflow-x: auto;
}

.col-id {
    width: 80px;
    color: var(--text-secondary);
    font-family: monospace;
}

.component-path {
    font-family: "Courier New", monospace;
    color: var(--text-muted);
    font-size: 0.85rem;
}

.status-dot {
    height: 12px;
    width: 12px;
    background-color: var(--border-base);
    border-radius: 50%;
    display: inline-block;
    transition: all var(--transition-base);
}

.status-dot.active {
    background-color: var(--success);
    box-shadow: 0 0 0 3px rgba(5, 150, 105, 0.2);
}

.col-center {
    text-align: center;
}

.col-actions {
    width: 120px;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

.btn-icon.danger:hover {
    background-color: rgba(220, 38, 38, 0.1);
}
</style>
