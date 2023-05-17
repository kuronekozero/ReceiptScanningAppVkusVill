package com.example.receiptscanningapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.annotations.SerializedName
import java.util.*
import java.util.Collections.emptyList
import java.util.function.Predicate

class ProductsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        val products = intent.getParcelableArrayListExtra<Product>("products") ?: emptyList()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Загрузка списка выбранных пользователем ингредиентов из SharedPreferences
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val savedIngredients = sharedPreferences.getStringSet("selectedIngredients", null)
        val selectedIngredients = if (savedIngredients != null) ArrayList(savedIngredients) else ArrayList<String>()

        recyclerView.adapter = ProductsAdapter(products, selectedIngredients)
    }
}

data class Product(
    @SerializedName("Аллерген")
    val allergen: String,
    @SerializedName("Название")
    val name: String,
    @SerializedName("Белки")
    val proteins: String,
    @SerializedName("Жиры")
    val fats: String,
    @SerializedName("Углеводы")
    val carbohydrates: String,
    @SerializedName("Энергетическая ценность")
    val energyValue: String,
    @SerializedName("Состав")
    val composition: String

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(allergen)
        parcel.writeString(name)
        parcel.writeString(proteins)
        parcel.writeString(fats)
        parcel.writeString(carbohydrates)
        parcel.writeString(energyValue)
        parcel.writeString(composition)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}

class ProductsAdapter(
    private val products: List<Product>,
    private val selectedIngredients: List<String>
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val compositionTextView: TextView = view.findViewById(R.id.compositionTextView)
        val proteinsTextView: TextView = view.findViewById(R.id.proteinsTextView)
        val fatsTextView: TextView = view.findViewById(R.id.fatsTextView)
        val carbohydratesTextView: TextView = view.findViewById(R.id.carbohydratesTextView)
        val energyValueTextView: TextView = view.findViewById(R.id.energyValueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.nameTextView.text = product.name
        holder.compositionTextView.text = product.composition
        holder.proteinsTextView.text = "Белки: ${product.proteins}"
        holder.fatsTextView.text = "Жиры: ${product.fats}"
        holder.carbohydratesTextView.text = "Углеводы: ${product.carbohydrates}"

        if (product.allergen == "Да") {
            Log.d("ProductsAdapter", "Changing background color for product: ${product.name}")
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        } else {
            Log.d("ProductsAdapter", "Product allergen: ${product.allergen}")
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }
}