package com.saludencamino.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.linktop.MonitorDataTransmissionManager
import com.linktop.infs.OnEcgResultListener
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBleWriteResponse
import com.mintti.visionsdk.ble.callback.IEcgResultListener
import com.saludencamino.myapplication.view.PPGDrawWave
import com.saludencamino.myapplication.view.WaveSurfaceView
import com.saludencamino.myapplication.view.WaveView
import java.sql.Time
import java.util.TimerTask

import java.util.Timer




class ecg_2 : AppCompatActivity() , IBleWriteResponse, IEcgResultListener, Handler.Callback,
    ISmctAlgoCallback,OnEcgResultListener{

    private var botonMedicion: ImageButton? = null
    private var enMedicion:Boolean = false

    private var rpiMax: TextView? = null
    private var rpiMin: TextView? = null

    private var respiracion: TextView? =null
    //private var graph: GraphView? = null
    //private var series: LineGraphSeries<DataPoint>? = null
    private var graph: WaveSurfaceView? = null
    private var oxWave: PPGDrawWave? = null
    private var tiempo = 0.0;
    private var prefs: SharedPreferences? = null
    private var tiempoTimer = 0;
    private var timer: Timer? = null;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )
        setContentView(R.layout.activity_ecg2)
        botonMedicion=findViewById(R.id.botonMedicionGlucosa)
        //ritmoCardiaco= findViewById(R.id.ritmoCardiaco)
        rpiMax=findViewById(R.id.rpiMax)
        rpiMin=findViewById(R.id.rpiMin)
        //duracion=findViewById(R.id.duracion)
        //hrv=findViewById(R.id.hrv)
        //respiracion=findViewById(R.id.respiracion)
        oxWave = PPGDrawWave()
        graph = findViewById(R.id.bo_wave_view)

        graph?.setDrawWave(oxWave)

        tiempo = 0.0
        tiempoTimer = 0

    }

    fun iniciarMedicion(view: View){

        if(!enMedicion){
            botonMedicion?.setImageResource(R.drawable.detener_medicion)

            if((this.application as App).version != 1) {
                BleManager.getInstance().setEcgResultListener(this)
                BleManager.getInstance().startMeasure(MeasureType.TYPE_ECG,this)
            }else{
                MonitorDataTransmissionManager.getInstance().setOnEcgResultListener(this)
                MonitorDataTransmissionManager.getInstance().startMeasure(com.linktop.whealthService.MeasureType.ECG)

                this.timer = Timer()
                this.timer!!.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            tiempoTimer ++;
                            this@ecg_2.runOnUiThread(java.lang.Runnable {
                                //duracion?.text =tiempoTimer.toString();

                            })
                        }
                    },
                    0,
                    1000
                )
            }


            oxWave?.clear()
            tiempo = 0.0
            tiempoTimer=0;
            //duracion?.text ="0";

            enMedicion=true

        }else{
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
            if(this.timer != null){
                this.timer!!.cancel();
            }
            if((this.application as App).version != 1) {
                BleManager.getInstance().stopMeasure(MeasureType.TYPE_ECG,this)
            }else{
                MonitorDataTransmissionManager.getInstance().stopMeasure()
            }

            enMedicion=false
        }


    }

    override fun onWriteSuccess() {

    }

    override fun onWriteFailed() {

    }

    override fun onDrawWave(p0: Int) {
        oxWave?.addData(p0)


    }

    override fun onHeartRate(p0: Int) {
        prefs?.edit()?.putInt("hr",p0)?.apply();
        /*this@ecg_2.runOnUiThread(java.lang.Runnable {
            ritmoCardiaco?.setText(p0.toString()).toString()
        })*/

    }

    override fun onRespiratoryRate(p0: Int) {
        prefs?.edit()?.putInt("rr",p0)?.apply();
        this@ecg_2.runOnUiThread(java.lang.Runnable {
            respiracion?.setText(p0.toString()).toString()
        })



    }

    override fun onEcgResult(rrMax: Int, rrMin: Int, hrv: Int) {

        prefs?.edit()?.putInt("rrmax",rrMax)?.apply();
        prefs?.edit()?.putInt("rrmin",rrMin)?.apply();
        prefs?.edit()?.putInt("hrv",hrv)?.apply();
        prefs?.edit()?.putBoolean("DatosCapturados",true)?.apply();
        prefs?.edit()?.putInt("duration",tiempoTimer)?.apply();



        this@ecg_2.runOnUiThread(java.lang.Runnable {
            this.rpiMax?.setText(rrMax.toString()).toString()
            this.rpiMin?.setText(rrMin.toString()).toString()
            //this.hrv?.setText(hrv.toString()).toString()
        })


    }

    override fun onEcgDuration(p0: Int, p1: Boolean) {

        if(p0 >= 40){
            prefs?.edit()?.putInt("duration",tiempoTimer)?.apply();
            BleManager.getInstance().stopMeasure(MeasureType.TYPE_ECG,this)
            this@ecg_2.runOnUiThread(java.lang.Runnable {
                Toast.makeText(applicationContext, "Examen Finalizado", Toast.LENGTH_SHORT).show()
                botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
            })
            enMedicion=false;
            return;
        }

        this@ecg_2.runOnUiThread(java.lang.Runnable {
            tiempo = p0.toDouble()
            //duracion?.setText(p0.toString()).toString()
        })
        //duracion?.setText(p0.toString()).toString()
        prefs?.edit()?.putInt("duration",p0)?.apply();
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

    override fun onDrawWave(p0: Any?) {

        var data = p0 as Int
        oxWave?.addData(data)


    }

    fun goBack(view: View){
        super.onBackPressed()

    }


    override fun onSignalQuality(p0: Int) {
        return
    }

    override fun onECGValues(key: Int, value: Int) {
        this@ecg_2.runOnUiThread(java.lang.Runnable {



            prefs?.edit()?.putBoolean("DatosCapturados",true)?.apply();
            when(key){
                0 ->{ //RRI MAX
                    this.rpiMax?.setText(value.toString()).toString()
                    prefs?.edit()?.putInt("rrmax",value)?.apply();

                }
                1 ->{ //RRI MIN
                    this.rpiMin?.setText(value.toString()).toString()
                    prefs?.edit()?.putInt("rrmin",value)?.apply();

                }
                2 ->{ //HR
                    //this.ritmoCardiaco?.setText(value.toString()).toString()
                    prefs?.edit()?.putInt("hr",value)?.apply();
                }
                3 ->{ //HRV
                    //this.hrv?.setText(value.toString()).toString()
                    prefs?.edit()?.putInt("hrv",value)?.apply();
                }
                4 ->{ //MOOD
                    prefs?.edit()?.putInt("mood",value)?.apply();
                }
                5 ->{ //RR
                    respiracion?.setText(value.toString()).toString()
                    prefs?.edit()?.putInt("rr",value)?.apply();
                }
            }
        })


    }

    override fun onEcgDuration(p0: Long) {
        enMedicion = false;
        this.timer!!.cancel();
        this@ecg_2.runOnUiThread(java.lang.Runnable {
            prefs?.edit()?.putInt("duration",p0.toInt())?.apply();
            //duracion?.setText(p0.toString()).toString()
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
            Toast.makeText(applicationContext, "Examen Finalizado", Toast.LENGTH_SHORT).show()

        })
/*botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
enMedicion = false

MonitorDataTransmissionManager.getInstance().stopMeasure()


if(p0 >= 40){
    MonitorDataTransmissionManager.getInstance().stopMeasure()
}

this@ecg_2.runOnUiThread(java.lang.Runnable {
    tiempo = p0.toDouble()
    graph?.onDataChanged(true,true)
    duracion?.setText(p0.toString()).toString()

})
graph?.onDataChanged(true,true)
duracion?.setText(p0.toString()).toString()*/


}

}