package com.saludencamino.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBleWriteResponse
import com.mintti.visionsdk.ble.callback.ISpo2ResultListener
import com.jjoe64.graphview.series.LineGraphSeries
import android.app.Activity
import android.content.Context
import com.linktop.MonitorDataTransmissionManager
import com.linktop.infs.OnSpO2ResultListener

class Saturacion_2 : AppCompatActivity(), ISpo2ResultListener,IBleWriteResponse,Handler.Callback,OnSpO2ResultListener{
    private var isRunning: Boolean = false
    private var progressBar: ProgressBar? = null
    private var tiempo: Double = 0.0
    private var resultadoText: TextView? = null
    private var graph: GraphView? = null
    private var series: LineGraphSeries<DataPoint>? = null
    private var resultado: TextView? = null
    private var corazonUI: TextView? = null
    private var botonInicio: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saturacion2)
        progressBar = findViewById(R.id.progressBar4)
        resultadoText = findViewById(R.id.textView15)
        resultado = findViewById(R.id.Spo2)
        graph = findViewById(R.id.graph)
        botonInicio = findViewById(R.id.imageButton5)
        corazonUI = findViewById(R.id.CORAZON)
        series = LineGraphSeries()


    }


    fun tomarMedicion(view: View){
        if(!isRunning){
            botonInicio?.setImageResource(R.drawable.detener_medicion)
            if((this.application as App).version != 1) {
                BleManager.getInstance().setSpo2ResultListener(this)
                BleManager.getInstance().startMeasure(MeasureType.TYPE_SPO2,this)
            }else{
                MonitorDataTransmissionManager.getInstance().setOnSpO2ResultListener(this)
                MonitorDataTransmissionManager.getInstance().startMeasure(com.linktop.whealthService.MeasureType.SPO2)
            }
            tiempo = 0.0
            isRunning=true
            series = LineGraphSeries()
            graph?.removeAllSeries()
            graph?.addSeries(series)
        }else{
            botonInicio?.setImageResource(R.drawable.iniciar_medicion)
            if((this.application as App).version != 1) {
                BleManager.getInstance().stopMeasure(MeasureType.TYPE_SPO2,this)
            }else{
                MonitorDataTransmissionManager.getInstance().stopMeasure()
            }

            isRunning=false;
            tiempo = 0.0
            series = LineGraphSeries();
        }

    }
    private fun toastFinalizado(){
        this@Saturacion_2.runOnUiThread(java.lang.Runnable {
            Toast.makeText(applicationContext, "Examen Finalizado", Toast.LENGTH_SHORT).show()
        })

    }

    //version 2.0

    override fun onWaveData(p0: Int) {
        tiempo+=1

        series?.appendData(DataPoint(tiempo,p0.toDouble()),true,50,true)
        if(tiempo.toInt() % 200 == 0){
            graph?.onDataChanged(false,true)
        }


    }

    override fun onBoResultData(corazon: Int, spo2: Double) {
        progressBar?.progress = spo2.toInt()
        resultado?.setText(spo2.toString() + " %").toString()
        corazonUI?.setText(corazon.toString() + " BPM").toString()
        if(spo2 > 95.0){
            resultadoText?.text = "Normal"
        }else if(spo2 < 95.0 && spo2 > 91.0){
            resultadoText?.text = "Bajo"
        }else{
            resultadoText?.text = "Extremadamente Bajo"
        }

        if(corazon != 0 && spo2 != 0.0){
            botonInicio?.setImageResource(R.drawable.iniciar_medicion)
            BleManager.getInstance().stopMeasure(MeasureType.TYPE_SPO2,this)
            val prefs = getSharedPreferences(
                "com.saludencamino.myapplication", Context.MODE_PRIVATE
            )
            prefs.edit().putFloat("saturacion_spo2",spo2.toFloat()).apply();
            prefs.edit().putInt("saturacion_corazon",corazon).apply();
            prefs.edit().putBoolean("DatosCapturados",true).apply();

            toastFinalizado()

            isRunning=false;
            tiempo = 0.0
            series = LineGraphSeries();


        }

    }

    override fun onWriteSuccess() {

    }

    override fun onWriteFailed() {

    }

    override fun handleMessage(p0: Message): Boolean {
        println(p0)
        return true
    }

    fun goBack(view: View){
        super.onBackPressed()
    }


    //Version 1.0

    override fun onSpO2Result(corazon: Int, spo2: Int) {
        val prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )
        prefs.edit().putFloat("saturacion_spo2",spo2.toFloat()).apply();
        prefs.edit().putInt("saturacion_corazon",corazon).apply();
        prefs.edit().putBoolean("DatosCapturados",true).apply();
        println("Finalizado");
        println(corazon);
        println(spo2);
        progressBar?.progress = spo2.toInt()
        resultado?.setText(spo2.toString() + " %").toString()
        corazonUI?.setText(corazon.toString() + " BPM").toString()
        if(spo2 > 95.0){
            resultadoText?.text = "Normal"
        }else if(spo2 < 95.0 && spo2 > 91.0){
            resultadoText?.text = "Bajo"
        }else{
            resultadoText?.text = "Extremadamente Bajo"
        }

        if(corazon != 0 && spo2 != 0){
            botonInicio?.setImageResource(R.drawable.iniciar_medicion)
            MonitorDataTransmissionManager.getInstance().stopMeasure()
            toastFinalizado()
            isRunning=false;
            tiempo = 0.0
            series = LineGraphSeries();


        }
    }

    override fun onSpO2Wave(p0: Int) {
        print(p0);
        tiempo+=1

        series?.appendData(DataPoint(tiempo,p0.toDouble()),true,8,true)
        if(tiempo.toInt() % 120 == 0){
            graph?.onDataChanged(false,true)
        }
    }

    override fun onSpO2End() {
        return
    }

    override fun onFingerDetection(p0: Int) {
        return
    }


}