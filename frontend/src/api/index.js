import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "/api/v1",
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
});

/* --- INTERCEPTOR DE ERRORES GLOBAL --- */
api.interceptors.response.use(
    (response) => response, // Si todo sale bien, deja pasar la respuesta
    (error) => {
        // Error de Red (Servidor apagado / Sin internet / Timeout)
        if (!error.response) {
            return Promise.reject(
                new Error(
                    "No se pudo conectar con el servidor. Verifica tu conexión."
                )
            );
        }

        // Error 500 (El servidor explotó o está apagado el core-service)
        if (error.response.status >= 500) {
            return Promise.reject(
                new Error(
                    "El servicio no está disponible temporalmente. Intenta más tarde."
                )
            );
        }

        // Otros errores (400, 401, 404)
        return Promise.reject(error);
    }
);

export default api;
