import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
    plugins: [vue()],
    server: {
        host: true, // Escucha en 0.0.0.0
        port: 5173, // Puerto explícito

        // --- VITAL PARA DOCKER ---
        // Obliga a revisar cambios de archivos
        watch: {
            usePolling: true,
        },
        // -------------------------

        proxy: {
            "/api": {
                // Nombre del servicio en docker-compose (core-service)
                target: "http://core-service:8080",
                changeOrigin: true,
                secure: false,
                // Tus logs de depuración (Mantenlos, son muy útiles)
                configure: (proxy, _options) => {
                    proxy.on("error", (err, _req, _res) => {
                        console.log("❌ VITE PROXY ERROR:", err);
                    });
                    proxy.on("proxyReq", (proxyReq, req, _res) => {
                        console.log("📤 Proxy -> Java:", req.method, req.url);
                    });
                    proxy.on("proxyRes", (proxyRes, req, _res) => {
                        console.log(
                            "📥 Java -> Proxy:",
                            proxyRes.statusCode,
                            req.url
                        );
                    });
                },
            },
        },
    },
});
