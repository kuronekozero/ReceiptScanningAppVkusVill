import os
from flask import Flask, request
import json
from PIL import Image
import sys
sys.path.insert(0, 'C:/Users/Kuroneko/Desktop/diploma/project/mobileAppPython/MobileAppVisionAPI')
from MAAPI import process_image  # Импортируем функцию из файла MAAPI.py

app = Flask(__name__)

# Путь к папке для сохранения изображений на сервере
UPLOAD_FOLDER = 'C:/Users/Kuroneko/Desktop/diploma/project/ServerFlask/images'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

@app.route('/upload', methods=['POST'])
def upload_image():
    image = request.files.get('image')
    ingredients_json = request.form.get('ingredients')
    if image and ingredients_json:
        # Сохранение изображения в папке uploads на сервере
        filename = image.filename
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        image.save(filepath)

        # Преобразование JSON-строки в список ингредиентов
        ingredients = json.loads(ingredients_json)

        # Открытие изображения с помощью библиотеки Pillow
        img = Image.open(filepath)


        # Поворот изображения на 90 градусов влево с помощью метода transpose
        img = img.transpose(Image.ROTATE_270)

        # Сохранение повернутого изображения
        img.save(filepath)

        # Вызываем функцию `process_image` из файла MAAPI.py и передаем ей путь к изображению и список ингредиентов
        json_data = process_image(filepath, ingredients)

        # Отправляем данные в формате json обратно на мобильное приложение
        return json_data
    else:
        return "No image found in request", 400

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)