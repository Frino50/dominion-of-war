<template>
    <div class="route-manager-container">
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
                            class="input-field"
                        />
                    </div>

                    <div class="form-group">
                        <label for="compPath">Page</label>
                        <select
                            id="compPath"
                            v-model="form.componentPath"
                            required
                            class="input-field select-field"
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
                            class="input-field select-field"
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
            <h3>Routes existantes</h3>
            <div class="table-wrapper">
                <table class="styled-table">
                    <thead>
                        <tr>
                            <th class="col-id">ID</th>
                            <th>Nom / URL</th>
                            <th>Composant</th>
                            <th>Rôle</th>
                            <th class="col-center">Auth</th>
                            <th class="col-actions">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="r in routes" :key="r.id!">
                            <td class="col-id">#{{ r.id }}</td>
                            <td class="font-bold">{{ r.name }}</td>
                            <td class="text-small">{{ r.componentPath }}</td>
                            <td>
                                <span
                                    class="badge role-badge"
                                    v-if="r.roleName"
                                    >{{ r.roleName }}</span
                                >
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
    const counts = new Map<string, number>();
    allViewPaths.forEach((p) => {
        const name = p.split("/").pop() || p;
        counts.set(name, (counts.get(name) || 0) + 1);
    });
    return allViewPaths
        .filter((p) => !p.endsWith("/Home.vue"))
        .map((p) => {
            const file = p.split("/").pop() || p;
            const duplicate = (counts.get(file) || 0) > 1;
            const label = p.replace(/^\/src\//, "");
            const value = duplicate ? p : file;
            return { label, value };
        })
        .sort((a, b) => a.label.localeCompare(b.label));
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
    form.value.roleName = (r as any).roleName || "";
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
    max-width: 900px;
    margin: 3rem auto;
    padding: 0 1.5rem;
    font-family: "Inter", sans-serif;
    color: #334155;
}

.card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow:
        0 4px 6px -1px rgba(0, 0, 0, 0.1),
        0 2px 4px -1px rgba(0, 0, 0, 0.06);
    padding: 2rem;
    margin-bottom: 2rem;
    border: 1px solid #e2e8f0;
}

.card-header {
    margin-bottom: 1.5rem;
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 1rem;
}

h2 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
    color: #1e293b;
}

.route-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
}

@media (max-width: 600px) {
    .form-row {
        grid-template-columns: 1fr;
    }
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

label {
    font-size: 0.875rem;
    font-weight: 500;
    color: #475569;
}

.input-field {
    padding: 0.75rem;
    border: 1px solid #cbd5e1;
    border-radius: 6px;
    font-size: 0.95rem;
    transition:
        border-color 0.2s,
        box-shadow 0.2s;
    background-color: #f8fafc;
}

.input-field:focus {
    outline: none;
    border-color: #6366f1;
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
    background-color: #fff;
}

.select-field {
    appearance: none;
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
    background-position: right 0.5rem center;
    background-repeat: no-repeat;
    background-size: 1.5em 1.5em;
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
    width: 44px;
    height: 24px;
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
    background-color: #cbd5e1;
    transition: 0.4s;
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
    height: 18px;
    width: 18px;
    left: 3px;
    bottom: 3px;
    background-color: white;
    transition: 0.4s;
}

input:checked + .slider {
    background-color: #6366f1;
}

input:checked + .slider:before {
    transform: translateX(20px);
}

.label-text {
    font-size: 0.9rem;
    font-weight: 500;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1rem;
}

.btn {
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
}

.btn-primary {
    background-color: #6366f1;
    color: white;
}
.btn-primary:hover {
    background-color: #4f46e5;
}

.btn-secondary {
    background-color: #e2e8f0;
    color: #475569;
}
.btn-secondary:hover {
    background-color: #cbd5e1;
}

.table-wrapper {
    overflow-x: auto;
}

.styled-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.95rem;
}

.styled-table th,
.styled-table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid #f1f5f9;
}

.styled-table th {
    background-color: #f8fafc;
    color: #64748b;
    font-weight: 600;
    text-transform: uppercase;
    font-size: 0.75rem;
    letter-spacing: 0.05em;
}

.styled-table tr:hover {
    background-color: #f8fafc;
}

.col-id {
    width: 60px;
    color: #94a3b8;
}

.text-small {
    font-size: 0.85rem;
    color: #64748b;
    font-family: monospace;
}

.font-bold {
    font-weight: 600;
}

.role-badge {
    background-color: #e0e7ff;
    color: #4338ca;
    padding: 0.25rem 0.6rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 600;
}

.status-dot {
    height: 10px;
    width: 10px;
    background-color: #cbd5e1;
    border-radius: 50%;
    display: inline-block;
}

.status-dot.active {
    background-color: #10b981;
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

.col-center {
    text-align: center;
}

.col-actions {
    text-align: right;
    width: 100px;
}

.btn-icon {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.1rem;
    padding: 0.4rem;
    border-radius: 4px;
    transition: background 0.2s;
}

.btn-icon:hover {
    background-color: #f1f5f9;
}

.btn-icon.danger:hover {
    background-color: #fee2e2;
}
</style>
