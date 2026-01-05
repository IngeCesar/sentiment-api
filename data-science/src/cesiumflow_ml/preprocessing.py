import re

def clean_text(text: str) -> str:
    """
    Normalizes and cleans text specifically for Spanish Sentiment Analysis.

    Operations:
    1. Lowercase conversion.
    2. Removal of URLs, mentions, and hashtags.
    3. Filtering non-alphabetic characters (preserving Spanish accents/ñ).
    4. Whitespace normalization.

    Args:
        text (str): Raw input text.

    Returns:
        str: Cleaned text ready for vectorization.
    """
    if not isinstance(text, str):
        return ""

    # 1. Convert to lowercase
    normalized_text = text.lower()

    # 2. Remove URLs (http/https/www)
    normalized_text = re.sub(r'http\S+|www\S+', '', normalized_text)

    # 3. Remove mentions (@username)
    normalized_text = re.sub(r'@\w+', '', normalized_text)

    # 4. Remove hashtags (removes the symbol #)
    normalized_text = re.sub(r'#', '', normalized_text)

    # 5. Filter characters: Keep only letters, numbers, spaces and SPANISH chars
    # Logic: [^a-záéíóúüñ\s] means "replace anything that is NOT a lowercase letter or spanish accent"
    normalized_text = re.sub(r'[^a-záéíóúüñ\s]', '', normalized_text, flags=re.IGNORECASE)

    # 6. Collapse multiple spaces into one
    normalized_text = re.sub(r'\s+', ' ', normalized_text)

    return normalized_text.strip()
