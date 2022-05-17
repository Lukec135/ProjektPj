package com.example.projekt



import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Base64.decode
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.create

import org.json.JSONObject
import java.io.*
import java.util.zip.ZipFile


const val MY_FILE_NAME = "mydata.txt"
const val TESTFORZIP = "token.zip"

private const val BUFFER_SIZE = 4096

class MainActivity : AppCompatActivity() {

    private lateinit var file: File
    private lateinit var file123: File
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)

        file = File(filesDir, MY_FILE_NAME)
        file123 = File(filesDir, TESTFORZIP)

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
/*
    fun saveToFile(data:String) {
        try {
            //for FileUtils import org.apache.commons.io.FileUtils
            //in gradle implementation 'org.apache.commons:commons-io:1.3.2'
            FileUtils.writeStringToFile(file, data)
        } catch (e: IOException) {
            println("SAVE TO FILE: Can't save " + file.path)
        }
    }

    */
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

                    //println("TOKEN:"+tokenInBase64)

                    //val decoder: Base64.Decoder = Base64.getDecoder()
                    //val encoder: Base64.Encoder = Base64.getEncoder()

                    val tokenInByteArray: ByteArray = tokenInBase64.encodeToByteArray()
                    //val tokenDecoded = String(decoder.decode(tokenInBase64))

                    //println("TOJETOKENDECODE:"+tokenDecoded)
                    println("TEST1")

                    ///TO SHRANI NOT PRAVI NIZ
                    //saveToFile(tokenInBase64)

                    //FileUtils.writeByteArrayToFile(file123,tokenInByteArray)

                    generateZip(tokenInBase64)

                    val zipFileName = filesDir.absolutePath + "/token.zip";

                    /*
                    ZipFile(zipFileName).use { zip ->
                        zip.entries().asSequence().forEach { entry ->
                            zip.getInputStream(entry).use { input ->
                                File(entry.name).outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
*/

                   // unzip(file123,file123.absolutePath)

                    //file123.writeBytes(tokenInByteArray)
                    //zipBytes(file123.name,tokenInByteArray)
                    //File("testniStringZipan").writeText(tokenDecoded)
                    //decoder(tokenInBase64,"FIleZaUporabo.zip")


                    println("TEST2")
                }
            }
        })

    }

    private fun generateZip(tokenInBase64:String) {
        val decodedBytes = Base64.decode(tokenInBase64,Base64.NO_WRAP)
        val fos = FileOutputStream(filesDir.absolutePath+"/token.zip")
        fos.write(decodedBytes)
        fos.flush()
        fos.close()
    }


    ///TO POMOJEM NE DELUJE
    fun unzip(zipFilePath: File, destDirectory: String) {

        val destDir =  File(destDirectory).run {
            if (!exists()) {
                mkdirs()
            }
        }

        ZipFile(zipFilePath).use { zip ->

            zip.entries().asSequence().forEach { entry ->

                zip.getInputStream(entry).use { input ->


                    val filePath = destDirectory + File.separator + entry.name

                    if (!entry.isDirectory) {
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        dir.mkdir()
                    }

                }

            }
        }
    }

    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

}


