"""
Test rápido para verificar que los módulos cargan correctamente
"""
import sys
from pathlib import Path

# Agregar src al path
sys.path.insert(0, str(Path(__file__).parent / "src"))

print("=" * 70)
print("TEST RÁPIDO DE CARGA DE MÓDULOS")
print("=" * 70)

try:
    print("\n[1/5] Importando config...")
    from cesiumflow_ml import config
    print(f"  ✓ BASE_DIR: {config.BASE_DIR}")
    print(f"  ✓ ARTIFACTS_DIR: {config.ARTIFACTS_DIR}")
    print(f"  ✓ MODEL_PATH: {config.MODEL_PATH}")
    print(f"  ✓ Archivo existe: {config.MODEL_PATH.exists()}")
    
    print("\n[2/5] Importando model_loader...")
    from cesiumflow_ml import model_loader
    print("  ✓ model_loader importado")
    
    print("\n[3/5] Importando predictor...")
    from cesiumflow_ml.predictor import SentimentPredictor
    print("  ✓ SentimentPredictor importado")
    
    print("\n[4/5] Inicializando predictor...")
    predictor = SentimentPredictor()
    print("  ✓ Predictor inicializado correctamente")
    
    print("\n[5/5] Probando predicción...")
    resultado = predictor.predict("Excelente producto, muy recomendado")
    print(f"  ✓ Predicción: {resultado.prediction}")
    print(f"  ✓ Probabilidad: {resultado.probability:.2f}")
    print(f"  ✓ Keywords: {resultado.keywords[:3]}")
    
    print("\n" + "=" * 70)
    print("✅ TODOS LOS TESTS PASARON - MÓDULOS FUNCIONAN CORRECTAMENTE")
    print("=" * 70)
    
except Exception as e:
    print(f"\n❌ ERROR: {e}")
    import traceback
    traceback.print_exc()
    print("\n" + "=" * 70)
    print("❌ TEST FALLÓ")
    print("=" * 70)
    sys.exit(1)
