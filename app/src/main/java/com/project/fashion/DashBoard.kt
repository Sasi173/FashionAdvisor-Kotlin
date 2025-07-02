package com.project.fashion

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.fashion.chat.DataResponse
import com.project.fashion.chat.GeminiAdapter
import com.project.fashion.databinding.ActivityDashBoardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class DashBoard : AppCompatActivity() {
    private val bind by lazy {
        ActivityDashBoardBinding.inflate(layoutInflater)
    }
    private val responseData = arrayListOf<DataResponse>()
    private lateinit var adapter: GeminiAdapter
    private var selectedImageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                handleImageUpload(it)
            }
        }

    // Permission launcher
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                imagePickerLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        with(bind) {
            setupRecyclerView()
            get().let {
                wishPoint.text = spanned("Hi ${it.username} 😊 !!")
            }
            askButton.setOnClickListener {
                handleUserQuery()
            }
            // Add image upload button listener
            uploadImageButton.setOnClickListener {
                checkPermissionAndPickImage()
            }
            clickPoint.setOnClickListener {
                MaterialAlertDialogBuilder(this@DashBoard).apply {
                    setTitle("Do you want to Logout ??")
                    setPositiveButton("Yes") { v, _ ->
                        v.dismiss()
                        getSharedPreferences("user", MODE_PRIVATE).edit { clear() }
                        finish()
                        startActivity(Intent(applicationContext, SplashScreen::class.java))
                    }
                    setNegativeButton("No") { v, _ ->
                        v.dismiss()
                    }
                    show()
                }
            }
        }
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            imagePickerLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun setupRecyclerView() {
        adapter = GeminiAdapter(this, responseData)
        bind.recyclerViewId.apply {
            layoutManager = LinearLayoutManager(this@DashBoard, LinearLayoutManager.VERTICAL, false)
            adapter = this@DashBoard.adapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State,
                ) {
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.top = 50.dpToPx()
                    }
                    outRect.bottom = 50.dpToPx()
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleUserQuery() {
        with(bind) {
            val userQuery = askEditText.text.toString()
            if (userQuery.isBlank()) {
                Toast.makeText(
                    applicationContext,
                    "Kindly provide any input to SuperBot",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            askEditText.setText("")
            responseData.add(DataResponse(0, userQuery, "imageTag"))
            adapter.notifyDataSetChanged()
            processQuery(userQuery, null)
        }
    }


    private fun handleImageUpload(imageUri: Uri) {
        with(bind) {
            responseData.add(DataResponse(0, "Image uploaded", imageUri.toString()))
            adapter.notifyDataSetChanged()
            processQuery("Analyze this outfit and suggest one upgrade.", imageUri)
        }
    }

    private fun processQuery(query: String, imageUri: Uri?) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = getString(R.string.apikey2)
        )

        val chasePrompt = """
        **Role**: You are a global fashion assistant. When users upload a photo, analyze the outfit and suggest 1 upgrade (e.g., "Add a belt!").  
        **Current Trends (2024)**:  
        - "Silk slip dress with metallic heels (Night out) | Zara has affordable versions."  
        - "Oversized blazer + biker shorts (Streetwear) | Try thrifting for vintage blazers."  
        **Rules**:  
        - Keep responses under 100 words.  
        - Use emojis (e.g., 👗, 👠).  
        - If no image, respond based on text query.  
        """.trimIndent()

        val completePrompt = "$chasePrompt\nUser Query: $query"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputContent = content {
                    text(completePrompt)
                    imageUri?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val byteArray = stream.toByteArray()
                        image(bitmap)
                    }
                }

                val response = generativeModel.generateContent(inputContent)
                withContext(Dispatchers.Main) {
                    responseData.add(
                        DataResponse(
                            1,
                            response.text ?: "I'm here to assist! 👗",
                            ""
                        )
                    )
                    bind.recyclerViewId.smoothScrollToPosition(responseData.size - 1)
                    bind.recyclerViewId.setItemViewCacheSize(responseData.size)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error generating content: ${e.localizedMessage}", e)
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to generate response. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}