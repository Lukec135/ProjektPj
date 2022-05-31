package com.example.projekt

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Window
import android.widget.TextView


object LoadingScreen {
    var dialog: Dialog? = null //obj
    fun displayLoadingWithText(
        context: Context?,
        text: String?,
        cancelable: Boolean,
        SPLASH_TIME_OUT: Long
    ) { // function -- context(parent (reference))
        dialog = Dialog(context!!)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.layout_loading_screen)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(cancelable)
        /*val textView = dialog!!.findViewById<TextView>(R.id.text)
        textView.text = text*/
        //val SPLASH_TIME_OUT = 4000
        try {

            dialog!!.show()
            Handler().postDelayed({
                dialog!!.dismiss()
            }, SPLASH_TIME_OUT.toLong())


        } catch (e: Exception) {
        }
    }


    fun hideLoading() {
        try {
            if (dialog != null) {
                dialog!!.dismiss()
            }
        } catch (e: Exception) {
        }
    }
}