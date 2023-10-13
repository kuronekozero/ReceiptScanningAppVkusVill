<h1 align="center">ReceiptScanningApp</h1>

This project is a mobile application for Android platform that allows people with allergies to quickly determine which products they can consume and which ones they should avoid. The application analyzes a photo of a grocery receipt from the VkusVill supermarket chain and identifies any potentially harmful ingredients in the purchased products.

This project is my diploma project, which was completed in 2023 at ItHub College in Moscow.

## Key Features

- Scan grocery receipts from VkusVill supermarkets
- Analyze the receipt image and detect harmful ingredients
- Provide information about products that are safe to consume
- Notify users about potential allergens in the products

## Technologies and Tools

- Programming Language: Kotlin, Python
- Flask framework for the server-side development
- Integration with Google Cloud Vision API for image analysis
- Android Studio for mobile app development
- PyCharm and Pandas for data processing

## Image Processing

After the user takes a photo of the grocery receipt using the app's camera, the following image processing steps take place:

1. The photo is sent to the application server.
2. The server sends the photo to the Google Cloud Vision API.
3. The Google Cloud Vision API processes the photo and returns the recognized text.
4. The recognized text is sent back to the application server.
5. The server uses the Pandas library to process the recognized text and match it with products in the database.
6. The information about products containing harmful ingredients is sent back to the mobile app.
7. The app displays information about products that are safe to consume and provides notifications about potential allergens.

This image processing workflow enables users to quickly and conveniently determine which products they can consume and which ones they should avoid.

## Installation and Setup

1. Clone the repository to your computer.
2. Open the project in Android Studio.
3. Build and install the app on your device or emulator.
4. Launch the app and follow the on-screen instructions.

## License

This project is licensed under the MIT License.
