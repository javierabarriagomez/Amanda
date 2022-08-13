package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class glucosa : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glucosa)
    }
    fun comenzarMedicion(view: View){
        val intent = Intent(this,glucosa2::class.java)
        startActivity(intent)
    }

    fun goBack(view: View){
        super.onBackPressed()
    }
}