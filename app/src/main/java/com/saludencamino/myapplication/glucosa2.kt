package com.saludencamino.myapplication

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.internal.ContextUtils.getActivity
import com.linktop.MonitorDataTransmissionManager
import com.linktop.constant.BgPagerCaliCode
import com.linktop.infs.OnBgResultListener
import com.linktop.whealthService.task.BgTask
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.bean.BgEvent
import com.mintti.visionsdk.ble.bean.MeasureType
import com.mintti.visionsdk.ble.callback.IBgResultListener
import com.mintti.visionsdk.ble.callback.IBleWriteResponse

class glucosa2 : AppCompatActivity(), IBgResultListener, Handler.Callback,
    IBleWriteResponse, OnBgResultListener {
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
    private var ayuna: SwitchCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glucosa2)
        resultado = findViewById(R.id.glucosaResultado)
        resultadoBarra = findViewById(R.id.resultadoBarra)
        resultadoBarra2 = findViewById(R.id.resultadoBarra2)
        mensaje= findViewById(R.id.mensaje)
        botonMedicionGlucosa = findViewById(R.id.botonMedicionGlucosa)
        ayuna = findViewById(R.id.switch1)
    }

    fun goBack(view: View){
        super.onBackPressed()
    }

    fun iniciarMedicion(view: View){
        //reset values
        resultado?.text= "0"
        resultadoBarra?.setProgress(0)
        resultadoBarra2?.setProgress(0)
        if(!enMedicion){
            botonMedicionGlucosa?.setImageResource(R.drawable.detener_medicion)

            ayuna?.isClickable=false




            if((this.application as App).version != 1) {
                BleManager.getInstance().setBgResultListener(this)
                BleManager.getInstance().startMeasure(MeasureType.TYPE_BG,this)
            }else{
                MonitorDataTransmissionManager.getInstance().setOnBgResultListener(this)
                MonitorDataTransmissionManager.getInstance().setBgPagerCaliCode(BgPagerCaliCode.C16);
                MonitorDataTransmissionManager.getInstance().startMeasure(com.linktop.whealthService.MeasureType.BG)
            }

            enMedicion=true
        }else{
            ayuna?.isClickable=true
            mensaje?.setText("").toString()
            botonMedicionGlucosa?.setImageResource(R.drawable.iniciar_medicion)
            if((this.application as App).version != 1) {
                BleManager.getInstance().setBgResultListener(this)
                BleManager.getInstance().stopMeasure(MeasureType.TYPE_BG,this)
            }else{
                MonitorDataTransmissionManager.getInstance().stopMeasure()
            }

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

        val prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )
        prefs.edit().putFloat("glucosa",res.toFloat()).apply();
        prefs.edit().putBoolean("ayuna",ayuna?.isChecked == true ).apply()
        prefs.edit().putBoolean("DatosCapturados",true).apply();



        enMedicion=false

        val valor = String.format("%.1f",res)


        this@glucosa2.runOnUiThread(java.lang.Runnable {
            resultado?.setText(valor).toString()
            resultadoBarra?.setProgress(res.toInt(),true)
            resultadoBarra2?.setProgress(res.toInt(),true)
            mensaje?.setText("Examen finalizado").toString()
            Toast.makeText(this, "Examen finalizado", Toast.LENGTH_LONG).show()
            botonMedicionGlucosa?.setImageResource(R.drawable.iniciar_medicion)
        })
    }

    override fun handleMessage(p0: Message): Boolean {
        return true
    }

    override fun onWriteSuccess() {

    }

    override fun onWriteFailed() {

    }

    override fun onBgEvent(eventId: Int, Object: Any?) {
        when(eventId){
            1 ->{
                this@glucosa2.runOnUiThread(java.lang.Runnable {
                    mensaje?.setText("El test ha sido insertado en el dispositivo").toString()
                })
            }
            BgTask.EVENT_PAGER_READ ->{
                this@glucosa2.runOnUiThread(java.lang.Runnable {
                    mensaje?.setText("Esperando muestra de sangre").toString()
                })

            }
        BgTask.EVENT_BLOOD_SAMPLE_DETECTING ->{
            this@glucosa2.runOnUiThread(java.lang.Runnable {
                mensaje?.setText("Analizando muestra").toString()
            })
        }
        BgTask.EVENT_TEST_RESULT ->{
            var resa = Object as Double;
            var res = resa*100/7.0;

            val prefs = getSharedPreferences(
                "com.saludencamino.myapplication", Context.MODE_PRIVATE
            )

            prefs.edit().putBoolean("ayuna",ayuna?.isChecked == true ).apply()

            prefs.edit().putBoolean("DatosCapturados",true).apply();


            enMedicion=false
            val mgdl = resa
            val valor = String.format("%.1f",mgdl)
            prefs.edit().putFloat("glucosa",valor.toFloat()).apply()
            this@glucosa2.runOnUiThread(java.lang.Runnable {
                resultado?.setText(valor).toString()
                resultadoBarra?.setProgress(res.toInt(),true)
                resultadoBarra2?.setProgress(res.toInt(),true)
                Toast.makeText(this, "Examen finalizado", Toast.LENGTH_LONG).show()
                botonMedicionGlucosa?.setImageResource(R.drawable.iniciar_medicion)
                mensaje?.setText("Examen finalizado").toString()
            })
        }
        }
    }

    override fun onBgException(p0: Int) {
        when(p0){
            0 ->{ //BgTask.EXCEPTION_PAGER_OUT
                mensaje?.setText("El test no esta insertado en el dispositivo").toString()
            }
            BgTask.EXCEPTION_TIMEOUT_FOR_CHECK_PAGER_IN ->{
                mensaje?.setText("Error porfavor volver a intentar").toString()
            }
            BgTask.EXCEPTION_PAPER_USED ->{
                mensaje?.setText("El Test ya fue utilizado").toString()
            }
            BgTask.EXCEPTION_TESTING_PAPER_OUT ->{
                mensaje?.setText("El test fue retirado del dispositivo antes de tiempo.").toString()
            }
            BgTask.EXCEPTION_TIMEOUT_FOR_DETECT_BLOOD_SAMPLE ->{
                mensaje?.setText("Error porfavor volver a intentar").toString()
            }
        }
        botonMedicionGlucosa?.setImageResource(R.drawable.iniciar_medicion)
        enMedicion=false
    }
}