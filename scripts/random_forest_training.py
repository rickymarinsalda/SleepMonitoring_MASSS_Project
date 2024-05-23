import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score
import joblib
from imblearn.over_sampling import SMOTE
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

ids = [1360686, 1449548, 1455390, 1818471, 2598705, 2638030, 3509524, 3997827, 4018081, 4314139, 4426783, 46343,
       5132496, 5383425, 5498603, 5797046, 6220552, 759667, 7749105, 781756, 8000685, 8173033, 8258170, 844359,
       8530312, 8686948, 8692923, 9106476, 9618981, 9961348]

def load_and_merge_data(subject_id):
    data_h = pd.read_csv(f"data/heart_rate/{subject_id}_heartrate.txt", names=["timestamp", "bpm"])
    data_a = pd.read_csv(f"data/motion/{subject_id}_acceleration.txt", names=["date", "x", "y", "z"], sep=' ')
    data_l = pd.read_csv(f"data/labels/{subject_id}_labeled_sleep.txt", names=["timestamp", "class"], sep=' ')

    data_h['timestamp'] = pd.to_numeric(data_h['timestamp'], errors='coerce')
    data_a['date'] = pd.to_numeric(data_a['date'], errors='coerce')
    data_a['x'] = pd.to_numeric(data_a['x'], errors='coerce')
    data_a['y'] = pd.to_numeric(data_a['y'], errors='coerce')
    data_a['z'] = pd.to_numeric(data_a['z'], errors='coerce')
    data_l['timestamp'] = pd.to_numeric(data_l['timestamp'], errors='coerce')

    data_h = data_h.dropna()
    data_a = data_a.dropna()
    data_l = data_l.dropna()

    data_h['timestamp_approx'] = data_h['timestamp'].round().astype('int64')
    data_a['timestamp_approx'] = data_a['date'].round().astype('int64')

    merged_data = pd.merge(data_h, data_a, left_on='timestamp_approx', right_on='timestamp_approx')

    data_l['timestamp_approx'] = data_l['timestamp'].round().astype('int64')
    merged_data = pd.merge(merged_data, data_l, left_on='timestamp_approx', right_on='timestamp_approx')

    return merged_data.dropna()

all_data = pd.DataFrame()
for subject_id in ids:
    subject_data = load_and_merge_data(subject_id)
    all_data = pd.concat([all_data, subject_data], ignore_index=True)

X = all_data[['bpm', 'x', 'y', 'z']]
y = all_data['class']

sm = SMOTE(random_state=42)
X_res, y_res = sm.fit_resample(X, y)

X_train, X_test, y_train, y_test = train_test_split(X_res, y_res, test_size=0.3, random_state=42)

model = RandomForestClassifier(n_estimators=80, max_depth=10, random_state=42)

model.fit(X_train, y_train)

y_pred = model.predict(X_test)
print("Valutazione sul set di test")
print(classification_report(y_test, y_pred))

# Convert the trained model to ONNX format
initial_type = [('float_input', FloatTensorType([None, X_train.shape[1]]))]
onnx_model = convert_sklearn(model, initial_types=initial_type)

# Save the ONNX model to a binary file
with open("random_forest_sleep_classifier_80t.onnx", "wb") as f:
    f.write(onnx_model.SerializeToString())