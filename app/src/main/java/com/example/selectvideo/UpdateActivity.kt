package com.example.selectvideo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage



interface VideoUpdateCallback {
    fun onVideoUpdated()
}

class UpdateActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var db: FirebaseFirestore
    private var intentId: String? = null
    private var videoUpdateCallback: VideoUpdateCallback? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        videoView = findViewById(R.id.update_videoView)
        db = FirebaseFirestore.getInstance()
        val intentvdo = intent.getStringExtra("videoUrl")
        intentId = intent.getStringExtra("videoId")

        // Set the video URI here if available (from the intent)
        val videoUrl = Uri.parse(intentvdo)
        videoView.setVideoURI(videoUrl)

        // Initialize the callback
        videoUpdateCallback = object : VideoUpdateCallback {
            override fun onVideoUpdated() {
                // This method will be called when the video is updated
                // You can add the logic here to handle the update
            }
        }

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        val updateBtn: Button = findViewById(R.id.updateBtn)

        val chooseVideoBtn: Button = findViewById(R.id.update_chooseBtn)
        chooseVideoBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "video/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Pick Video"), 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val videoUrl = data?.data
            if (videoUrl != null) {
                // Set the video URI after picking from the gallery
                videoView.setVideoURI(videoUrl)
                uploadToFirebaseStorage(videoUrl)
            } else {
                Toast.makeText(this, "Failed to get video URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadToFirebaseStorage(fileUri: Uri) {
        val storageRef = Firebase.storage.reference
        val fileName = "videos/${System.currentTimeMillis()}" // Unique filename

        val uploadTask = storageRef.child(fileName).putFile(fileUri)
            .addOnSuccessListener {
                // Upload succeeded
                it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()
                    // Use downloadUrl as needed (e.g., store it in Firestore, display it, etc.)
                    saveVideoUrlToFirestore(downloadUrl)
                    Toast.makeText(this, "Upload successful! Download URL: $downloadUrl", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    // Handle failure to get download URL
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                // Handle unsuccessful uploads
                Toast.makeText(this, "Upload failed: $it", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveVideoUrlToFirestore(videoUrl: String) {
        // Assuming you want to update the 'url' field in Firestore
        val videoData = hashMapOf(
            "url" to videoUrl
            // Add other fields as needed
        )

        db.collection("videos").document(intentId!!)
            .update(videoData as Map<String, Any>)
            .addOnSuccessListener {
                // Document updated successfully
                Toast.makeText(this, "Video URL Update to Firestore!", Toast.LENGTH_SHORT).show()

                // Notify the callback about the update
                videoUpdateCallback?.onVideoUpdated()
            }
            .addOnFailureListener {
                // Handle unsuccessful update
                Toast.makeText(this, "Failed to update video URL in Firestore", Toast.LENGTH_SHORT).show()
            }
    }
}