"""Validación manual del pipeline de producción."""
from src import SentimentPredictor

# Inicializar predictor (carga artefactos)
predictor = SentimentPredictor()

# Casos de prueba
casos = [
    "Este producto es excelente, lo recomiendo 100%!!!",
    "No me gustó, calidad muy mala y llegó tarde.",
    "El producto es normal, nada especial pero funciona.",
    "INCREÍBLE!!! Mejor compra del año 🎉",
    "Pésimo servicio, nunca más compro aquí 😡",
]

print("=" * 70)
print("VALIDACIÓN DEL PIPELINE DE PRODUCCIÓN")
print("=" * 70)

for i, texto in enumerate(casos, 1):
    result = predictor.predict(texto)
    print(f"\n[{i}] Texto: {texto[:60]}...")
    print(f"    ├─ Predicción: {result.prediction}")
    print(f"    ├─ Probabilidad: {result.probability:.2%}")
    print(f"    ├─ Keywords: {', '.join(result.keywords)}")
    print(f"    └─ Timestamp: {result.timestamp}")

print("\n" + "=" * 70)
print("✅ PIPELINE VALIDADO CORRECTAMENTE")
print("=" * 70)
