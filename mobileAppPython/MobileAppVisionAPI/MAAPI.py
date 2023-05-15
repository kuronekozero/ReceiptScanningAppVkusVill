import os, io
import json
from google.cloud import vision
from google.cloud.vision_v1 import types
import pandas as pd
from product_matching import match_products  # Импортируем функцию из другого файла

os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = r'C:\Users\Kuroneko\Desktop\diploma\project\mobileAppPython\MobileAppVisionAPI\ServiceAccountToken.json'

client = vision.ImageAnnotatorClient()

def process_image(image_path: str, ingredients: list) -> dict:
    with io.open(image_path, 'rb') as image_file:
        content = image_file.read()

    image = vision.Image(content=content)
    response = client.text_detection(image=image)
    texts = response.text_annotations

    # Обрабатываем распознанный текст
    recognized_text = '\n'.join(text.description for text in texts)

    # Используем функцию `match_products` для получения отфильтрованных данных
    matched_products_df = match_products(recognized_text, 'C:/Users/Kuroneko/Desktop/diploma/project/mobileAppPython/MobileAppVisionAPI/vkusvillnew.xlsx', 'C:/Users/Kuroneko/Desktop/diploma/project/mobileAppPython/MobileAppVisionAPI/productsList.xlsx')

    def check_allergens(row):
        product_ingredients = row['Состав'].split(', ')
        for ingredient in ingredients:
            if ingredient in product_ingredients:
                return 'Да'
        return 'Нет'

    matched_products_df['Аллерген'] = matched_products_df.apply(check_allergens, axis=1)

    # Сохраняем отфильтрованные данные в файл json
    json_data = matched_products_df.to_dict(orient='records')
    with open('output.json', 'w', encoding='utf-8') as f:
        json.dump(json_data, f, ensure_ascii=False)

    # Возвращаем отфильтрованные данные в виде словаря
    return json_data