"""
API de Análisis de Sentimientos
================================
Módulo para predicción de sentimientos en tweets en español.

Funciones principales:
- predecir_sentimiento(): Predicción completa con keywords
- limpiar_texto(): Preprocesamiento de texto
- extraer_keywords(): Extracción de palabras clave

Autor: Data Science Expert
Fecha: 2025
"""

import re
import numpy as np
import joblib
from typing import Dict, List


def limpiar_texto(texto: str) -> str:
    """
    Limpia el texto removiendo URLs, menciones, hashtags especiales y caracteres no deseados.
    Preserva tildes y ñ para el español.

    Args:
        texto (str): Texto a limpiar

    Returns:
        str: Texto limpio en minúsculas
    """
    # Remover URLs
    texto = re.sub(r'http\S+|www\S+', '', texto)
    # Remover menciones (@usuario)
    texto = re.sub(r'@\w+', '', texto)
    # Remover hashtags pero preservar el texto
    texto = re.sub(r'#', '', texto)
    # Remover caracteres especiales pero preservar tildes, ñ y espacios
    texto = re.sub(r'[^a-záéíóúüñ\s]', '', texto, flags=re.IGNORECASE)
    # Remover espacios múltiples
    texto = re.sub(r'\s+', ' ', texto)
    # Convertir a minúsculas y remover espacios al inicio/final
    return texto.lower().strip()


def extraer_keywords(texto: str, modelo, vectorizador, top_n: int = 5) -> List[str]:
    """
    Extrae las palabras clave más relevantes del texto usando los coeficientes del modelo.

    Args:
        texto (str): Texto de entrada a analizar
        modelo: Modelo de clasificación entrenado (LogisticRegression)
        vectorizador: Vectorizador TF-IDF entrenado
        top_n (int): Número de keywords a retornar

    Returns:
        list: Lista de strings con las top N palabras presentes en el texto
    """
    # Limpiar el texto de entrada
    texto_limpio = limpiar_texto(texto)

    # Vectorizar el texto
    texto_tfidf = vectorizador.transform([texto_limpio])

    # Obtener la clase predicha
    clase_predicha = modelo.predict(texto_tfidf)[0]

    # Mapear clase a índice
    clases = modelo.classes_
    idx_clase = np.where(clases == clase_predicha)[0][0]

    # Obtener coeficientes para la clase predicha
    coeficientes = modelo.coef_[idx_clase]

    # Obtener nombres de características
    feature_names = vectorizador.get_feature_names_out()

    # Obtener las palabras presentes en el texto (TF-IDF > 0)
    texto_vector = texto_tfidf.toarray()[0]
    palabras_presentes_idx = np.where(texto_vector > 0)[0]

    # Calcular contribución: coeficiente × TF-IDF para palabras presentes
    contribuciones = []
    for idx in palabras_presentes_idx:
        palabra = feature_names[idx]
        contribucion = abs(coeficientes[idx] * texto_vector[idx])
        contribuciones.append((palabra, contribucion))

    # Ordenar por contribución y tomar las top N
    contribuciones.sort(key=lambda x: x[1], reverse=True)
    keywords = [palabra for palabra, _ in contribuciones[:top_n]]

    return keywords


def predecir_sentimiento(texto: str, modelo, vectorizador, top_n_keywords: int = 5) -> Dict:
    """
    Función completa de predicción para API de sentimientos.
    INCLUYE SANITIZACIÓN: Elimina los corchetes [ ] de la predicción.
    """
    # Limpiar texto
    texto_limpio = limpiar_texto(texto)

    # Vectorizar
    texto_tfidf = vectorizador.transform([texto_limpio])

    # Predicción RAW (Tal cual sale del modelo, ej: "[POS]")
    prediccion_raw = modelo.predict(texto_tfidf)[0]

    # Probabilidades
    probabilidades = modelo.predict_proba(texto_tfidf)[0]

    # IMPORTANTE: Usamos prediccion_raw para buscar en las clases del modelo
    # (El modelo no sabe que queremos quitar los corchetes)
    clases = modelo.classes_
    idx_prediccion = np.where(clases == prediccion_raw)[0][0]
    confianza = float(probabilidades[idx_prediccion])

    # Extraer keywords
    keywords = extraer_keywords(texto, modelo, vectorizador, top_n=top_n_keywords)

    # --- SANITIZACIÓN EN LA FUENTE ---
    # Limpiamos la predicción justo antes de entregarla al mundo
    prediccion_limpia = prediccion_raw.replace("[", "").replace("]", "")

    # Retornar resultado en formato API con datos limpios
    return {
        "prediction": prediccion_limpia, # Ej: "POS"
        "probability": confianza,
        "keywords": keywords
    }


def cargar_modelo(modelo_path: str = 'sentiment_model.joblib',
                  vectorizador_path: str = 'tfidf_vectorizer.joblib'):
    """
    Carga el modelo y vectorizador desde archivos joblib.

    Args:
        modelo_path (str): Ruta al archivo del modelo
        vectorizador_path (str): Ruta al archivo del vectorizador

    Returns:
        tuple: (modelo, vectorizador)
    """
    modelo = joblib.load(modelo_path)
    vectorizador = joblib.load(vectorizador_path)
    return modelo, vectorizador


# Ejemplo de uso
if __name__ == "__main__":
    # Cargar modelo y vectorizador
    modelo, vectorizador = cargar_modelo()

    # Ejemplos de predicción
    textos_prueba = [
        "Estoy muy feliz y emocionado por este logro increíble",
        "Me siento triste y desmotivado, todo sale mal",
        "Hoy fue un día normal, nada especial",
        "¡Qué furia! Esto es completamente inaceptable",
        "Tengo miedo de lo que pueda pasar mañana"
    ]

    print("=" * 80)
    print("ANÁLISIS DE SENTIMIENTOS - API")
    print("=" * 80)

    for i, texto in enumerate(textos_prueba, 1):
        resultado = predecir_sentimiento(texto, modelo, vectorizador)
        print(f"\n{i}. TEXTO: {texto}")
        print(f"   SENTIMIENTO: {resultado['prediction']}")
        print(f"   CONFIANZA: {resultado['probability']:.2%}")
        print(f"   KEYWORDS: {', '.join(resultado['keywords'])}")

    print("\n" + "=" * 80)
