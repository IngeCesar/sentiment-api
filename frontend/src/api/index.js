import axios from "axios";

const api = axios.create({
    // Si no existe la variable, usa '/api/v1' por defecto
    baseURL: import.meta.env.VITE_API_BASE_URL || "/api/v1",
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
});

export default api;
