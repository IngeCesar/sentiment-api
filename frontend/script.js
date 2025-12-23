function analyzeSentiment() {
    const textInput = document.getElementById("textInput");
    const resultDiv = document.getElementById("result");

    const text = textInput.value.trim();

    // Validación frontend
    if (!text) {
        resultDiv.innerHTML = "⚠️ El campo de texto es obligatorio.";
        return;
    }

    if (text.length < 3) {
        resultDiv.innerHTML = "⚠️ El texto debe tener al menos 3 caracteres.";
        return;
    }

    if (text.length > 5000) {
        resultDiv.innerHTML = "⚠️ El texto no puede exceder 5000 caracteres.";
        return;
    }

    fetch("http://localhost:8080/sentiment", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ text: text })
    })
    .then(async response => {
        const data = await response.json();

        // Manejo de errores HTTP definidos en contrato
        if (!response.ok) {
            resultDiv.innerHTML = `
                ❌ Error (${data.code}): ${data.error}<br>
                <small>${data.timestamp}</small>
            `;
            return;
        }

        // Respuesta exitosa
        resultDiv.innerHTML = `
            ✅ Sentimiento: ${data.prediction}<br>
            📊 Probabilidad: ${(data.probability * 100).toFixed(2)}%<br>
            🕒 Fecha: ${data.timestamp}
        `;
    })
    .catch(error => {
        console.error(error);
        resultDiv.innerHTML = "❌ Error de conexión con el servidor.";
    });
}
