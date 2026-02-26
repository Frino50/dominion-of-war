<template>
    <div class="selection-container">
        <header class="header">
            <h1 class="title">Sélection des unités</h1>
            <div class="timer-container">
                <div
                    class="timer"
                    :class="{ 'timer-warning': remainingTime <= 10 }"
                >
                    <span class="time">{{ formatTime(remainingTime) }}</span>
                </div>
            </div>
        </header>

        <!-- Statut des joueurs -->
        <div class="players-status">
            <!-- Mon panel -->
            <div
                class="player-status my-status"
                :class="{ 'is-locked': isLocked }"
            >
                <div class="panel-header">
                    <h3 class="panel-title">Mes unités</h3>
                    <span
                        class="counter"
                        :class="{ full: listSpriteInfo.length === 5 }"
                    >
                        {{ listSpriteInfo.length }}/5
                    </span>
                </div>

                <div class="unit-slots">
                    <div
                        v-for="index in 5"
                        :key="'slot-' + index"
                        class="unit-slot"
                        :class="{
                            filled: listSpriteInfo[index - 1],
                            'slot-locked': isLocked,
                        }"
                    >
                        <template v-if="listSpriteInfo[index - 1]">
                            <Animation
                                :key="listSpriteInfo[index - 1].name"
                                :sprite-src="listSpriteInfo[index - 1].imageUrl"
                                :frames="listSpriteInfo[index - 1].frames"
                                :width="listSpriteInfo[index - 1].width"
                                :height="listSpriteInfo[index - 1].height"
                                :scale="listSpriteInfo[index - 1].scale"
                                :frame-rate="
                                    listSpriteInfo[index - 1].frameRate
                                "
                                class="slot-sprite"
                            />
                            <div v-if="!isLocked" class="slot-remove-hint">
                                ✕
                            </div>
                        </template>
                        <template v-else>
                            <span class="slot-index">{{ index }}</span>
                        </template>
                    </div>
                </div>

                <div v-if="!isLocked" class="lock-area">
                    <button
                        @click="lockLoadout"
                        class="lock-button"
                        :class="{ ready: listSpriteInfo.length === 5 }"
                        :disabled="listSpriteInfo.length !== 5"
                    >
                        {{
                            listSpriteInfo.length === 5
                                ? "Verrouiller ma sélection"
                                : `Choisissez encore ${5 - listSpriteInfo.length} unité(s)`
                        }}
                    </button>
                </div>
                <div v-else class="locked-indicator">
                    <span>Verrouillé</span>
                </div>
            </div>

            <!-- Panel adversaire -->
            <div
                v-if="opponent"
                class="player-status opponent-status"
                :class="{ 'opponent-locked': opponent.locked }"
            >
                <div class="panel-header">
                    <h3 class="panel-title">{{ opponent.playerPseudo }}</h3>
                    <span
                        class="counter"
                        :class="{ full: opponent.unitsSelected === 5 }"
                    >
                        {{ opponent.unitsSelected }}/5
                    </span>
                </div>

                <div class="unit-slots">
                    <div
                        v-for="index in 5"
                        :key="'opp-' + index"
                        class="unit-slot opponent-slot"
                        :class="{
                            filled: index <= opponent.unitsSelected,
                            'slot-locked': opponent.locked,
                        }"
                    >
                        <span
                            v-if="index <= opponent.unitsSelected"
                            class="slot-hidden"
                            >?</span
                        >
                        <span v-else class="slot-index">{{ index }}</span>
                    </div>
                </div>

                <div v-if="opponent.locked" class="locked-indicator">
                    <span>Verrouillé</span>
                </div>
                <div v-else class="waiting-indicator">
                    <span class="waiting-dots">
                        <span></span><span></span><span></span>
                    </span>
                    En train de choisir...
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
                    disabled:
                        (listSpriteInfo.length >= 5 &&
                            !isUnitSelected(sprite.name)) ||
                        isLocked,
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
                    <div
                        v-if="isUnitSelected(sprite.name)"
                        class="card-selected-overlay"
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
import { GameStatus } from "@/models/enumerations/GameStatus.ts";

const router = useRouter();
const gameRoomId = ref<number>(0);
const listSpritesInfos = ref<SpriteInfo[]>([]);
const listSpriteInfo = ref<SpriteInfo[]>([]);
const isLocked = ref(false);
const opponent = ref<LoadoutUpdateDto>();
const remainingTime = ref(60);

let timerInterval: number | null = null;

onMounted(async function () {
    gameRoomId.value = await gameService.findGameRoomIdByPlayerIdAndStatus(
        GameStatus.UNIT_SELECTION
    );

    const response = await spriteService.getAllSpritesInfos();
    listSpritesInfos.value = response.data;

    opponent.value = await loadoutService.getOpponent(gameRoomId.value);

    await gameService.startSelectionPhase(gameRoomId.value);

    gameWebSocket.subscribeToLoadout(
        gameRoomId.value,
        opponent.value.playerPseudo,
        handleLoadoutUpdate
    );

    gameWebSocket.subscribeToPhase(gameRoomId.value, (data) => {
        if (data.phase === GameStatus.IN_PROGRESS) {
            router.push(`/game/fight`);
        }
    });
    await updateTimer();
});

onUnmounted(function () {
    if (timerInterval) clearInterval(timerInterval);
    gameWebSocket.unsubscribeFromGameRoom(gameRoomId.value);
});

async function updateTimer() {
    remainingTime.value = await gameService.getRemainingTime(gameRoomId.value);

    timerInterval = window.setInterval(() => {
        if (remainingTime.value > 0) {
            remainingTime.value--;
        } else {
            clearInterval(timerInterval!);
        }
    }, 1000);
}

function handleLoadoutUpdate(update: LoadoutUpdateDto) {
    opponent.value = update;
}

async function selectUnit(sprite: SpriteInfo) {
    if (
        isLocked.value ||
        (listSpriteInfo.value.length >= 5 && !isUnitSelected(sprite.name))
    )
        return;

    const res = await loadoutService.selectUnit(gameRoomId.value, sprite.name);
    if (res) {
        listSpriteInfo.value.push(res);
    } else {
        listSpriteInfo.value = listSpriteInfo.value.filter(
            (u) => u.name !== sprite.name
        );
    }
}

function isUnitSelected(spriteName: string): boolean {
    return listSpriteInfo.value.some((u) => u.name === spriteName);
}

async function lockLoadout() {
    if (listSpriteInfo.value.length !== 5 || isLocked.value) return;
    await loadoutService.lockLoadout(gameRoomId.value);
    isLocked.value = true;
}

function formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
}
</script>

<style scoped>
/* ── Variables locales (surcharge / complément du global) ── */
:root {
    --slot-filled-my: var(--primary);
    --slot-filled-opp: var(--danger);
}

/* ── Layout ─────────────────────────────────────────────── */
.selection-container {
    padding: 2rem;
    min-height: 100vh;
    color: var(--text-primary);
}

/* ── Header ─────────────────────────────────────────────── */
.header {
    text-align: center;
    margin-bottom: 2.5rem;
}

.title {
    font-size: 2rem;
    font-weight: 800;
    letter-spacing: 0.05em;
    background: linear-gradient(90deg, var(--primary), var(--secondary));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 1rem;
}

.timer {
    display: inline-flex;
    align-items: center;
    gap: 0.6rem;
    background: var(--bg-surface);
    padding: 0.8rem 2rem;
    border-radius: 50px;
    border: 2px solid var(--border-base);
    font-size: 1.6rem;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    transition: border-color var(--transition-slow);
}

.timer-warning {
    border-color: var(--danger) !important;
    animation: pulse-red 0.8s infinite;
}

@keyframes pulse-red {
    0%,
    100% {
        box-shadow: 0 0 0 0 rgba(220, 38, 38, 0.4);
    }
    50% {
        box-shadow: 0 0 0 8px rgba(220, 38, 38, 0);
    }
}

/* ── Players panels ─────────────────────────────────────── */
.players-status {
    display: flex;
    gap: 2rem;
    margin-bottom: 3rem;
    justify-content: center;
    flex-wrap: wrap;
}

.player-status {
    background: var(--bg-surface);
    padding: 1.5rem;
    border-radius: 16px;
    border: 2px solid var(--border-base);
    min-width: 340px;
    transition:
        border-color var(--transition-slow),
        box-shadow var(--transition-slow);
}

.my-status {
    border-color: var(--primary);
}
.opponent-status {
    border-color: var(--danger);
}

.opponent-status.opponent-locked {
    box-shadow: 0 0 20px rgba(220, 38, 38, 0.3);
}

.panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1rem;
}

.panel-title {
    font-size: 1.1rem;
    font-weight: 700;
    margin: 0;
    color: var(--text-bright);
}

.counter {
    font-size: 0.9rem;
    font-weight: 700;
    color: var(--text-muted);
    background: var(--bg-card);
    padding: 0.2rem 0.7rem;
    border-radius: 20px;
    transition:
        color var(--transition-base),
        background var(--transition-base);
}

.counter.full {
    color: var(--success);
    background: rgba(5, 150, 105, 0.15);
}

/* ── Unit slots ─────────────────────────────────────────── */
.unit-slots {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
    flex-wrap: wrap;
}

.unit-slot {
    width: 90px;
    height: 110px;
    background: var(--bg-elevated);
    border: 2px dashed var(--border-base);
    border-radius: 10px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
    transition:
        border-color var(--transition-base),
        background var(--transition-base),
        box-shadow var(--transition-base);
}

.unit-slot.filled {
    border-style: solid;
    border-color: var(--primary);
    background: var(--bg-card);
}

.opponent-slot.filled {
    border-color: var(--danger);
    background: var(--bg-card);
}

.opponent-slot.slot-locked.filled {
    box-shadow: 0 0 10px rgba(220, 38, 38, 0.2);
}

.slot-index {
    color: var(--border-light);
    font-size: 1.6rem;
    font-weight: 700;
}

.slot-hidden {
    color: var(--danger);
    font-size: 2rem;
    font-weight: 700;
}

.slot-remove-hint {
    display: none;
    position: absolute;
    top: 4px;
    right: 4px;
    width: 18px;
    height: 18px;
    background: var(--danger);
    border-radius: 50%;
    font-size: 0.65rem;
    color: white;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
}

.unit-slot:hover .slot-remove-hint {
    display: flex;
}

/* ── Lock button ─────────────────────────────────────────── */
.lock-area {
    margin-top: 1.2rem;
}

.lock-button {
    width: 100%;
    padding: 0.85rem 1.5rem;
    background: var(--bg-card);
    border: 2px solid var(--border-base);
    border-radius: 10px;
    color: var(--text-muted);
    font-weight: 700;
    font-size: 0.9rem;
    transition: all var(--transition-slow);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
}

.lock-button.ready {
    background: linear-gradient(
        135deg,
        rgba(59, 130, 246, 0.15),
        rgba(139, 92, 246, 0.15)
    );
    border-color: var(--primary);
    color: var(--primary-light);
    box-shadow: var(--shadow-glow);
}

.lock-button.ready:hover {
    background: linear-gradient(
        135deg,
        rgba(59, 130, 246, 0.25),
        rgba(139, 92, 246, 0.25)
    );
    box-shadow: 0 0 25px rgba(59, 130, 246, 0.5);
    transform: translateY(-2px);
}

.lock-button.ready:active {
    transform: scale(0.97);
}

/* ── Locked indicator ────────────────────────────────────── */
.locked-indicator {
    margin-top: 1.2rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    font-weight: 700;
    font-size: 1rem;
    border-radius: 10px;
    padding: 0.7rem;
}

/* ── Waiting indicator ───────────────────────────────────── */
.waiting-indicator {
    margin-top: 1.2rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    color: var(--text-muted);
    font-size: 0.85rem;
}

.waiting-dots {
    display: flex;
    gap: 3px;
}

.waiting-dots span {
    width: 5px;
    height: 5px;
    background: var(--text-muted);
    border-radius: 50%;
    animation: dot-bounce 1.2s infinite;
}

.waiting-dots span:nth-child(2) {
    animation-delay: 0.2s;
}
.waiting-dots span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes dot-bounce {
    0%,
    80%,
    100% {
        transform: translateY(0);
    }
    40% {
        transform: translateY(-5px);
    }
}

/* ── Units grid ─────────────────────────────────────────── */
.units-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
    gap: 1.5rem;
    max-width: 1200px;
    margin: 0 auto;
}

.unit-card {
    position: relative;
    background: var(--bg-surface);
    border: 1px solid var(--border-base);
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.unit-card:hover:not(.disabled) {
    transform: translateY(-8px);
    border-color: var(--primary);
    box-shadow: var(--shadow-lg), var(--shadow-glow);
}

.unit-card.selected {
    border-color: var(--success);
    background: linear-gradient(180deg, var(--bg-card), var(--bg-surface));
    box-shadow: 0 0 15px rgba(5, 150, 105, 0.2);
}

.unit-card.selected:hover:not(.disabled) {
    border-color: var(--danger);
    box-shadow:
        var(--shadow-lg),
        0 0 15px rgba(220, 38, 38, 0.3);
}

.unit-card.disabled {
    opacity: 0.4;
    cursor: not-allowed;
    filter: grayscale(0.5);
}

.unit-card:active:not(.disabled) {
    transform: scale(0.96);
}

/* ── Unit preview ────────────────────────────────────────── */
.unit-preview {
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    background: radial-gradient(
        ellipse at 50% 80%,
        var(--bg-surface) 0%,
        var(--bg-elevated) 100%
    );
    overflow: hidden;
}

.pedestal {
    position: absolute;
    bottom: 18%;
    width: 55px;
    height: 12px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 50%;
    filter: blur(6px);
}

.sprite-animation {
    position: relative;
    z-index: 2;
    filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.6));
}

.card-selected-overlay {
    position: absolute;
    inset: 0;
    background: transparent;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition:
        opacity var(--transition-base),
        background var(--transition-base);
    z-index: 5;
}

.unit-card.selected:hover .card-selected-overlay {
    opacity: 1;
    background: rgba(220, 38, 38, 0.25);
}

/* ── Unit info ───────────────────────────────────────────── */
.unit-info {
    padding: 0.75rem 1rem;
    text-align: center;
    background: var(--bg-card);
    border-top: 1px solid var(--border-base);
}

.unit-name {
    margin: 0;
    font-size: 0.95rem;
    font-weight: 700;
    color: var(--text-primary);
}

/* ── Selection badge ─────────────────────────────────────── */
.selection-indicator {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 26px;
    height: 26px;
    background: var(--success);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    color: white;
    font-weight: 900;
    box-shadow: 0 2px 8px rgba(5, 150, 105, 0.5);
    animation: badge-pop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    z-index: 6;
}

@keyframes badge-pop {
    from {
        transform: scale(0);
        opacity: 0;
    }
    to {
        transform: scale(1);
        opacity: 1;
    }
}
</style>
