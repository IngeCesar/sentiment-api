/**
 * Sentiment Analysis Frontend Script
 * Team: H12-25-L-EQUIPO-55 (CesiumFlow)
 */

// Configuration constants
// If you changed the frontend port in docker-compose, this URL remains localhost:8080 (Backend)
const API_URL = "http://localhost:8080/api/reviews/analyze";

async function analyzeSentiment() {
    // 1. DOM Elements
    const textInput = document.getElementById("textInput");
    const resultDiv = document.getElementById("result");
    const keywordsDiv = document.getElementById("keywords-container");

    // 2. Input Cleaning
    const text = textInput.value.trim();

    // 3. Reset UI
    resultDiv.innerHTML = "";
    keywordsDiv.innerHTML = "";

    // 4. Frontend Validation
    if (!text) {
        resultDiv.innerHTML =
            "<span style='color:red'>⚠️ El campo es obligatorio.</span>";
        return;
    }
    if (text.length < 3) {
        resultDiv.innerHTML =
            "<span style='color:orange'>⚠️ Mínimo 3 caracteres.</span>";
        return;
    }
    if (text.length > 5000) {
        resultDiv.innerHTML =
            "<span style='color:red'>⚠️ Máximo 5000 caracteres.</span>";
        return;
    }

    // Show loading state
    resultDiv.innerHTML = "⏳ Analizando...";

    try {
        // 5. API Request (Using Async/Await)
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ text: text }),
        });

        // 6. Response Parsing
        const data = await response.json();

        // Handle logical errors from the backend (e.g. Connection refused to Python)
        if (
            !response.ok ||
            data.prediction === "ERROR_CONEXION" ||
            data.prediction === "ERROR"
        ) {
            throw new Error(data.error || "Error interno del servidor");
        }

        // 7. Render Success Result
        // Determine color based on sentiment
        const color = data.prediction === "Positivo" ? "#16a34a" : "#dc2626"; // Green or Red

        resultDiv.innerHTML = `
            <div style="font-size: 1.2em; color: ${color}; font-weight: bold;">
                ${data.prediction.toUpperCase()}
            </div>
            <div>Confianza: ${(data.probability * 100).toFixed(1)}%</div>
            <small style="color: #888">Procesado: ${data.timestamp}</small>
        `;

        // Render Keywords (if available)
        if (data.keywords && data.keywords.length > 0) {
            keywordsDiv.innerHTML = data.keywords
                .map((word) => `<span class="keyword-tag">#${word}</span>`)
                .join("");
        }
    } catch (error) {
        // 8. Error Handling
        console.error("Analysis failed:", error);
        resultDiv.innerHTML = `
            ❌ Error de conexión.<br>
            <small>Verifica que el Backend (Docker) esté corriendo.</small>
        `;
    }
}
