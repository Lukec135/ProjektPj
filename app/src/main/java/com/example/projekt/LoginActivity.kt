package com.example.projekt

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64.encodeToString
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.projekt.databinding.ActivityLoginBinding

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.apache.commons.io.FileUtils
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

var USERID: String = ""  //Globalna spremenljivka ki jo lahko uporabljamo v vseh datotekah.

class LoginActivity : AppCompatActivity() {

    private var sporocilo: String = "false"
    private lateinit var binding: ActivityLoginBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.usernameInput.text
        val password = binding.passwordInput.text

        binding.sendButton.setOnClickListener() {
            LoadingScreen.displayLoadingWithText(this, "PRIJAVLJANJE...", false)
            if (!username.isEmpty() || !password.isEmpty()) {
                post(
                    "https://silent-eye-350012.oa.r.appspot.com/users/loginAPI", "{\n" +
                            "\"username\": \"${username}\",\n" +
                            "\"password\": \"${password}\"\n" +
                            "}"
                )
            }
            Thread.sleep(2500)
            if (sporocilo == "true") {
                //ODPERI MAIN
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                binding.EroorMessageView.text = "NAPAČNO UPORABNIŠKO IME ALI GESLO!"
            }
            sporocilo = "false"

            /*  LoadingScreen.hideLoading()
              Thread.sleep(3000)
              LoadingScreen.displayLoadingWithText(this,"Prijavljanje...",false)*/
        }

        val getData =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    var slika: Uri = "${data?.data.toString()}".toUri()

                    val slikaBase64 = getBase64ForUriAndPossiblyCrash(slika)

                    println("VRNE:"+slikaBase64)

                    //val fileContent: ByteArray = FileUtils.readFileToByteArray(File("media/external/images/media/4091"))
                    //val encodedString: String = Base64.getEncoder().encodeToString(fileContent)

                    //binding.EroorMessageView.text = encodedString
                    //binding.imageView2.setImageURI(slika)

                    generateZip(slikaBase64)

                    postSlike(
                        "https://helloworld-43fq37x3bq-ew.a.run.app/dodaj", "{\n" +
                                "\"ime\": \"tektIme\",\n" +
                                "\"slika\": \"${slikaBase64}\"\n" +
                                "}"
                    )

                }
            }

        binding.TESTNIButton.setOnClickListener(){

            //private fun openGalleryForImage() {

            //val intent = Intent(Intent.ACTION_PICK)
            //intent.type = "image/*"
            //startActivityForResult(intent, 100)

            try {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                getData.launch(intent)
            } catch (e: Exception) {
                //val marketUri = Uri.parse("market://details?id=com.google.zxing.client.android")
                //val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                //startActivity(marketIntent)

            }
            //}
            /*
            postSlike(
                "https://helloworld-43fq37x3bq-ew.a.run.app/", "{\n" +
                        "\"username\": \"${username}\",\n" +
                        "\"password\": \"${password}\"\n" +
                        "}"
            )
             */
        }

    }


    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    var client = OkHttpClient()


    @RequiresApi(Build.VERSION_CODES.O)
    fun getBase64ForUriAndPossiblyCrash(uri: Uri): String {

        try {
            val bytes = contentResolver.openInputStream(uri)?.readBytes()
            val encodedString: String = Base64.getEncoder().encodeToString(bytes)

            return encodedString
        } catch (error: IOException) {
            error.printStackTrace() // This exception always occurs
            return "NE DELA"
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

                    println("Dobimo1:" + sporocilo)
                    println("Dobimo2:" + USERID)

                    //val tokenInBase64 = respondeBody.getString("data")

                    println("TEST2")
                }
            }
        })

    }

    private fun generateZip(tokenInBase64: String) {
        val decodedBytes = android.util.Base64.decode(tokenInBase64, android.util.Base64.NO_WRAP)
        val fos = FileOutputStream(filesDir.absolutePath + "/slika.jpg")
        fos.write(decodedBytes)
        fos.flush()
        fos.close()
    }

    @Throws(IOException::class)
    fun postSlike(url: String, json: String) {
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

                    var respondeBody = response.body!!.string()

                    println("RESPONSE "+respondeBody)

                    println("Konec")
                }
            }
        })

    }


}