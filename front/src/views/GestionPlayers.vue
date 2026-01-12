<template>
    <div class="players-management">
        <div class="header">
            <h1>Gestion des utilisateurs</h1>
        </div>

        <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>Chargement des données...</p>
        </div>

        <div v-else class="content">
            <div class="card">
                <div class="table-wrapper">
                    <table class="users-table">
                        <thead>
                            <tr>
                                <th class="col-id">ID</th>
                                <th>Pseudo</th>
                                <th>Rôles actuels</th>
                                <th>Modifier les rôles</th>
                                <th class="col-actions">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr
                                v-for="user in users"
                                :key="user.id"
                                :class="{ editing: user.isEditing }"
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
                                <td class="edit-roles">
                                    <div class="role-checkboxes">
                                        <label
                                            v-for="role in roles"
                                            :key="role"
                                            class="role-checkbox"
                                            :class="{
                                                checked:
                                                    user.editRoles.includes(
                                                        role
                                                    ),
                                                changed: hasRoleChanged(
                                                    user,
                                                    role
                                                ),
                                            }"
                                        >
                                            <input
                                                v-model="user.editRoles"
                                                :value="role"
                                                type="checkbox"
                                                @change="user.isEditing = true"
                                            />
                                            <span class="checkbox-label">{{
                                                role
                                            }}</span>
                                        </label>
                                    </div>
                                </td>
                                <td class="col-actions">
                                    <div class="action-buttons">
                                        <button
                                            v-if="user.isEditing"
                                            class="btn btn-success btn-sm"
                                            @click="save(user)"
                                            :disabled="saving === user.id"
                                        >
                                            <span v-if="saving === user.id"
                                                >💾</span
                                            >
                                            <span v-else>Enregistrer</span>
                                        </button>
                                        <button
                                            v-if="user.isEditing"
                                            class="btn btn-secondary btn-sm"
                                            @click="cancelEdit(user)"
                                        >
                                            Annuler
                                        </button>
                                    </div>
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
const loading = ref(true);
const saving = ref<number | null>(null);

async function load() {
    loading.value = true;
    try {
        const players = await playerService.getAll();
        roles.value = await roleService.getAll();
        users.value = players.map((user: PlayerRolesDto) => ({
            id: user.id,
            pseudo: user.pseudo,
            roleNames: user.roleNames || [],
            editRoles: [...(user.roleNames || [])],
            isEditing: false,
        }));
    } finally {
        loading.value = false;
    }
}

function hasRoleChanged(user: PlayerRolesDto, role: string): boolean {
    const hadRole = user.roleNames.includes(role);
    const hasRole = user.editRoles.includes(role);
    return hadRole !== hasRole;
}

function cancelEdit(user: PlayerRolesDto) {
    user.editRoles = [...user.roleNames];
    user.isEditing = false;
}

async function save(user: PlayerRolesDto) {
    saving.value = user.id;
    try {
        await playerService.updateRoles(user.id, user.editRoles);
        toast.show(`Rôles de ${user.pseudo} mis à jour`, "success");
        await load();
    } finally {
        saving.value = null;
    }
}

onMounted(load);
</script>

<style scoped>
.players-management {
    max-width: 1400px;
    margin: 2rem auto;
    padding: 0 1.5rem;
}

.header {
    margin-bottom: 2rem;
}

.table-wrapper {
    overflow-x: auto;
}

.users-table tbody tr.editing {
    background-color: rgba(245, 158, 11, 0.1);
    border-left: 3px solid var(--warning);
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

.edit-roles {
    min-width: 300px;
}

.role-checkboxes {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
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
    background-color: var(--bg-input);
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

.col-actions {
    width: 200px;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

@media (max-width: 1200px) {
    .users-table {
        font-size: 0.85rem;
    }
}
</style>
