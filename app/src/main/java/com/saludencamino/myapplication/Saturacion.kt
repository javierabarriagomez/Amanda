package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class Saturacion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saturacion)
    }

    fun comenzarMedicion(view: View){
        val intent = Intent(this,Saturacion_2::class.java)
        startActivity(intent)

    }
}