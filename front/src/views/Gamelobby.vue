<template>
    <div class="game-lobby">
        <header class="lobby-header">
            <h1>Parties</h1>
            <button @click="showCreateModal = true" class="btn-create">
                Créer une partie
            </button>
        </header>

        <!-- Liste des parties -->
        <div class="rooms-grid">
            <div
                v-for="room in rooms"
                :key="room.id"
                class="room-card"
                :class="{ 'has-password': room.hasPassword }"
                @click="selectRoom(room)"
            >
                <div class="room-header">
                    <h3>{{ room.name }}</h3>
                    <span
                        class="status-badge"
                        :class="room.status.toLowerCase()"
                    >
                        {{ translateStatus(room.status) }}
                    </span>
                </div>
                <div class="room-info">
                    <div class="info-item">
                        <span class="icon">👥</span>
                        <span>{{ room.playerCount }}/2 joueurs</span>
                    </div>
                    <div class="info-item">
                        <span class="icon">👁️</span>
                        <span>{{ room.spectatorCount }}/10 spectateurs</span>
                    </div>
                    <div v-if="room.hasPassword" class="info-item">
                        <span class="icon">🔒</span>
                        <span>Protégée</span>
                    </div>
                </div>
            </div>

            <div v-if="rooms.length === 0" class="empty-state">
                Aucune partie disponible. Créez-en une !
            </div>
        </div>

        <!-- Modal: Créer une partie -->
        <teleport to="body">
            <div
                v-if="showCreateModal"
                class="modal-overlay"
                @click.self="showCreateModal = false"
            >
                <div class="modal-content">
                    <h2>Créer une nouvelle partie</h2>
                    <form @submit.prevent="createRoom">
                        <div class="form-group">
                            <label for="room-name">Nom de la partie</label>
                            <input
                                id="room-name"
                                v-model="newRoomName"
                                type="text"
                                placeholder="Ma partie épique"
                                required
                                maxlength="50"
                            />
                        </div>
                        <div class="form-group">
                            <label for="room-password"
                                >Mot de passe (optionnel)</label
                            >
                            <input
                                id="room-password"
                                v-model="newRoomPassword"
                                type="password"
                                placeholder="Laissez vide pour une partie publique"
                            />
                        </div>
                        <div class="modal-actions">
                            <button
                                type="button"
                                @click="showCreateModal = false"
                                class="btn-cancel"
                            >
                                Annuler
                            </button>
                            <button type="submit" class="btn-submit">
                                Créer
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </teleport>

        <!-- Modal: Rejoindre une partie -->
        <teleport to="body">
            <div
                v-if="selectedRoom && showJoinModal"
                class="modal-overlay"
                @click.self="showJoinModal = false"
            >
                <div class="modal-content">
                    <h2>Rejoindre "{{ selectedRoom.name }}"</h2>
                    <form @submit.prevent="joinRoom">
                        <div v-if="selectedRoom.hasPassword" class="form-group">
                            <label for="join-password">Mot de passe</label>
                            <input
                                id="join-password"
                                v-model="joinPassword"
                                type="password"
                                placeholder="Entrez le mot de passe"
                                required
                            />
                        </div>
                        <p v-else>
                            Cette partie est publique, vous pouvez la rejoindre
                            directement.
                        </p>
                        <div class="modal-actions">
                            <button
                                type="button"
                                @click="showJoinModal = false"
                                class="btn-cancel"
                            >
                                Annuler
                            </button>
                            <button type="submit" class="btn-submit">
                                Rejoindre
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </teleport>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import gameService from "@/services/gameService";
import type { GameRoomInfo } from "@/models/dtos/GameRoomInfo";
import { localStore } from "@/store/local";
import { gameWebSocket } from "@/views/gamewebsocket.ts";

const router = useRouter();

const rooms = ref<GameRoomInfo[]>([]);
const showCreateModal = ref(false);
const showJoinModal = ref(false);
const selectedRoom = ref<GameRoomInfo | null>(null);

const newRoomName = ref("");
const newRoomPassword = ref("");
const joinPassword = ref("");

onMounted(async () => {
    rooms.value = await gameService.listRooms();

    if (!gameWebSocket.isConnected()) {
        await gameWebSocket.connect(localStore.token);
    }

    gameWebSocket.subscribeToRooms((updatedRooms) => {
        rooms.value = updatedRooms;
    });
});

async function createRoom() {
    const roomId = await gameService.createRoom({
        name: newRoomName.value,
        password: newRoomPassword.value || undefined,
    });
    await router.push(`/game/waiting-room/${roomId}`);
}

function selectRoom(room: GameRoomInfo) {
    selectedRoom.value = room;
    showJoinModal.value = true;
}

async function joinRoom() {
    if (!selectedRoom.value) return;

    await gameService.joinRoom({
        name: selectedRoom.value.name,
        password: joinPassword.value,
    });

    showJoinModal.value = false;
    joinPassword.value = "";

    await router.push(`/game/unit-selection/${selectedRoom.value.id}`);
}

function translateStatus(status: string): string {
    const translations: Record<string, string> = {
        WAITING: "En attente",
        UNIT_SELECTION: "Sélection",
        IN_PROGRESS: "En cours",
        FINISHED: "Terminée",
    };
    return translations[status] || status;
}
</script>

<style scoped>
.game-lobby {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
}

.lobby-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

.lobby-header h1 {
    font-size: 2rem;
    margin: 0;
}

.btn-create {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 1rem 2rem;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.2s;
}

.btn-create:hover {
    transform: translateY(-2px);
}

.rooms-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1.5rem;
}

.room-card {
    background: var(--bg-surface);
    border: 2px solid var(--border-base);
    border-radius: 12px;
    padding: 1.5rem;
    cursor: pointer;
    transition: all 0.3s;
}

.room-card:hover {
    border-color: var(--border-focus);
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.room-card.has-password {
    border-left: 4px solid #f59e0b;
}

.room-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.room-header h3 {
    margin: 0;
    font-size: 1.25rem;
}

.status-badge {
    padding: 0.25rem 0.75rem;
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-badge.waiting {
    background: #fef3c7;
    color: #92400e;
}

.status-badge.unit_selection {
    background: #dbeafe;
    color: #1e40af;
}

.status-badge.in_progress {
    background: #d1fae5;
    color: #065f46;
}

.room-info {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-secondary);
}

.info-item .icon {
    font-size: 1.25rem;
}

.empty-state {
    grid-column: 1 / -1;
    text-align: center;
    padding: 4rem 2rem;
    color: var(--text-secondary);
    font-size: 1.1rem;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.75);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background: var(--bg-elevated);
    border-radius: 16px;
    padding: 2rem;
    width: 90%;
    max-width: 500px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.modal-content h2 {
    margin: 0 0 1.5rem;
}

.form-group {
    margin-bottom: 1.5rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: var(--text-bright);
}

.form-group input {
    width: 100%;
    padding: 0.75rem;
    border: 2px solid var(--border-base);
    border-radius: 8px;
    background: var(--bg-input);
    color: var(--text-bright);
    font-size: 1rem;
    box-sizing: border-box;
}

.form-group input:focus {
    outline: none;
    border-color: var(--border-focus);
}

.modal-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
}

.btn-cancel,
.btn-submit {
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.btn-cancel {
    background: var(--bg-hover);
    color: var(--text-bright);
}

.btn-submit {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.btn-cancel:hover,
.btn-submit:hover {
    transform: translateY(-2px);
}
</style>
