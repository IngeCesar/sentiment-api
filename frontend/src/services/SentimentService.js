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

    async uploadBatch(file) {
        const formData = new FormData();
        formData.append("file", file);

        const { data } = await api.post("/sentiment/batch/csv", formData, {
            // Eliminamos el Content-Type manual para que Axios
            // y el navegador pongan el boundary correcto.
            headers: {
                "Content-Type": undefined,
            },
            timeout: 180000,
        });

        return data;
    },

    async getStats() {
        const { data } = await api.get("/sentiment/stats");
        return data;
    },
};
