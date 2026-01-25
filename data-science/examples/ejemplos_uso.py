"""
Ejemplo de uso del clasificador de sentimientos.
Demuestra diferentes casos de uso y funcionalidades.
"""
from src import SentimentPredictor

def ejemplo_basico():
    """Ejemplo básico de predicción."""
    print("=" * 70)
    print("EJEMPLO 1: Uso Básico")
    print("=" * 70)
    
    predictor = SentimentPredictor()
    
    texto = "Excelente producto, superó mis expectativas!!!"
    result = predictor.predict(texto)
    
    print(f"Texto: {texto}")
    print(f"├─ Predicción: {result.prediction}")
    print(f"├─ Probabilidad: {result.probability:.0%}")
    print(f"├─ Keywords: {', '.join(result.keywords)}")
    print(f"└─ Timestamp: {result.timestamp}")
    print()

def ejemplo_multiple():
    """Ejemplo con múltiples reseñas."""
    print("=" * 70)
    print("EJEMPLO 2: Múltiples Reseñas")
    print("=" * 70)
    
    predictor = SentimentPredictor()
    
    reseñas = [
        "El producto llegó roto, muy mala calidad",
        "Cumple su función, nada especial",
        "¡Increíble! Mejor compra del año",
        "NO funciona, HORRIBLE experiencia",
        "Producto correcto por el precio que tiene"
    ]
    
    print(f"{'Predicción':<12} {'Prob':<6} {'Texto'}")
    print("-" * 70)
    
    for texto in reseñas:
        result = predictor.predict(texto)
        texto_corto = texto[:45] + "..." if len(texto) > 45 else texto
        print(f"{result.prediction:<12} {result.probability:>4.0%}   {texto_corto}")
    print()

def ejemplo_manejo_errores():
    """Ejemplo de manejo de errores."""
    print("=" * 70)
    print("EJEMPLO 3: Manejo de Errores")
    print("=" * 70)
    
    predictor = SentimentPredictor()
    
    casos_invalidos = [
        ("", "Texto vacío"),
        (None, "None"),
        ("   ", "Solo espacios"),
    ]
    
    for texto, descripcion in casos_invalidos:
        try:
            result = predictor.predict(texto)
            print(f"✗ {descripcion}: Debería haber lanzado error")
        except ValueError as e:
            print(f"✓ {descripcion}: {e}")
    print()

def ejemplo_formato_json():
    """Ejemplo de conversión a JSON."""
    print("=" * 70)
    print("EJEMPLO 4: Formato JSON")
    print("=" * 70)
    
    predictor = SentimentPredictor()
    
    texto = "Producto de buena calidad, lo recomiendo"
    result = predictor.predict(texto)
    
    # Convertir a diccionario
    data = result.to_dict()
    
    print(f"Texto: {texto}")
    print("\nRespuesta JSON:")
    import json
    print(json.dumps(data, indent=2, ensure_ascii=False))
    print()

def ejemplo_keywords():
    """Ejemplo enfocado en keywords."""
    print("=" * 70)
    print("EJEMPLO 5: Análisis de Keywords")
    print("=" * 70)
    
    predictor = SentimentPredictor()
    
    textos = [
        "EXCELENTE calidad, muy recomendado, producto perfecto",
        "Pésimo servicio, llegó tarde y roto, nunca más",
        "El producto es normal, funciona bien pero nada especial"
    ]
    
    for texto in textos:
        result = predictor.predict(texto)
        print(f"\nTexto: {texto}")
        print(f"Sentimiento: {result.prediction} ({result.probability:.0%})")
        print(f"Keywords más relevantes:")
        for i, kw in enumerate(result.keywords, 1):
            print(f"  {i}. {kw}")
    print()

def ejemplo_batch_processing():
    """Ejemplo de procesamiento por lotes eficiente."""
    print("=" * 70)
    print("EJEMPLO 6: Procesamiento por Lotes (Eficiente)")
    print("=" * 70)
    
    import time
    
    # ✅ CORRECTO: Instanciar predictor una sola vez
    predictor = SentimentPredictor()
    
    # Simular 100 reseñas
    reseñas = [
        "Producto excelente" if i % 3 == 0 else
        "Producto horrible" if i % 3 == 1 else
        "Producto normal"
        for i in range(100)
    ]
    
    start = time.time()
    resultados = []
    for texto in reseñas:
        result = predictor.predict(texto)
        resultados.append(result)
    
    elapsed = time.time() - start
    
    print(f"Procesadas: {len(reseñas)} reseñas")
    print(f"Tiempo total: {elapsed*1000:.0f} ms")
    print(f"Tiempo promedio: {elapsed*1000/len(reseñas):.1f} ms/reseña")
    print()
    
    # Resumen de resultados
    from collections import Counter
    sentimientos = [r.prediction for r in resultados]
    contador = Counter(sentimientos)
    
    print("Distribución de sentimientos:")
    for sent, count in contador.items():
        pct = count / len(resultados) * 100
        print(f"  {sent}: {count} ({pct:.1f}%)")
    print()

def main():
    """Ejecutar todos los ejemplos."""
    print("\n" + "=" * 70)
    print(" EJEMPLOS DE USO - CLASIFICADOR DE SENTIMIENTOS")
    print("=" * 70 + "\n")
    
    ejemplo_basico()
    ejemplo_multiple()
    ejemplo_manejo_errores()
    ejemplo_formato_json()
    ejemplo_keywords()
    ejemplo_batch_processing()
    
    print("=" * 70)
    print("✅ Todos los ejemplos ejecutados correctamente")
    print("=" * 70)

if __name__ == "__main__":
    main()
