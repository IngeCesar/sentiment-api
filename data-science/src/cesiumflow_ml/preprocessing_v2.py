"""Funciones de preprocesamiento para el clasificador de sentimientos."""
from __future__ import annotations

import logging
import re
from pathlib import Path
from typing import List

logger = logging.getLogger(__name__)

TOKEN_PATTERN = r'<[A-Z_0-9]+>|\b\w\w+\b'
DEFAULT_STOPWORDS: List[str] = []

NUMEROS_TEXTO = {
    '0': 'cero', '1': 'uno', '2': 'dos', '3': 'tres', '4': 'cuatro',
    '5': 'cinco', '6': 'seis', '7': 'siete', '8': 'ocho', '9': 'nueve',
    '10': 'diez', '11': 'once', '12': 'doce', '13': 'trece', '14': 'catorce',
    '15': 'quince', '16': 'dieciseis', '17': 'diecisiete', '18': 'dieciocho',
    '19': 'diecinueve', '20': 'veinte', '21': 'veintiuno', '22': 'veintidos',
    '23': 'veintitres', '24': 'veinticuatro', '25': 'veinticinco',
    '30': 'treinta', '40': 'cuarenta', '50': 'cincuenta', '60': 'sesenta',
    '70': 'setenta', '80': 'ochenta', '90': 'noventa', '100': 'cien'
}

ORDINALES = {
    '1': 'primer', '2': 'segundo', '3': 'tercer', '4': 'cuarto', '5': 'quinto',
    '6': 'sexto', '7': 'septimo', '8': 'octavo', '9': 'noveno', '10': 'decimo'
}

EMOJI_PATTERN = re.compile(
    "["
    "\U0001F600-\U0001F64F"  # emoticons
    "\U0001F300-\U0001F5FF"  # symbols & pictographs
    "\U0001F680-\U0001F6FF"  # transport & map symbols
    "\U0001F700-\U0001F77F"  # alchemical symbols
    "\U0001F780-\U0001F7FF"  # geometric shapes extended
    "\U0001F800-\U0001F8FF"  # supplemental arrows-C
    "\U0001F900-\U0001F9FF"  # supplemental symbols and pictographs
    "\U0001FA00-\U0001FA6F"  # chess symbols
    "\U0001FA70-\U0001FAFF"  # symbols and pictographs extended-A
    "\U00002702-\U000027B0"  # dingbats
    "\U00002300-\U000023FF"  # miscellaneous technical
    "\U0001F1E0-\U0001F1FF"  # flags (iOS)
    "]+",
    flags=re.UNICODE
)

def _cargar_stopwords(path: Path | None) -> List[str]:
    """Carga stopwords desde un archivo; si falla, retorna la lista por defecto."""
    if path and path.exists():
        try:
            return [w.strip() for w in path.read_text(encoding="utf-8").splitlines() if w.strip()]
        except Exception as exc:  # pragma: no cover
            logger.warning("No se pudieron cargar stopwords desde %s: %s", path, exc)
    return DEFAULT_STOPWORDS.copy()

def _numero_a_texto(num_str: str) -> str:
    """Convierte un numero string a texto espanol."""
    return NUMEROS_TEXTO.get(num_str, num_str)

def limpiar_ruido(texto: str) -> str:
    """
    Elimina elementos de ruido del texto que no aportan valor semantico.
    Elimina URLs, emails, menciones, hashtags y caracteres especiales.
    """
    if not texto:
        return ""
    
    texto = str(texto)
    
    # Eliminar URLs
    texto = re.sub(r'https?://\S+|www\.\S+', '', texto)
    
    # Eliminar correos electronicos
    texto = re.sub(r'\S+@\S+\.\S+', '', texto)
    
    # Eliminar menciones y hashtags
    texto = re.sub(r'@\w+', '', texto)
    texto = re.sub(r'#\w+', '', texto)
    
    # Eliminar caracteres < y > (CRITICO: antes de tokenizacion)
    texto = re.sub(r'[<>]', '', texto)
    
    # Normalizar espacios multiples
    texto = re.sub(r'\s+', ' ', texto).strip()
    
    return texto

def eliminar_emojis(texto: str) -> str:
    """Elimina todos los emojis del texto."""
    if not texto:
        return ""
    
    texto = EMOJI_PATTERN.sub('', str(texto))
    texto = re.sub(r'\s+', ' ', texto).strip()
    
    return texto

def tokenizar_caracteres_especiales(texto: str) -> str:
    """
    Convierte patrones de precio y porcentaje a tokens especiales.
    """
    if not texto:
        return ""
    
    texto = str(texto)
    
    # Precio: numero + euro (con o sin espacio)
    texto = re.sub(r'\d+[.,]?\d*\s*€', ' <PRECIO> ', texto)
    
    # Precio: euro + numero
    texto = re.sub(r'€\s*\d+[.,]?\d*', ' <PRECIO> ', texto)
    
    # Precio: $ + numero
    texto = re.sub(r'\$\s*\d+[.,]?\d*', ' <PRECIO> ', texto)
    
    # Precio: numero + $
    texto = re.sub(r'\d+[.,]?\d*\s*\$', ' <PRECIO> ', texto)
    
    # Porcentaje: numero + % (con o sin espacio)
    texto = re.sub(r'\d+\s*%', ' <PORCENTAJE> ', texto)
    
    # Normalizar espacios multiples
    texto = re.sub(r'\s+', ' ', texto).strip()
    
    return texto

def tokenizar_patrones_numericos(texto: str) -> str:
    """
    Transforma patrones numericos con significado semantico a texto.
    Elimina numeros aislados sin contexto.
    """
    if not texto:
        return ""
    
    texto = str(texto)
    
    # Duracion: "2 dias" a "dos dias"
    def reemplazar_duracion(match):
        num = match.group(1)
        unidad = match.group(2).lower()
        texto_num = _numero_a_texto(num)
        return f"{texto_num} {unidad}"
    
    texto = re.sub(
        r'\b(\d{1,2})\s*(dias?|semanas?|meses?|anos?|horas?|minutos?)\b',
        reemplazar_duracion, texto, flags=re.IGNORECASE
    )
    
    # Numero de veces: "3 veces" a "tres veces"
    def reemplazar_veces(match):
        num = match.group(1)
        texto_num = _numero_a_texto(num)
        return f"{texto_num} veces"
    
    texto = re.sub(r'\b(\d{1,2})\s*veces\b', reemplazar_veces, texto, flags=re.IGNORECASE)
    
    # Cantidad: "5 unidades" a "cinco unidades"
    def reemplazar_cantidad(match):
        num = match.group(1)
        unidad = match.group(2).lower()
        texto_num = _numero_a_texto(num)
        return f"{texto_num} {unidad}"
    
    texto = re.sub(
        r'\b(\d{1,2})\s*(unidades?|piezas?|productos?|articulos?|paquetes?)\b',
        reemplazar_cantidad, texto, flags=re.IGNORECASE
    )
    
    # Ordinales typo: "3er" a "tercer"
    def reemplazar_ordinal(match):
        num = match.group(1)
        return ORDINALES.get(num, match.group(0))
    
    texto = re.sub(r'\b(\d)(er|do|ro|to|vo|mo|no)\b', reemplazar_ordinal, texto, flags=re.IGNORECASE)
    
    # Estrellas: "5 estrellas" a "cinco estrellas"
    def reemplazar_estrellas(match):
        num = match.group(1)
        texto_num = _numero_a_texto(num)
        return f"{texto_num} estrellas"
    
    texto = re.sub(r'\b([1-5])\s*estrellas?\b', reemplazar_estrellas, texto, flags=re.IGNORECASE)
    
    # Eliminar numeros aislados (sin contexto semantico)
    texto = re.sub(r'\b\d+[.,]?\d*\b', '', texto)
    
    # Normalizar espacios multiples
    texto = re.sub(r'\s+', ' ', texto).strip()
    
    return texto

def tokenizar_puntuacion_repetida(texto: str) -> str:
    """
    Convierte puntuacion repetida a tokens que capturan intensidad emocional.
    IMPORTANTE: El orden de las regex es critico (mas largo primero).
    """
    if not texto:
        return ""
    
    texto = str(texto)
    
    # Exclamaciones (orden: mas largo primero)
    texto = re.sub(r'!{4,}', ' <EXCL_MULT> ', texto)   # 4 o mas extremo
    texto = re.sub(r'!{3}', ' <EXCL_3> ', texto)       # exactamente 3 alto
    texto = re.sub(r'!{2}', ' <EXCL_2> ', texto)       # exactamente 2 moderado
    
    # Interrogaciones (orden: mas largo primero)
    texto = re.sub(r'\?{4,}', ' <INTER_MULT> ', texto)  # 4 o mas extremo
    texto = re.sub(r'\?{3}', ' <INTER_3> ', texto)      # exactamente 3 alto
    texto = re.sub(r'\?{2}', ' <INTER_2> ', texto)      # exactamente 2 moderado
    
    # Puntos suspensivos (orden: mas largo primero)
    texto = re.sub(r'\.{4,}', ' <SUSP_MULT> ', texto)  # 4 o mas puntos
    texto = re.sub(r'\.{3}', ' <SUSP_3> ', texto)      # exactamente 3 puntos
    
    # Puntuacion mixta (!?, ?!, etc.)
    texto = re.sub(r'[!?]{2,}', ' <MIXTO> ', texto)
    
    # Normalizar espacios multiples
    texto = re.sub(r'\s+', ' ', texto).strip()
    
    return texto

def eliminar_caracteres_restantes(texto: str) -> str:
    """
    Elimina caracteres especiales que no aportan valor semantico.
    NOTA: NO eliminar < > porque los tokens especiales deben preservarse.
    """
    if not texto:
        return ""
    
    texto = str(texto)
    
    # Lista de tokens especiales a preservar
    tokens_especiales = [
        '<PRECIO>', '<PORCENTAJE>', 
        '<EXCL_2>', '<EXCL_3>', '<EXCL_MULT>',
        '<INTER_2>', '<INTER_3>', '<INTER_MULT>',
        '<SUSP_3>', '<SUSP_MULT>', '<MIXTO>'
    ]
    
    # Reemplazar tokens especiales con placeholders unicos
    placeholders = {}
    for i, token in enumerate(tokens_especiales):
        placeholder = f"PLACEHOLDER{i}HOLDER"
        placeholders[placeholder] = token
        texto = texto.replace(token, placeholder)
    
    # Caracteres a eliminar (sin < >)
    caracteres_eliminar = r'[\(\)\-/\+\*=#&\^~\\|\[\]\{\}@_]'
    texto = re.sub(caracteres_eliminar, ' ', texto)
    
    # Restaurar tokens especiales
    for placeholder, token in placeholders.items():
        texto = texto.replace(placeholder, token)
    
    # Normalizar espacios multiples
    texto = re.sub(r'\s+', ' ', texto).strip()
    
    return texto

def normalizar_mayusculas(texto: str) -> str:
    """
    Normaliza mayusculas preservando palabras ALL-CAPS.
    Logica: Palabras completamente en mayusculas (>=2 chars) se PRESERVAN.
    Razon: ALL-CAPS indica enfasis emocional importante para sentiment.
    """
    if not texto:
        return ""
    
    texto = str(texto)
    palabras = texto.split()
    resultado = []
    
    for palabra in palabras:
        # Preservar tokens especiales como estan
        if palabra.startswith('<') and palabra.endswith('>'):
            resultado.append(palabra)
        # Preservar palabras ALL-CAPS de 2+ caracteres
        elif palabra.isupper() and len(palabra) >= 2:
            resultado.append(palabra)
        else:
            resultado.append(palabra.lower())
    
    return ' '.join(resultado)

def preprocesar_texto(texto: str) -> str:
    """Pipeline completo de preprocesamiento (7 pasos)."""
    t = limpiar_ruido(texto)
    t = eliminar_emojis(t)
    t = tokenizar_caracteres_especiales(t)
    t = tokenizar_patrones_numericos(t)
    t = tokenizar_puntuacion_repetida(t)
    t = eliminar_caracteres_restantes(t)
    t = normalizar_mayusculas(t)
    return t

__all__ = [
    "TOKEN_PATTERN",
    "DEFAULT_STOPWORDS",
    "NUMEROS_TEXTO",
    "ORDINALES",
    "EMOJI_PATTERN",
    "limpiar_ruido",
    "eliminar_emojis",
    "tokenizar_caracteres_especiales",
    "tokenizar_patrones_numericos",
    "tokenizar_puntuacion_repetida",
    "eliminar_caracteres_restantes",
    "normalizar_mayusculas",
    "preprocesar_texto",
    "_cargar_stopwords",
]
