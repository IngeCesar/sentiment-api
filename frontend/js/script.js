// ✅ CONFIGURATION
// Apuntamos a la base. Nginx se encarga del puerto 8080.
const API_URL = "/api/v1/sentiment";
const STATS_URL = "/api/v1/sentiment/stats";

let sentimentChartInstance = null;
let keywordsChartInstance = null;

document.addEventListener("DOMContentLoaded", () => {
    loadDashboard();
});

async function analyzeSentiment() {
    const textInput = document.getElementById("textInput");
    const resultDiv = document.getElementById("result");
    const keywordsDiv = document.getElementById("keywords-container");
    const submitBtn = document.querySelector("button");
    const text = textInput.value.trim();

    resultDiv.innerHTML = "";
    keywordsDiv.innerHTML = "";
    submitBtn.disabled = true;
    submitBtn.innerHTML = "Procesando...";

    if (!text) {
        showError("⚠️ Escribe algo.");
        resetButton(submitBtn);
        return;
    }

    resultDiv.innerHTML = `<span style="color:#666">⏳ Analizando...</span>`;

    try {
        // 🔴 CORRECCIÓN AQUÍ: Quitamos "/analyze" porque API_URL ya tiene la ruta completa
        const response = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ text: text }),
        });

        const data = await response.json();

        if (!response.ok) throw new Error(data.message || "Error del Servidor");
        if (data.prediction === "CONNECTION_ERROR")
            throw new Error("IA no disponible.");

        renderSuccess(resultDiv, keywordsDiv, data);
        loadDashboard(); // Recargar gráficos tras éxito
    } catch (error) {
        console.error(error);
        showError("Error: " + error.message);
    } finally {
        resetButton(submitBtn);
    }
}

async function loadDashboard() {
    try {
        const response = await fetch(STATS_URL);
        if (!response.ok) return;

        const stats = await response.json();

        document.getElementById("total-counter").innerText =
            stats.totalAnalyzed || 0;
        updateSentimentChart(stats.sentimentDistribution);
        updateKeywordsChart(stats.topKeywords);
    } catch (e) {
        console.warn("No se pudieron cargar las estadísticas", e);
    }
}

// 4. RENDERIZAR GRÁFICO DE TORTA (SENTIMIENTOS) - VERSIÓN ROBUSTA 🛡️
function updateSentimentChart(distMap) {
    const ctx = document.getElementById("sentimentChart").getContext("2d");

    // 1. Debug: Mira en la consola qué llaves exactas llegan
    console.log("📊 Datos recibidos para el gráfico:", distMap);

    const map = distMap || {};

    // 2. Lógica Flexible: Sumar valores buscando coincidencias de texto
    // Así funciona con "Positive", "[POS]", "POS", "Positivo", etc.
    let posCount = 0;
    let negCount = 0;
    let neuCount = 0;

    for (const [key, value] of Object.entries(map)) {
        const k = key.toUpperCase(); // Convertir a mayúsculas para comparar

        if (k.includes("POS")) {
            posCount += value;
        } else if (k.includes("NEG")) {
            negCount += value;
        } else {
            // Todo lo que no sea explícitamente POS o NEG, lo contamos como NEUTRO
            neuCount += value;
        }
    }

    const dataValues = [posCount, neuCount, negCount];

    // Si ya existe, lo destruimos para volver a crear
    if (sentimentChartInstance) sentimentChartInstance.destroy();

    sentimentChartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Positivo", "Neutro", "Negativo"],
            datasets: [
                {
                    data: dataValues,
                    backgroundColor: ["#16a34a", "#9ca3af", "#dc2626"], // Verde, Gris, Rojo
                    borderWidth: 2,
                    borderColor: "#ffffff",
                },
            ],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom" },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            let label = context.label || "";
                            let value = context.raw || 0;
                            // Calcular porcentaje
                            let total =
                                context.chart._metasets[context.datasetIndex]
                                    .total;
                            let percentage =
                                Math.round((value / total) * 100) + "%";
                            return `${label}: ${value} (${percentage})`;
                        },
                    },
                },
            },
        },
    });
}

function updateKeywordsChart(keywordsList) {
    const ctx = document.getElementById("keywordsChart").getContext("2d");
    const list = keywordsList || [];
    const labels = list.map((k) => k.keyword);
    const values = list.map((k) => k.count);

    if (keywordsChartInstance) keywordsChartInstance.destroy();

    keywordsChartInstance = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [
                {
                    label: "Frecuencia",
                    data: values,
                    backgroundColor: "#4f46e5",
                    borderRadius: 5,
                },
            ],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: "y",
            plugins: { legend: { display: false } },
            scales: { x: { beginAtZero: true, ticks: { precision: 0 } } },
        },
    });
}

// Helpers
function resetButton(btn) {
    btn.disabled = false;
    btn.innerHTML = "Analizar Sentimiento";
}
function showError(msg) {
    document.getElementById("result").innerHTML =
        `<div style='color:#dc2626;'>${msg}</div>`;
}
function renderSuccess(resultDiv, keywordsDiv, data) {
    const pred = data.prediction || "";
    const isPositive =
        pred.includes("POS") || pred.toLowerCase().includes("positive");
    const isNegative =
        pred.includes("NEG") || pred.toLowerCase().includes("negative");

    let color = "#9ca3af";
    let icon = "😐";
    let label = "NEUTRO";

    if (isPositive) {
        color = "#16a34a";
        icon = "😊";
        label = "POSITIVO";
    }
    if (isNegative) {
        color = "#dc2626";
        icon = "😡";
        label = "NEGATIVO";
    }

    resultDiv.innerHTML = `
        <div style="margin-top: 20px; padding: 20px; border-radius: 12px; background-color: #fff; border: 2px solid ${color};">
            <div style="font-size: 2em;">${icon}</div>
            <div style="font-size: 1.5em; color: ${color}; font-weight: 800;">${label}</div>
            <div style="color: #555;">Certeza: ${(data.probability * 100).toFixed(1)}%</div>
        </div>
    `;

    if (data.keywords && data.keywords.length > 0) {
        keywordsDiv.innerHTML = data.keywords
            .map((w) => `<span class="keyword-tag">#${escapeHtml(w)}</span>`)
            .join("");
    }
}
function escapeHtml(text) {
    if (!text) return text;
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}
