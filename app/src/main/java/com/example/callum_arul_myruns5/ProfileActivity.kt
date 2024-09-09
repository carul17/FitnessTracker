package com.example.callum_arul_myruns5

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var imageViewProfilePic: ImageView
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var editTextClass: EditText
    private lateinit var editTextMajor: EditText

    private lateinit var tempImgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var myViewModel: MyViewModel
    private var imageBitmap: Bitmap? = null
    private val tempImgFileName = "profile_temp_img.jpg"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        imageViewProfilePic = findViewById(R.id.imageViewProfilePic)
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        editTextClass = findViewById(R.id.editTextClass)
        editTextMajor = findViewById(R.id.editTextMajor)

        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonCancel: Button = findViewById(R.id.buttonCancel)
        val buttonChangePhoto: Button = findViewById(R.id.buttonChangePhoto)

        val tempImgFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), tempImgFileName) //XD: try Environment.DIRECTORY_PICTURES instead of "null"
        tempImgUri = FileProvider.getUriForFile(this,
            "com.example.callum_arul_myruns5", tempImgFile)

        buttonChangePhoto.setOnClickListener(){
            selectPhoto()

        }
        imageOperations(tempImgFile)

        buttonSave.setOnClickListener { saveProfile() }
        buttonCancel.setOnClickListener {
            imageBitmap = null
            finish()
        }

        loadProfile()
    }

    private fun selectPhoto() {
        val options = arrayOf("Open Camera", "Select from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Profile Picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> selectFromGallery()
            }
        }
        builder.show()
    }

    private fun selectFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)

    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    private fun imageOperations(tempImgFile: File) {
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap = Util.getBitmap(this, tempImgUri)
                myViewModel.userImage.value = bitmap
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    val bitmap = Util.getBitmap(this, selectedImageUri)
                    myViewModel.userImage.value = bitmap
                }
            }
        }


        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.userImage.observe(this) { it ->
            imageViewProfilePic.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            imageViewProfilePic.setImageBitmap(it)
            imageBitmap = it
        }

    }

    private fun loadProfile() {
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val phoneNumber = sharedPreferences.getString("phoneNumber", "")
        val gender = sharedPreferences.getInt("gender", -1)
        val classString = sharedPreferences.getString("class", "")
        val major = sharedPreferences.getString("major", "")
        val imageBytes = sharedPreferences.getString("imageBytes", "")
        editTextName.setText(name)
        editTextEmail.setText(email)
        editTextPhoneNumber.setText(phoneNumber)
        editTextClass.setText(classString)
        editTextMajor.setText(major)

        if(gender != -1) {
            radioGroupGender.check(gender)
        }
        if (imageBytes != null) {
            if (imageBytes.isNotEmpty()) {
                val decodedImageBytes = android.util.Base64.decode(imageBytes, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedImageBytes, 0, decodedImageBytes.size)
                imageViewProfilePic.setImageBitmap(bitmap)
            }
        }
    }

    private fun saveProfile(){
        val name = editTextName.text.toString()
        val email = editTextEmail.text.toString()
        val phoneNumber = editTextPhoneNumber.text.toString()
        val gender = radioGroupGender.checkedRadioButtonId
        val classString = editTextClass.text.toString()
        val major = editTextMajor.text.toString()

        val imageBytes = if (imageBitmap != null) {
            //convert to string for storage in sharedPreferences
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            android.util.Base64.encodeToString(byteArrayOutputStream.toByteArray(), android.util.Base64.DEFAULT)
        } else if (imageViewProfilePic.drawable != null) {
            //in case the save button is clicked without taking a picture, imageBitmap will be null so we have to set it to the current image in imageViewProfilePic
            val drawable = imageViewProfilePic.drawable
            val bitmap = (drawable as BitmapDrawable).bitmap
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            android.util.Base64.encodeToString(byteArrayOutputStream.toByteArray(), android.util.Base64.DEFAULT)
        } else {
            ""
        }



        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("phoneNumber", phoneNumber)
        editor.putInt("gender", gender)
        editor.putString("class", classString)
        editor.putString("major", major)
        editor.putString("imageBytes", imageBytes)
        editor.apply()

        Toast.makeText(this, "Your Profile has been saved.", Toast.LENGTH_SHORT).show()
    }


}