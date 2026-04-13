<template>
    <div class="roles-management">
        <div class="header">
            <h1>Gestion des rôles</h1>
        </div>

        <div class="content">
            <div class="card form-card">
                <div class="card-header">
                    <h2>
                        {{
                            editing
                                ? "Modifier le rôle"
                                : "Créer un nouveau rôle"
                        }}
                    </h2>
                </div>
                <form @submit.prevent="submit" class="role-form">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="roleName">Nom du rôle</label>
                            <input
                                id="roleName"
                                v-model="form.name"
                                required
                                placeholder="Entrez le nom du rôle"
                            />
                        </div>
                    </div>
                    <div class="form-actions">
                        <button
                            v-if="editing"
                            type="button"
                            class="btn-secondary"
                            @click="cancelEdit"
                        >
                            Annuler
                        </button>
                        <button type="submit" class="btn-primary">
                            {{
                                editing
                                    ? "Enregistrer les modifications"
                                    : "Créer le rôle"
                            }}
                        </button>
                    </div>
                </form>
            </div>

            <div class="card list-card">
                <div class="card-header">
                    <h3>Rôles existants</h3>
                </div>

                <div class="table-wrapper">
                    <table class="roles-table">
                        <thead>
                            <tr>
                                <th class="col-id">ID</th>
                                <th>Nom</th>
                                <th class="col-actions">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="r in roles" :key="r.id!">
                                <td class="col-id">#{{ r.id }}</td>
                                <td>
                                    <span class="role-name">{{ r.name }}</span>
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
                                            class="btn-icon"
                                            @click="remove(r)"
                                            title="Supprimer"
                                        >
                                            🗑️
                                        </button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="roles.length === 0">
                                <td colspan="3" class="empty-state">
                                    Aucun rôle configuré.
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue";
import roleService from "@/services/roleService";
import { useToast } from "@/services/toast";
import RoleDto from "@/models/dtos/RoleDto";

const toast = useToast();

const roles = ref<RoleDto[]>([]);
const form = ref<RoleDto>({ id: null, name: "" });
const editing = ref(false);

async function loadRoles() {
    roles.value = await roleService.getAllRoles();
}

function startEdit(r: RoleDto) {
    editing.value = true;
    form.value = { id: r.id, name: r.name };
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function cancelEdit() {
    form.value = { id: null, name: "" };
    editing.value = false;
}

async function submit() {
    if (!form.value.name) {
        toast.show("Le nom du rôle est obligatoire.", "error");
        return;
    }

    if (editing.value && form.value.id) {
        const updatedRole = await roleService.updateRole(form.value);

        const index = roles.value.findIndex((r) => r.id === updatedRole.id);
        if (index !== -1) {
            roles.value[index] = updatedRole;
        }

        toast.show("Rôle mis à jour avec succès", "success");
    } else {
        const newRole = await roleService.createRole(form.value.name);

        roles.value.push(newRole);

        toast.show("Rôle créé avec succès", "success");
    }

    cancelEdit();
}

async function remove(r: RoleDto) {
    if (
        !confirm(
            `Supprimer le rôle "${r.name}" ?\n\nCette action est irréversible.`
        )
    ) {
        return;
    }

    await roleService.remove(r.id!);
    toast.show("Rôle supprimé avec succès", "success");
    await loadRoles();
}

onMounted(loadRoles);
</script>

<style scoped>
.roles-management {
    max-width: 1000px;
    margin: 2rem auto;
    padding: 0 1.5rem;
}

.header {
    margin-bottom: 2rem;
}

.form-card,
.list-card {
    margin-bottom: 2rem;
}

.role-form {
    padding: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr;
    gap: 1.5rem;
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

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1rem;
}

.table-wrapper {
    overflow-x: auto;
}

.roles-table {
    width: 100%;
}

.col-id {
    width: 80px;
    color: var(--text-secondary);
    font-weight: 500;
    font-family: monospace;
}

.role-name {
    font-weight: 600;
    color: var(--text-bright);
}

.col-actions {
    width: 120px;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

@media (max-width: 768px) {
    .roles-management {
        padding: 0 1rem;
    }

    .role-form {
        padding: 1.5rem;
    }
}
</style>
