package com.example.receiptscanningapp

import okhttp3.*
import java.io.BufferedReader
import java.io.InputStreamReader
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import java.lang.Class
import com.google.gson.Gson
import com.google.gson.JsonObject
import androidx.core.content.FileProvider
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val selectedIngredients = ArrayList<String>()
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private val cameraRequest = 1888
    lateinit var imageView: ImageView
    lateinit var photoFile: File
    private var deletePhotos = false

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpeg",
            storageDir
        )
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectedIngredientsAdapter = SelectedIngredientsAdapter(this, selectedIngredients)
        val selectedIngredientsListView = findViewById<ListView>(R.id.selectedIngredientsListView)
        selectedIngredientsListView.adapter = selectedIngredientsAdapter

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        title = "KotlinApp"
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraRequest)
        imageView = findViewById(R.id.imageView)
        val photoButton: Button = findViewById(R.id.button)
        photoButton.setOnClickListener {
            photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(this,
                "com.example.receiptscanningapp.fileprovider",
                photoFile)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(cameraIntent, cameraRequest)
        }

        // Перемещенный код установки обработчика событий для чекбокса
        val deletePhotosCheckBox: CheckBox = findViewById(R.id.deletePhotosCheckBox)
        deletePhotosCheckBox.setOnCheckedChangeListener { _, isChecked ->
            deletePhotos = isChecked
        }

        // Чтение CSV-файла
        val inputStream = assets.open("ingredientsList.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val ingredients = ArrayList<String>()
        var line = reader.readLine()
        while (line != null) {
            ingredients.add(line)
            line = reader.readLine()
        }

        Log.d("MainActivity", "Ingredients: $ingredients")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ingredients)
        autoCompleteTextView.setAdapter(adapter)

        // Сохранение выбранных пользователем ингредиентов
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedIngredient = adapter.getItem(position) as String
            selectedIngredients.add(selectedIngredient)
            selectedIngredientsAdapter.notifyDataSetChanged()

            // Сохранение списка в SharedPreferences
            val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putStringSet("selectedIngredients", HashSet(selectedIngredients))
            editor.apply()
        }

        // Загрузка списка из SharedPreferences
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val savedIngredients = sharedPreferences.getStringSet("selectedIngredients", null)
        if (savedIngredients != null) {
            selectedIngredients.addAll(savedIngredients)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequest && resultCode == Activity.RESULT_OK) {
            val photoURI = FileProvider.getUriForFile(this,
                "com.example.receiptscanningapp.fileprovider",
                photoFile)
            val inputStream = contentResolver.openInputStream(photoURI)
            if (inputStream != null) {
                val photo = BitmapFactory.decodeStream(inputStream)
                if (photo != null) {
                    // Отправка изображения на сервер
                    val client = OkHttpClient()
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "image", "image.jpg",
                            RequestBody.create(
                                "image/jpeg".toMediaTypeOrNull(),
                                bitmapToByteArray(photo)
                            )
                        )
                        .addFormDataPart("ingredients", Gson().toJson(selectedIngredients))
                        .build()
                    val request = Request.Builder()
                        .url("") // paste your server adress in here
                        .post(requestBody)
                        .build()

                    val photoFile = this@MainActivity.photoFile
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            // Обработка ошибки
                            Log.e("MainActivity", "Error sending image to server", e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            // Получение строки с данными от сервера
                            val responseData = response.body?.string()

                            // Запуск ProductsActivity с передачей JSON-строки
                            val intent = Intent()
                            intent.setClassName(this@MainActivity, "com.example.receiptscanningapp.ProductsActivity")
                            val productsType = object : TypeToken<List<Product>>() {}.type
                            val products = Gson().fromJson<List<Product>>(responseData, productsType)
                            intent.putParcelableArrayListExtra("products", ArrayList(products))
                            startActivity(intent)

                            // Преобразование строки в массив JSON с помощью класса JSONArray
                            val jsonArray = JSONArray(responseData)

                            // Обработка каждого объекта JSON в массиве
                            for (i in 0..jsonArray.length() - 1) {
                                val json = jsonArray.getJSONObject(i)

                                // Проверка наличия ключа "someKey" в объекте JSON
                                if (json.has("someKey")) {
                                    // Ключ "someKey" присутствует в объекте JSON
                                    val someData = json.getString("someKey")
                                    //Log.i("MainActivity", "Received data from server: $someData")
                                    // Обработка данных
                                } else {
                                    // Ключ "someKey" отсутствует в объекте JSON
                                    Log.e("MainActivity", "Failed to receive data from server")
                                    // Обработка ошибки
                                }
                            }

                            // Обработка ответа
                            //Log.i("MainActivity", "Response from server: $responseData")
                            Log.i("MainActivity", "Delete photos: $deletePhotos")
                            if (deletePhotos) {
                                val deleted = photoFile.delete()
                                if (deleted) {
                                    Log.i("MainActivity", "Photo file deleted successfully")
                                } else {
                                    Log.e("MainActivity", "Failed to delete photo file")
                                    Log.e("MainActivity", "Photo file path: ${photoFile.absolutePath}")
                                    Log.e("MainActivity", "Photo file exists: ${photoFile.exists()}")
                                }
                            }
                        }
                    })
                } else {
                    Log.e("MainActivity", "Failed to decode image")
                }
            } else {
                Log.e("MainActivity", "Failed to open input stream for photoURI")
            }
        }
    }
}

class SelectedIngredientsAdapter(context: Context, private val selectedIngredients: MutableList<String>) :
    ArrayAdapter<String>(context, R.layout.selected_ingredient_item, selectedIngredients) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.selected_ingredient_item, parent, false)

        val ingredientNameTextView = view.findViewById<TextView>(R.id.ingredientNameTextView)
        val deleteIngredientButton = view.findViewById<ImageButton>(R.id.deleteIngredientButton)

        val ingredient = selectedIngredients[position]
        ingredientNameTextView.text = ingredient

        deleteIngredientButton.setOnClickListener {
            selectedIngredients.removeAt(position)
            notifyDataSetChanged()
        }

        return view
    }
}
