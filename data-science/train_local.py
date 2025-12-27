# ==========================================
# LOCAL TRAINING SCRIPT (RE-TRAINING)
# ==========================================
# Usage: python train_local.py
# This script re-trains the model using your local Python version
# to avoid "InconsistentVersionWarning" errors.

import os
import re
import joblib
import kagglehub
import pandas as pd
import numpy as np
from sklearn.pipeline import Pipeline
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression

# 1. CONFIGURATION
# ==========================================
MODEL_DIR = "models"
MODEL_FILENAME = "sentiment_model_alpha.joblib" # Name expected by main.py
OUTPUT_PATH = os.path.join(MODEL_DIR, MODEL_FILENAME)

print("🚀 STARTING LOCAL RE-TRAINING PROTOCOL")
print(f"python version: {os.sys.version.split()[0]}")

# 2. DATA LOADING (ETL)
# ==========================================
print("\n⬇️  Step 1: downloading/loading dataset via KaggleHub...")
try:
    # Downloads to local cache
    path = kagglehub.dataset_download("mexwell/amazon-reviews-multi")
    print(f"   Cache path: {path}")

    # Load CSVs
    df_train = pd.read_csv(os.path.join(path, "train.csv"))
    df_val = pd.read_csv(os.path.join(path, "validation.csv"))

    # Filter Spanish
    df_train = df_train[df_train['language'] == 'es']
    df_val = df_val[df_val['language'] == 'es']

    print(f"   ✅ Data Loaded. Train: {len(df_train)} | Val: {len(df_val)}")

except Exception as e:
    print(f"❌ Error loading data: {e}")
    print("   Check your internet connection or KaggleHub installation.")
    exit(1)

# 3. MERGE (ALL-IN STRATEGY)
# ==========================================
print("\n🔄 Step 2: Merging Train + Validation (All-In Strategy)...")
df_full = pd.concat([df_train, df_val])
print(f"   🔥 Full Dataset Size: {len(df_full)} rows")

# 4. PREPROCESSING
# ==========================================
print("\n🧹 Step 3: Cleaning Text (Matching API Logic)...")

def map_sentiment_score(stars):
    if stars <= 2: return 'Negative'
    elif stars == 3: return 'Neutral'
    else: return 'Positive'

def clean_text_advanced(text):
    """
    Same logic as main.py (includes 'ü' and 'ñ')
    """
    if not isinstance(text, str): return ""
    text = text.lower()
    # Updated Regex to match your API safety check
    text = re.sub(r'[^a-záéíóúñü\s]', '', text)
    text = " ".join(text.split())
    return text

# Apply Logic
df_full['sentiment'] = df_full['stars'].apply(map_sentiment_score)
df_full['clean_text'] = df_full['review_body'].astype(str).apply(clean_text_advanced)

# Handle NaNs created by cleaning
df_full['clean_text'] = df_full['clean_text'].replace('', np.nan)
df_full.dropna(subset=['clean_text', 'sentiment'], inplace=True)

print(f"   ✅ Cleaned. Final rows for training: {len(df_full)}")

# 5. TRAINING
# ==========================================
print("\n🏋️  Step 4: Training Pipeline (TF-IDF + LogReg)...")
print("   This might take 1-2 minutes...")

pipeline = Pipeline([
    # V2 Params: Max features 5000, No stopword removal here (context preservation)
    ('tfidf', TfidfVectorizer(max_features=5000)),
    ('clf', LogisticRegression(max_iter=1000, class_weight='balanced'))
])

pipeline.fit(df_full['clean_text'], df_full['sentiment'])
print("   ✅ Training Complete.")

# 6. EXPORT
# ==========================================
print("\n💾 Step 5: Saving Artifact...")

# Ensure 'models' directory exists
if not os.path.exists(MODEL_DIR):
    os.makedirs(MODEL_DIR)
    print(f"   Created directory: {MODEL_DIR}")

joblib.dump(pipeline, OUTPUT_PATH)

print(f"   ✅ MODEL SAVED AT: {OUTPUT_PATH}")
print("   -------------------------------------------------------")
print("   🎉 You can now restart your API. Version mismatch is fixed.")
print("   -------------------------------------------------------")
