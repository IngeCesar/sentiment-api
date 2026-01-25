"""
Prueba del modelo de producción con reseñas controladas.
Compara el desempeño del modelo final en producción vs. las métricas esperadas.
"""
from sklearn.metrics import accuracy_score, f1_score, classification_report, confusion_matrix
import numpy as np
from src import SentimentPredictor

# Reseñas controladas (60 reseñas: 20 por clase)
RESENAS_CONTROLADAS = [
    # =========================================================================
    # NEGATIVAS (20)
    # =========================================================================
    # Claras/directas (7)
    ("El producto llegó completamente roto y no sirve para nada, una pérdida total de dinero", "negativo"),
    ("Pésima calidad, se desarmó a los dos días de uso, no lo recomiendo para nada", "negativo"),
    ("No funciona como debería, tuve que devolverlo porque era inservible", "negativo"),
    ("Una estafa total, el producto no se parece en nada a las fotos del anuncio", "negativo"),
    ("Muy decepcionado con esta compra, el material es de muy mala calidad", "negativo"),
    ("El peor producto que he comprado en mi vida, no cumple ninguna de sus funciones", "negativo"),
    ("Llegó defectuoso y el vendedor no quiso hacerse responsable, muy mala experiencia", "negativo"),
    
    # Con matices (7)
    ("Aunque el diseño es bonito, la calidad del material es horrible y se nota barato", "negativo"),
    ("El envío fue rápido pero el producto vino dañado y con piezas faltantes", "negativo"),
    ("La idea del producto es buena pero la ejecución es pésima, no vale lo que cuesta", "negativo"),
    ("Esperaba mucho más por el precio que pagué, la realidad es que es muy inferior", "negativo"),
    ("El empaque estaba bien pero al abrir el producto noté que estaba usado y sucio", "negativo"),
    ("Funcionó bien el primer día pero después empezó a fallar constantemente", "negativo"),
    ("A pesar de las buenas reseñas que leí, mi experiencia fue muy negativa", "negativo"),
    
    # Largos descriptivos (6)
    ("Compré este producto con mucha ilusión porque las fotos se veían muy bien, pero cuando llegó me llevé una gran decepción. El material es de plástico muy barato que parece que se va a romper en cualquier momento. Además, las instrucciones vienen en un idioma que no entiendo y algunas piezas no encajan correctamente. No lo recomiendo.", "negativo"),
    ("Después de esperar tres semanas por el envío, el producto llegó en mal estado. La caja estaba aplastada y el contenido completamente dañado. Intenté contactar al vendedor pero nunca me respondió. Tuve que abrir una reclamación y fue un proceso muy tedioso. Definitivamente no volvería a comprar aquí.", "negativo"),
    ("Este es el tercer producto de esta marca que compro y los tres han salido defectuosos. No entiendo cómo siguen vendiendo cosas de tan mala calidad. El motor hace un ruido horrible desde el primer uso y se calienta demasiado. Muy peligroso y poco confiable.", "negativo"),
    ("La descripción del producto prometía características que simplemente no tiene. Decía que era resistente al agua y a la primera gota se dañó por completo. El vendedor dice que es mi culpa pero claramente es publicidad engañosa. No compren este producto.", "negativo"),
    ("Llevaba meses ahorrando para comprar este producto y la decepción fue enorme. La calidad es muy inferior a lo esperado, los acabados son toscos y el funcionamiento es errático. El dinero que gasté fue completamente desperdiciado en algo que no sirve.", "negativo"),
    ("He comprado productos similares de otras marcas a menor precio y son muchísimo mejores. Este producto es caro para la pésima calidad que ofrece. Los botones se traban, la pantalla tiene píxeles muertos y el sonido es distorsionado. Una completa basura.", "negativo"),
    
    # =========================================================================
    # NEUTRAS (20)
    # =========================================================================
    # Claras/directas (7)
    ("El producto cumple su función básica pero no tiene nada especial que destacar", "neutro"),
    ("Es un producto normal, ni muy bueno ni muy malo, simplemente cumple", "neutro"),
    ("Relación calidad precio aceptable, no es excepcional pero tampoco es malo", "neutro"),
    ("El producto es correcto para el precio, hace lo que debe hacer sin más", "neutro"),
    ("Una compra estándar, el producto llegó bien y funciona de manera normal", "neutro"),
    ("No tengo quejas pero tampoco me ha sorprendido, es un producto del montón", "neutro"),
    ("Cumple con lo básico que promete, aunque podría mejorar en algunos aspectos", "neutro"),
    
    # Con matices (7)
    ("El diseño me gusta bastante pero los materiales podrían ser de mejor calidad", "neutro"),
    ("Funciona bien para tareas sencillas aunque para uso intensivo se queda corto", "neutro"),
    ("El producto tiene sus pros y sus contras, depende de para qué lo quieras usar", "neutro"),
    ("Algunas características son buenas mientras que otras dejan bastante que desear", "neutro"),
    ("Por un lado la entrega fue rápida, pero por otro el empaquetado era muy simple", "neutro"),
    ("La calidad es aceptable considerando el precio bajo, aunque esperaba algo más", "neutro"),
    ("Tiene funciones útiles pero también algunas limitaciones que hay que considerar", "neutro"),
    
    # Largos descriptivos (6)
    ("Compré este producto hace un mes y mi opinión es bastante mixta. Por un lado funciona correctamente y hace lo que promete, pero por otro lado los materiales no son de la mejor calidad y se nota que han ahorrado en algunos componentes. No es un mal producto pero tampoco es excepcional. Creo que por el precio está bien.", "neutro"),
    ("El producto llegó en el tiempo estimado y venía bien empaquetado. En cuanto a su funcionamiento, hace lo básico que necesito aunque tiene algunas limitaciones. La batería no dura tanto como esperaba pero tampoco es terrible. Es un producto correcto para uso ocasional pero no lo recomendaría para uso profesional.", "neutro"),
    ("Tengo sentimientos encontrados con esta compra. El diseño exterior es bastante atractivo y moderno, pero el software tiene algunos bugs que pueden ser molestos. El servicio al cliente fue amable cuando los contacté aunque tardaron en responder. En general es un producto aceptable con margen de mejora.", "neutro"),
    ("Después de usar el producto durante varias semanas puedo decir que es bastante normal. Cumple con las funciones principales sin problemas aunque algunas características secundarias no funcionan tan bien. El manual de instrucciones es confuso pero eventualmente logré configurarlo. Es un producto estándar.", "neutro"),
    ("Este producto está bien para el precio que tiene. No esperaba algo premium y eso exactamente es lo que recibí. Funciona de manera correcta para tareas básicas. Los acabados son simples pero funcionales. Si buscas algo económico que cumpla sin pretensiones, puede servirte.", "neutro"),
    ("La experiencia de compra fue normal, ni buena ni mala. El producto hace lo que dice aunque no destaca en nada particular. He visto productos mejores pero también peores a este precio. Es una opción válida si no tienes expectativas muy altas y solo necesitas algo funcional.", "neutro"),
    
    # =========================================================================
    # POSITIVAS (20)
    # =========================================================================
    # Claras/directas (7)
    ("Excelente producto, superó todas mis expectativas, muy recomendado", "positivo"),
    ("Estoy muy contento con esta compra, funciona perfectamente y es de gran calidad", "positivo"),
    ("El mejor producto que he comprado en mucho tiempo, vale cada centavo", "positivo"),
    ("Increíble calidad por este precio, no puedo estar más satisfecho con la compra", "positivo"),
    ("Todo perfecto, llegó antes de tiempo y funciona de maravilla, muy feliz", "positivo"),
    ("Una compra excepcional, el producto es exactamente lo que buscaba y más", "positivo"),
    ("Muy satisfecho con el producto, la calidad es impresionante y el diseño hermoso", "positivo"),
    
    # Con matices (7)
    ("Aunque tardó unos días más en llegar, el producto es fantástico y vale la pena esperar", "positivo"),
    ("El empaque era simple pero el producto en sí es de excelente calidad y muy funcional", "positivo"),
    ("A pesar de mis dudas iniciales, el producto me sorprendió gratamente con su rendimiento", "positivo"),
    ("El precio me parecía alto al principio pero ahora veo que la calidad lo justifica completamente", "positivo"),
    ("Las instrucciones no eran muy claras pero el producto es genial una vez configurado", "positivo"),
    ("Tuve un pequeño problema con el envío pero el producto final es excelente sin duda", "positivo"),
    ("No esperaba mucho por el precio pero resultó ser un producto de muy buena calidad", "positivo"),
    
    # Largos descriptivos (6)
    ("Este producto ha superado completamente mis expectativas. Desde que llegó he estado usándolo diariamente y funciona de manera impecable. La calidad de los materiales es excelente, se nota que está bien fabricado. El diseño es moderno y elegante. Definitivamente recomiendo esta compra a cualquiera que esté buscando algo confiable.", "positivo"),
    ("Estoy encantado con esta compra. El producto llegó muy bien empaquetado y en perfectas condiciones. Las funciones son exactamente las que necesitaba y algunas características adicionales que no esperaba. La batería dura muchísimo y el rendimiento es excelente. Sin duda la mejor compra que he hecho este año.", "positivo"),
    ("Llevaba tiempo buscando un producto así y finalmente lo encontré. La calidad es premium, los acabados son perfectos y el funcionamiento es suave y preciso. El vendedor también fue muy atento respondiendo mis dudas antes de la compra. Una experiencia de compra completa y satisfactoria de principio a fin.", "positivo"),
    ("No suelo dejar reseñas pero este producto se lo merece. Es simplemente fantástico. Desde el momento que lo saqué de la caja supe que era de buena calidad. El diseño es precioso, los materiales son resistentes y todas las funciones trabajan perfectamente. Vale cada euro que pagué y más.", "positivo"),
    ("He probado varios productos similares de otras marcas y este es definitivamente el mejor. La diferencia de calidad es notable desde el primer momento. El funcionamiento es fluido, silencioso y eficiente. Mi familia también lo ha usado y todos coinciden en que es un producto excelente.", "positivo"),
    ("Compré este producto para regalar y fue un éxito total. La persona que lo recibió quedó encantada con la calidad y el diseño. Funciona de maravilla y tiene todas las características prometidas. Ya estoy pensando en comprar otro para mí porque realmente vale la pena la inversión.", "positivo"),
]

# Separar textos y etiquetas verdaderas
textos_ctrl = [texto for texto, _ in RESENAS_CONTROLADAS]
y_true = [label for _, label in RESENAS_CONTROLADAS]

# Inicializar predictor
print("Cargando modelo de producción...")
predictor = SentimentPredictor()

# Realizar predicciones
print(f"Prediciendo {len(textos_ctrl)} reseñas controladas...\n")
predicciones = []

for texto in textos_ctrl:
    result = predictor.predict(texto)
    # Convertir de formato API (capitalizado) a formato interno (minúsculas)
    pred_lower = result.prediction.lower()
    predicciones.append(pred_lower)

# Calcular métricas
accuracy = accuracy_score(y_true, predicciones)
f1_macro = f1_score(y_true, predicciones, labels=['negativo', 'neutro', 'positivo'], average='macro')
f1_por_clase = f1_score(y_true, predicciones, labels=['negativo', 'neutro', 'positivo'], average=None)

# Matriz de confusión
cm = confusion_matrix(y_true, predicciones, labels=['negativo', 'neutro', 'positivo'])

# Aciertos por clase
aciertos_neg = cm[0, 0]
aciertos_neu = cm[1, 1]
aciertos_pos = cm[2, 2]

# Mostrar resultados
print("=" * 76)
print("📊 RESULTADOS DEL MODELO EN PRODUCCIÓN")
print("=" * 76)
print(f"   Accuracy:  {accuracy:.2%}")
print(f"   F1-Macro:  {f1_macro:.4f}")
print(f"   F1-Neg:    {f1_por_clase[0]:.4f}")
print(f"   F1-Neu:    {f1_por_clase[1]:.4f}")
print(f"   F1-Pos:    {f1_por_clase[2]:.4f}")
print(f"   Aciertos:  Neg={aciertos_neg}/20  Neu={aciertos_neu}/20  Pos={aciertos_pos}/20")
print()

print("=" * 76)
print("🎯 MÉTRICAS ESPERADAS (E0 - LogReg Baseline)")
print("=" * 76)
print("   Accuracy:  83.33%")
print("   F1-Macro:  0.8318")
print("   F1-Neg:    0.9268")
print("   F1-Neu:    0.7778")
print("   F1-Pos:    0.7907")
print("   Aciertos:  Neg=19/20  Neu=14/20  Pos=17/20")
print()

# Comparación
print("=" * 76)
print("📈 COMPARACIÓN (Producción vs. Esperado)")
print("=" * 76)
diff_acc = (accuracy - 0.8333) * 100
diff_f1_macro = f1_macro - 0.8318
diff_f1_neg = f1_por_clase[0] - 0.9268
diff_f1_neu = f1_por_clase[1] - 0.7778
diff_f1_pos = f1_por_clase[2] - 0.7907

print(f"   Accuracy:   {diff_acc:+.2f} puntos porcentuales")
print(f"   F1-Macro:   {diff_f1_macro:+.4f}")
print(f"   F1-Neg:     {diff_f1_neg:+.4f}")
print(f"   F1-Neu:     {diff_f1_neu:+.4f}")
print(f"   F1-Pos:     {diff_f1_pos:+.4f}")
print()

# Matriz de confusión
print("=" * 76)
print("📋 MATRIZ DE CONFUSIÓN")
print("=" * 76)
print("             Pred: Neg  Neu  Pos")
print(f"Real: Neg       {cm[0, 0]:3d}   {cm[0, 1]:3d}   {cm[0, 2]:3d}")
print(f"      Neu       {cm[1, 0]:3d}   {cm[1, 1]:3d}   {cm[1, 2]:3d}")
print(f"      Pos       {cm[2, 0]:3d}   {cm[2, 1]:3d}   {cm[2, 2]:3d}")
print()

# Reporte detallado
print("=" * 76)
print("📝 REPORTE DE CLASIFICACIÓN DETALLADO")
print("=" * 76)
print(classification_report(y_true, predicciones, labels=['negativo', 'neutro', 'positivo'], 
                          target_names=['Negativo', 'Neutro', 'Positivo'], digits=4))

# Verificación de cumplimiento
print("=" * 76)
print("✅ VERIFICACIÓN DE CUMPLIMIENTO")
print("=" * 76)

cumple_acc = accuracy >= 0.8333
cumple_f1_macro = f1_macro >= 0.8318
cumple_f1_neg = f1_por_clase[0] >= 0.9268
cumple_f1_neu = f1_por_clase[1] >= 0.7778
cumple_f1_pos = f1_por_clase[2] >= 0.7907

print(f"   Accuracy ≥ 83.33%:  {'✓ SÍ' if cumple_acc else '✗ NO'}")
print(f"   F1-Macro ≥ 0.8318:  {'✓ SÍ' if cumple_f1_macro else '✗ NO'}")
print(f"   F1-Neg ≥ 0.9268:    {'✓ SÍ' if cumple_f1_neg else '✗ NO'}")
print(f"   F1-Neu ≥ 0.7778:    {'✓ SÍ' if cumple_f1_neu else '✗ NO'}")
print(f"   F1-Pos ≥ 0.7907:    {'✓ SÍ' if cumple_f1_pos else '✗ NO'}")
print()

cumple_todo = all([cumple_acc, cumple_f1_macro, cumple_f1_neg, cumple_f1_neu, cumple_f1_pos])
if cumple_todo:
    print("🎉 EL MODELO EN PRODUCCIÓN CUMPLE TODAS LAS MÉTRICAS ESPERADAS 🎉")
else:
    print("⚠️  El modelo NO cumple todas las métricas esperadas")

print("=" * 76)
