<template>
    <div class="selection-container">
        <header class="header">
            <h1>Unités Disponibles</h1>
        </header>

        <div class="units-grid">
            <div
                v-for="sprite in listSpritesInfos"
                :key="sprite.name"
                class="unit-card"
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

                <div class="selection-indicator"></div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import spriteService from "@/services/spriteService.ts";
import SpriteInfo from "@/models/SpriteInfos.ts";
import Animation from "@/components/GestionSprites/Animation.vue";

const listSpritesInfos = ref<SpriteInfo[]>([]);

onMounted(async () => {
    const response = await spriteService.getAllSpritesInfos();
    listSpritesInfos.value = response.data;
});

const selectUnit = (unit: SpriteInfo) => {
    console.log("Unité sélectionnée :", unit.name);
    // Ajoutez ici votre logique de sélection (ex: router-link ou store Pinia)
};
</script>
<style scoped>
.selection-container {
    padding: 2rem;
    background-color: #1a1a1a; /* Fond sombre pour faire ressortir les sprites */
    min-height: 100vh;
    color: white;
    font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.header {
    text-align: center;
    margin-bottom: 3rem;
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

.unit-card:hover {
    transform: translateY(-10px);
    border-color: #00d4ff;
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.5);
}

.unit-preview {
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    background: radial-gradient(circle, #333 0%, #1a1a1a 100%);
}

/* Socle sous le sprite pour l'effet de profondeur */
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
    /* On s'assure que l'animation n'est pas trop petite */
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

/* Animation au clic */
.unit-card:active {
    transform: scale(0.95);
}
</style>
