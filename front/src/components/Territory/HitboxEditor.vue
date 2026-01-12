<template>
    <div class="hitbox-editor-overlay" @click.self="$emit('close')">
        <div class="hitbox-editor">
            <header class="editor-header">
                <div class="header-title">
                    <h2>Éditeur de Hitbox</h2>
                </div>
                <div class="header-actions">
                    <span class="zoom-info">{{ Math.round(zoom * 100) }}%</span>
                    <button
                        class="close-btn"
                        @click="$emit('close')"
                        title="Fermer"
                        aria-label="Fermer"
                    >
                        ✕
                    </button>
                </div>
            </header>

            <div class="editor-layout">
                <div class="canvas-wrapper" @wheel="handleWheel">
                    <div class="canvas-scroller">
                        <canvas
                            ref="canvasRef"
                            @mousedown="startDrag"
                            @mousemove="onMouseMove"
                            @mouseup="endDrag"
                            @mouseleave="endDrag"
                            class="hitbox-canvas"
                        />
                    </div>
                    <div class="canvas-toolbar">
                        <button @click="zoomOut" title="Zoom Arrière">-</button>
                        <button @click="resetZoom" title="Réinitialiser Zoom">
                            1:1
                        </button>
                        <button @click="zoomIn" title="Zoom Avant">+</button>
                    </div>
                </div>

                <aside class="sidebar">
                    <div class="form-section">
                        <h4>Position</h4>
                        <div class="input-row">
                            <div class="input-group">
                                <label>X</label>
                                <input
                                    type="number"
                                    v-model.number="hitbox.x"
                                    @input="requestDraw"
                                />
                            </div>
                            <div class="input-group">
                                <label>Y</label>
                                <input
                                    type="number"
                                    v-model.number="hitbox.y"
                                    @input="requestDraw"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="form-section">
                        <h4>Dimensions</h4>
                        <div class="input-row">
                            <div class="input-group">
                                <label>Largeur</label>
                                <input
                                    type="number"
                                    v-model.number="hitbox.width"
                                    @input="requestDraw"
                                />
                            </div>
                            <div class="input-group">
                                <label>Hauteur</label>
                                <input
                                    type="number"
                                    v-model.number="hitbox.height"
                                    @input="requestDraw"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="info-box">
                        <small
                            >💡 Utilisez les flèches du clavier pour déplacer la
                            sélection au pixel près.</small
                        >
                    </div>

                    <div class="sidebar-footer">
                        <button class="btn btn-primary" @click="saveHitbox">
                            Sauvegarder
                        </button>
                        <div class="btn-row">
                            <button
                                class="btn btn-secondary"
                                @click="resetHitbox"
                            >
                                Reset
                            </button>
                            <button
                                class="btn btn-danger"
                                @click="deleteHitbox"
                                v-if="hasHitbox"
                            >
                                Supprimer
                            </button>
                        </div>
                    </div>
                </aside>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, onUnmounted } from "vue";
import type SpriteInfo from "@/models/SpriteInfos.ts";
import type { Hitbox } from "@/models/SpriteInfos.ts";
import spriteService from "@/services/spriteService.ts";

const props = defineProps<{
    sprite: SpriteInfo;
}>();

const emit = defineEmits(["close", "saved"]);

const canvasRef = ref<HTMLCanvasElement | null>(null);
const zoom = ref(2);
const VIEW_PADDING = 100;

const hitbox = ref<Hitbox>({
    x: props.sprite.hitboxX ?? 0,
    y: props.sprite.hitboxY ?? 0,
    width: props.sprite.hitboxWidth ?? 32,
    height: props.sprite.hitboxHeight ?? 32,
});

const isDragging = ref(false);
const dragMode = ref<"move" | "resize" | null>(null);
const dragCorner = ref<string | null>(null);
const dragStart = ref({ x: 0, y: 0 });
const initialHitbox = ref<Hitbox>({ ...hitbox.value });

let spriteImage: HTMLImageElement | null = null;
const frameWidth = computed(() => props.sprite.width / props.sprite.frames);
const hasHitbox = computed(
    () => props.sprite.hitboxX !== undefined && props.sprite.hitboxX !== null
);

watch(() => props.sprite, loadSprite, { immediate: true });

onMounted(() => {
    window.addEventListener("keydown", handleKeyDown);
    updateCanvasSize();
});

onUnmounted(() => {
    window.removeEventListener("keydown", handleKeyDown);
});

watch(zoom, () => {
    updateCanvasSize();
    requestDraw();
});

async function loadSprite() {
    const blobUrl = await spriteService.getImage(props.sprite.imageUrl);
    spriteImage = new Image();
    spriteImage.onload = () => {
        updateCanvasSize();
        requestDraw();
    };
    spriteImage.src = blobUrl;
}

function updateCanvasSize() {
    if (canvasRef.value) {
        const logicalWidth = frameWidth.value + VIEW_PADDING * 2;
        const logicalHeight = props.sprite.height + VIEW_PADDING * 2;

        canvasRef.value.width = logicalWidth * zoom.value;
        canvasRef.value.height = logicalHeight * zoom.value;
        canvasRef.value.style.width = `${logicalWidth * zoom.value}px`;
        canvasRef.value.style.height = `${logicalHeight * zoom.value}px`;
    }
}

function requestDraw() {
    requestAnimationFrame(draw);
}

function draw() {
    if (!canvasRef.value || !spriteImage) return;
    const ctx = canvasRef.value.getContext("2d");
    if (!ctx) return;

    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height);

    ctx.imageSmoothingEnabled = false;

    ctx.translate(VIEW_PADDING * zoom.value, VIEW_PADDING * zoom.value);

    ctx.drawImage(
        spriteImage,
        0,
        0,
        frameWidth.value,
        props.sprite.height,
        0,
        0,
        frameWidth.value * zoom.value,
        props.sprite.height * zoom.value
    );

    ctx.save();

    ctx.strokeStyle = "#06b6d4";
    ctx.lineWidth = 2;
    ctx.setLineDash([6, 4]);
    ctx.strokeRect(
        0,
        0,
        frameWidth.value * zoom.value,
        props.sprite.height * zoom.value
    );

    ctx.restore();

    const hx = hitbox.value.x * zoom.value;
    const hy = hitbox.value.y * zoom.value;
    const hw = hitbox.value.width * zoom.value;
    const hh = hitbox.value.height * zoom.value;

    ctx.fillStyle = "rgba(239, 68, 68, 0.3)";
    ctx.fillRect(hx, hy, hw, hh);

    ctx.strokeStyle = "#ff0000";
    ctx.lineWidth = 2;
    ctx.strokeRect(hx, hy, hw, hh);

    drawHandles(ctx, hx, hy, hw, hh);
}
function drawHandles(
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    w: number,
    h: number
) {
    const handleRadius = 5;
    ctx.fillStyle = "#ffffff";
    ctx.strokeStyle = "#ff0000";
    ctx.lineWidth = 2;

    const coords = [
        { x: x, y: y },
        { x: x + w, y: y },
        { x: x, y: y + h },
        { x: x + w, y: y + h },
    ];

    coords.forEach((p) => {
        ctx.beginPath();
        ctx.arc(p.x, p.y, handleRadius, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();
    });
}

function getMousePos(e: MouseEvent) {
    if (!canvasRef.value) return { x: 0, y: 0 };
    const rect = canvasRef.value.getBoundingClientRect();
    const clientX = e.clientX - rect.left;
    const clientY = e.clientY - rect.top;

    const spriteX = clientX / zoom.value - VIEW_PADDING;
    const spriteY = clientY / zoom.value - VIEW_PADDING;

    return { x: spriteX, y: spriteY };
}

function getHandleAt(mx: number, my: number): string | null {
    const tolerance = 8 / Math.min(1, zoom.value);
    const { x, y, width: w, height: h } = hitbox.value;

    if (dist(mx, my, x, y) <= tolerance) return "tl";
    if (dist(mx, my, x + w, y) <= tolerance) return "tr";
    if (dist(mx, my, x, y + h) <= tolerance) return "bl";
    if (dist(mx, my, x + w, y + h) <= tolerance) return "br";

    const inX = mx >= x && mx <= x + w;
    const inY = my >= y && my <= y + h;

    if (inY && Math.abs(mx - x) <= tolerance) return "l";
    if (inY && Math.abs(mx - (x + w)) <= tolerance) return "r";
    if (inX && Math.abs(my - y) <= tolerance) return "t";
    if (inX && Math.abs(my - (y + h)) <= tolerance) return "b";

    return null;
}

function dist(x1: number, y1: number, x2: number, y2: number) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
}

function onMouseMove(e: MouseEvent) {
    const { x, y } = getMousePos(e);
    if (isDragging.value) handleDrag(x, y);
    else updateCursor(x, y);
}

function startDrag(e: MouseEvent) {
    const { x, y } = getMousePos(e);
    const handle = getHandleAt(x, y);

    initialHitbox.value = { ...hitbox.value };
    dragStart.value = { x, y };

    if (handle) {
        isDragging.value = true;
        dragMode.value = "resize";
        dragCorner.value = handle;
    } else if (
        x >= hitbox.value.x &&
        x <= hitbox.value.x + hitbox.value.width &&
        y >= hitbox.value.y &&
        y <= hitbox.value.y + hitbox.value.height
    ) {
        isDragging.value = true;
        dragMode.value = "move";
    }
}

function handleDrag(currX: number, currY: number) {
    const dx = Math.round(currX - dragStart.value.x);
    const dy = Math.round(currY - dragStart.value.y);

    if (dragMode.value === "move") {
        hitbox.value.x = initialHitbox.value.x + dx;
        hitbox.value.y = initialHitbox.value.y + dy;
    } else if (dragMode.value === "resize") {
        let { x, y, width: w, height: h } = initialHitbox.value;
        const c = dragCorner.value;

        if (c?.includes("r")) w += dx;
        if (c?.includes("l")) {
            x += dx;
            w -= dx;
        }
        if (c?.includes("b")) h += dy;
        if (c?.includes("t")) {
            y += dy;
            h -= dy;
        }

        if (w < 1) {
            w = 1;
            if (c?.includes("l"))
                x = initialHitbox.value.x + initialHitbox.value.width - 1;
        }
        if (h < 1) {
            h = 1;
            if (c?.includes("t"))
                y = initialHitbox.value.y + initialHitbox.value.height - 1;
        }

        hitbox.value = { x, y, width: w, height: h };
    }
    requestDraw();
}

function endDrag() {
    isDragging.value = false;
    dragMode.value = null;
    dragCorner.value = null;
    hitbox.value.x = Math.round(hitbox.value.x);
    hitbox.value.y = Math.round(hitbox.value.y);
    hitbox.value.width = Math.round(hitbox.value.width);
    hitbox.value.height = Math.round(hitbox.value.height);
    requestDraw();
}

function updateCursor(x: number, y: number) {
    if (!canvasRef.value) return;
    const handle = getHandleAt(x, y);
    if (handle) {
        const cursorMap: Record<string, string> = {
            tl: "nwse-resize",
            br: "nwse-resize",
            tr: "nesw-resize",
            bl: "nesw-resize",
            t: "ns-resize",
            b: "ns-resize",
            l: "ew-resize",
            r: "ew-resize",
        };
        canvasRef.value.style.cursor = cursorMap[handle];
    } else if (
        x >= hitbox.value.x &&
        x <= hitbox.value.x + hitbox.value.width &&
        y >= hitbox.value.y &&
        y <= hitbox.value.y + hitbox.value.height
    ) {
        canvasRef.value.style.cursor = "move";
    } else {
        canvasRef.value.style.cursor = "default";
    }
}

function handleWheel(e: WheelEvent) {
    if (e.ctrlKey || e.metaKey) {
        e.preventDefault();
        if (e.deltaY < 0) zoomIn();
        else zoomOut();
    }
}

function zoomIn() {
    zoom.value = Math.min(zoom.value + 0.5, 10);
}
function zoomOut() {
    zoom.value = Math.max(zoom.value - 0.5, 0.5);
}
function resetZoom() {
    zoom.value = 2;
}

function handleKeyDown(e: KeyboardEvent) {
    const step = e.shiftKey ? 10 : 1;
    switch (e.key) {
        case "ArrowUp":
            hitbox.value.y -= step;
            break;
        case "ArrowDown":
            hitbox.value.y += step;
            break;
        case "ArrowLeft":
            hitbox.value.x -= step;
            break;
        case "ArrowRight":
            hitbox.value.x += step;
            break;
    }
    requestDraw();
}

async function saveHitbox() {
    await spriteService.saveHitbox(props.sprite.animationId, hitbox.value);
    emit("saved", hitbox.value);
    emit("close");
}

async function deleteHitbox() {
    if (!confirm("Supprimer la hitbox ?")) return;
    await spriteService.deleteHitbox(props.sprite.animationId);
    emit("saved", null);
    emit("close");
}

function resetHitbox() {
    hitbox.value = {
        x: props.sprite.hitboxX ?? 0,
        y: props.sprite.hitboxY ?? 0,
        width: props.sprite.hitboxWidth ?? 32,
        height: props.sprite.hitboxHeight ?? 32,
    };
    requestDraw();
}
</script>

<style scoped>
*,
*::before,
*::after {
    box-sizing: border-box;
}

.hitbox-editor-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.8);
    backdrop-filter: blur(4px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.hitbox-editor {
    background: #0f172a;
    border: 1px solid #334155;
    border-radius: 16px;
    width: 90%;
    max-width: 1200px;
    height: 90%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    color: #f8fafc;
}

.editor-header {
    background: #1e293b;
    padding: 1.5rem 2rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #334155;
}
.header-title h2 {
    margin: 0;
    color: #e2e8f0;
    font-size: 1.5rem;
}
.header-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
}
.zoom-info {
    font-family: monospace;
    background: #0f172a;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    color: #94a3b8;
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

.editor-layout {
    display: grid;
    grid-template-columns: 1fr 320px;
    flex: 1;
    overflow: hidden;
}

.canvas-wrapper {
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    background-color: #0f172a;
    background-image: repeating-conic-gradient(
        #1e293b 0 25%,
        transparent 0 50%
    );
    background-size: 20px 20px;
}

.canvas-scroller {
    flex: 1;
    overflow: auto;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 0;
    scrollbar-width: thin;
    scrollbar-color: #475569 #0f172a;
}

.hitbox-canvas {
    background: transparent;
    display: block;
}

.canvas-toolbar {
    position: absolute;
    bottom: 1.5rem;
    left: 50%;
    transform: translateX(-50%);
    background: #1e293b;
    border: 1px solid #475569;
    border-radius: 999px;
    padding: 0.25rem;
    display: flex;
    gap: 0.25rem;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
}
.canvas-toolbar button {
    background: transparent;
    border: none;
    color: #e2e8f0;
    padding: 0.5rem 1rem;
    cursor: pointer;
    font-weight: 600;
    border-radius: 999px;
    transition: background 0.2s;
}
.canvas-toolbar button:hover {
    background: #334155;
}

.sidebar {
    background: #1e293b;
    border-left: 1px solid #334155;
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 2rem;
    overflow-y: auto;
    z-index: 10;
}
.form-section h4 {
    color: #94a3b8;
    text-transform: uppercase;
    font-size: 0.75rem;
    letter-spacing: 0.05em;
    margin: 0 0 1rem 0;
    font-weight: 700;
}
.input-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}
.input-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}
.input-group label {
    font-size: 0.875rem;
    color: #cbd5e1;
}
.input-group input {
    background: #0f172a;
    border: 1px solid #334155;
    color: white;
    padding: 0.6rem;
    border-radius: 6px;
    font-size: 1rem;
    font-family: monospace;
}
.input-group input:focus {
    outline: none;
    border-color: #06b6d4;
}

.info-box {
    background: #334155;
    padding: 1rem;
    border-radius: 8px;
    color: #cbd5e1;
    font-size: 0.875rem;
}

.sidebar-footer {
    margin-top: auto;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}
.btn-row {
    display: flex;
    gap: 0.75rem;
}
.btn {
    padding: 0.75rem 1rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: 0.2s;
    font-size: 0.95rem;
}
.btn-primary {
    background: #06b6d4;
    color: white;
    width: 100%;
}
.btn-primary:hover {
    background: #0891b2;
}
.btn-secondary {
    background: #475569;
    color: white;
    flex: 1;
}
.btn-secondary:hover {
    background: #64748b;
}
.btn-danger {
    background: rgba(239, 68, 68, 0.2);
    color: #ef4444;
    flex: 1;
}
.btn-danger:hover {
    background: rgba(239, 68, 68, 0.3);
}
</style>
