import api from "../api";

// Mapa de seguridad por si el backend envía variaciones (opcional pero recomendado)
const SENTIMENT_MAP = {
    POSITIVE: "POSITIVO",
    NEGATIVE: "NEGATIVO",
    NEUTRAL: "NEUTRO",
    POSITIVO: "POSITIVO",
    NEGATIVO: "NEGATIVO",
    NEUTRO: "NEUTRO",
};

export default {
    /** Transforma DTO Backend -> ViewModel Frontend */
    async analyze(text) {
        const response = await api.post("/sentiment", { text });
        const data = response.data;

        if (data.prediction === "CONNECTION_ERROR") {
            throw new Error(
                "El servicio de IA no está disponible temporalmente."
            );
        }

        return {
            sentiment:
                SENTIMENT_MAP[data.prediction?.toUpperCase()] || "DEFAULT",
            confidence: Math.round(data.probability * 100),
            keywords: data.keywords || [],
        };
    },

    async getStats() {
        const response = await api.get("/sentiment/stats");
        return response.data;
    },
};
