<template>
    <header class="library-header">
        <div class="header-content">
            <h1>Bestiaire <span class="accent">v1.0</span></h1>
            <div class="actions-bar">
                <button class="btn-secondary" @click="openFilePicker">
                    Importer un sprite
                </button>

                <input
                    id="fileInput"
                    type="file"
                    accept=".zip,application/zip"
                    @change="onFileSelected($event)"
                    style="display: none"
                />
            </div>
        </div>
    </header>
</template>

<script setup lang="ts">
import spriteService from "@/services/spriteService.ts";
import { useToast } from "@/services/toast.ts";

const toast = useToast();

const emit = defineEmits(["add-to-list"]);

function openFilePicker() {
    document.getElementById("fileInput")?.click();
}

async function onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
        const formData = new FormData();
        formData.append("file", input.files[0]);

        const newSprite = await spriteService.uploadSprite(formData);
        emit("add-to-list", newSprite.data);
    }
    toast.show("Le sprite a été ajouté avec succès", "success");
}
</script>
<style scoped>
.library-header {
    background: var(--bg-surface);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid var(--border-base);
    padding: 1.5rem 2rem;
    position: sticky;
    top: 0;
    z-index: 10;
    box-shadow: var(--shadow-md);
}

.header-content {
    max-width: 1400px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

h1 {
    margin: 0;
    font-size: 1.75rem;
    font-weight: 700;
    letter-spacing: -0.5px;
    color: var(--text-bright);
}

.accent {
    color: var(--primary-light);
    font-size: 0.9rem;
    background: rgba(59, 130, 246, 0.1);
    padding: 0.25rem 0.75rem;
    border-radius: 12px;
    border: 1px solid var(--border-base);
}

.actions-bar {
    display: flex;
    align-items: center;
    gap: 1rem;
}

@media (max-width: 768px) {
    .library-header {
        padding: 1rem;
    }

    h1 {
        font-size: 1.25rem;
    }
}
</style>
