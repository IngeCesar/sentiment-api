/**
 * Sentiment Analysis Frontend Script
 * Team: H12-25-L-EQUIPO-55 (CesiumFlow)
 */

// ✅ CONFIGURATION UPDATE
// Points to the versioned Java API
const API_URL = "http://localhost:8080/api/v1/sentiment";

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
            "<span style='color:red'>⚠️ This field is required.</span>";
        return;
    }
    if (text.length < 3) {
        resultDiv.innerHTML =
            "<span style='color:orange'>⚠️ Minimum 3 characters required.</span>";
        return;
    }
    if (text.length > 5000) {
        resultDiv.innerHTML =
            "<span style='color:red'>⚠️ Maximum 5000 characters allowed.</span>";
        return;
    }

    // Show loading state
    resultDiv.innerHTML = "⏳ Analyzing...";

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

        // 6. Error Handling (Network or Logic)
        if (!response.ok) {
            // Should capture the Spring Boot 400 Bad Request message if available
            throw new Error(data.message || data.error || "Server Error");
        }

        if (
            data.prediction === "CONNECTION_ERROR" ||
            data.prediction === "ERROR"
        ) {
            throw new Error("Internal Engine Unavailable");
        }

        // 7. Render Success Result
        const color = data.prediction === "Positivo" ? "#16a34a" : "#dc2626";

        resultDiv.innerHTML = `
            <div style="font-size: 1.2em; color: ${color}; font-weight: bold;">
                ${data.prediction.toUpperCase()}
            </div>
            <div>Confidence: ${(data.probability * 100).toFixed(1)}%</div>
            <small style="color: #888">Processed: ${data.timestamp}</small>
        `;

        if (data.keywords && data.keywords.length > 0) {
            keywordsDiv.innerHTML = data.keywords
                .map((word) => `<span class="keyword-tag">#${word}</span>`)
                .join("");
        }
    } catch (error) {
        console.error("Analysis failed:", error);
        resultDiv.innerHTML = `
            ❌ Connection Error.<br>
            <small>${error.message}</small>
        `;
    }
}
