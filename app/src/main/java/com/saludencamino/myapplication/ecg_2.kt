package com.saludencamino.myapplication

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.kl.vision_ecg.ISmctAlgoCallback
import com.kl.vision_ecg.SmctConstant
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBleWriteResponse
import com.mintti.visionsdk.ble.callback.IEcgResultListener

class ecg_2 : AppCompatActivity() , IBleWriteResponse, IEcgResultListener, Handler.Callback,
    ISmctAlgoCallback{

    private var botonMedicion: ImageButton? = null
    private var enMedicion:Boolean = false
    private var ritmoCardiaco: TextView? = null
    private var rpiMax: TextView? = null
    private var rpiMin: TextView? = null
    private var duracion: TextView? = null
    private var hrv: TextView? = null
    private var respiracion: TextView? =null
    private var graph: GraphView? = null
    private var series: LineGraphSeries<DataPoint>? = null
    private var tiempo = 0.0;




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ecg2)
        botonMedicion=findViewById(R.id.botonMedicionGlucosa)
        ritmoCardiaco= findViewById(R.id.ritmoCardiaco)
        rpiMax=findViewById(R.id.rpiMax)
        rpiMin=findViewById(R.id.rpiMin)
        duracion=findViewById(R.id.duracion)
        hrv=findViewById(R.id.hrv)
        respiracion=findViewById(R.id.respiracion)
        graph = findViewById(R.id.graph)
        series = LineGraphSeries()
        tiempo = 0.0
        graph?.viewport?.isYAxisBoundsManual= true
        graph?.viewport?.setMaxY(100.0);
        graph?.viewport?.setMinY(-100.0);

    }

    fun iniciarMedicion(view: View){

        if(!enMedicion){
            botonMedicion?.setImageResource(R.drawable.detener_medicion)

            BleManager.getInstance().setEcgResultListener(this)
            BleManager.getInstance().startMeasure(MeasureType.TYPE_ECG,this)
            series = LineGraphSeries();
            graph?.removeAllSeries()
            tiempo = 0.0

            graph?.addSeries(series)

            enMedicion=true
        }else{
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
            BleManager.getInstance().stopMeasure(MeasureType.TYPE_ECG,this)
            enMedicion=false
        }


    }

    override fun onWriteSuccess() {

    }

    override fun onWriteFailed() {

    }

    override fun onDrawWave(p0: Int) {

        //
        //series?.appendData(DataPoint(tiempo,p0.toDouble()),true,40,true)
        this@ecg_2.runOnUiThread(java.lang.Runnable {
            tiempo+=0.001
            series?.appendData(DataPoint(tiempo,p0.toDouble()),true,40,true)
        })


    }

    override fun onHeartRate(p0: Int) {
        this@ecg_2.runOnUiThread(java.lang.Runnable {
            ritmoCardiaco?.setText(p0.toString()).toString()
        })

    }

    override fun onRespiratoryRate(p0: Int) {
        this@ecg_2.runOnUiThread(java.lang.Runnable {
            respiracion?.setText(p0.toString()).toString()
        })



    }

    override fun onEcgResult(rrMax: Int, rrMin: Int, hrv: Int) {
        this@ecg_2.runOnUiThread(java.lang.Runnable {
            this.rpiMax?.setText(rrMax.toString()).toString()
            this.rpiMax?.setText(rrMin.toString()).toString()
            this.hrv?.setText(hrv.toString()).toString()
        })


    }

    override fun onEcgDuration(p0: Int, p1: Boolean) {

        this@ecg_2.runOnUiThread(java.lang.Runnable {
            tiempo = p0.toDouble()
            graph?.onDataChanged(true,true)
            duracion?.setText(p0.toString()).toString()
        })
        graph?.onDataChanged(true,true)
        duracion?.setText(p0.toString()).toString()

        if(p1){
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
            BleManager.getInstance().stopMeasure(MeasureType.TYPE_ECG,this)
            enMedicion=false
        }

    }

    override fun handleMessage(p0: Message): Boolean {
        return true
    }

    override fun algoData(key: Int, p1: Int) {

    }

    override fun algoData(p0: Int, p1: Int, p2: Int) {

    }
}