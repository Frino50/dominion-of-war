<template>
    <div class="auth-page">
        <div v-loading="isLoading" class="auth-card">
            <h2 class="title">{{ props.title }}</h2>

            <div class="input-group">
                <input
                    v-model="pseudo"
                    placeholder="Pseudo"
                    @keyup.enter="handleSubmit"
                />
            </div>

            <div class="input-group">
                <input
                    v-model="password"
                    placeholder="Mot de passe"
                    type="password"
                    @keyup.enter="handleSubmit"
                />
            </div>

            <button @click="handleSubmit">{{ props.buttonText }}</button>

            <div class="alt-text">
                {{ props.altText }}
                <div class="alt-button" @click="redirection()">
                    {{ props.altButtonText }}
                </div>
            </div>

            <p v-if="internalError" class="error-text">{{ internalError }}</p>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import ConnexionDto from "@/models/dtos/connexionDto.ts";
import auth from "@/services/authService.ts";
import LoginResponseDto from "@/models/dtos/loginResponseDto.ts";
import router from "@/router.ts";
import { localStore } from "@/store/local.ts";

const props = defineProps({
    title: String,
    buttonText: String,
    altText: String,
    altButtonText: String,
    mode: String,
});

const pseudo = ref("");
const password = ref("");
const internalError = ref("");
const isLoading = ref(false);

async function handleSubmit() {
    if (!pseudo.value || !password.value) {
        internalError.value = "Veuillez remplir tous les champs";
        return;
    }

    internalError.value = "";
    isLoading.value = true;

    try {
        if (props.mode === "login") {
            await login();
        } else {
            await register();
        }
    } finally {
        isLoading.value = false;
    }
}

async function login() {
    const res = await auth.login(connexionDto());
    const loginResponseDto: LoginResponseDto = res.data;
    localStore.pseudo = loginResponseDto.pseudo;
    localStore.token = loginResponseDto.token;
    await router.push("/");
}

async function register() {
    await auth.register(connexionDto());
    await router.push("/login");
}

function connexionDto(): ConnexionDto {
    return new ConnexionDto(pseudo.value, password.value);
}

function redirection() {
    router.push(props.mode === "login" ? "/register" : "/login");
}
</script>
<style scoped>
.auth-page {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    overflow: hidden;
}

.auth-card {
    position: relative;
    padding: 2rem 3rem;
    border-radius: 1rem;
    width: 100%;
    max-width: 400px;
    display: flex;
    flex-direction: column;
    align-items: center;
    z-index: 1;
    overflow: hidden;
}

.title {
    margin-bottom: 1rem;
}

.input-group {
    margin-bottom: 1rem;
    width: 100%;
    z-index: 1;
}

.alt-button {
    display: inline-block;
    margin-left: 0.5rem;
    font-weight: bold;
    text-decoration: underline;
    cursor: pointer;
    color: var(--primary);
    font-size: 0.9rem;
    transition: color 0.2s;
    z-index: 1;
}

.alt-button:hover {
    text-decoration-thickness: 2px;
    transform: translateY(-2px);
}

.alt-text {
    display: flex;
    align-items: center;
    margin-top: 1.5rem;
    font-size: 0.9rem;
    z-index: 1;
}

.error-text {
    margin-top: 1rem;
    color: var(--danger);
    font-size: 0.9rem;
    z-index: 1;
}
</style>
