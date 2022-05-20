package com.example.projekt

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.projekt.databinding.ActivityLoginBinding
import com.example.projekt.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private var sporocilo:String = "false"
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.usernameInput.text
        val password = binding.passwordInput.text



        binding.sendButton.setOnClickListener(){
            post("https://silent-eye-350012.oa.r.appspot.com/users/loginAPI","{\n" +
                    "\"username\": \"${username}\",\n" +
                    "\"password\": \"${password}\"\n" +
                    "}")

            if(sporocilo == "true"){
                //ODPERI MAIN
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else{
                binding.EroorMessageView.text = "NAPAČNO UPORABNIŠKO IME ALI GESLO!"
            }

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
                println("NAPAKA")
                e.printStackTrace()
                println("Konec Erorja")
            }


            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        println("NAPAKA2")
                        throw IOException("Unexpected code $response")
                    }
                    println("PRED KONZOLO")
                    for ((name, value) in response.headers) {
                        println("|$name|: |$value|")
                    }

                    //println(response.body!!.string())

                    //var respondeBody = JSONObject(Objects.requireNonNull(response.body).toString())       //NESMES 2x izpisat bodyja
                    var respondeBody = JSONObject(response.body!!.string())

                    sporocilo = respondeBody.getString("message")

                    println("Dobimo:"+ sporocilo)

                    //val tokenInBase64 = respondeBody.getString("data")

                    println("TEST2")
                }
            }
        })

    }
}