package com.example.projekt

import android.R.attr.bitmap
import android.R.attr.visible
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Base64.encodeToString
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.projekt.databinding.ActivityTwoFactorAuthenticationBinding

import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.internal.wait
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
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

    lateinit var currentPhotoPath: String
    val REQUEST_IMAGE_CAPTURE = 1

    var uriOfImage: Uri = Uri.EMPTY

    var zaznanoIme = "neznan"


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

                    println("URI:" + slika)

                    //println("VRNE:"+slikaBase64)

                    generateZip(slikaBase64)


                    binding.odgovor.text = "Prosim, počakajte na odgovor strežnika."

                    postSlike(
                        "https://silent-eye-350012.oa.r.appspot.com/images/dodajAPI", "{\n" +
                                "\"ime\": \"${USERNAME}\",\n" +
                                "\"slika\": \"${slikaBase64}\"\n" +
                                "}"
                    )
                    LoadingScreen.displayLoadingWithText(this, "PRIJAVLJANJE...", false, 300000)
                }
            }

        val getData =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data

                    val takenImage = data?.extras?.get("data") as Bitmap


                    //https://www.youtube.com/watch?v=DPHkhamDoyc&ab_channel=RahulPandey
                    // KER JE PRESLABA KVALITETA MORAMO UPORABITI DRUG NACIN
                    // TAK DA SHRANIMO NA FON IN NATO POSLJEMO

                    val byteArrayOutputStream = ByteArrayOutputStream()
                    takenImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()

                    val encoded: String =
                        android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

                    //var slika: Uri = "${data?.data.toString()}".toUri()

                    //val slikaBase64 = getBase64ForUriAndPossiblyCrash(takenImage)

                    generateZip(encoded)

                    println("VRNE:" + encoded)

                    //val fileContent: ByteArray = FileUtils.readFileToByteArray(File("media/external/images/media/4091"))
                    //val encodedString: String = Base64.getEncoder().encodeToString(fileContent)

                    //binding.EroorMessageView.text = encodedString
                    //binding.imageView2.setImageURI(slika)

                    //generateZip(slikaBase64)

                    binding.odgovor.text = "Prosim, počakajte na odgovor strežnika."


                    postSlike(
                        "https://helloworld-43fq37x3bq-ew.a.run.app/preveri", "{\n" +
                                "\"ime\": \"${USERNAME}\",\n" +
                                "\"slika\": \"${encoded}\"\n" +
                                "}"
                    )

                }

            }

        //binding.testniText.text = "TO JE IME"+USERNAME+",\n"+ USERID

        binding.dodajOseboButton.setOnClickListener() {
            try {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                getDataDodajOsebo.launch(intent)
            } catch (e: Exception) {
                println("ERROR PRI GUMBU")
            }
        }

        binding.OdperiSlikoButton.setOnClickListener() {
            /*
            try {

                 val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)    //ODPIRANJE KAMERE
                 //intent.type = "image/*"
                 getData.launch(intent)
             } catch (e: Exception) {
                 println("ERROR PRI GUMBU")
             }
 */*/
            /*

            try {
                val intent = Intent(Intent.ACTION_PICK)         //ODPIRANJE GALERIJE
                intent.type = "image/*"
                getData.launch(intent)
            } catch (e: Exception) {
                println("ERROR PRI GUMBU")
            }
            */*/


            uriOfImage = dispatchTakePictureIntent()
            Handler(Looper.myLooper()!!).postDelayed({

                binding.PosljiButton.visibility = View.VISIBLE
                binding.odgovor.text = ""
                binding.testniText.text = ""
            }, 1000)


        }

        binding.PosljiButton.setOnClickListener() {
            println("URI: " + uriOfImage)

            val slikaBase64 = getBase64ForUriAndPossiblyCrash(uriOfImage)

            generateZip(slikaBase64)

            binding.odgovor.text = "Prosim, počakajte na odgovor strežnika."


            LoadingScreen.displayLoadingWithText(this, "PRIJAVLJANJE...", false, 300000)
            postSlikePython(
                "https://helloworld-43fq37x3bq-ew.a.run.app/preveri", "{\n" +
                        "\"ime\": \"${USERNAME}\",\n" +
                        "\"slika\": \"${slikaBase64}\"\n" +
                        "}"
            )

            binding.PosljiButton.isEnabled = false


        }

        binding.GoToMainButton.setOnClickListener() {
            if (zaznanoIme == USERNAME) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                binding.testniText.text = "Sistem je zaznal napačno osebo!"
            }
        }


/*
            binding.odgovor.text = "Prosim pocakajte na server da obdela vaso zahtevo!"


            postSlike(
                "https://helloworld-43fq37x3bq-ew.a.run.app/preveri", "{\n" +
                        "\"ime\": \"${USERNAME}\",\n" +
                        "\"slika\": \"${slikaBase64}\"\n" +
                        "}"
            )
*/

    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dispatchTakePictureIntent(): Uri {
        println("Pride not")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            println("ENA")
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    println("NAPAKA DSIPATCHER")
                    null
                }

                println("PO TRY")
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.projekt",
                        it
                    )
                    println("DOBIMO URI")
                    var x = takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    var y = startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

                    binding.PosljiButton.isEnabled = true

                    return photoURI
                    //Thread.sleep(25000) //pocakamo da uporabnik nalozi sliko
/*
                    println("URI: "+photoURI)

                    val slikaBase64 = getBase64ForUriAndPossiblyCrash(photoURI)

                    generateZip(slikaBase64)

                    binding.odgovor.text = "Prosim pocakajte na server da obdela vaso zahtevo!"


                    postSlike(
                        "https://helloworld-43fq37x3bq-ew.a.run.app/preveri", "{\n" +
                                "\"ime\": \"${USERNAME}\",\n" +
                                "\"slika\": \"${slikaBase64}\"\n" +
                                "}"
                    )

 */
                }
            }
        }
        return Uri.EMPTY
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
                    binding.odgovor.text = "Prosim, poskusite znova."
                    LoadingScreen.hideLoading()
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
                            binding.odgovor.text = "Prosim, poskusite znova."
                            LoadingScreen.hideLoading()
                        })
                        throw IOException("Unexpected code $response")
                    }
                    println("PRED KONZOLO")
                    for ((name, value) in response.headers) {
                        println("|$name|: |$value|")
                    }

                    var respondeBody = response.body!!.string()

                    println("RESPONSE " + respondeBody)

                    println("Konec")

                    runOnUiThread(Runnable {                                //Kot neki dispatcher
                        binding.odgovor.text = "Strežnik je končal obdelavo."
                        LoadingScreen.hideLoading()
                    })

                }
            }
        })
        LoadingScreen.hideLoading()
    }

    @Throws(IOException::class)
    fun postSlikePython(url: String, json: String) {
        val body: RequestBody = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //binding.odgovor.text = "Prislo je do napake"
                println("NAPAKA")
                runOnUiThread(Runnable {//Kot neki dispatcher
                    binding.odgovor.text = "Prosim, poskusite znova."
                    LoadingScreen.hideLoading()
                })
                e.printStackTrace()
                println("Konec Erorja")
            }


            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        println("NAPAKA ------> response not successful")
                        runOnUiThread(Runnable {//Kot neki dispatcher
                            binding.odgovor.text = "Prosim, poskusite znova."
                            LoadingScreen.hideLoading()
                        })
                        throw IOException("Unexpected code $response")
                    }
                    println("pred konzolo")
                    for ((name, value) in response.headers) {
                        println("|$name|: |$value|")
                    }
                    //println("/////RESPONSE-----------------> "+response.body!!.string())
                    //var respondeBody = JSONObject(response.body!!.string())
                    var pythonRes = response.body!!.string()

                    println("/////RESPONSE-----------------> " + pythonRes)

                    //val responseIme = respondeBody.getString("ime")
                    if (pythonRes == "ERROR_no_face_detected") {
                        runOnUiThread(Runnable {
                            binding.odgovor.text = "Prosim, poskusite znova."
                            LoadingScreen.hideLoading()
                            //
                        })
                        //throw IOException("Unexpected code $response")

                    } else {
                        zaznanoIme = pythonRes.substring(15, USERNAME.length + 15)
                        println("Vrnjeno ime = $zaznanoIme")
                        LoadingScreen.hideLoading()


                        println("Konec")

                        runOnUiThread(Runnable {                                //Kot neki dispatcher
                            binding.odgovor.text = "Strežnik je končal obdelavo."
                            binding.GoToMainButton.isEnabled = true
                            binding.GoToMainButton.visibility = View.VISIBLE
                            LoadingScreen.hideLoading()

                        })

                    }
                }
            }
        })
        LoadingScreen.hideLoading()
    }


}