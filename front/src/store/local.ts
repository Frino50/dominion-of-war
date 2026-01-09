import { reactive } from "vue";

export interface LocalState {
    pseudo: string;
    token: string;
}

const storedState = JSON.parse(localStorage.getItem("localState") || "{}");

const state = reactive<LocalState>({
    pseudo: storedState.pseudo || "",
    token: storedState.token || "",
});

export const localStore = new Proxy(state, {
    set(target, prop: keyof LocalState, value) {
        target[prop] = value;
        localStorage.setItem("localState", JSON.stringify(target));
        return true;
    },
}) as LocalState;
