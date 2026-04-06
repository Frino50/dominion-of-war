<template>
    <div class="auth-page">
        <div v-loading="isLoading" class="auth-card">
            <h2 class="title">{{ props.title }}</h2>

            <div class="input-group">
                <input
                    v-model="email"
                    placeholder="Email"
                    type="email"
                    @keyup.enter="handleSubmit"
                    :class="{ 'input-error': errors.email }"
                />
                <p v-if="errors.email" class="field-error">
                    {{ errors.email }}
                </p>
            </div>

            <div v-if="props.mode === 'register'" class="input-group">
                <input
                    v-model="pseudo"
                    placeholder="Pseudo (3 à 20 caractères)"
                    @keyup.enter="handleSubmit"
                    :class="{ 'input-error': errors.pseudo }"
                />
                <p v-if="errors.pseudo" class="field-error">
                    {{ errors.pseudo }}
                </p>
            </div>

            <div class="input-group">
                <input
                    v-model="password"
                    placeholder="Mot de passe"
                    type="password"
                    @keyup.enter="handleSubmit"
                    :class="{ 'input-error': errors.password }"
                />
                <p v-if="errors.password" class="field-error">
                    {{ errors.password }}
                </p>
            </div>

            <button class="btn-primary" @click="handleSubmit">
                {{ props.buttonText }}
            </button>

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
import { ref, reactive } from "vue";
import RegisterDto from "@/models/dtos/registerDto.ts";
import LoginDto from "@/models/dtos/loginDto.ts";
import auth from "@/services/authService.ts";
import LoginResponseDto from "@/models/dtos/loginResponseDto.ts";
import router from "@/router.ts";
import { localStore } from "@/store/local.ts";

const EMAIL_REGEX = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
const PSEUDO_REGEX = /^[a-zA-Z0-9_\-]{3,20}$/;

const props = defineProps({
    title: String,
    buttonText: String,
    altText: String,
    altButtonText: String,
    mode: String,
});

const email = ref("");
const pseudo = ref("");
const password = ref("");
const internalError = ref("");
const isLoading = ref(false);
const errors = reactive({ email: "", pseudo: "", password: "" });

function validate(): boolean {
    errors.email = "";
    errors.pseudo = "";
    errors.password = "";

    let valid = true;

    if (!email.value) {
        errors.email = "L'email est requis.";
        valid = false;
    } else if (!EMAIL_REGEX.test(email.value)) {
        errors.email = "Format d'email invalide.";
        valid = false;
    }

    if (props.mode === "register") {
        if (!pseudo.value) {
            errors.pseudo = "Le pseudo est requis.";
            valid = false;
        } else if (!PSEUDO_REGEX.test(pseudo.value)) {
            errors.pseudo = "3 à 20 caractères : lettres, chiffres, _ ou -.";
            valid = false;
        }
    }

    if (!password.value) {
        errors.password = "Le mot de passe est requis.";
        valid = false;
    }

    return valid;
}

async function handleSubmit() {
    if (!validate()) return;

    internalError.value = "";
    isLoading.value = true;

    try {
        if (props.mode === "login") {
            await login();
        } else {
            await register();
        }
    } catch (error: any) {
        internalError.value = error.response?.data?.message || "Erreur serveur";
    } finally {
        isLoading.value = false;
    }
}

async function login() {
    const res = await auth.login(new LoginDto(email.value, password.value));
    const loginResponseDto: LoginResponseDto = res.data;

    localStore.token = loginResponseDto.token;
    localStore.pseudo = loginResponseDto.pseudo;

    await router.push("/");
}

async function register() {
    await auth.register(
        new RegisterDto(email.value, pseudo.value, password.value)
    );
    await router.push("/login");
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
    background-color: var(--bg-card);
}

.title {
    margin-bottom: 1rem;
}

.input-group {
    margin-bottom: 1rem;
    width: 100%;
    z-index: 1;
}

.input-error {
    border-color: var(--danger) !important;
}

.field-error {
    margin-top: 0.25rem;
    color: var(--danger);
    font-size: 0.8rem;
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
