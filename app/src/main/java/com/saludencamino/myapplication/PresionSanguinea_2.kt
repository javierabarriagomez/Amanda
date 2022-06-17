package com.saludencamino.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.TextView
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBleWriteResponse
import com.mintti.visionsdk.ble.callback.IBpResultListener
import com.mintti.visionsdk.ble.callback.ISpo2ResultListener

class PresionSanguinea_2 : AppCompatActivity(), IBleWriteResponse, Handler.Callback,
    IBpResultListener {

    private var sistolica: TextView? = null
    private var diastolica: TextView?= null
    private var ritmoCardiaco: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presion_sanguinea_2)

        sistolica = findViewById(R.id.presionSistolica)
        diastolica = findViewById(R.id.presionDiastolica)
        ritmoCardiaco = findViewById(R.id.ritmoCardico)

    }


    fun tomarMedicion(view: View){
        println("Empezando medicion")
        BleManager.getInstance().setBpResultListener(this)
        BleManager.getInstance().startMeasure(MeasureType.TYPE_BP,this)

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
        BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
        sistolica?.setText(sys.toString()).toString()
        diastolica?.setText(dias.toString()).toString()
        ritmoCardiaco?.setText(hr.toString()).toString()

    }

    override fun onLeadError() {
        BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
    }

    override fun onBpError() {
        BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
    }
}