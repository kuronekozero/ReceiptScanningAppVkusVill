import pandas as pd
from fuzzywuzzy import fuzz


def match_products(recognized_text: str, excel_file_path: str, first_words_file_path: str) -> pd.DataFrame:
    # Загружаем данные из файла Excel в DataFrame
    products_df = pd.read_excel(excel_file_path)

    # Заменяем пустые строки на текст "no data"
    products_df = products_df.fillna("no data")

    first_words_df = pd.read_excel(first_words_file_path)

    # Создаем список стоп-слов
    stop_words = ['ТОВАР', 'НАЛИЧНЫМИ:', 'ПОЛУЧЕНО:', 'ИТОГ:', 'ЗА ПОКУПКАМИ', 'КАССОВЫЙ ЧЕК']

    # Обрабатываем распознанный текст и находим в нем названия продуктов
    stop_phrase = 'ПОЛНЫЙ РАСЧЕТ'
    if stop_phrase in recognized_text:
        recognized_text = recognized_text[:recognized_text.index(stop_phrase)]

    # Выводим обрезанный текст в консоль
    # print(recognized_text)

    matched_products = []
    for line in recognized_text.split('\n'):
        line = line.lower()
        # Пропускаем строки, которые содержат стоп-слова
        if any(stop_word.lower() in line for stop_word in stop_words):
            continue

        words = line.split()
        if not words:
            continue

        first_word = words[0]
        if first_word in first_words_df['Название'].str.lower().values:
            for product_name in products_df['Название']:
                if product_name.lower().startswith(first_word):
                    similarity = fuzz.ratio(product_name.lower(), line)
                    for threshold in [70, 50, 30]:
                        if similarity > threshold:
                            matched_products.append(product_name)
                            break
                    else:
                        continue
                    break

    # Фильтруем строки DataFrame, чтобы оставить только те, которые содержат названия продуктов из `matched_products`
    matched_products_df = products_df[products_df['Название'].isin(matched_products)]
    return matched_products_df