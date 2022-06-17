package com.saludencamino.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBleWriteResponse

class TemperaturaCorporal_2 : AppCompatActivity(), IBleWriteResponse {

    private var progressBar: ProgressBar? = null
    private var resultado: TextView? = null
    private var textoResultado: TextView? = null
    private var progressOverlay: View? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperatura_corporal_2)
        this.resultado = findViewById(R.id.textView3)
        this.textoResultado = findViewById(R.id.textView4)
        progressBar = findViewById(R.id.progressBar)
        progressOverlay = findViewById(R.id.progress_overlay)

    }

    fun ocultarOverlay(){
        progressOverlay?.visibility = View.GONE;

    }
    fun tomarMedicion(view: View){
        println("Empezando medicion")
        progressOverlay?.visibility = View.VISIBLE;
        BleManager.getInstance().setBtResultListener {
            println(it)


            resultado?.setText(it.toString() + " °C").toString()
            progressBar?.progress = it.toInt()
            if(it < 36.5){
                textoResultado?.setText("Baja").toString()
            }else if(it > 36.5 && it < 37.5 ){
                textoResultado?.setText("Normal").toString()
            }else{
                textoResultado?.setText("Alta").toString()
            }
            ocultarOverlay()
        }
        BleManager.getInstance().startMeasure(MeasureType.TYPE_BT,this)
    }
    override fun onWriteSuccess() {
        println("WRITE SUCCESS")
    }

    override fun onWriteFailed() {
        println("WRITE FAILED")
    }
}