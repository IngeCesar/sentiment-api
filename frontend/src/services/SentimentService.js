import api from "../api";

const SENTIMENT_MAP = {
    POSITIVE: "POSITIVO",
    NEGATIVE: "NEGATIVO",
    NEUTRAL: "NEUTRO",
    // Fallbacks
    POSITIVO: "POSITIVO",
    NEGATIVO: "NEGATIVO",
    NEUTRO: "NEUTRO",
};

export default {
    /**
     * Analiza el texto y normaliza la respuesta para la UI.
     * @param {string} text
     */
    async analyze(text) {
        const { data } = await api.post("/sentiment", { text });

        if (data.prediction === "CONNECTION_ERROR") {
            throw new Error("El motor de IA no está disponible.");
        }

        return {
            id: data.id,
            sentiment:
                SENTIMENT_MAP[data.prediction?.toUpperCase()] || "DESCONOCIDO",
            // Convertimos probabilidad (0.98) a porcentaje entero (98)
            confidence: Math.round((data.probability || 0) * 100),
            keywords: Array.isArray(data.keywords) ? data.keywords : [],
        };
    },

    async getStats() {
        const { data } = await api.get("/sentiment/stats");
        return data;
    },
};
