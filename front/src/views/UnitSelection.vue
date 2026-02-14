<template>
    <div class="selection-container">
        <header class="header">
            <h1>Sélection des Unités</h1>
            <div class="timer-container">
                <div
                    class="timer"
                    :class="{ 'timer-warning': remainingTime <= 10 }"
                >
                    <i class="clock-icon">⏱️</i>
                    <span class="time">{{ formatTime(remainingTime) }}</span>
                </div>
            </div>
        </header>

        <!-- Statut des joueurs -->
        <div class="players-status">
            <div class="player-status my-status">
                <h3>Mes unités ({{ listSpriteInfo.length }}/5)</h3>
                <div class="unit-slots">
                    <div
                        v-for="index in 5"
                        :key="'slot-' + index"
                        class="unit-slot"
                        :class="{ filled: optimisticListSpriteInfo[index - 1] }"
                    >
                        <Animation
                            v-if="optimisticListSpriteInfo[index - 1]"
                            :key="optimisticListSpriteInfo[index - 1].name"
                            :sprite-src="
                                optimisticListSpriteInfo[index - 1].imageUrl
                            "
                            :frames="optimisticListSpriteInfo[index - 1].frames"
                            :width="optimisticListSpriteInfo[index - 1].width"
                            :height="optimisticListSpriteInfo[index - 1].height"
                            :scale="
                                optimisticListSpriteInfo[index - 1].scale * 0.5
                            "
                            :frame-rate="
                                optimisticListSpriteInfo[index - 1].frameRate
                            "
                            class="slot-sprite"
                        />
                        <span v-else class="slot-empty">{{ index }}</span>
                    </div>
                </div>
                <button
                    v-if="!isLocked"
                    @click="lockLoadout"
                    class="lock-button"
                >
                    Verrouiller ma sélection
                </button>
                <div v-if="isLocked" class="locked-indicator">
                    ✓ Sélection verrouillée
                </div>
            </div>

            <div v-if="opponent" class="player-status opponent-status">
                <h3>
                    {{ opponent.playerPseudo }} ({{ opponent.unitsSelected }}/5)
                </h3>
                <div class="unit-slots">
                    <div
                        v-for="index in 5"
                        :key="index"
                        class="unit-slot"
                        :class="{ filled: index <= opponent.unitsSelected }"
                    >
                        <span
                            v-if="index <= opponent.unitsSelected"
                            class="slot-hidden"
                            >?</span
                        >
                        <span v-else class="slot-empty">{{ index }}</span>
                    </div>
                </div>
                <div v-if="opponent.isLocked" class="locked-indicator">
                    ✓ Sélection verrouillée
                </div>
            </div>
        </div>

        <!-- Grille des unités disponibles -->
        <div class="units-grid">
            <div
                v-for="sprite in listSpritesInfos"
                :key="sprite.name"
                class="unit-card"
                :class="{
                    selected: isUnitSelected(sprite.name),
                    disabled: listSpriteInfo.length >= 5 || isLocked,
                }"
                @click="selectUnit(sprite)"
            >
                <div class="unit-preview">
                    <div class="pedestal"></div>
                    <Animation
                        :sprite-src="sprite.imageUrl"
                        :frames="sprite.frames"
                        :width="sprite.width"
                        :height="sprite.height"
                        :scale="sprite.scale"
                        :frame-rate="sprite.frameRate"
                        class="sprite-animation"
                    />
                </div>

                <div class="unit-info">
                    <h3 class="unit-name">{{ sprite.name }}</h3>
                </div>

                <div
                    v-if="isUnitSelected(sprite.name)"
                    class="selection-indicator"
                >
                    ✓
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import spriteService from "@/services/spriteService.ts";
import loadoutService from "@/services/loadoutService.ts";
import gameService from "@/services/gameService.ts";
import SpriteInfo from "@/models/SpriteInfos.ts";
import Animation from "@/components/GestionSprites/Animation.vue";
import { gameWebSocket } from "@/views/gamewebsocket.ts";
import type { LoadoutUpdateDto } from "@/models/dtos/LoadoutUpdateDto.ts";

const router = useRouter();
const gameRoomId = ref<number>(0);
const listSpritesInfos = ref<SpriteInfo[]>([]);
const listSpriteInfo = ref<SpriteInfo[]>([]);
const isLocked = ref(false);
const opponent = ref<LoadoutUpdateDto>();
const remainingTime = ref(60);

let timerInterval: number | null = null;

onMounted(async function () {
    gameRoomId.value =
        await gameService.findGameRoomIdByPlayerIdAndStatusUnitSelection();

    const response = await spriteService.getAllSpritesInfos();
    listSpritesInfos.value = response.data;

    opponent.value = await loadoutService.loadOpponentStatus(gameRoomId.value);

    // Démarrer le timer côté serveur
    await gameService.startSelectionPhase(gameRoomId.value);

    // Vérifier si le WebSocket est déjà connecté, sinon le connecter
    if (!gameWebSocket.isConnected()) {
        const token = localStorage.getItem("token") || "";
        await gameWebSocket.connect(token);
    }

    // S'abonner aux mises à jour de loadout
    gameWebSocket.subscribeToLoadout(
        gameRoomId.value,
        opponent.value.playerPseudo,
        handleLoadoutUpdate
    );

    // S'abonner aux changements de phase
    gameWebSocket.subscribeToPhase(gameRoomId.value, handlePhaseChange);

    // Mise à jour du timer toutes les secondes
    timerInterval = window.setInterval(updateTimer, 1000);
});

onUnmounted(function () {
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    // Se désabonner des topics de cette room
    gameWebSocket.unsubscribeFromGameRoom(gameRoomId.value);
});

async function updateTimer() {
    remainingTime.value = await gameService.getRemainingTime(gameRoomId.value);

    if (remainingTime.value <= 0) {
        if (timerInterval) {
            clearInterval(timerInterval);
        }
    }
}

function handleLoadoutUpdate(update: LoadoutUpdateDto) {
    opponent.value = update;
}

function handlePhaseChange(data: { phase: string; duration?: number }) {
    if (data.phase === "FIGHT") {
        router.push(`/game/fight/${gameRoomId.value}`);
    }
}

// File d'attente typée
const actionQueue: string[] = [];
let isProcessingQueue = false;

// État optimiste pour l'affichage immédiat
const optimisticListSpriteInfo = ref<SpriteInfo[]>([]);

/**
 * Gère le clic sur une unité avec mise à jour immédiate de l'interface
 */
const selectUnit = (sprite: SpriteInfo): void => {
    if (isLocked.value) return;

    // 1. MISE À JOUR OPTIMISTE
    const index = optimisticListSpriteInfo.value.findIndex(
        (u) => u.name === sprite.name
    );

    if (index !== -1) {
        // Suppression : on retire l'élément
        optimisticListSpriteInfo.value.splice(index, 1);
    } else if (optimisticListSpriteInfo.value.length < 5) {
        // Ajout : On fait une copie de l'objet pour éviter les références partagées
        // qui causent souvent les bugs d'image
        optimisticListSpriteInfo.value.push({ ...sprite });
    } else {
        return;
    }

    // 2. EMPILE L'ACTION (le nom suffit pour l'API)
    actionQueue.push(sprite.name);

    // 3. TRAITEMENT DE LA QUEUE
    processQueue();

    // 4. FEEDBACK SONORE
    const audio = new Audio("/sounds/click.mp3");
    audio.volume = 0.3;
    audio.play().catch(() => {});
};

const processQueue = async (): Promise<void> => {
    if (isProcessingQueue || actionQueue.length === 0) return;
    isProcessingQueue = true;

    while (actionQueue.length > 0) {
        const spriteName = actionQueue.shift();
        try {
            const res = await loadoutService.selectUnit(
                gameRoomId.value,
                spriteName!
            );

            listSpriteInfo.value.push(res);
        } catch (error) {
            const res = await loadoutService.selectUnit(
                gameRoomId.value,
                spriteName!
            );
            listSpriteInfo.value.push(res);

            optimisticListSpriteInfo.value = [...listSpriteInfo.value];
        }
    }
    isProcessingQueue = false;
};

/**
 * Helper de vérification visuelle (utilisé pour les classes CSS)
 */
function isUnitSelected(spriteName: string): boolean {
    return optimisticListSpriteInfo.value.some((u) => u.name === spriteName);
}

const isLocking = ref(false);

async function lockLoadout(): Promise<void> {
    // On se base sur l'affichage (optimiste) pour la condition des 5 unités
    if (
        optimisticListSpriteInfo.value.length !== 5 ||
        isLocked.value ||
        isLocking.value
    ) {
        return;
    }

    isLocking.value = true;

    try {
        // Sécurité : Si l'utilisateur clique très vite sur le 5ème sprite puis sur Verrouiller,
        // on attend que processQueue ait fini d'envoyer le 5ème sprite au serveur.
        while (isProcessingQueue) {
            await new Promise((resolve) => setTimeout(resolve, 50));
        }

        await loadoutService.lockLoadout(gameRoomId.value);

        // On change l'état local immédiatement après le succès API
        isLocked.value = true;
    } catch (error) {
        console.error("Erreur verrouillage:", error);
    } finally {
        isLocking.value = false;
    }
}

function formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
}
</script>

<style scoped>
.selection-container {
    padding: 2rem;
    background-color: #1a1a1a;
    min-height: 100vh;
    color: white;
    font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.header {
    text-align: center;
    margin-bottom: 2rem;
}

.timer-container {
    margin-top: 1rem;
}

.timer {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    background: linear-gradient(145deg, #2a2a2a, #222);
    padding: 1rem 2rem;
    border-radius: 12px;
    border: 2px solid #444;
    font-size: 1.5rem;
    font-weight: bold;
}

.timer-warning {
    border-color: #ff4444;
    animation: pulse 1s infinite;
}

@keyframes pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.7;
    }
}

.clock-icon {
    font-style: normal;
    font-size: 1.8rem;
}

.players-status {
    display: flex;
    gap: 2rem;
    margin-bottom: 3rem;
    justify-content: center;
    flex-wrap: wrap;
}

.player-status {
    background: linear-gradient(145deg, #2a2a2a, #222);
    padding: 1.5rem;
    border-radius: 12px;
    border: 2px solid #444;
    min-width: 300px;
}

.my-status {
    border-color: #00d4ff;
}

.opponent-status {
    border-color: #ff4444;
}

.unit-slots {
    display: flex;
    gap: 0.5rem;
    margin-top: 1rem;
    justify-content: center;
}

.unit-slot {
    width: 60px;
    height: 60px;
    background: #1a1a1a;
    border: 2px solid #333;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

.unit-slot.filled {
    border-color: #00d4ff;
    background: #2a2a2a;
}

.slot-empty {
    color: #666;
    font-size: 1.5rem;
}

.slot-hidden {
    color: #ff4444;
    font-size: 2rem;
}

.slot-sprite {
    max-width: 100%;
    max-height: 100%;
}

.lock-button {
    margin-top: 1rem;
    padding: 0.8rem 1.5rem;
    background: linear-gradient(145deg, #00d4ff, #0099cc);
    border: none;
    border-radius: 8px;
    color: white;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s;
    width: 100%;
}

.lock-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 212, 255, 0.4);
}

.locked-indicator {
    margin-top: 1rem;
    color: #00ff00;
    font-weight: bold;
    text-align: center;
    font-size: 1.1rem;
}

.units-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 2rem;
    max-width: 1200px;
    margin: 0 auto;
}

.unit-card {
    position: relative;
    background: linear-gradient(145deg, #2a2a2a, #222);
    border: 1px solid #444;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.unit-card:hover:not(.disabled) {
    transform: translateY(-10px);
    border-color: #00d4ff;
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.5);
}

.unit-card.selected {
    border-color: #00ff00;
    background: linear-gradient(145deg, #2a4a2a, #224422);
}

.unit-card.disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.unit-preview {
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    background: radial-gradient(circle, #333 0%, #1a1a1a 100%);
}

.pedestal {
    position: absolute;
    bottom: 30%;
    width: 60px;
    height: 20px;
    background: rgba(0, 0, 0, 0.4);
    border-radius: 50%;
    filter: blur(4px);
}

.sprite-animation {
    position: relative;
    z-index: 2;
    filter: drop-shadow(0 5px 15px rgba(0, 0, 0, 0.5));
}

.unit-info {
    padding: 1rem;
    text-align: center;
    background: #222;
    border-top: 1px solid #333;
}

.unit-name {
    margin: 0;
    font-size: 1.1rem;
    font-weight: bold;
}

.selection-indicator {
    position: absolute;
    top: 10px;
    right: 10px;
    width: 30px;
    height: 30px;
    background: #00ff00;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.2rem;
    color: white;
}

.unit-card:active:not(.disabled) {
    transform: scale(0.95);
}
</style>
