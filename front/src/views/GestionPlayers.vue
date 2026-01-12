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
                            class="btn btn-secondary"
                            @click="cancelEdit"
                        >
                            Annuler
                        </button>
                        <button
                            type="button"
                            class="btn btn-primary"
                            @click="save"
                            :disabled="saving"
                        >
                            <span v-if="saving">💾 Enregistrement...</span>
                            <span v-else>Enregistrer les modifications</span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Liste des utilisateurs -->
            <div class="card list-card">
                <div class="card-header">
                    <h3>Utilisateurs existants</h3>
                </div>
                <div class="table-wrapper">
                    <table class="users-table">
                        <thead>
                            <tr>
                                <th class="col-id">ID</th>
                                <th>Pseudo</th>
                                <th>Rôles</th>
                                <th class="col-actions">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr
                                v-for="user in users"
                                :key="user.id"
                                :class="{
                                    editing:
                                        editing && editingUser?.id === user.id,
                                }"
                            >
                                <td class="col-id">#{{ user.id }}</td>
                                <td class="user-pseudo">
                                    <span class="pseudo-text">{{
                                        user.pseudo
                                    }}</span>
                                </td>
                                <td class="current-roles">
                                    <div
                                        v-if="user.roleNames.length > 0"
                                        class="role-badges"
                                    >
                                        <span
                                            v-for="role in user.roleNames"
                                            :key="role"
                                            class="badge badge-primary"
                                        >
                                            {{ role }}
                                        </span>
                                    </div>
                                    <span v-else class="text-muted"
                                        >Aucun rôle</span
                                    >
                                </td>
                                <td class="col-actions">
                                    <div class="action-buttons">
                                        <button
                                            class="btn-icon"
                                            @click="startEdit(user)"
                                            title="Modifier les rôles"
                                        >
                                            ✏️
                                        </button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="users.length === 0">
                                <td colspan="4" class="empty-state">
                                    Aucun utilisateur.
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
import playerService from "@/services/playerService";
import { useToast } from "@/services/toast";
import PlayerRolesDto from "@/models/dtos/PlayerRolesDto.ts";

const toast = useToast();
const roles = ref<string[]>([]);
const users = ref<PlayerRolesDto[]>([]);
const saving = ref(false);
const editing = ref(false);
const editingUser = ref<PlayerRolesDto | null>(null);

async function load() {
    const players = await playerService.getAll();
    roles.value = await roleService.getAll();
    users.value = players.map((user: PlayerRolesDto) => ({
        id: user.id,
        pseudo: user.pseudo,
        roleNames: user.roleNames || [],
        editRoles: [...(user.roleNames || [])],
        isEditing: false,
    }));
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
        await playerService.updateRoles(
            editingUser.value.id,
            editingUser.value.editRoles
        );
        toast.show(
            `Rôles de ${editingUser.value.pseudo} mis à jour avec succès`,
            "success"
        );
        cancelEdit();
        await load();
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

.table-wrapper {
    overflow-x: auto;
}

.users-table tbody tr.editing {
    background-color: rgba(59, 130, 246, 0.1);
    border-left: 3px solid var(--primary);
}

.col-id {
    width: 80px;
    color: var(--text-secondary);
    font-weight: 500;
    font-family: monospace;
}

.user-pseudo {
    font-weight: 600;
    color: var(--text-bright);
}

.pseudo-text {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.current-roles {
    min-width: 200px;
}

.role-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
}

.col-actions {
    width: 100px;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

.btn-icon:hover {
    background-color: var(--bg-hover);
}

@media (max-width: 1200px) {
    .users-table {
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
