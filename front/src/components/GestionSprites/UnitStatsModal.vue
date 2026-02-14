<template>
    <teleport to="body">
        <transition
            name="fade"
            @before-enter="onBeforeEnter"
            @after-leave="onAfterLeave"
        >
            <div
                v-if="isVisible"
                class="modal-overlay"
                @click.self="isVisible = false"
            >
                <div class="modal-content">
                    <header class="modal-header">
                        <h2>Statistiques : {{ spriteName }}</h2>
                        <button class="btn-close" @click="isVisible = false">
                            ✕
                        </button>
                    </header>

                    <div class="stats-container">
                        <div v-if="unitStats" class="stats-form">
                            <div class="stat-group">
                                <div class="stat-row">
                                    <div class="stat-field">
                                        <label>Santé</label>
                                        <input
                                            v-model.number="unitStats.health"
                                            type="number"
                                        />
                                    </div>
                                    <div class="stat-field">
                                        <label>Attaque</label>
                                        <input
                                            v-model.number="unitStats.attack"
                                            type="number"
                                        />
                                    </div>
                                    <div class="stat-field">
                                        <label>Vitesse d'Attaque</label>
                                        <input
                                            v-model.number="
                                                unitStats.attackSpeed
                                            "
                                            type="number"
                                            step="0.1"
                                        />
                                    </div>
                                </div>
                                <div class="stat-row">
                                    <div class="stat-field">
                                        <label>Vitesse de Déplacement</label>
                                        <input
                                            v-model.number="unitStats.moveSpeed"
                                            type="number"
                                            step="0.1"
                                        />
                                    </div>
                                    <div class="stat-field">
                                        <label>Portée</label>
                                        <input
                                            v-model.number="
                                                unitStats.attackRange
                                            "
                                            type="number"
                                        />
                                    </div>
                                    <div class="stat-field">
                                        <label>Coût</label>
                                        <input
                                            v-model.number="unitStats.cost"
                                            type="number"
                                        />
                                    </div>
                                </div>
                                <div class="stat-row">
                                    <div class="stat-field">
                                        <label>Cooldown</label>
                                        <input
                                            v-model.number="unitStats.cooldown"
                                            type="number"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button
                            class="btn-secondary"
                            @click="isVisible = false"
                        >
                            Annuler
                        </button>
                        <button
                            v-if="unitStats"
                            class="btn-primary"
                            @click="saveStats"
                            :disabled="!hasChanges"
                        >
                            Sauvegarder
                        </button>
                    </div>
                </div>
            </div>
        </transition>
    </teleport>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import unitStatsService from "@/services/unitStatsService";
import { UnitStatsDto } from "@/models/dtos/UnitStatsDto.ts";

const props = defineProps<{
    spriteName: string;
}>();

const isVisible = defineModel<boolean>("visible", { default: false });

const unitStats = ref<UnitStatsDto | null>(null);
const originalStats = ref<UnitStatsDto | null>(null);

const hasChanges = computed(() => {
    if (!unitStats.value || !originalStats.value) return false;
    return (
        JSON.stringify(unitStats.value) !== JSON.stringify(originalStats.value)
    );
});

async function onBeforeEnter() {
    const stats = await unitStatsService.getUnitStatsBySpriteName(
        props.spriteName
    );
    unitStats.value = stats;
    originalStats.value = JSON.parse(JSON.stringify(stats));
}

function onAfterLeave() {
    unitStats.value = null;
    originalStats.value = null;
}

async function saveStats() {
    if (!unitStats.value || !hasChanges.value) return;

    await unitStatsService.createOrUpdateUnitStats(unitStats.value);
    originalStats.value = JSON.parse(JSON.stringify(unitStats.value));
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
    max-width: 800px;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    box-shadow: var(--shadow-xl);
    border: 1px solid var(--border-base);
    overflow: hidden;
    max-height: 90vh;
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

.stats-container {
    padding: 2rem;
    overflow-y: auto;
    flex: 1;
}

.stats-form {
    background: var(--bg-surface);
    padding: 2rem;
    border-radius: 12px;
    border: 1px solid var(--border-base);
}

.stat-group {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.stat-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
}

.stat-field {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.stat-field label {
    color: var(--text-secondary);
    font-size: 0.875rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.modal-footer {
    padding: 1rem 2rem;
    background: var(--bg-surface);
    border-top: 1px solid var(--border-base);
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity var(--transition-slow);
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

@media (max-width: 768px) {
    .modal-content {
        width: 95%;
        max-height: 95vh;
    }

    .stats-container {
        padding: 1rem;
    }

    .stat-row {
        grid-template-columns: 1fr;
    }

    .modal-footer {
        flex-direction: column;
    }
}
</style>
