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
                                    spriteInfo.height * spriteInfo.scale < 80,
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
                                <button
                                    @click="openHitboxEditor(spriteInfo)"
                                >
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
import {ref} from "vue";
import type {Hitbox} from "@/models/SpriteInfos.ts";
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
    background: #0f172a;
    width: 90%;
    max-width: 1200px;
    height: 90%;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    border: 1px solid #334155;
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem 2rem;
    background: #1e293b;
    border-bottom: 1px solid #334155;
}

.modal-header h2 {
    margin: 0;
    color: #e2e8f0;
    font-size: 1.5rem;
}

.close-btn {
    background: transparent;
    color: #94a3b8;
    font-size: 2rem;
    line-height: 1;
    cursor: pointer;
    transition: color 0.2s;
    padding: 0;
    box-shadow: none;
}

.close-btn:hover {
    color: #fff;
}

.sprites-grid {
    padding: 2rem;
    overflow-y: auto;
    display: grid;
    gap: 1.5rem;
}

.sprite-card {
    background: #1e293b;
    border-radius: 12px;
    border: 1px solid #334155;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    transition: transform 0.2s;
    height: clamp(30rem, 40vh, 32rem);
}

.sprite-card.small {
    height: clamp(25rem, 32vh, 26rem);
}

.sprite-card:hover {
    border-color: #475569;
}

.card-header {
    padding: 0.75rem 1rem;
    background: #253146;
    border-bottom: 1px solid #334155;
    display: flex;
    gap: 0.5rem;
}

.badge {
    background: #334155;
    color: #94a3b8;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
    font-weight: bold;
    font-family: monospace;
}

.badge-hitbox {
    background: #059669;
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
    color: #64748b;
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
    background: #182336;
    border-top: 1px solid #334155;
    display: flex;
    justify-content: center;
    flex-direction: column;
    gap: 1rem;
}

.empty-state {
    text-align: center;
    color: #94a3b8;
    margin-top: 4rem;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.dark-input {
    background: #0f172a;
    border: 1px solid #334155;
    color: white;
    padding: 0.6rem;
    border-radius: 6px;
    font-size: 1rem;
    flex: 1;
    box-sizing: border-box;
    transition: border-color 0.2s;
}

.dark-input:focus {
    outline: none;
    border-color: #3b82f6;
}

.btn-icon {
    flex: 1;
    padding: 0.6rem;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    margin-left: 1.2rem;
}

.btn-save {
    background: #059669;
    color: white;
}

.input-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.input-row label {
    color: #94a3b8;
    text-align: right;
}
</style>
