package com.example.projekt

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.projekt.databinding.ActivityObvestilaBinding
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.util.*


private lateinit var binding: ActivityObvestilaBinding

val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
var client = OkHttpClient()
private var sporocilo: String = "false"

var deliveries = listOf<Delivery>()



class ObvestilaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_obvestila)

        binding = ActivityObvestilaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("USERNAME: "+ USERNAME)
        println(("{\n" +
                "\"username\": \"${USERNAME}\"\n" +
                "}"))

        post(
            "https://silent-eye-350012.oa.r.appspot.com/deliveries/deliveryListAPI", "{\n" +
                    "\"username\": \"${USERNAME}\"\n" +
                    "}"
        )

        Thread.sleep(2000)  // sleep for 2 seconds

        class DeliveryAdapter(context: Context, deliveries: List<Delivery>) : ArrayAdapter<Delivery>(context, 0, deliveries) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val delivery = getItem(position)
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.obvestilo_layout, parent, false)

                //val radioButton = view.findViewById<RadioButton>(R.id.radio_button)
                val deliveryhourTextView = view.findViewById<TextView>(R.id.hour)
                val deliverydayTextView = view.findViewById<TextView>(R.id.day)
                val deliverysignedTextView = view.findViewById<TextView>(R.id.signed)

                val deliveryButtonPaketnik = view.findViewById<Button>(R.id.buttonPaketnik)
                val deliveryButtonRoke = view.findViewById<Button>(R.id.buttonRoke)
                val deliveryButtonPosta = view.findViewById<Button>(R.id.buttonPosta)


                deliveryButtonPaketnik.setOnClickListener(){
                    //println("Kliknil si Paketnik")
                    //{"username":"Ziga","deliveryId":"63a4bb35b53ea0000a017dde","hour":"7","day":"pon","weather":"sonce","holiday":"ne","signed":"ne","rating":"None"}
                    var jsonToSend: String = "{\"username\":\"${delivery!!.username}\",\"deliveryId\":\"${delivery!!.deliveryId}\",\"hour\":\"${delivery!!.hour}\",\"day\":\"${delivery!!.day}\",\"weather\":\"${delivery!!.weather}\",\"holiday\":\"${delivery!!.holiday}\",\"signed\":\"${delivery!!.signed}\",\"rating\":\"paketnik\"}"

                    postDelivery("https://silent-eye-350012.oa.r.appspot.com/users/addToArffAPI", jsonToSend)

                    finish()  // close the current activity
                    startActivity(intent)  // start the activity again

                }

                deliveryButtonPosta.setOnClickListener(){
                    //println("Kliknil si Paketnik")
                    //{"username":"Ziga","deliveryId":"63a4bb35b53ea0000a017dde","hour":"7","day":"pon","weather":"sonce","holiday":"ne","signed":"ne","rating":"None"}
                    var jsonToSend: String = "{\"username\":\"${delivery!!.username}\",\"deliveryId\":\"${delivery!!.deliveryId}\",\"hour\":\"${delivery!!.hour}\",\"day\":\"${delivery!!.day}\",\"weather\":\"${delivery!!.weather}\",\"holiday\":\"${delivery!!.holiday}\",\"signed\":\"${delivery!!.signed}\",\"rating\":\"posta\"}"

                    postDelivery("https://silent-eye-350012.oa.r.appspot.com/users/addToArffAPI", jsonToSend)

                    finish()  // close the current activity
                    startActivity(intent)  // start the activity again

                }

                deliveryButtonRoke.setOnClickListener(){
                    //println("Kliknil si Paketnik")
                    //{"username":"Ziga","deliveryId":"63a4bb35b53ea0000a017dde","hour":"7","day":"pon","weather":"sonce","holiday":"ne","signed":"ne","rating":"None"}
                    var jsonToSend: String = "{\"username\":\"${delivery!!.username}\",\"deliveryId\":\"${delivery!!.deliveryId}\",\"hour\":\"${delivery!!.hour}\",\"day\":\"${delivery!!.day}\",\"weather\":\"${delivery!!.weather}\",\"holiday\":\"${delivery!!.holiday}\",\"signed\":\"${delivery!!.signed}\",\"rating\":\"roke\"}"

                    postDelivery("https://silent-eye-350012.oa.r.appspot.com/users/addToArffAPI", jsonToSend)

                    finish()  // close the current activity
                    startActivity(intent)  // start the activity again

                }

                //radioButton.text = delivery.option1
                //radioButton.text = "deilveryoption 1"

                println("Tukaj not pridem")
                if (delivery != null) {
                    deliveryhourTextView.text = "ura:"+delivery.hour
                    deliverydayTextView.text = "dan:"+delivery.day
                    deliverysignedTextView.text = "po prevzetju:"+delivery.signed
                }

                return view
            }
        }

        println("St elemetnov:"+ deliveries.size)

        binding.deliveryList.adapter = DeliveryAdapter(this, deliveries)
        binding.deliveryList.setOnItemClickListener { _, _, position, _ ->
            val selectedDelivery = deliveries[position]
            println(selectedDelivery)
            // handle the user's selection
        }


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
                    println("RESPONSE")

                    //println(response.body!!.string())

//                    var respondeBody = JSONObject(Objects.requireNonNull(response.body).toString())       //NESMES 2x izpisat bodyja
                    //var respondeBody = JSONObject(response.body!!.string())

                    var respondeBody = response.body!!.string()


                    println(respondeBody)


                    val gson = Gson()
                    deliveries = gson.fromJson(respondeBody, Array<Delivery>::class.java).toList()

                    println("ST EL1:"+ deliveries.size)

                    //println(deliveries)

                    println("KONEC RESPONSA")
                }
            }
        })
}


@Throws(IOException::class)
fun postDelivery(url: String, json: String) {
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
                println("RESPONSE")

                //println(response.body!!.string())

//                    var respondeBody = JSONObject(Objects.requireNonNull(response.body).toString())       //NESMES 2x izpisat bodyja
                //var respondeBody = JSONObject(response.body!!.string())

                var respondeBody = response.body!!.string()


                println(respondeBody)


                println("KONEC RESPONSA")
            }
        }
    })
}