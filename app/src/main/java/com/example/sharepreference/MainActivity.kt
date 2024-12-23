package com.example.sharepreference

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView // Library for circular ImageView
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextAge: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var imageView: CircleImageView
    private lateinit var sharedPreferences: SharedPreferences

    private var photoUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageView.setImageURI(it)
                imageView.visibility = ImageView.VISIBLE
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
            if (isSuccess) {
                imageView.setImageURI(photoUri)
                imageView.visibility = ImageView.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        editTextName = findViewById(R.id.editTextName)
        editTextAge = findViewById(R.id.editTextAge)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        imageView = findViewById(R.id.imageView) // Ensure this is a CircleImageView in your XML layout

        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonUpdate: Button = findViewById(R.id.buttonUpdate)
        val buttonDelete: Button = findViewById(R.id.buttonDelete)
        val buttonView: Button = findViewById(R.id.buttonView)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Set click listener for circular ImageView
        imageView.setOnClickListener { showImageOptionDialog() }

        buttonSave.setOnClickListener { if (validateInputs()) saveOrUpdateData() }
        buttonUpdate.setOnClickListener { if (validateInputs()) saveOrUpdateData() }
        buttonDelete.setOnClickListener { deleteData() }
        buttonView.setOnClickListener { viewData() }

        // Load existing data if available
        loadData()
    }

    private fun showImageOptionDialog() {
        val options = arrayOf("Take Photo", "Pick from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> launchCamera()
                1 -> pickImageFromGallery()
            }
        }
        builder.show()
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, android.content.ContentValues())
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        photoUri?.let { takePhotoLauncher.launch(it) }
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun validateInputs(): Boolean {
        val name = editTextName.text.toString().trim()
        val age = editTextAge.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()

        if (name.isEmpty()) {
            editTextName.error = "Name is required"
            return false
        }
        if (age.isEmpty()) {
            editTextAge.error = "Age is required"
            return false
        }
        if (email.isEmpty()) {
            editTextEmail.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Enter a valid email"
            return false
        }
        if (phone.isEmpty()) {
            editTextPhone.error = "Phone is required"
            return false
        }
        if (!phone.matches(Regex("^[+]?[0-9]{10,13}\$"))) {
            editTextPhone.error = "Enter a valid phone number"
            return false
        }
        if (imageView.drawable == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveOrUpdateData() {
        val name = editTextName.text.toString()
        val age = editTextAge.text.toString()
        val email = editTextEmail.text.toString()
        val phone = editTextPhone.text.toString()
        val imageBase64 = getImageBase64()

        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("age", age)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.putString("image", imageBase64)
        editor.apply()

        Toast.makeText(this, "Data saved/updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun deleteData() {
        // Show a confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Confirmation")
        builder.setMessage("Are you sure you want to delete all data? This action cannot be undone.")

        builder.setPositiveButton("Yes") { _, _ ->
            // User confirmed deletion
            val editor = sharedPreferences.edit()
            editor.clear() // Clears all data
            editor.apply()

            editTextName.text.clear()
            editTextAge.text.clear()
            editTextEmail.text.clear()
            editTextPhone.text.clear()
            imageView.setImageDrawable(null)

            Toast.makeText(this, "Data deleted successfully", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            // User canceled deletion
            dialog.dismiss()
        }

        // Display the dialog
        builder.create().show()
    }


    private fun loadData() {
        val name = sharedPreferences.getString("name", "")
        val age = sharedPreferences.getString("age", "")
        val email = sharedPreferences.getString("email", "")
        val phone = sharedPreferences.getString("phone", "")
        val imageBase64 = sharedPreferences.getString("image", "")

        editTextName.setText(name)
        editTextAge.setText(age)
        editTextEmail.setText(email)
        editTextPhone.setText(phone)

        if (!imageBase64.isNullOrEmpty()) {
            val decodedString = Base64.decode(imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = ImageView.VISIBLE
        }
    }

    private fun getImageBase64(): String {
        return imageView.drawable?.let {
            val bitmap = (it as android.graphics.drawable.BitmapDrawable).bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        } ?: ""
    }

    private fun viewData() {
        val intent = Intent(this, DisplayActivity::class.java)
        startActivity(intent)
    }
}
