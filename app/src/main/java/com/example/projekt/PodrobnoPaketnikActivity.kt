package com.example.projekt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.projekt.databinding.ActivityLoginBinding

import com.example.projekt.databinding.ActivityPodrobnoPaketnikBinding
import org.json.JSONArray
import org.json.JSONTokener

class PodrobnoPaketnikActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPodrobnoPaketnikBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_podrobno_paketnik)
        binding = ActivityPodrobnoPaketnikBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toast.makeText(applicationContext,"TO JE "+ idPaketnika,Toast.LENGTH_SHORT).show()


        var jsonArray = JSONTokener(res).nextValue() as JSONArray

        for (i in 0 until jsonArray.length()) {
            if (i == idPaketnika.toInt()) {
                val _id = jsonArray.getJSONObject(i).getString("_id")
                val naziv = jsonArray.getJSONObject(i).getString("naziv")
                val poln = jsonArray.getJSONObject(i).getString("poln")

                binding.lastnik.text = "IME: " + naziv
                binding.paketnikID.text = "ID PAKETNIKA: " + _id.replace("0", "")
                var vrednost: String = ""
                if (poln == "true") {
                    vrednost = "POLN"
                } else if (poln == "false") {
                    vrednost = "PRAZEN"
                }
                binding.paketnikID.text = "ID: " + _id.replace("0", "")
                binding.poln.text = vrednost


                println(_id + " " + naziv + " " + poln)
                val osebeZDostopom = jsonArray.getJSONObject(i).getString("osebeZDostopom")
                val jsonArray2 = JSONTokener(osebeZDostopom).nextValue() as JSONArray
                var dostop: String = "DOSTOP:\n"
                for (i in 0 until jsonArray2.length()) {
                    val osebaUsername = jsonArray2.getJSONObject(i).getString("osebaUsername")
                    dostop += osebaUsername
                    println(osebaUsername)
                }
                binding.dostop.text = dostop
                val odklep = jsonArray.getJSONObject(i).getString("odklepi")
                val jsonArray3 = JSONTokener(odklep).nextValue() as JSONArray
                var odklepi: String = "ODKLEPI:\n"
                for (i in 0 until jsonArray3.length()) {
                    val odkleno = jsonArray3.getJSONObject(i).getString("oseba")
                    val datum = jsonArray3.getJSONObject(i).getString("datum")
                    odklepi += "*" + odkleno + " " + datum + "\n"
                    println(odkleno + " " + datum)
                }
                binding.odklepi.text = odklepi
            }
        }


    }
}


