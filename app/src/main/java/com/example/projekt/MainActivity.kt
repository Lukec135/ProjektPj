package com.example.projekt


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding //ADD THIS LINE

    //lateinit var app: MyApplication

    //val btnShowQR = findViewById(R.id.ShowQr) as Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater) //ADD THIS LINE
        setContentView(binding.root)

        val getData = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
               //izpise vsebino qr kode
                Toast.makeText(applicationContext,"VSEBINA QR KODE: \n${data?.getStringExtra("SCAN_RESULT")}", Toast.LENGTH_SHORT).show()
                //var podatki = data?.getStringExtra("SCAN_RESULT").toString()

            }
        }

        binding.ShowQr.setOnClickListener {
            //binding.textView.text = "DELUJE"
            try {
                val intent = Intent("com.google.zxing.client.android.SCAN")
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE") // use “PRODUCT_MODE” for barcodes
                getData.launch(intent)
            } catch (e: Exception) {
                val marketUri = Uri.parse("market://details?id=com.google.zxing.client.android")
                val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                startActivity(marketIntent)

            }
        }
    }

}