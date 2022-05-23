package com.example.projekt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.projekt.databinding.ActivityMojPaketnikiBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import org.json.JSONException


class MojPaketnikiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMojPaketnikiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_moj_paketniki)
        binding = ActivityMojPaketnikiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //USERID id od userja
        Toast.makeText(
            applicationContext,
            "ID: \n${USERID}",
            Toast.LENGTH_SHORT
        ).show()


        binding.button.setOnClickListener() {
            post(
                "https://silent-eye-350012.oa.r.appspot.com/paketnik/listAPI", "{\n" +
                        "\"lastnikId\": \"${USERID}\"\n" +
                        "}"
            )

        }


    }

    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    var client = OkHttpClient()

    @Throws(IOException::class)
    fun post(url: String, json: String) {
        val body: RequestBody = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }


            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    var respondeBody = JSONObject(response.body!!.string())

                    /*USERID = respondeBody.getString("userId")
                    println("Dobimo2:"+ USERID)*/
                    println(respondeBody)
/*
                    val jsonString = "{\"Employee\":{\"Name\":\"Niyaz\",\"Salary\":56000}}"


                        try {
                            val emp = JSONObject(jsonString).getJSONObject("Employee")
                            val empName = emp.getString("Name")
                            val empSalary = emp.getInt("Salary")
                            val string =
                                "Employee Name: $empName\nEmployee Salary: $empSalary"
                            print(string)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

*/


                }
            }
        })

    }
}


