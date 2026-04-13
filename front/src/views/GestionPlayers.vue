<template>
    <div class="players-management">
        <div class="header">
            <h1>Gestion des utilisateurs</h1>
        </div>

        <div class="content">
            <div v-if="editing" class="card form-card">
                <div class="card-header">
                    <h2>Modifier les rôles de {{ editingUser?.pseudo }}</h2>
                </div>
                <div class="edit-form">
                    <div class="form-group">
                        <label>Sélectionner les rôles</label>
                        <div class="role-checkboxes">
                            <label
                                v-for="role in roles"
                                :key="role"
                                class="role-checkbox"
                                :class="{
                                    checked:
                                        editingUser?.editRoles.includes(role),
                                    changed: hasRoleChanged(editingUser!, role),
                                }"
                            >
                                <input
                                    v-model="editingUser!.editRoles"
                                    :value="role"
                                    type="checkbox"
                                />
                                <span class="checkbox-label">{{ role }}</span>
                            </label>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button
                            type="button"
                            class="btn-secondary"
                            @click="cancelEdit"
                        >
                            Annuler
                        </button>
                        <button
                            type="button"
                            class="btn-primary"
                            @click="save"
                            :disabled="saving"
                        >
                            <span v-if="saving">💾 Enregistrement...</span>
                            <span v-else>Enregistrer les modifications</span>
                        </button>
                    </div>
                </div>
            </div>

            <div class="card list-card">
                <div class="card-header">
                    <h3>Utilisateurs existants</h3>
                </div>

                <DataTable
                    :data="users"
                    row-key="id"
                    empty-text="Aucun utilisateur."
                    :row-editing="
                        (row) => editing && editingUser?.id === row.id
                    "
                >
                    <Column field="id" header="ID" width="80px">
                        <template #body="{ value }">
                            <span class="col-id">#{{ value }}</span>
                        </template>
                    </Column>

                    <Column field="pseudo" header="Pseudo">
                        <template #body="{ value }">
                            <span class="column-style">{{ value }}</span>
                        </template>
                    </Column>

                    <Column field="email" header="Email">
                        <template #body="{ value }">
                            <span class="column-style">{{ value }}</span>
                        </template>
                    </Column>

                    <Column
                        field="roleNames"
                        header="Rôles"
                        body-class="current-roles"
                    >
                        <template #body="{ value }">
                            <div v-if="value.length > 0" class="role-badges">
                                <span
                                    v-for="role in value"
                                    :key="role"
                                    class="badge badge-primary"
                                >
                                    {{ role }}
                                </span>
                            </div>
                            <span v-else class="text-muted">Aucun rôle</span>
                        </template>
                    </Column>

                    <template #actions="{ row }">
                        <button
                            class="btn-icon"
                            @click="startEdit(row)"
                            title="Modifier les rôles"
                        >
                            ✏️
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
import playerService from "@/services/playerService";
import { useToast } from "@/services/toast";
import PlayerRolesDto from "@/models/dtos/PlayerRolesDto.ts";
import DataTable from "@/components/Utils/DataTable.vue";
import Column from "@/components/Utils/Column.vue";

const toast = useToast();
const roles = ref<string[]>([]);
const users = ref<PlayerRolesDto[]>([]);
const saving = ref(false);
const editing = ref(false);
const editingUser = ref<PlayerRolesDto | null>(null);

async function load() {
    users.value = await playerService.getAllPlayers();
    roles.value = await roleService.getAllRoleNames();
}

function startEdit(user: PlayerRolesDto) {
    editing.value = true;
    editingUser.value = {
        ...user,
        editRoles: [...user.roleNames],
    };
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function cancelEdit() {
    editing.value = false;
    editingUser.value = null;
}

function hasRoleChanged(user: PlayerRolesDto, role: string): boolean {
    const hadRole = user.roleNames.includes(role);
    const hasRole = user.editRoles.includes(role);
    return hadRole !== hasRole;
}

async function save() {
    if (!editingUser.value) return;

    saving.value = true;
    try {
        const updatedUser = await playerService.updatePlayerRoles(
            editingUser.value.id,
            editingUser.value.editRoles
        );

        const index = users.value.findIndex((u) => u.id === updatedUser.id);

        if (index !== -1) {
            users.value[index] = updatedUser;
        }

        toast.show(
            `Rôles de ${editingUser.value.pseudo} mis à jour avec succès`,
            "success"
        );

        cancelEdit();
    } finally {
        saving.value = false;
    }
}

onMounted(load);
</script>

<style scoped>
.players-management {
    max-width: 1200px;
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

.edit-form {
    padding: 2rem;
    display: flex;
    flex-direction: column;
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

.role-checkboxes {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    padding: 1rem;
    background: var(--bg-input);
    border-radius: 8px;
    border: 1px solid var(--border-base);
}

.role-checkbox {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    border: 2px solid var(--border-base);
    border-radius: 8px;
    cursor: pointer;
    transition: all var(--transition-base);
    background-color: var(--bg-elevated);
    user-select: none;
}

.role-checkbox:hover {
    border-color: var(--border-light);
    background-color: var(--bg-hover);
}

.role-checkbox.checked {
    border-color: var(--primary);
    background-color: rgba(59, 130, 246, 0.1);
}

.role-checkbox.changed {
    border-color: var(--warning);
    background-color: rgba(245, 158, 11, 0.1);
    animation: pulse 2s infinite;
}

.role-checkbox input[type="checkbox"] {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: var(--primary);
}

.checkbox-label {
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--text-secondary);
}

.role-checkbox.checked .checkbox-label {
    color: var(--primary-light);
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

.column-style {
    font-weight: 600;
    color: var(--text-bright);
}

.current-roles {
    min-width: 200px;
}

.role-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
}

@media (max-width: 1200px) {
    :deep(.data-table) {
        font-size: 0.85rem;
    }

    .role-checkboxes {
        gap: 0.5rem;
    }
}

@media (max-width: 768px) {
    .players-management {
        padding: 0 1rem;
    }

    .edit-form {
        padding: 1.5rem;
    }

    .role-checkboxes {
        padding: 0.75rem;
    }
}
</style>
