package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View


class homeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }


    fun presion(view: View) {
        val intent = Intent(this,PresionSanguinea::class.java)
        startActivity(intent)
        // Do something in response to button click
    }
    fun temperatura(view: View) {
        val intent = Intent(this,TemperaturaCorporal::class.java)
        startActivity(intent)
        // Do something in response to button click
    }
    fun saturacion(view: View) {
        val intent = Intent(this,Saturacion::class.java)
        startActivity(intent)
        // Do something in response to button click
    }
    fun glucosa(view: View) {
        val intent = Intent(this,glucosa::class.java)
        startActivity(intent)
        // Do something in response to button click
    }
    fun ecg(view: View) {
        val intent = Intent(this,ecg::class.java)
        startActivity(intent)
        // Do something in response to button click
    }
}