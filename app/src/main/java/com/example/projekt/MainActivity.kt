package com.example.projekt



import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.create
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.zip.GZIPInputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val getData =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data

                    //izpise vsebino qr kode
                    Toast.makeText(
                        applicationContext,
                        "VSEBINA QR KODE: \n${data?.getStringExtra("SCAN_RESULT")}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        binding.ShowQr.setOnClickListener {
            //binding.textView.text = "DELUJE"
            try {
                val intent = Intent("com.google.zxing.client.android.SCAN")
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
                getData.launch(intent)
            } catch (e: Exception) {
                val marketUri = Uri.parse("market://details?id=com.google.zxing.client.android")
                val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                startActivity(marketIntent)

            }
        }

        binding.test123.setOnClickListener{
            post("https://api-ms-stage.direct4.me/sandbox/v1/Access/openbox","{\n" +
                    "\"boxId\": 352,\n" +
                    "\"tokenFormat\": 2\n" +
                    "}")
        }


    }


    val token = "9ea96945-3a37-4638-a5d4-22e89fbc998f"

    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    var client = OkHttpClient()

    @Throws(IOException::class)
    fun post(url: String, json: String) {
        val body: RequestBody = create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + token)
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

                    //println("TOJETOKENKODIRAN:"+ respondeBody.getString("data"))

                    val tokenInBase64 = respondeBody.getString("data")

                    val decoder: Base64.Decoder = Base64.getDecoder()
                    val tokenDecoded = String(decoder.decode(tokenInBase64))

                    //println("TOJETOKENDECODE:"+tokenDecoded)
                    println("TEST1")

                    //File("testniStringZipan").writeText(tokenDecoded)
                    decoder(tokenInBase64,"FIleZaUporabo.zip")


                    println("TEST2")
                }
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decoder(base64Str: String, pathFile: String): Unit{
        val imageByteArray = Base64.getDecoder().decode(base64Str)
        File(pathFile).writeBytes(imageByteArray)
    }
}


