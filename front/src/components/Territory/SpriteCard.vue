<template>
    <div class="sprite-card">
        <div class="visual-stage">
            <Animation
                :key="animationKey"
                :frame-rate="Number(sprite.frameRate)"
                :frames="sprite.frames"
                :height="sprite.height"
                :scale="Number(sprite.scale)"
                :sprite-src="sprite.imageUrl"
                :width="sprite.width"
            />
        </div>

        <div class="card-body">
            <div class="info-group">
                <div class="input-row">
                    <label>Scale:</label>
                    <input v-model.number="sprite.scale" class="dark-input"/>
                </div>

                <div class="input-row">
                    <label>Nom:</label>
                    <input
                        v-model="sprite.newName"
                        class="dark-input"
                        type="text"
                    />
                </div>

                <button @click="searchAllSprites">
                    Voir tous les sprites
                </button>
                <button @click="jouer">Jouer</button>
            </div>
        </div>

        <div class="card-footer">
            <button
                :disabled="!sprite.scale"
                class=" btn-save"
                @click="renameSprite()"
            >
                💾
            </button>
            <button
                class=" btn-delete"
                title="Supprimer l'unité"
                @click="$emit('delete')"
            >
                🗑️
            </button>
        </div>

        <SpriteModal
            v-model="listSpriteInfo"
            v-model:sprite="sprite"
            :visible="showModal"
            @close="
                showModal = false;
                animationKey++;
            "
            @frame-rate="(value) => (sprite.frameRate = value)"
        />
        <Test v-if="showModal2" :sprite-play="spritePlay!"/>
    </div>
</template>

<script lang="ts" setup>
import Animation from "@/components/Territory/Animation.vue";
import type SpriteInfo from "@/models/SpriteInfos.ts";
import ModifSpriteDto from "@/models/dtos/modifSpriteDto.ts";
import spriteService from "@/services/spriteService.ts";
import {computed, onMounted, ref} from "vue";
import SpriteModal from "@/components/Territory/SpriteModal.vue";
import SpritePlay from "@/models/SpritePlay.ts";
import Test from "@/components/Territory/Test.vue";

defineEmits(["delete"]);

const sprite = defineModel<SpriteInfo>({required: true});

const listSpriteInfo = ref<SpriteInfo[]>([]);
const showModal = ref(false);
const animationKey = ref(0);
const showModal2 = ref(false);
const spritePlay = ref<SpritePlay>();

async function jouer() {
    spritePlay.value = await spriteService.getSpritePlay(sprite.value.name);
    showModal2.value = true;
}

onMounted(() => {
    if (!sprite.value.newName) {
        sprite.value.newName = sprite.value.name;
    }
});

const hasChanges = computed(() => {
    return (
        sprite.value.newName?.trim() !== sprite.value.name ||
        sprite.value.scale !== undefined
    );
});

async function searchAllSprites() {
    listSpriteInfo.value = await spriteService.getAllAnimationsBySpriteName(
        sprite.value.name
    );
    showModal.value = true;
}

async function renameSprite() {
    if (!hasChanges.value) return;

    const newName = sprite.value.newName.trim();

    const dto = new ModifSpriteDto(
        sprite.value.name,
        newName,
        sprite.value.scale
    );

    await spriteService.renameSprite(dto);

    sprite.value.name = newName;
}
</script>

<style scoped>
.sprite-card {
    background: #1e293b;
    border: 1px solid #334155;
    border-radius: 16px;
    overflow: hidden;
    transition: transform 0.3s ease,
    box-shadow 0.3s ease;
    display: flex;
    flex-direction: column;
}

.sprite-card:hover {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
    border-color: #3b82f6;
}

.card-body {
    padding: 0 1rem 0.5rem 1rem;
    flex: 1;
}

.info-group {
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
}

.input-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.input-row label {
    min-width: 50px;
    color: #94a3b8;
    font-size: 0.9rem;
    text-align: right;
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

.card-footer {
    padding: 1rem;
    display: flex;
    gap: 0.5rem;
    border-top: 1px solid #334155;
    margin-top: 1rem;
}

.btn-save {
    flex: 1;
    background: #059669;
    color: white;
}

.btn-save:hover:not(:disabled) {
    background: #10b981;
}

.btn-save:disabled {
    background: #334155;
    opacity: 0.5;
    cursor: not-allowed;
}

.btn-delete {
    flex: 1;
    background: #be123c;
    color: white;
}

.btn-delete:hover {
    background: #e11d48;
}

.visual-stage {
    background: #0f172a;
    min-height: 150px;
    display: flex;
    align-items: end;
    justify-content: center;
    margin: 1rem 1rem 1rem 1rem;
    border-radius: 8px;
    border: 1px dashed #334155;
    position: relative;
    overflow: hidden;
}
</style>