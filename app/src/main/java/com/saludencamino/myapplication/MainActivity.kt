package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Gallery
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



    }
    /** Called when the user touches the button */
    fun login(view: View) {
        println("LOGIN")
val intent = Intent(this,homeActivity::class.java)
        startActivity(intent)
        // Do something in response to button click
    }


}

