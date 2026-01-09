<template>
    <div class="app-container">
        <Toast />
        <router-view />
    </div>
</template>

<script setup lang="ts">
import Toast from "@/components/Toast.vue";
</script>
