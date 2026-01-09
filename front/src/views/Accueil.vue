<template>
    <div
        class="futurist-layout"
        @mousemove="onMouseMove"
        @mouseleave="onResetTilt"
        :class="{ 'layout-exit': exiting }"
    >
        <div class="ambient-grid"></div>
        <div class="ambient-particles"></div>

        <header class="futurist-header" ref="headerRef">
            <h1 class="main-title glitch" data-text="ARCADE">ARCADE</h1>
        </header>

        <main class="menu-container">
            <TransitionGroup
                tag="div"
                class="menu-grid"
                appear
                name="card-stagger"
            >
                <div
                    v-for="(item, index) in hexagonItems"
                    :key="item.title"
                    class="menu-card"
                    :data-index="index"
                    @click="navigateTo(item.link)"
                    :style="cardStyle"
                >
                    <div class="card-content-3d">
                        <span class="card-title">{{ item.title }}</span>
                        <div class="scanline"></div>
                    </div>
                    <div class="card-glow-border"></div>
                </div>
            </TransitionGroup>
        </main>

        <footer class="futurist-footer" ref="footerRef">
            <p class="typing-effect">V.0.0.1</p>
        </footer>
    </div>
</template>
<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";

const exiting = ref(false);

interface MenuItem {
    title: string;
    link: string;
}

const hexagonItems: MenuItem[] = [{ title: "Territory", link: "Territory" }];

const router = useRouter();
const navigateTo = (link: string) => {
    exiting.value = true;
    setTimeout(() => {
        router.push({ name: link });
    }, 200);
};

const TILT_AMOUNT = 12;

const tiltX = ref(0);
const tiltY = ref(0);

const onMouseMove = (event: MouseEvent) => {
    const { clientX, clientY } = event;
    const { innerWidth, innerHeight } = window;

    const mouseX = (clientX - innerWidth / 2) / (innerWidth / 2);
    const mouseY = (clientY - innerHeight / 2) / (innerHeight / 2);

    tiltY.value = mouseX * TILT_AMOUNT;
    tiltX.value = -mouseY * TILT_AMOUNT;
};

const onResetTilt = () => {
    tiltX.value = 0;
    tiltY.value = 0;
};

const cardStyle = computed(() => ({
    transform: `
        perspective(1000px)
        rotateY(${tiltY.value}deg)
        rotateX(${tiltX.value}deg)
        translateZ(20px)
    `,
    boxShadow: `
        ${-tiltY.value}px
        ${-tiltX.value}px
        30px
        var(--futurist-shadow-strong)
    `,
}));
</script>

<style scoped>
.futurist-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background: rgba(30, 41, 59, 0.8);
    color: var(--futurist-text-light);
    font-family: "Droid Sans Mono", "Consolas", monospace;
    padding: 30px;
    box-sizing: border-box;
    overflow: hidden;
    position: relative;
    perspective: 1500px;

    transition:
        opacity 0.2s ease-in,
        transform 0.2s ease-in;
}

.futurist-layout.layout-exit {
    opacity: 0;
    transform: scale(1.05);
}

.ambient-grid {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background-image:
        linear-gradient(var(--futurist-border) 1px, transparent 1px),
        linear-gradient(90deg, var(--futurist-border) 1px, transparent 1px);
    background-size: 50px 50px;
    opacity: 0.08;
    transform: perspective(500px) rotateX(60deg) translateY(0);
    will-change: transform, background-position;
    backface-visibility: hidden;
    animation: grid-scroll 20s linear infinite;
    pointer-events: none;
    z-index: 0;
}

@keyframes grid-scroll {
    0% {
        transform: perspective(500px) rotateX(60deg) translateY(0);
    }
    100% {
        transform: perspective(500px) rotateX(60deg) translateY(50px);
    }
}

.ambient-particles {
    position: absolute;
    width: 100%;
    height: 100%;
    background: radial-gradient(
        circle,
        var(--futurist-accent) 1px,
        transparent 1px
    );
    background-size: 60px 60px;
    opacity: 0.1;
    animation: particle-pulse 8s ease-in-out infinite alternate;
    pointer-events: none;
    z-index: 0;
    will-change: opacity, transform;
}

@keyframes particle-pulse {
    0% {
        opacity: 0.05;
        transform: scale(0.95);
    }
    100% {
        opacity: 0.15;
        transform: scale(1.05);
    }
}

.futurist-header {
    text-align: center;
    margin-bottom: 60px;
    position: relative;
    z-index: 10;
    opacity: 0;
    transform: translateY(-50px);
    animation: header-boot 1s ease-out forwards;
}

.futurist-footer {
    opacity: 0;
    transform: translateY(20px);
    animation: footer-boot 0.8s ease-out 0.5s forwards;

    text-align: center;
    padding-top: 30px;
    color: var(--futurist-text-weak);
    font-size: 0.9em;
    z-index: 10;
}

@keyframes header-boot {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
@keyframes footer-boot {
    to {
        opacity: 0.7;
        transform: translateY(0);
    }
}

.main-title.glitch {
    font-size: 4em;
    color: var(--futurist-accent-light);
    letter-spacing: 8px;
    position: relative;
    text-shadow: 0 0 20px var(--futurist-accent);
}
.main-title.glitch::before,
.main-title.glitch::after {
    content: attr(data-text);
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: transparent;
}
.main-title.glitch::before {
    left: 2px;
    text-shadow: -2px 0 var(--futurist-danger);
    clip-path: inset(44% 0 61% 0);
    animation: glitch-anim-1 3s infinite linear alternate-reverse;
}
.main-title.glitch::after {
    left: -2px;
    text-shadow: -2px 0 var(--piece-j-light);
    clip-path: inset(20% 0 50% 0);
    animation: glitch-anim-2 2.5s infinite linear alternate-reverse;
}
@keyframes glitch-anim-1 {
    0% {
        clip-path: inset(20% 0 80% 0);
    }
    20% {
        clip-path: inset(60% 0 10% 0);
    }
    40% {
        clip-path: inset(10% 0 60% 0);
    }
    60% {
        clip-path: inset(80% 0 5% 0);
    }
    80% {
        clip-path: inset(0% 0 90% 0);
    }
    100% {
        clip-path: inset(50% 0 30% 0);
    }
}
@keyframes glitch-anim-2 {
    0% {
        clip-path: inset(10% 0 60% 0);
    }
    100% {
        clip-path: inset(80% 0 5% 0);
    }
}

.menu-container {
    flex-grow: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20px 0;
    z-index: 10;
    perspective: 1000px;
}

.menu-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 50px;
    max-width: 1300px;
    width: 100%;
    transform-style: preserve-3d;
}

@keyframes card-stagger-in {
    0% {
        opacity: 0;
        transform: perspective(1000px) rotateY(0deg) rotateX(-45deg)
            translateZ(0px) scale(0.5) translateY(60px);
    }
    100% {
        opacity: 1;
        transform: perspective(1000px) rotateY(0deg) rotateX(0deg)
            translateZ(0px) scale(1) translateY(0);
    }
}

.menu-card {
    position: relative;
    height: 200px;
    background-color: rgba(var(--futurist-bg-mid), 0.6);
    backdrop-filter: blur(5px);
    border-radius: 12px;
    cursor: pointer;

    transform-style: preserve-3d;
    will-change: transform, box-shadow;
    transform-origin: center center;
    border: 1px solid transparent;

    transition:
        transform 0.6s cubic-bezier(0.25, 1, 0.5, 1),
        box-shadow 0.6s cubic-bezier(0.25, 1, 0.5, 1),
        border-color 0.3s;

    transform: perspective(1000px) rotateY(0deg) rotateX(0deg) translateZ(0px)
        scale(1);
    box-shadow: 0 0 10px var(--futurist-shadow);

    animation: card-stagger-in 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275)
        forwards;
    opacity: 0;
}

.card-content-3d {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    transform: translateZ(40px);
    overflow: hidden;
    border-radius: 12px;
}
.card-title {
    font-size: 1.8em;
    font-weight: 800;
    color: var(--futurist-text-light);
    text-transform: uppercase;
    letter-spacing: 3px;
    text-shadow: 0 0 10px var(--futurist-accent);
}
.card-glow-border {
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border: 2px solid var(--futurist-border);
    border-radius: 14px;
    opacity: 0.5;
    transition: all 0.3s ease;
    transform: translateZ(5px);
    pointer-events: none;
}
.menu-card:hover .card-glow-border {
    border-color: var(--futurist-accent-light);
    opacity: 1;
    box-shadow: 0 0 10px var(--futurist-accent);
}
.menu-card:hover .card-title {
    color: var(--futurist-accent-light);
}
.scanline {
    position: absolute;
    top: 0;
    left: -100%;
    width: 50%;
    height: 100%;
    background: linear-gradient(
        to right,
        transparent,
        var(--futurist-shadow-strong),
        transparent
    );
    transform: skewX(-25deg);
    pointer-events: none;
    transition: left 0.5s ease-in-out;
}
.menu-card:hover .scanline {
    left: 150%;
    transition: left 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.typing-effect {
    border-right: 2px solid var(--futurist-accent);
    white-space: nowrap;
    overflow: hidden;
    display: inline-block;
    animation: blink-caret 0.75s step-end infinite;
}
@keyframes typing {
    from {
        width: 0;
    }
    to {
        width: 100%;
    }
}
@keyframes blink-caret {
    from,
    to {
        border-color: transparent;
    }
    50% {
        border-color: var(--futurist-accent);
    }
}
</style>
