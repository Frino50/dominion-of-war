<template>
    <teleport to="body">
        <transition name="fade">
            <div
                v-if="visible"
                class="modal-overlay"
                @click.self="$emit('close')"
            >
                <div class="modal-content">
                    <header class="modal-header">
                        <h2>Inspecteur de Sprites</h2>
                        <button
                            class="close-btn"
                            @click="$emit('close')"
                            aria-label="Fermer"
                        >
                            ✕
                        </button>
                    </header>

                    <div class="sprites-grid">
                        <div
                            class="sprite-card"
                            :class="{
                                small:
                                    spriteInfo.height * spriteInfo.scale < 50,
                            }"
                            v-for="spriteInfo in listSprites"
                            :key="spriteInfo.animationId"
                        >
                            <div class="card-header">
                                <span class="badge"
                                    >ID: {{ spriteInfo.animationId }}</span
                                >
                                <span
                                    v-if="
                                        spriteInfo.hitboxX !== undefined &&
                                        spriteInfo.hitboxX !== null
                                    "
                                    class="badge badge-hitbox"
                                    >Hitbox définie</span
                                >
                            </div>

                            <div class="card-body">
                                <div class="preview-box">
                                    <span class="label">Rendu Animation</span>
                                    <div class="animation-container">
                                        <Animation
                                            :key="`${spriteInfo.animationId}-${refreshTrigger}`"
                                            :sprite-src="spriteInfo.imageUrl"
                                            :width="spriteInfo.width"
                                            :height="spriteInfo.height"
                                            :frames="spriteInfo.frames"
                                            :scale="Number(spriteInfo.scale)"
                                            :frame-rate="
                                                Number(spriteInfo.frameRate)
                                            "
                                        />
                                    </div>
                                </div>

                                <div class="preview-box">
                                    <span class="label">Planche Source</span>
                                    <div class="sheet-container">
                                        <SpriteSheet
                                            :key="`${spriteInfo.animationId}-${refreshTrigger}`"
                                            :sprite-src="spriteInfo.imageUrl"
                                            :width="spriteInfo.width"
                                            :height="spriteInfo.height"
                                        />
                                    </div>
                                </div>
                            </div>

                            <div class="card-footer">
                                <button @click="openHitboxEditor(spriteInfo)">
                                    <span>Éditer Hitbox</span>
                                </button>
                                <button
                                    @click="
                                        reBuildImage(
                                            spriteInfo.animationId,
                                            spriteInfo.imageUrl
                                        )
                                    "
                                >
                                    <span>Améliorer le sprite</span>
                                </button>
                                <button
                                    @click="
                                        flipHorizontal(
                                            spriteInfo.animationId,
                                            spriteInfo.imageUrl
                                        )
                                    "
                                >
                                    <span>Tourner le sprite</span>
                                </button>
                                <div class="input-row">
                                    <label>Frame Rate:</label>
                                    <input
                                        type="text"
                                        v-model="spriteInfo.frameRate"
                                        class="dark-input"
                                    />
                                    <button
                                        class="btn-icon btn-save"
                                        @click="
                                            saveFrameRate(
                                                spriteInfo.animationId,
                                                spriteInfo.frameRate
                                            )
                                        "
                                        :disabled="!spriteInfo.frameRate"
                                    >
                                        💾
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div v-if="!listSprites?.length" class="empty-state">
                        Aucun sprite à afficher.
                    </div>
                </div>
            </div>
        </transition>

        <transition name="fade">
            <div
                v-if="showHitboxEditor"
                class="modal-overlay"
                @click.self="closeHitboxEditor"
            >
                <HitboxEditor
                    :sprite="currentSpriteForHitbox!"
                    @close="closeHitboxEditor"
                    @saved="onHitboxSaved"
                />
            </div>
        </transition>
    </teleport>
</template>

<script setup lang="ts">
import Animation from "@/components/Territory/Animation.vue";
import type SpriteInfo from "@/models/SpriteInfos.ts";
import spriteService from "@/services/spriteService.ts";
import SpriteSheet from "@/components/Territory/SpriteSheet.vue";
import { ref } from "vue";
import type { Hitbox } from "@/models/SpriteInfos.ts";
import HitboxEditor from "@/components/Territory/HitboxEditor.vue";

defineProps<{
    visible: boolean;
}>();

const emit = defineEmits(["close", "frameRate"]);
const sprite = defineModel<SpriteInfo>("sprite");
const listSprites = defineModel<SpriteInfo[]>();
const refreshTrigger = ref(Date.now());
const showHitboxEditor = ref(false);
const currentSpriteForHitbox = ref<SpriteInfo | null>(null);

async function reBuildImage(animationId: number, spriteUrl: string) {
    if (!listSprites.value) return;

    const updatedSprite: SpriteInfo = await spriteService.normalizeSpriteSheet(
        animationId,
        spriteUrl
    );

    const index = listSprites.value.findIndex(
        (s) => s.animationId === animationId
    );

    if (index !== -1) {
        listSprites.value[index] = updatedSprite;
    }
    if (updatedSprite.animationId === sprite.value?.animationId) {
        sprite.value.height = updatedSprite.height;
        sprite.value.width = updatedSprite.width;
    }
    refreshTrigger.value = Date.now();
}

async function flipHorizontal(animationId: number, spriteUrl: string) {
    if (!listSprites.value) return;

    await spriteService.flipHorizontal(animationId, spriteUrl);
    refreshTrigger.value = Date.now();
}

async function saveFrameRate(animationId: number, frameRate: number) {
    await spriteService.saveFrameRate(animationId, frameRate);
    emit("frameRate", frameRate);
}

function openHitboxEditor(spriteInfo: SpriteInfo) {
    currentSpriteForHitbox.value = spriteInfo;
    showHitboxEditor.value = true;
}

function closeHitboxEditor() {
    showHitboxEditor.value = false;
    currentSpriteForHitbox.value = null;
}

function onHitboxSaved(hitbox: Hitbox | null) {
    if (!listSprites.value || !currentSpriteForHitbox.value) return;

    const index = listSprites.value.findIndex(
        (s) => s.animationId === currentSpriteForHitbox.value!.animationId
    );

    if (index !== -1) {
        if (hitbox) {
            listSprites.value[index].hitboxX = hitbox.x;
            listSprites.value[index].hitboxY = hitbox.y;
            listSprites.value[index].hitboxWidth = hitbox.width;
            listSprites.value[index].hitboxHeight = hitbox.height;
        } else {
            listSprites.value[index].hitboxX = undefined;
            listSprites.value[index].hitboxY = undefined;
            listSprites.value[index].hitboxWidth = undefined;
            listSprites.value[index].hitboxHeight = undefined;
        }
    }

    refreshTrigger.value = Date.now();
}
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.75);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 999;
}

.modal-content {
    background: var(--bg-elevated);
    width: 90%;
    max-width: 1200px;
    height: 90%;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    box-shadow: var(--shadow-xl);
    border: 1px solid var(--border-base);
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem 2rem;
    background: var(--bg-surface);
    border-bottom: 1px solid var(--border-base);
}

.modal-header h2 {
    margin: 0;
    color: var(--text-bright);
    font-size: 1.5rem;
}

.close-btn {
    background: transparent;
    color: var(--text-secondary);
    font-size: 2rem;
    line-height: 1;
    cursor: pointer;
    transition: color var(--transition-base);
    padding: 0;
    box-shadow: none;
    border: none;
}

.close-btn:hover {
    color: var(--text-bright);
    transform: none;
}

.sprites-grid {
    padding: 2rem;
    overflow-y: auto;
    display: grid;
    gap: 1.5rem;
}

.sprite-card {
    background: var(--bg-surface);
    border-radius: 12px;
    border: 1px solid var(--border-base);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    transition: transform var(--transition-base);
    height: clamp(30rem, 40vh, 32rem);
}

.sprite-card.small {
    height: clamp(25rem, 32vh, 26rem);
}

.sprite-card:hover {
    border-color: var(--border-light);
}

.card-header {
    padding: 0.75rem 1rem;
    background: var(--bg-card);
    border-bottom: 1px solid var(--border-base);
    display: flex;
    gap: 0.5rem;
}

.badge {
    background: var(--bg-hover);
    color: var(--text-secondary);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
    font-weight: 600;
    font-family: monospace;
}

.badge-hitbox {
    background: var(--success);
    color: white;
}

.card-body {
    padding: 1rem 1rem 0 1rem;
    display: flex;
    gap: 1rem;
    flex-grow: 1;
    align-items: flex-start;
    justify-content: space-between;
}

.card-body .preview-box:first-child {
    flex: 0 0 20%;
}

.card-body .preview-box:last-child {
    flex: 0 0 80%;
}

.label {
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-muted);
    font-weight: 600;
}

.preview-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    flex: 1;
    overflow: hidden;
    height: 100%;
}

.animation-container {
    display: flex;
    align-items: flex-end;
    justify-content: center;
    height: 100%;
    width: 100%;
}

.sheet-container {
    overflow-x: auto;
    overflow-y: hidden;
    display: flex;
    align-items: end;
    width: 100%;
    height: 100%;
    padding-bottom: 1rem;
}

.card-footer {
    padding: 1rem;
    background: var(--bg-card);
    border-top: 1px solid var(--border-base);
    display: flex;
    justify-content: center;
    flex-direction: column;
    gap: 1rem;
}

.card-footer button {
    text-transform: none;
    letter-spacing: normal;
}

.empty-state {
    text-align: center;
    color: var(--text-secondary);
    margin-top: 4rem;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity var(--transition-slow);
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.dark-input {
    background: var(--bg-input);
    border: 1px solid var(--border-base);
    color: var(--text-bright);
    padding: 0.6rem;
    border-radius: 6px;
    font-size: 1rem;
    flex: 1;
    box-sizing: border-box;
    transition: border-color var(--transition-base);
}

.dark-input:focus {
    outline: none;
    border-color: var(--border-focus);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-icon {
    flex: 1;
    padding: 0.6rem;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: all var(--transition-base);
    margin-left: 1.2rem;
}

.btn-save {
    background: var(--success);
    color: white;
    box-shadow: 0 0 15px rgba(5, 150, 105, 0.4);
}

.btn-save:hover:not(:disabled) {
    background: #10b981;
}

.input-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.input-row label {
    color: var(--text-secondary);
    text-align: right;
    font-size: 0.875rem;
}

@media (max-width: 768px) {
    .modal-content {
        width: 95%;
        height: 95%;
    }

    .sprites-grid {
        padding: 1rem;
    }

    .card-body {
        flex-direction: column;
    }

    .card-body .preview-box:first-child,
    .card-body .preview-box:last-child {
        flex: 1 1 auto;
    }
}
</style>
