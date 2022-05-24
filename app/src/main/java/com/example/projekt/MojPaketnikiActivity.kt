package com.example.projekt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt.databinding.ActivityMojPaketnikiBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

lateinit var recyclerview: RecyclerView
var IDPAKETNIKA: String = ""

class MojPaketnikiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMojPaketnikiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_moj_paketniki)
        binding = ActivityMojPaketnikiBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        recyclerview.layoutManager = LinearLayoutManager(this)

        val data = ArrayList<ItemsViewModel>()

        var jsonArray = JSONTokener(res).nextValue() as JSONArray

        for (i in 0 until jsonArray.length()) {
            val _id = jsonArray.getJSONObject(i).getString("_id")
            val naziv = jsonArray.getJSONObject(i).getString("naziv")
            val poln = jsonArray.getJSONObject(i).getString("poln")
            println(_id + " " + naziv + " " + poln)
            val osebeZDostopom = jsonArray.getJSONObject(i).getString("osebeZDostopom")
            val jsonArray2 = JSONTokener(osebeZDostopom).nextValue() as JSONArray
            for (i in 0 until jsonArray2.length()) {
                val osebaUsername = jsonArray2.getJSONObject(i).getString("osebaUsername")
                println(osebaUsername)
            }
            data.add(
                ItemsViewModel(
                    R.drawable.pametni_paketnik,
                    naziv + ":" + _id.replace("0", "")
                )
            )
        }

        val adapter = CustomAdapter(data)

        recyclerview.adapter = adapter


    }

}

