package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ecg : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ecg)
    }
    fun comenzarMedicion(view: View){
        val intent = Intent(this,ecg_2::class.java)
        startActivity(intent)
    }

    fun goBack(view: View){
        super.onBackPressed()
    }
}