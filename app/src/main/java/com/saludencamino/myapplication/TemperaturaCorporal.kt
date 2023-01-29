package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton

class TemperaturaCorporal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperatura_corporal)
    }


    fun comenzarMedicion(view: View){
        val intent = Intent(this,TemperaturaCorporal_2::class.java)
        startActivity(intent)

    }

    fun goBack(view: View){
        super.onBackPressed()
    }


}