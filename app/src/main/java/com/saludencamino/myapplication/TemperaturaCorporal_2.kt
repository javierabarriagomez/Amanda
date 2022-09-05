package com.saludencamino.myapplication

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.linktop.MonitorDataTransmissionManager
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBleWriteResponse


class TemperaturaCorporal_2 : AppCompatActivity(), IBleWriteResponse {

    private var progressBar: ProgressBar? = null
    private var resultado: TextView? = null
    private var textoResultado: TextView? = null
    private var progressOverlay: View? = null
    private var botonMedicion: ImageButton? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperatura_corporal_2)
        this.resultado = findViewById(R.id.textView3)
        this.textoResultado = findViewById(R.id.textView4)
        progressBar = findViewById(R.id.progressBar)
        progressOverlay = findViewById(R.id.progress_overlay)
        botonMedicion=findViewById(R.id.botonMedicionGlucosa)

    }

    fun ocultarOverlay(){
        progressOverlay?.visibility = View.GONE;

    }

    fun goBack(view: View){
        super.onBackPressed()
    }
    fun tomarMedicion(view: View){
        println("Empezando medicion")

        println((this.application as App).version )
        this@TemperaturaCorporal_2.runOnUiThread(java.lang.Runnable {
            progressOverlay?.visibility = View.VISIBLE;
            botonMedicion?.setImageResource(R.drawable.detener_medicion)
        })

        if((this.application as App).version != 1){

            BleManager.getInstance().setBtResultListener {
                val prefs = getSharedPreferences(
                    "com.saludencamino.myapplication", Context.MODE_PRIVATE
                )
                prefs.edit().putFloat("temperatura",it.toFloat()).apply();
                prefs.edit().putBoolean("DatosCapturados",true).apply();
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
                this@TemperaturaCorporal_2.runOnUiThread(java.lang.Runnable {
                    Toast.makeText(this, "Examen finalizado", Toast.LENGTH_SHORT).show()
                    botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
                })
                ocultarOverlay()
            }
            BleManager.getInstance().startMeasure(MeasureType.TYPE_BT,this)
        }else{
            MonitorDataTransmissionManager.getInstance().setOnBtResultListener{
                val prefs = getSharedPreferences(
                    "com.saludencamino.myapplication", Context.MODE_PRIVATE
                )
                prefs.edit().putFloat("temperatura",it.toFloat()).apply();
                prefs.edit().putBoolean("DatosCapturados",true).apply();
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
            MonitorDataTransmissionManager.getInstance()
                .startMeasure(com.linktop.whealthService.MeasureType.BT)

        }
    }
    override fun onWriteSuccess() {
        println("WRITE SUCCESS")
    }

    override fun onWriteFailed() {
        println("WRITE FAILED")
    }
}