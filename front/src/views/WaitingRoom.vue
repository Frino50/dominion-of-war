<template>
    <div class="waiting-room">
        <div class="room-info-card">
            <div class="room-header">
                <h1>{{ room?.name || "Chargement..." }}</h1>
                <span v-if="room?.password" class="password-badge">
                    🔒 Protégée
                </span>
            </div>

            <div class="status-section">
                <div class="status-icon">⏳</div>
                <h2>En attente d'un adversaire...</h2>
                <p class="status-text">
                    {{ participants.length }}/2 joueurs présents
                </p>
            </div>
        </div>

        <div class="participants-section">
            <h3>Participants</h3>
            <div class="participants-grid">
                <div
                    v-for="(participant, index) in participants"
                    :key="index"
                    class="participant-card"
                    :class="{
                        'is-you': participant.pseudo === localStore.pseudo,
                    }"
                >
                    <div class="participant-avatar">
                        {{ getInitials(participant.pseudo) }}
                    </div>
                    <div class="participant-info">
                        <div class="participant-name">
                            {{ participant.pseudo }}
                        </div>
                        <div class="participant-role">
                            {{ translateRole(participant.role) }}
                        </div>
                    </div>
                </div>

                <div
                    v-if="participants.length < 2"
                    class="participant-card empty"
                >
                    <div class="participant-avatar empty">?</div>
                    <div class="participant-info">
                        <div class="participant-name">En attente...</div>
                        <div class="participant-role">Joueur 2</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="actions-section">
            <button @click="copyInviteLink" class="btn-primary">
                Copier le lien d'invitation
            </button>
            <button @click="leaveRoom" class="btn-secondary">
                ← Quitter la partie
            </button>
        </div>

        <div v-if="showCopiedMessage" class="toast">Lien copié !</div>
    </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import gameService from "@/services/gameService";
import { localStore } from "@/store/local";
import { gameWebSocket } from "@/sockets/gamewebsocket.ts";
import { GameParticipantWaitingDto } from "@/models/dtos/GameParticipantWaitingDto.ts";
import { GameRoomDto } from "@/models/dtos/GameRoomDto.ts";
import { GameStatus } from "@/models/enumerations/GameStatus.ts";

const router = useRouter();

let gameRoomId = <number>0;
const room = ref<GameRoomDto | null>(null);
const participants = ref<GameParticipantWaitingDto[]>([]);
const showCopiedMessage = ref(false);

onMounted(async () => {
    window.addEventListener("beforeunload", handleBeforeUnload);
    gameRoomId = await gameService.findGameRoomActive();
    room.value = await gameService.findRoomLightDtoById(gameRoomId);
    participants.value =
        await gameService.getParticipantsWaitingDto(gameRoomId);

    gameWebSocket.subscribeToParticipants(gameRoomId, (updatedParticipants) => {
        participants.value = updatedParticipants;
    });

    gameWebSocket.subscribeToPhase(gameRoomId, (data) => {
        if (data.phase === GameStatus.UNIT_SELECTION) {
            router.push(`/game/unit-selection`);
        }
    });
});

onBeforeUnmount(async () => {
    window.removeEventListener("beforeunload", handleBeforeUnload);
});

// Gérer la fermeture brutale
function handleBeforeUnload() {
    gameService.leaveRoom(gameRoomId);
}

function getInitials(pseudo: string): string {
    if (!pseudo) return "?";

    return pseudo
        .split(" ")
        .map((word) => word[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);
}

function translateRole(role: string): string {
    const translations: Record<string, string> = {
        PLAYER_1: "Joueur 1",
        PLAYER_2: "Joueur 2",
        SPECTATOR: "Spectateur",
    };
    return translations[role] || role;
}
async function copyInviteLink() {
    const link = `${window.location.origin}/game`;

    await navigator.clipboard.writeText(link);
    showCopiedMessage.value = true;
    setTimeout(() => (showCopiedMessage.value = false), 3000);
}

async function leaveRoom() {
    await gameService.leaveRoom(gameRoomId);

    gameWebSocket.unsubscribe(`/topic/game/${gameRoomId}/participants`);
    gameWebSocket.unsubscribe(`/topic/game/${gameRoomId}/phase`);

    await router.push("/game");
}
</script>

<style scoped>
.waiting-room {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem;
    display: flex;
    flex-direction: column;
    gap: 2rem;
}

.room-info-card {
    background: var(--bg-surface);
    border-radius: 16px;
    padding: 2rem;
    border: 2px solid var(--border-base);
}

.room-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 2rem;
}

.room-header h1 {
    margin: 0;
    font-size: 2rem;
}

.password-badge {
    background: #fef3c7;
    color: #92400e;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    font-size: 0.9rem;
    font-weight: 600;
}

.status-section {
    text-align: center;
}

.status-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
    animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.5;
    }
}

.status-section h2 {
    margin: 0 0 0.5rem;
    color: var(--text-bright);
}

.status-text {
    color: var(--text-secondary);
    font-size: 1.1rem;
}

.participants-section {
    background: var(--bg-surface);
    border-radius: 16px;
    padding: 2rem;
    border: 2px solid var(--border-base);
}

.participants-section h3 {
    margin: 0 0 1.5rem;
    font-size: 1.3rem;
}

.participants-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
}

.participant-card {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: var(--bg-card);
    border-radius: 12px;
    border: 2px solid var(--border-base);
    transition: all 0.3s;
}

.participant-card.is-you {
    border-color: var(--primary);
    background: linear-gradient(
        135deg,
        rgba(102, 126, 234, 0.1) 0%,
        rgba(118, 75, 162, 0.1) 100%
    );
}

.participant-card.empty {
    opacity: 0.5;
    border-style: dashed;
}

.participant-avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    font-weight: 700;
    color: white;
}

.participant-avatar.empty {
    background: var(--bg-hover);
    color: var(--text-muted);
}

.participant-info {
    flex: 1;
}

.participant-name {
    font-weight: 600;
    font-size: 1.1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.25rem;
}

.participant-role {
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.actions-section {
    display: flex;
    gap: 1rem;
    justify-content: center;
}

.toast {
    position: fixed;
    bottom: 2rem;
    right: 2rem;
    background: #10b981;
    color: white;
    padding: 1rem 2rem;
    border-radius: 8px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@media (max-width: 768px) {
    .participants-grid {
        grid-template-columns: 1fr;
    }

    .actions-section {
        flex-direction: column;
    }
}
</style>
