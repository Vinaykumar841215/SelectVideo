package com.example.selectvideo

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextClock
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class VideoActivity : AppCompatActivity(),Video {
    @SuppressLint("MissingInflatedId")
    lateinit var myAdapter: MyAdapter
    private lateinit var listItem : ArrayList<User>
    lateinit var db : FirebaseFirestore
    lateinit var recyclerView : RecyclerView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        recyclerView  = findViewById(R.id.recyclyView)
        val tClock = findViewById<TextClock>(R.id.textClock)
        recyclerView.layoutManager = GridLayoutManager(this,1)
        listItem = arrayListOf()
        myAdapter = MyAdapter(this,listItem,this)
        db = FirebaseFirestore.getInstance()
//        progressBar.max = 1000
//        ObjectAnimator.ofInt(progressBar,"progress",600)
//            .setDuration(2000)
//            .start()
        db.collection("videos").get()

            .addOnSuccessListener {
                for (document in it.documents) {
                    val videoUrl = document.getString("url")
                    Uri.parse(videoUrl)
                    val text = document.getString("text")
                    val videoId = document.getString("id")
                    val user = User(videoUrl,text, videoId)
                    listItem.add(user)
                }
                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener{
                // Handle unsuccessful addition
                Toast.makeText(this, "Failed to add video URL to Firestore", Toast.LENGTH_SHORT).show()
            }
        recyclerView.adapter = myAdapter

//        tClock.setOnClickListener {
//            Toast.makeText(this@VideoActivity, "This is Text Clock", Toast.LENGTH_SHORT).show()
//        }
    }


    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    override fun onDeleteClicked(position: String, position1: Int) {
        val db = FirebaseFirestore.getInstance()
        if (position != null) {
            db.collection("videos")
                .document(position)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Video deleted", Toast.LENGTH_SHORT).show()
                    listItem.removeAt(position1)
                    myAdapter.notifyItemRemoved(position1)
                    myAdapter.notifyItemRangeChanged(position1, listItem.size)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this, "Error deleting video: $e", Toast.LENGTH_SHORT).show()
                }

        } else {
            Toast.makeText(this, "Video Id is empty or null", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onUpdateClicked(position: String, position1: Int) {
        if (position!=null){
            val intent = Intent(this@VideoActivity,UpdateActivity::class.java)
            intent.putExtra("videoUrl",listItem[position1].url.toString())
            intent.putExtra("videoId",listItem[position1].id.toString())
            startActivity(intent)
        }
    }

}