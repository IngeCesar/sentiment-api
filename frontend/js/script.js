/**
 * Sentiment Analysis Frontend Script
 * Team: H12-25-L-EQUIPO-55 (CesiumFlow)
 */

// ✅ CONFIGURATION:
// Relative path to leverage Nginx Reverse Proxy
const API_URL = "/api/v1/sentiment";

async function analyzeSentiment() {
    // 1. DOM Elements
    const textInput = document.getElementById("textInput");
    const resultDiv = document.getElementById("result");
    const keywordsDiv = document.getElementById("keywords-container");
    const submitBtn = document.querySelector("button");

    // 2. Input Cleaning
    const text = textInput.value.trim();

    // 3. Reset UI & Lock Interface
    resultDiv.innerHTML = "";
    keywordsDiv.innerHTML = "";
    submitBtn.disabled = true;
    submitBtn.innerHTML = "Procesando..."; // Feedback visual inmediato

    // 4. Frontend Validation
    if (!text) {
        showError("⚠️ Debes escribir un comentario.");
        resetButton(submitBtn);
        return;
    }
    if (text.length < 3) {
        showError("⚠️ El texto es muy corto (mínimo 3 caracteres).");
        resetButton(submitBtn);
        return;
    }
    if (text.length > 5000) {
        showError("⚠️ Texto demasiado largo (máximo 5000 caracteres).");
        resetButton(submitBtn);
        return;
    }

    // Loading State
    resultDiv.innerHTML = `<span style="color:#666">⏳ Analizando emociones con IA...</span>`;

    try {
        // 5. API Request
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ text: text }),
        });

        const data = await response.json();

        // 6. Error Handling
        if (!response.ok) {
            const msg = data.message || "Error del Servidor";
            throw new Error(msg);
        }

        if (data.prediction === "CONNECTION_ERROR") {
            throw new Error("El motor de IA no está disponible temporalmente.");
        }

        // 7. Render Success Result
        renderSuccess(resultDiv, keywordsDiv, data);
    } catch (error) {
        console.error("Analysis failed:", error);
        resultDiv.innerHTML = `
            <div style="color: #dc2626; background: #fee2e2; padding: 15px; border-radius: 8px; margin-top: 10px;">
                ❌ <strong>Ocurrió un error</strong><br>
                <small>${escapeHtml(error.message)}</small>
            </div>
        `;
    } finally {
        // Always re-enable button
        resetButton(submitBtn);
    }
}

// --- Helper Functions ---

function resetButton(btn) {
    btn.disabled = false;
    btn.innerHTML = "Analizar Sentimiento";
}

function showError(msg) {
    const resultDiv = document.getElementById("result");
    resultDiv.innerHTML = `<div style='color:#dc2626; font-weight:500; margin-top:10px;'>${msg}</div>`;
}

function renderSuccess(resultDiv, keywordsDiv, data) {
    // 🛠️ FIX LOGIC: Check against both English and Spanish just in case
    // The backend log showed "Positive", so we need to catch that.
    const pred = data.prediction || "";
    const isPositive = pred === "Positivo" || pred === "Positive";

    // Visual Config
    const color = isPositive ? "#16a34a" : "#dc2626"; // Green vs Red
    const icon = isPositive ? "😊" : "😡"; // Happy vs Angry
    const label = isPositive ? "POSITIVO" : "NEGATIVO"; // Normalized Label

    // 🛠️ FIX ID: Removed the ID line as requested
    resultDiv.innerHTML = `
        <div style="margin-top: 20px; padding: 20px; border-radius: 12px; background-color: ${isPositive ? "#f0fdf4" : "#fef2f2"}; border: 1px solid ${color};">
            <div style="font-size: 2em; margin-bottom: 10px;">
                ${icon}
            </div>
            <div style="font-size: 1.5em; color: ${color}; font-weight: 800; letter-spacing: 1px;">
                ${label}
            </div>
            <div style="color: #555; font-weight: 500; margin-top: 5px;">
                Certeza: ${(data.probability * 100).toFixed(1)}%
            </div>
            <small style="color: #9ca3af; display:block; margin-top:10px; border-top: 1px solid #eee; padding-top: 5px;">
                Procesado: ${new Date().toLocaleTimeString()}
            </small>
        </div>
    `;

    if (data.keywords && data.keywords.length > 0) {
        keywordsDiv.innerHTML = data.keywords
            .map(
                (word) =>
                    `<span class="keyword-tag">#${escapeHtml(word)}</span>`,
            )
            .join("");
    }
}

// XSS Protection Helper
function escapeHtml(text) {
    if (!text) return text;
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
