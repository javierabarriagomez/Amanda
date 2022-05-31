package com.saludencamino.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3


private const val NUM_PAGES = 1

class MainActivity : AppCompatActivity() {

    private var imageList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageList.add(R.drawable.login_1)
        imageList.add(R.drawable.login_2)
        imageList.add(R.drawable.login_3)
        imageList.add(R.drawable.login_4)


        val view_pager2 = findViewById<ViewPager2>(R.id.view_pager2)

        view_pager2.adapter = ViewPageAdapter(imageList)
        view_pager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val indicator = findViewById<CircleIndicator3>(R.id.circleindicator)
        indicator.setViewPager(view_pager2)



    }
    /** Called when the user touches the button */
    fun login(view: View) {
        println("LOGIN")
val intent = Intent(this,homeActivity::class.java)
        startActivity(intent)
        // Do something in response to button click
    }


}

