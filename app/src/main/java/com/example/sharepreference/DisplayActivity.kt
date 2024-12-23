package com.example.sharepreference



import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisplayActivity : AppCompatActivity() {

    private lateinit var textViewName: TextView
    private lateinit var textViewAge: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewPhone: TextView
    private lateinit var imageViewDisplay: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        textViewName = findViewById(R.id.textViewName)
        textViewAge = findViewById(R.id.textViewAge)
        textViewEmail = findViewById(R.id.textViewEmail)
        textViewPhone = findViewById(R.id.textViewPhone)
        imageViewDisplay = findViewById(R.id.imageViewDisplay)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        displayData()
    }

    @SuppressLint("SetTextI18n")
    private fun displayData() {
        val name = sharedPreferences.getString("name", "N/A")
        val age = sharedPreferences.getString("age", "N/A")
        val email = sharedPreferences.getString("email", "N/A")
        val phone = sharedPreferences.getString("phone", "N/A")
        val imageBase64 = sharedPreferences.getString("image", "")

        textViewName.text = "Name: $name"
        textViewAge.text = "Age: $age"
        textViewEmail.text = "Email: $email"
        textViewPhone.text = "Phone: $phone"

        if (imageBase64 != null && imageBase64.isNotEmpty()) {
            val decodedString = Base64.decode(imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageViewDisplay.setImageBitmap(bitmap)
            imageViewDisplay.visibility = ImageView.VISIBLE
        }

        var button = findViewById<Button>(R.id.editbtn)
        button.setOnClickListener {
            val intent1 = Intent(this, MainActivity::class.java)
            startActivity(intent1)
        }

    }
}
