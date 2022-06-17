package com.saludencamino.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.BgEvent
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBgResultListener
import com.mintti.visionsdk.ble.callback.IBleWriteResponse

class glucosa2 : AppCompatActivity(), IBgResultListener, Handler.Callback,
    IBleWriteResponse {
    private val MSG_ADJUST_FAILED = 2
    private val MSG_WAIT_INSERT = 3
    private val MSG_WAIT_DRIP = 4
    private val MSG_DRIP_BLOOD = 5
    private val MSG_BG_MEASURE_OVER = 6
    private val MSG_PAPER_USED = 7
    private val BUNDLE_BG_RESULT = "bg_result"
    private var resultado: TextView? = null
    private var resultadoBarra: ProgressBar? =null
    private var resultadoBarra2: ProgressBar? =null
    private var mensaje: TextView? =null
    private var botonMedicionGlucosa: ImageButton? = null
    private var enMedicion:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glucosa2)
        resultado = findViewById(R.id.glucosaResultado)
        resultadoBarra = findViewById(R.id.resultadoBarra)
        resultadoBarra2 = findViewById(R.id.resultadoBarra2)

        mensaje= findViewById(R.id.mensaje)
        botonMedicionGlucosa = findViewById(R.id.botonMedicionGlucosa)
    }

    fun iniciarMedicion(view: View){
        if(!enMedicion){
            botonMedicionGlucosa?.setImageResource(R.drawable.detener_medicion)
            BleManager.getInstance().setBgResultListener(this)
            BleManager.getInstance().startMeasure(MeasureType.TYPE_BG,this)
            enMedicion=true
        }else{
            mensaje?.setText("").toString()
            botonMedicionGlucosa?.setImageResource(R.drawable.iniciar_medicion)
            BleManager.getInstance().setBgResultListener(this)
            BleManager.getInstance().stopMeasure(MeasureType.TYPE_BG,this)
            enMedicion=false

        }


    }

    override fun onBgEvent(p0: BgEvent?) {
        when(p0){
            BgEvent.BG_EVENT_CALIBRATION_FAILED -> {
                mensaje?.setText("Error de calibración").toString()

            }
            BgEvent.BG_EVENT_WAIT_PAGER_INSERT -> {
                mensaje?.setText("Esperando que inserte el test").toString()
            }
            BgEvent.BG_EVENT_WAIT_DRIP_BLOOD -> {
                mensaje?.setText("Esperando muestra de sangre").toString()
            }
            BgEvent.BG_EVENT_BLOOD_SAMPLE_DETECTING-> {
                mensaje?.setText("Analizando muestra").toString()
            }
            BgEvent.BG_EVENT_PAPER_USED-> {
                mensaje?.setText("Test ya utilizado, utilice uno nuevo").toString()
            }
        }
    }

    override fun onBgResult(p0: Double) {
        var res = p0*100/7.0;


        resultado?.setText(p0.toString()).toString()
        resultadoBarra?.setProgress(res.toInt(),true)
        resultadoBarra2?.setProgress(res.toInt(),true)
        mensaje?.setText("Examen finalizado").toString()
        enMedicion=false
        botonMedicionGlucosa?.setImageResource(R.drawable.iniciar_medicion)
    }

    override fun handleMessage(p0: Message): Boolean {
        return true
    }

    override fun onWriteSuccess() {

    }

    override fun onWriteFailed() {

    }
}