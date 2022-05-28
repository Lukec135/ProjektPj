package com.example.projekt

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.projekt.databinding.ActivityLoginBinding
import com.example.projekt.databinding.ActivityTwoFactorAuthenticationBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class TwoFactorAuthentication : AppCompatActivity() {

    private lateinit var binding: ActivityTwoFactorAuthenticationBinding
    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    val client = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_two_factor_authentication)

        binding = ActivityTwoFactorAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)




        val getDataDodajOsebo =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    var slika: Uri = "${data?.data.toString()}".toUri()

                    val slikaBase64 = getBase64ForUriAndPossiblyCrash(slika)

                    //println("VRNE:"+slikaBase64)

                    generateZip(slikaBase64)


                    binding.odgovor.text = "Prosim pocakajte na server da obdela vaso zahtevo!"

                    postSlike(
                        "https://silent-eye-350012.oa.r.appspot.com/images/dodajAPI", "{\n" +
                                "\"ime\": \"${USERNAME}\",\n" +
                                "\"slika\": \"${slikaBase64}\"\n" +
                                "}"
                    )

                }
            }

        val getData =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    var slika: Uri = "${data?.data.toString()}".toUri()

                    val slikaBase64 = getBase64ForUriAndPossiblyCrash(slika)

                    generateZip(slikaBase64)

                    println("VRNE:"+slikaBase64)

                    //val fileContent: ByteArray = FileUtils.readFileToByteArray(File("media/external/images/media/4091"))
                    //val encodedString: String = Base64.getEncoder().encodeToString(fileContent)

                    //binding.EroorMessageView.text = encodedString
                    //binding.imageView2.setImageURI(slika)

                    //generateZip(slikaBase64)

                    binding.odgovor.text = "Prosim pocakajte na server da obdela vaso zahtevo!"

                    postSlike(
                        "https://helloworld-43fq37x3bq-ew.a.run.app/preveri", "{\n" +
                                "\"ime\": \"${USERNAME}\",\n" +
                                "\"slika\": \"${slikaBase64}\"\n" +
                                "}"
                    )

                }
            }

        //binding.testniText.text = "TO JE IME"+USERNAME+",\n"+ USERID

        binding.dodajOseboButton.setOnClickListener(){
            try {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                getDataDodajOsebo.launch(intent)
            } catch (e: Exception) {
                println("ERROR PRI GUMBU")
            }
        }

        binding.posljiPython.setOnClickListener(){
           /*
           try {

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)    //ODPIRANJE KAMERE
                //intent.type = "image/*"
                getData.launch(intent)
            } catch (e: Exception) {
                println("ERROR PRI GUMBU")
            }
*/*/
            ///*

            try {
                val intent = Intent(Intent.ACTION_PICK)         //ODPIRANJE GALERIJE
                intent.type = "image/*"
                getData.launch(intent)
            } catch (e: Exception) {
                println("ERROR PRI GUMBU")
            }
            //*/*/
        }


    }

    private fun generateZip(tokenInBase64: String) {
        val decodedBytes = android.util.Base64.decode(tokenInBase64, android.util.Base64.NO_WRAP)
        val fos = FileOutputStream(filesDir.absolutePath + "/obraz.png")
        fos.write(decodedBytes)
        fos.flush()
        fos.close()
    }


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
    fun postSlike(url: String, json: String) {
        val body: RequestBody = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //binding.odgovor.text = "Prislo je do napake"
                println("NAPAKA")
                runOnUiThread(Runnable {                                //Kot neki dispatcher
                    binding.odgovor.text = "Prislo je do napake"
                })
                e.printStackTrace()
                println("Konec Erorja")
            }


            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        println("NAPAKA2")
                        runOnUiThread(Runnable {                                //Kot neki dispatcher
                            binding.odgovor.text = "Prislo je do napake"
                        })
                        throw IOException("Unexpected code $response")
                    }
                    println("PRED KONZOLO")
                    for ((name, value) in response.headers) {
                        println("|$name|: |$value|")
                    }

                    var respondeBody = response.body!!.string()

                    println("RESPONSE "+respondeBody)

                    println("Konec")

                    runOnUiThread(Runnable {                                //Kot neki dispatcher
                        binding.odgovor.text = "Server je zaključil delo"
                    })

                }
            }
        })


    }






}