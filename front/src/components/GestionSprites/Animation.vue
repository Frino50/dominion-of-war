<template>
    <div
        ref="spriteEl"
        class="sprite-renderer"
        :class="{ 'is-paused': !isVisible }"
    />
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, shallowRef, computed } from "vue";
import spriteService from "@/services/spriteService.ts";

const props = defineProps({
    spriteSrc: { type: String, required: true },
    frames: { type: Number, default: 8 },
    frameRate: { type: Number, default: 8 },
    width: { type: Number, default: 120 },
    height: { type: Number, default: 120 },
    scale: { type: Number, default: 1 },
});

const spriteEl = ref<HTMLElement | null>(null);
const spriteBlobUrl = shallowRef("");
const isVisible = ref(true);
let observer: IntersectionObserver | null = null;

const cssWidth = computed(() => `${props.width / props.frames}px`);
const cssHeight = computed(() => `${props.height}px`);
const cssFullWidth = computed(() => `${props.width}px`);
const cssNegativeFullWidth = computed(() => `-${props.width}px`);
const cssDuration = computed(() => `${props.frames / props.frameRate}s`);
const cssBgUrl = computed(() => `url(${spriteBlobUrl.value})`);
const cssScale = computed(() => props.scale);

onMounted(async () => {
    observer = new IntersectionObserver(
        (entries) => {
            isVisible.value = entries[0].isIntersecting;
        },
        { threshold: 0.1 }
    );

    if (spriteEl.value) {
        observer.observe(spriteEl.value);
    }

    spriteBlobUrl.value = await spriteService.getImage(props.spriteSrc);
});

onBeforeUnmount(() => {
    if (observer) observer.disconnect();
});
</script>

<style scoped>
.sprite-renderer {
    image-rendering: pixelated;

    width: v-bind(cssWidth);
    height: v-bind(cssHeight);

    background-image: v-bind(cssBgUrl);
    background-repeat: no-repeat;
    background-size: v-bind(cssFullWidth) v-bind(cssHeight);

    transform: scale(v-bind(cssScale));
    transform-origin: bottom;

    animation: play-sprite v-bind(cssDuration) steps(v-bind("props.frames"))
        infinite;

    will-change: background-position;
}

.is-paused {
    animation-play-state: paused;
}

@keyframes play-sprite {
    from {
        background-position: 0 0;
    }
    to {
        background-position: v-bind(cssNegativeFullWidth) 0;
    }
}
</style>
