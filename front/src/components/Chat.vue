<template>
    <div class="chat" v-loading="isLoading">
        <div class="messages" ref="messagesContainer" @scroll="onScroll">
            <div v-for="msg in messages" :key="msg.id" class="message">
                <strong>{{ msg.player }}: </strong>
                <span>{{ parseEmojis(msg.content) }}</span>
                <small>{{ formatTime(msg.timestamp) }}</small>
            </div>
        </div>

        <form @submit.prevent="sendMessage" class="input-container">
            <input
                v-model="text"
                placeholder="Écris un message..."
                maxlength="50"
            />

            <div class="emoji-btn" @click="showEmojiMenu = !showEmojiMenu">
                {{ Emoji.COOL }}
            </div>

            <button type="submit">Envoyer</button>

            <div v-if="showEmojiMenu" class="emoji-menu">
                <span
                    v-for="(emoji, shortcut) in emojiMap"
                    :key="shortcut"
                    class="emoji-item"
                    @click="selectEmoji(shortcut)"
                >
                    {{ emoji }}
                </span>
            </div>
        </form>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onBeforeUnmount } from "vue";
import messageService from "@/services/messageService.ts";
import { Emoji, emojiMap } from "@/models/enums/emoji.ts";
import { connexionChat } from "@/sockets/websocket-client.ts";
import Message from "@/models/message.ts";
import { Client } from "@stomp/stompjs";

const text = ref("");
const messages = ref<Message[]>([]);
const showEmojiMenu = ref(false);
const isLoading = ref(false);
const messagesContainer = ref<HTMLDivElement | null>(null);

let stompClient: Client | null = null;

const page = ref(0);
const size = 20;
const loading = ref(false);
let reachedEnd = false;

function parseEmojis(text: string): string {
    let parsedText = text;
    for (const [shortcut, emoji] of Object.entries(emojiMap)) {
        parsedText = parsedText.split(shortcut).join(emoji);
    }
    return parsedText;
}

function selectEmoji(shortcut: string) {
    text.value += shortcut + " ";
    showEmojiMenu.value = false;
}

async function loadMessages() {
    if (loading.value || reachedEnd) return;
    loading.value = true;

    if (!messagesContainer.value) return;

    const container = messagesContainer.value;
    const scrollHeightBefore = container.scrollHeight;

    const res = await messageService.loadMessages(page.value, size);
    const newMessages = res.data.content;

    if (newMessages.length === 0) {
        reachedEnd = true;
    } else {
        messages.value = [...newMessages.reverse(), ...messages.value];

        await nextTick();

        if (page.value === 0) {
            container.scrollTop = container.scrollHeight;
        } else {
            const scrollHeightAfter = container.scrollHeight;
            container.scrollTop = scrollHeightAfter - scrollHeightBefore;
        }

        page.value++;
    }

    loading.value = false;
}

async function sendMessage() {
    if (!text.value.trim() || text.value.length > 50) return;
    await messageService.sendMessage(text.value);
    text.value = "";
}

function formatTime(ts: string) {
    const date = new Date(ts);
    return date.toLocaleString("fr-FR", {
        day: "2-digit",
        month: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
}

function onScroll() {
    if (messagesContainer.value && messagesContainer.value.scrollTop === 0) {
        loadMessages();
    }
}
async function handleNewMessage(newMsg: Message) {
    messages.value.push(newMsg);
    await scrollToBottom();
}

async function scrollToBottom() {
    await nextTick();
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop =
            messagesContainer.value.scrollHeight;
    }
}
onMounted(async () => {
    try {
        isLoading.value = true;
        await loadMessages();
        stompClient = connexionChat(handleNewMessage);
    } finally {
        isLoading.value = false;
    }
});

onBeforeUnmount(async () => {
    if (stompClient) {
        await stompClient.deactivate();
    }
});
</script>

<style scoped>
.chat {
    background: radial-gradient(
        circle at top left,
        var(--futurist-bg-dark),
        var(--futurist-bg-light)
    );
    border-radius: 1rem;
    padding: 1rem;
    width: 30rem;
    color: var(--futurist-text-light);
    box-shadow: 0 0 20px var(--futurist-shadow);
    border: 1px solid var(--futurist-border);
    backdrop-filter: blur(6px);
    position: relative;
    overflow: hidden;
    margin-top: 2.5rem;
}

.chat::before {
    content: "";
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: conic-gradient(
        from 180deg,
        transparent,
        var(--futurist-shadow-strong),
        transparent
    );
    animation: rotate 6s linear infinite;
    z-index: 0;
}

@keyframes rotate {
    100% {
        transform: rotate(360deg);
    }
}

.messages {
    height: 28.5rem;
    overflow-y: auto;
    padding: 1rem;
    border-radius: 0.75rem;
    background: var(--futurist-blur-bg);
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
    z-index: 1;
    backdrop-filter: blur(4px);
    box-shadow: inset 0 0 10px var(--futurist-shadow);
    scrollbar-width: thin;
    scrollbar-color: var(--futurist-accent) var(--futurist-bg-dark);
}

.message {
    padding: 10px 15px;
    background: linear-gradient(
        135deg,
        var(--futurist-bg-dark),
        var(--futurist-bg-mid)
    );
    border: 1px solid var(--futurist-border);
    border-radius: 15px;
    word-wrap: break-word;
    color: var(--futurist-text-light);
    position: relative;
    box-shadow: 0 0 8px var(--futurist-shadow);
    animation: fadeIn 0.3s ease;
}

.message strong {
    color: var(--futurist-accent);
    text-shadow: 0 0 5px var(--futurist-accent);
}

.message small {
    display: block;
    text-align: right;
    font-size: 0.7em;
    color: var(--futurist-accent-light);
    opacity: 0.8;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.input-container {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 1rem;
    position: relative;
    z-index: 2;
}

.emoji-btn {
    cursor: pointer;
    width: 2rem;
    height: 2rem;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 1.3rem;
    transition: all 0.3s ease;
    border-radius: 8px;
    color: var(--futurist-accent);
    box-shadow: 0 0 8px var(--futurist-shadow);
}

.emoji-btn:hover {
    background-color: var(--futurist-emoji-hover-bg);
    box-shadow: 0 0 10px var(--futurist-shadow-strong);
}

.emoji-menu {
    position: absolute;
    bottom: 3.5rem;
    left: 50%;
    transform: translateX(-50%);
    background-color: var(--futurist-emoji-bg);
    border: 1px solid var(--futurist-border-strong);
    border-radius: 10px;
    padding: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    max-width: 300px;
    z-index: 10;
    box-shadow: 0 0 15px var(--futurist-shadow-strong);
    backdrop-filter: blur(6px);
}

.emoji-item {
    cursor: pointer;
    font-size: 1.4rem;
    padding: 4px;
    border-radius: 6px;
    transition:
        background 0.2s ease,
        transform 0.2s ease;
}

.emoji-item:hover {
    background-color: var(--futurist-emoji-hover-bg);
    transform: scale(1.2);
}
</style>
