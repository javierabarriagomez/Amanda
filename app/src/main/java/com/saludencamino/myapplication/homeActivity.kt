package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.widget.Gallery




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
}