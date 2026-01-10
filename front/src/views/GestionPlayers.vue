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
                                            class="badge role-current"
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
                                    <button
                                        v-if="user.isEditing"
                                        class="btn btn-primary btn-sm"
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
        users.value = players.map((user: any) => ({
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
    font-family: "Inter", sans-serif;
}

.header {
    margin-bottom: 2rem;
}

.header h1 {
    margin: 0 0 0.5rem 0;
    font-size: 2rem;
    font-weight: 600;
    color: #1e293b;
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 0;
    color: #64748b;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #e2e8f0;
    border-top-color: #6366f1;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-bottom: 1rem;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow:
        0 4px 6px -1px rgba(0, 0, 0, 0.1),
        0 2px 4px -1px rgba(0, 0, 0, 0.06);
    border: 1px solid #e2e8f0;
    overflow: hidden;
}

.table-wrapper {
    overflow-x: auto;
}

.users-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.95rem;
}

.users-table th,
.users-table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid #f1f5f9;
}

.users-table th {
    background-color: #f8fafc;
    color: #64748b;
    font-weight: 600;
    text-transform: uppercase;
    font-size: 0.75rem;
    letter-spacing: 0.05em;
    position: sticky;
    top: 0;
    z-index: 10;
}

.users-table tbody tr {
    transition: background-color 0.2s;
}

.users-table tbody tr:hover {
    background-color: #f8fafc;
}

.users-table tbody tr.editing {
    background-color: #fef3c7;
}

.col-id {
    width: 80px;
    color: #94a3b8;
    font-weight: 500;
}

.user-pseudo {
    font-weight: 600;
    color: #1e293b;
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

.badge {
    padding: 0.25rem 0.75rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 600;
}

.role-current {
    background-color: #dbeafe;
    color: #1e40af;
}

.text-muted {
    color: #94a3b8;
    font-style: italic;
    font-size: 0.875rem;
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
    border: 2px solid #e2e8f0;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background-color: #ffffff;
    user-select: none;
}

.role-checkbox:hover {
    border-color: #cbd5e1;
    background-color: #f8fafc;
}

.role-checkbox.checked {
    border-color: #6366f1;
    background-color: #eef2ff;
}

.role-checkbox.changed {
    border-color: #f59e0b;
    background-color: #fffbeb;
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.8;
    }
}

.role-checkbox input[type="checkbox"] {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: #6366f1;
}

.checkbox-label {
    font-size: 0.875rem;
    font-weight: 500;
    color: #475569;
}

.role-checkbox.checked .checkbox-label {
    color: #4338ca;
}

.col-actions {
    width: 200px;
    text-align: right;
}

.col-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

.btn {
    padding: 0.5rem 1rem;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
    font-size: 0.875rem;
}

.btn-sm {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
}

.btn-primary {
    background-color: #6366f1;
    color: white;
}

.btn-primary:hover:not(:disabled) {
    background-color: #4f46e5;
    transform: translateY(-1px);
    box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.3);
}

.btn-primary:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.btn-secondary {
    background-color: #e2e8f0;
    color: #475569;
}

.btn-secondary:hover {
    background-color: #cbd5e1;
}

@media (max-width: 1200px) {
    .users-table {
        font-size: 0.85rem;
    }

    .users-table th,
    .users-table td {
        padding: 0.75rem;
    }
}
</style>
