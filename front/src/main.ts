import {createApp} from "vue";
import App from "./App.vue";
import router from "./router";
import {loading} from "@/directives/loading.ts";
import {clickOutside} from "@/directives/clickOutside.ts";

import "./style.css";

const app = createApp(App);
app.use(router);
app.directive("loading", loading);
app.directive("clickOutside", clickOutside);
app.mount("#app");
