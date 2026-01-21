<template>
    <div
        class="character-container"
        :style="containerStyle"
        @mousedown="handleAttack"
    >
        <Animation
            :key="currentActiveSprite.imageUrl"
            :sprite-src="currentActiveSprite.imageUrl"
            :width="currentActiveSprite.width"
            :height="currentActiveSprite.height"
            :frames="currentActiveSprite.frames"
            :scale="Number(currentActiveSprite.scale)"
            :frame-rate="Number(currentActiveSprite.frameRate)"
            :style="flipStyle"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, type CSSProperties } from "vue";
import Animation from "@/components/Territory/Animation.vue";
import SpritePlay from "@/models/SpritePlay.ts";

const props = defineProps<{ spritePlay: SpritePlay }>();

const x = ref(0);
const direction = ref(1);
const isWalking = ref(false);
const isAttacking = ref(false);

const keys = {
    ArrowRight: false,
    ArrowLeft: false,
};

let animationFrameId: number;

const currentActiveSprite = computed(() => {
    if (isAttacking.value) return props.spritePlay.attack;
    return isWalking.value ? props.spritePlay.walk : props.spritePlay.idle;
});

const containerStyle = computed(
    (): CSSProperties => ({
        position: "absolute",
        left: `${x.value}px`,
        bottom: "50px",
        cursor: "pointer",
    })
);

const flipStyle = computed(
    (): CSSProperties => ({
        transform: `scaleX(${direction.value})`,
        display: "block",
    })
);

const updateMovement = () => {
    if (!isAttacking.value) {
        let moving = false;

        if (keys.ArrowRight) {
            x.value += 2;
            direction.value = 1;
            moving = true;
        } else if (keys.ArrowLeft) {
            x.value -= 2;
            direction.value = -1;
            moving = true;
        }

        isWalking.value = moving;
    }
    animationFrameId = requestAnimationFrame(updateMovement);
};

const handleAttack = () => {
    if (isAttacking.value) return;
    isAttacking.value = true;
    setTimeout(() => (isAttacking.value = false), 600);
};

const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key in keys) keys[e.key as keyof typeof keys] = true;
};

const handleKeyUp = (e: KeyboardEvent) => {
    if (e.key in keys) keys[e.key as keyof typeof keys] = false;
};

onMounted(() => {
    window.addEventListener("keydown", handleKeyDown);
    window.addEventListener("keyup", handleKeyUp);
    animationFrameId = requestAnimationFrame(updateMovement);
});

onUnmounted(() => {
    window.removeEventListener("keydown", handleKeyDown);
    window.removeEventListener("keyup", handleKeyUp);
    cancelAnimationFrame(animationFrameId);
});
</script>
