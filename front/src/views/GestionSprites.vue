<template>
    <div class="page-container">
        <GestionSpritesHeader
            @add-to-list="(addedSprite) => sprites.push(addedSprite)"
        />

        <main class="sprite-library">
            <EmptyState v-if="sprites.length === 0" />
            <div v-else class="sprite-grid">
                <SpriteCard
                    v-for="(sprite, index) in sprites"
                    :key="sprite.name"
                    v-model="sprites[index]"
                    @delete="deleteSprite(sprite.name)"
                />
            </div>
        </main>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import GestionSpritesHeader from "../components/GestionSprites/GestionSpritesHeader.vue";
import SpriteCard from "../components/GestionSprites/SpriteCard.vue";
import EmptyState from "../components/GestionSprites/EmptyState.vue";
import spriteService from "@/services/spriteService.ts";
import SpriteInfo from "@/models/SpriteInfos.ts";

const sprites = ref<SpriteInfo[]>([]);

async function getAllSpritesInfos() {
    const response = await spriteService.getAllSpritesInfos();
    sprites.value = response.data;
}

async function deleteSprite(name: string) {
    await spriteService.deleteSprite(name);
    sprites.value = sprites.value.filter((s) => s.name !== name);
}

onMounted(getAllSpritesInfos);
</script>
<style scoped>
.page-container {
    background: var(--bg-base);
    min-height: 100vh;
}

.sprite-library {
    padding: 2rem;
    max-width: 1400px;
    margin: 0 auto;
}

.sprite-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 2rem;
}

@media (max-width: 768px) {
    .sprite-library {
        padding: 1rem;
    }

    .sprite-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }
}
</style>
