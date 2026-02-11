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
                    <input v-model.number="sprite.scale" />
                </div>

                <div class="input-row">
                    <label>Nom:</label>
                    <input v-model="sprite.newName" type="text" />
                </div>

                <button class="btn-primary" @click="searchAllSprites">
                    Voir tous les sprites
                </button>
                <button class="btn-primary" @click="openStatsModal">
                    Statistiques
                </button>
            </div>
        </div>

        <div class="card-footer">
            <button
                :disabled="!sprite.scale"
                class="btn-success flex"
                @click="renameSprite()"
            >
                💾
            </button>
            <button
                class="btn-danger flex"
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

        <UnitStatsModal
            v-model:visible="showStatsModal"
            :sprite-name="sprite.name"
        />
    </div>
</template>

<script lang="ts" setup>
import Animation from "@/components/GestionSprites/Animation.vue";
import type SpriteInfo from "@/models/SpriteInfos.ts";
import ModifSpriteDto from "@/models/dtos/modifSpriteDto.ts";
import spriteService from "@/services/spriteService.ts";
import { computed, onMounted, ref } from "vue";
import SpriteModal from "@/components/GestionSprites/SpriteModal.vue";
import UnitStatsModal from "@/components/GestionSprites/UnitStatsModal.vue";

defineEmits(["delete"]);

const sprite = defineModel<SpriteInfo>({ required: true });

const listSpriteInfo = ref<SpriteInfo[]>([]);
const showModal = ref(false);
const showStatsModal = ref(false);
const animationKey = ref(0);

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

function openStatsModal() {
    showStatsModal.value = true;
}
</script>

<style scoped>
.sprite-card {
    background: var(--bg-surface);
    border: 1px solid var(--border-base);
    border-radius: 12px;
    overflow: hidden;
    transition:
        transform var(--transition-slow),
        box-shadow var(--transition-slow);
    display: flex;
    flex-direction: column;
    box-shadow: var(--shadow-md);
}

.sprite-card:hover {
    box-shadow: var(--shadow-lg);
    border-color: var(--primary);
    transform: translateY(-2px);
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
    color: var(--text-secondary);
    font-size: 0.875rem;
    text-align: right;
    font-weight: 500;
}

.card-footer {
    padding: 1rem;
    display: flex;
    gap: 0.5rem;
    border-top: 1px solid var(--border-base);
    margin-top: 1rem;
}

.card-footer button {
    text-transform: none;
    letter-spacing: normal;
}

.visual-stage {
    background: var(--bg-base);
    min-height: 150px;
    display: flex;
    align-items: end;
    justify-content: center;
    margin: 1rem 1rem 1rem 1rem;
    border-radius: 8px;
    border: 1px dashed var(--border-base);
    position: relative;
    overflow: hidden;
    transition: border-color var(--transition-base);
}

.visual-stage:hover {
    border-color: var(--border-light);
}

@media (max-width: 768px) {
    .input-row {
        flex-direction: column;
        align-items: stretch;
    }

    .input-row label {
        text-align: left;
        min-width: unset;
    }
}
</style>
