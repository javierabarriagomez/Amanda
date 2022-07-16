package com.saludencamino.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.linktop.MonitorDataTransmissionManager
import com.linktop.infs.OnBpResultListener
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBleWriteResponse
import com.mintti.visionsdk.ble.callback.IBpResultListener
import com.mintti.visionsdk.ble.callback.ISpo2ResultListener

class PresionSanguinea_2 : AppCompatActivity(), IBleWriteResponse, Handler.Callback,
    IBpResultListener,OnBpResultListener {

    private var sistolica: TextView? = null
    private var diastolica: TextView?= null
    private var ritmoCardiaco: TextView? = null
    private var botonMedicion: ImageButton? = null
    private var barra_sis: ProgressBar? = null
    private var barra_dias: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presion_sanguinea_2)

        sistolica = findViewById(R.id.presionSistolica)
        diastolica = findViewById(R.id.presionDiastolica)
        ritmoCardiaco = findViewById(R.id.ritmoCardico)
        botonMedicion = findViewById(R.id.imageButton5)
        barra_sis = findViewById(R.id.progressBar2)
        barra_dias = findViewById(R.id.progressBar3)


    }


    fun tomarMedicion(view: View){
        println("Empezando medicion")

        if((this.application as App).version != 1){
            BleManager.getInstance().setBpResultListener(this)
            BleManager.getInstance().startMeasure(MeasureType.TYPE_BP,this)
        }else{
            MonitorDataTransmissionManager.getInstance().setOnBpResultListener(this);
            MonitorDataTransmissionManager.getInstance().startMeasure(com.linktop.whealthService.MeasureType.BP)
        }

        botonMedicion?.setImageResource(R.drawable.iniciar_medicion)


    }

    override fun onWriteSuccess() {
        println("WRITE SUCCESS")
    }

    override fun onWriteFailed() {
        println("WRITE FAILED")
    }

    override fun handleMessage(p0: Message): Boolean {

        return true
    }

    override fun onBpResult(sys: Int, dias: Int, hr: Int) {
        if((this.application as App).version != 1) {
            BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
        }else{
            MonitorDataTransmissionManager.getInstance().stopMeasure()
        }
        sistolica?.setText(sys.toString()).toString()
        diastolica?.setText(dias.toString()).toString()
        ritmoCardiaco?.setText(hr.toString()).toString()
        barra_sis?.setProgress(sys)
        barra_dias?.setProgress(dias)



    }

    override fun onBpResultError() {
        println("Error de mierda")
    }

    override fun onLeakError(p0: Int) {
        println("Otro error de mierda")
    }

    override fun onLeadError() {
        BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
    }

    override fun onBpError() {
        BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
    }
}