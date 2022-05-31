package com.example.projekt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.databinding.ActivityLoginBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException


var USERID: String = ""  //Globalna spremenljivka ki jo lahko uporabljamo v vseh datotekah.
var USERNAME: String = "null"

class LoginActivity : AppCompatActivity() {


    private fun test() {
        runOnUiThread {
            if (sporocilo == "true") {
                //ODPERI MAIN
                val intent = Intent(this, TwoFactorAuthentication::class.java)
                startActivity(intent)
            } else {
                binding.EroorMessageView.text =
                    "PRIŠLO JE DO NAPAKE:\n-NAPAKA NA STREŽNIKU ALI POVEZAVI\n-NAPAČNO UPORABNIŠKO IME ALI GESLO!"
            }
        }
    }

    private var sporocilo: String = "false"
    private lateinit var binding: ActivityLoginBinding

    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    var client = OkHttpClient()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.usernameInput.text
        val password = binding.passwordInput.text



        binding.sendButton.setOnClickListener() {
            LoadingScreen.displayLoadingWithText(this, "PRIJAVLJANJE...", false, 60000)
            if (!username.isEmpty() || !password.isEmpty() || username.isBlank() || password.isBlank()) {
                post(
                    "https://silent-eye-350012.oa.r.appspot.com/users/loginAPI", "{\n" +
                            "\"username\": \"${username}\",\n" +
                            "\"password\": \"${password}\"\n" +
                            "}"
                )
                //println("To je username:"+username)
                //println("To je username2:"+username.toString())
                USERNAME = username.toString()
            }
            //Thread.sleep(2500)
            /*Handler(Looper.myLooper()!!).postDelayed({

                if (sporocilo == "true") {
                    //ODPERI MAIN
                    val intent = Intent(this, TwoFactorAuthentication::class.java)
                    startActivity(intent)
                } else {
                    binding.EroorMessageView.text =
                        "PRIŠLO JE DO NAPAKE:\n-NAPAKA NA STREŽNIKU ALI POVEZAVI\n-NAPAČNO UPORABNIŠKO IME ALI GESLO!"
                }

            },3000)
*/
            sporocilo = "false"

        }
    }

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
                        LoadingScreen.hideLoading()
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
                    if (sporocilo == "true") {
                        USERID = respondeBody.getString("userId")
                    }
                    test()
                    println("Dobimo1:" + sporocilo)
                    LoadingScreen.hideLoading()
                    println("Dobimo2:" + USERID)

                    //val tokenInBase64 = respondeBody.getString("data")

                    println("TEST2")

                }
                LoadingScreen.hideLoading()
            }
        })

    }
}
