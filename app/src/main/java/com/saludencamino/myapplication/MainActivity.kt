package com.saludencamino.myapplication

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3

import com.saludencamino.myapplication.Server


private const val NUM_PAGES = 1

class MainActivity : AppCompatActivity() {

    private var imageList = mutableListOf<Int>()
    private var textoUsuario: EditText? = null;
    private var textoContra: EditText? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )
        val iniciado  = prefs.getBoolean("sesion_iniciada",false);

        if(iniciado){
            val intent = Intent(this,homeActivity::class.java)
            startActivity(intent)
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageList.add(R.drawable.login_1)
        imageList.add(R.drawable.login_2)
        imageList.add(R.drawable.login_3)
        imageList.add(R.drawable.login_4)

        textoUsuario = findViewById(R.id.Username)
        textoContra = findViewById(R.id.Password)

        val view_pager2 = findViewById<ViewPager2>(R.id.view_pager2)

        view_pager2.adapter = ViewPageAdapter(imageList)
        view_pager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val indicator = findViewById<CircleIndicator3>(R.id.circleindicator)
        indicator.setViewPager(view_pager2)

    }

    fun login(view: View) {

        if(textoContra?.text?.isEmpty() == true || textoUsuario?.text?.isEmpty() == true){
            Toast.makeText(this, "Porfavor llene todos los campos", Toast.LENGTH_SHORT).show()
            return;
        }
        Toast.makeText(this, "Iniciando sesión", Toast.LENGTH_SHORT).show()
        val server = Server()
        println("HOla")
        if(server.login(textoUsuario?.text.toString(),textoContra?.text.toString(), this)){
            val intent = Intent(this,homeActivity::class.java)
            startActivity(intent)
        }else{
            println("Chao")
            Toast.makeText(this, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show()
        }


    }
}

