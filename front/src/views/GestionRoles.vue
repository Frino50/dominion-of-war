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
                                v-model="editingRole.name"
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

                <DataTable
                    :data="roles"
                    row-key="id"
                    empty-text="Aucun rôle configuré."
                    :row-editing="
                        (row) => editing && editingRole?.id === row.id
                    "
                >
                    <Column field="id" header="ID" width="80px">
                        <template #body="{ value }">
                            <span class="col-id">#{{ value }}</span>
                        </template>
                    </Column>

                    <Column field="name" header="Nom">
                        <template #body="{ value }">
                            <span class="role-name">{{ value }}</span>
                        </template>
                    </Column>

                    <template #actions="{ row }">
                        <button
                            class="btn-icon"
                            @click="startEdit(row)"
                            title="Modifier"
                        >
                            ✏️
                        </button>
                        <button
                            class="btn-icon"
                            @click="remove(row)"
                            title="Supprimer"
                        >
                            🗑️
                        </button>
                    </template>
                </DataTable>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue";
import roleService from "@/services/roleService";
import { useToast } from "@/services/toast";
import RoleDto from "@/models/dtos/RoleDto";
import Column from "@/components/Utils/Column.vue";
import DataTable from "@/components/Utils/DataTable.vue";

const toast = useToast();

const roles = ref<RoleDto[]>([]);
const editingRole = ref<RoleDto>({ id: null, name: "" });
const editing = ref(false);

async function loadRoles() {
    roles.value = await roleService.getAllRoles();
}

function startEdit(r: RoleDto) {
    editing.value = true;
    editingRole.value = { id: r.id, name: r.name };
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function cancelEdit() {
    editingRole.value = { id: null, name: "" };
    editing.value = false;
}

async function submit() {
    if (!editingRole.value.name) {
        toast.show("Le nom du rôle est obligatoire.", "error");
        return;
    }

    if (editing.value && editingRole.value.id) {
        const updatedRole = await roleService.updateRole(editingRole.value);

        const index = roles.value.findIndex((r) => r.id === updatedRole.id);
        if (index !== -1) {
            roles.value[index] = updatedRole;
        }

        toast.show("Rôle mis à jour avec succès", "success");
    } else {
        const newRole = await roleService.createRole(editingRole.value.name);

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

.col-id {
    color: var(--text-secondary);
    font-weight: 500;
    font-family: monospace;
}

.role-name {
    font-weight: 600;
    color: var(--text-bright);
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
