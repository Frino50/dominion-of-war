<template>
    <div class="fight-container">
        <!-- Header -->
        <header class="fight-header">
            <div class="player-info player-left">
                <span class="player-name my-name">{{ myPseudo }}</span>
            </div>
            <div class="vs-divider">⚔</div>
            <div class="player-info player-right">
                <span class="player-name opp-name">{{ opponentPseudo }}</span>
            </div>
        </header>

        <!-- Zone de combat -->
        <div class="battlefield" ref="battlefield">
            <!-- Sol -->
            <div class="ground">
                <div class="ground-line"></div>
            </div>

            <!-- Unités actives -->
            <div
                v-for="unit in activeUnits"
                :key="unit.id"
                class="unit-instance"
                :class="{
                    'unit-mine': unit.ownerPseudo === myPseudo,
                    'unit-enemy': unit.ownerPseudo !== myPseudo,
                    'facing-right': unit.speed > 0,
                    'facing-left': unit.speed < 0,
                }"
                :style="{ left: unit.x + '%' }"
            >
                <Animation
                    :sprite-src="getSpriteInfo(unit.spriteName)?.imageUrl ?? ''"
                    :frames="getSpriteInfo(unit.spriteName)?.frames ?? 1"
                    :width="getSpriteInfo(unit.spriteName)?.width ?? 64"
                    :height="getSpriteInfo(unit.spriteName)?.height ?? 64"
                    :scale="getSpriteInfo(unit.spriteName)?.scale ?? 1"
                    :frame-rate="getSpriteInfo(unit.spriteName)?.frameRate ?? 8"
                />
            </div>
        </div>

        <!-- HUD bas — mes unités -->
        <div class="hud">
            <div class="hud-units">
                <button
                    v-for="sprite in mySprites"
                    :key="sprite.name"
                    class="hud-unit-btn"
                    @click="spawnUnit(sprite)"
                    :title="sprite.name"
                >
                    <Animation
                        :sprite-src="sprite.imageUrl"
                        :frames="sprite.frames"
                        :width="sprite.width"
                        :height="sprite.height"
                        :scale="sprite.scale * 0.5"
                        :frame-rate="sprite.frameRate"
                    />
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import Animation from "@/components/GestionSprites/Animation.vue";
import { gameWebSocket } from "@/sockets/gamewebsocket.ts";
import fightService from "@/services/fightService.ts";
import loadoutService from "@/services/loadoutService.ts";
import spriteService from "@/services/spriteService.ts";
import SpriteInfo from "@/models/SpriteInfos.ts";
import { localStore } from "@/store/local.ts";
import gameService from "@/services/gameService.ts";

interface UnitInstance {
    id: string;
    spriteName: string;
    ownerPseudo: string;
    x: number;
    speed: number;
}

const gameRoomId = ref<number>(0);

const myPseudo = ref<string>(localStore.pseudo);
const opponentPseudo = ref<string>("");
const mySprites = ref<SpriteInfo[]>([]);
const allSprites = ref<SpriteInfo[]>([]);
const activeUnits = ref<UnitInstance[]>([]);

onMounted(async () => {
    gameRoomId.value = await gameService.findGameRoomActive();

    const res = await spriteService.getAllSpritesInfos();
    allSprites.value = res.data;
    // Charger mon loadout (les 5 unités sélectionnées)
    mySprites.value = await loadoutService.getMyLoadout(gameRoomId.value);

    // Charger l'adversaire
    const opponent = await loadoutService.getOpponent(gameRoomId.value);
    opponentPseudo.value = opponent.playerPseudo;

    // S'abonner à l'état du jeu (broadcast du backend toutes les 100ms)
    gameWebSocket.subscribeToGameState(
        gameRoomId.value,
        (units: UnitInstance[]) => {
            activeUnits.value = units;
        }
    );
});

onUnmounted(() => {
    gameWebSocket.unsubscribeFromGameRoom(gameRoomId.value);
});

function getSpriteInfo(spriteName: string): SpriteInfo | undefined {
    return allSprites.value.find((s) => s.name === spriteName);
}

async function spawnUnit(sprite: SpriteInfo) {
    console.log(sprite.name);
    await fightService.spawnUnit(gameRoomId.value, sprite.name);
}
</script>

<style scoped>
/* ── Layout ─────────────────────────────────────────────── */
.fight-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: hidden;
    background: var(--bg-base);
    color: var(--text-primary);
}

/* ── Header ─────────────────────────────────────────────── */
.fight-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.75rem 2rem;
    background: var(--bg-elevated);
    border-bottom: 1px solid var(--border-base);
    flex-shrink: 0;
    z-index: 10;
}

.player-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.player-name {
    font-size: 1.1rem;
    font-weight: 700;
    color: var(--text-bright);
}

.vs-divider {
    font-size: 1.5rem;
    color: var(--text-muted);
}

/* ── Battlefield ─────────────────────────────────────────── */
.battlefield {
    flex: 1;
    position: relative;
    overflow: hidden;
    background: linear-gradient(
        180deg,
        #0a0e1a 0%,
        #0f172a 60%,
        #1a2744 85%,
        #243560 100%
    );
}

/* Étoiles en fond */
.battlefield::before {
    content: "";
    position: absolute;
    inset: 0;
    background-image:
        radial-gradient(
            1px 1px at 10% 15%,
            rgba(255, 255, 255, 0.4) 0%,
            transparent 100%
        ),
        radial-gradient(
            1px 1px at 30% 8%,
            rgba(255, 255, 255, 0.3) 0%,
            transparent 100%
        ),
        radial-gradient(
            1px 1px at 55% 20%,
            rgba(255, 255, 255, 0.5) 0%,
            transparent 100%
        ),
        radial-gradient(
            1px 1px at 75% 5%,
            rgba(255, 255, 255, 0.3) 0%,
            transparent 100%
        ),
        radial-gradient(
            1px 1px at 90% 18%,
            rgba(255, 255, 255, 0.4) 0%,
            transparent 100%
        ),
        radial-gradient(
            1px 1px at 20% 35%,
            rgba(255, 255, 255, 0.2) 0%,
            transparent 100%
        ),
        radial-gradient(
            1px 1px at 65% 12%,
            rgba(255, 255, 255, 0.3) 0%,
            transparent 100%
        );
    pointer-events: none;
}

/* Sol */
.ground {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80px;
    background: linear-gradient(180deg, #1e3a1e 0%, #0f2010 100%);
}

.ground-line {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(
        90deg,
        transparent,
        rgba(59, 130, 246, 0.5) 20%,
        rgba(59, 130, 246, 0.8) 50%,
        rgba(220, 38, 38, 0.8) 50%,
        rgba(220, 38, 38, 0.5) 80%,
        transparent
    );
    box-shadow: 0 0 10px rgba(100, 150, 255, 0.3);
}

/* ── Unités sur le champ ─────────────────────────────────── */
.unit-instance {
    position: absolute;
    bottom: 80px; /* posé sur le sol */
    transform: translateX(-50%);
    transition: left 0.1s linear;
    z-index: 5;
}

.unit-mine {
    filter: drop-shadow(0 0 6px rgba(59, 130, 246, 0.7));
}

.unit-enemy {
    filter: drop-shadow(0 0 6px rgba(220, 38, 38, 0.7));
}

/* Retourner le sprite selon la direction */
.facing-left :deep(canvas),
.facing-left :deep(img) {
    transform: scaleX(-1);
}

/* ── HUD ─────────────────────────────────────────────────── */
.hud {
    flex-shrink: 0;
    background: var(--bg-elevated);
    border-top: 2px solid var(--border-base);
    padding: 0.75rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 1.5rem;
    z-index: 10;
}

.hud-units {
    display: flex;
    gap: 0.75rem;
    flex: 1;
    justify-content: center;
}

.hud-unit-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.3rem;
    padding: 0.5rem 0.75rem;
    background: var(--bg-surface);
    border: 2px solid var(--border-base);
    border-radius: 10px;
    cursor: pointer;
    transition: all var(--transition-base);
    color: var(--text-primary);
    min-width: 80px;
}

.hud-unit-btn:hover {
    border-color: var(--primary);
    background: var(--bg-hover);
    transform: translateY(-4px);
    box-shadow: var(--shadow-glow);
}

.hud-unit-btn:active {
    transform: translateY(-1px) scale(0.97);
}
</style>
