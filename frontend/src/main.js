import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

/* --- IMPORTACIÓN DE ESTILOS ---
   El orden importa: 
   1. Librerías (Bootstrap)
   2. Iconos (mediante npm)
   3. CSS propio (Para poder sobrescribir lo anterior)
*/
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
// import "bootstrap-icons/font/bootstrap-icons.css";
import "./assets/main.css";

// Inicialización one line (Method Chaining)
createApp(App).use(router).mount("#app");
