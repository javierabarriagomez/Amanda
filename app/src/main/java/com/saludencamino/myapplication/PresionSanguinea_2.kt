package com.saludencamino.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.Context
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
    private var enMedicion:Boolean = false
    private var progressOverlay: View? = null
    private var textenmedicion: TextView? = null



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presion_sanguinea_2)
        sistolica = findViewById(R.id.presionSistolica)
        diastolica = findViewById(R.id.presionDiastolica)
        ritmoCardiaco = findViewById(R.id.ritmoCardico)
        botonMedicion = findViewById(R.id.imageButton5)
        barra_sis = findViewById(R.id.progressBar2)
        barra_dias = findViewById(R.id.progressBar3)
        progressOverlay = findViewById(R.id.progress_overlay)
        textenmedicion = findViewById(R.id.textView16)



    }





    fun tomarMedicion(view: View){
        println("Empezando medicion")

        //reset values
        sistolica?.text="0"
        diastolica?.text="0"
        barra_sis?.setProgress(0)
        barra_dias?.setProgress(0)
        if(!enMedicion){
            progressOverlay?.visibility = View.VISIBLE;
            textenmedicion?.visibility = View.VISIBLE;
            if((this.application as App).version != 1){

                BleManager.getInstance().setBpResultListener(this)
                BleManager.getInstance().startMeasure(MeasureType.TYPE_BP,this)
            }else{
                MonitorDataTransmissionManager.getInstance().setOnBpResultListener(this);
                MonitorDataTransmissionManager.getInstance().startMeasure(com.linktop.whealthService.MeasureType.BP)
            }
            enMedicion = true
            this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
                Toast.makeText(this, "Iniciando medición", Toast.LENGTH_LONG).show()
            })
            botonMedicion?.setImageResource(R.drawable.detener_medicion)
        }else{
            if((this.application as App).version != 1){
                BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
            }else{
                MonitorDataTransmissionManager.getInstance().stopMeasure()
            }
            enMedicion=false;


            this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
                ocultarOverlay()
                Toast.makeText(this, "Se detuvo la medición", Toast.LENGTH_LONG).show()
            })
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)

        }
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

    fun goBack(view: View){
        super.onBackPressed()
    }
    fun ocultarOverlay(){
        progressOverlay?.visibility = View.GONE;
        textenmedicion?.visibility = View.GONE;

    }

    override fun onBpResult(sys: Int, dias: Int, hr: Int) {

        if((this.application as App).version != 1) {

            BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
        }else{
            MonitorDataTransmissionManager.getInstance().stopMeasure()
        }

        val prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )

        prefs.edit().putInt("presion_sistolica",sys).apply();
        prefs.edit().putInt("presion_diastolica",dias).apply();
        prefs.edit().putInt("presion_ritmo",hr).apply();
        prefs.edit().putBoolean("DatosCapturados",true).apply();


        sistolica?.setText(sys.toString()).toString()
        diastolica?.setText(dias.toString()).toString()
        ritmoCardiaco?.setText(hr.toString()).toString()
        barra_sis?.setProgress(sys)
        barra_dias?.setProgress(dias)
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            ocultarOverlay()
            Toast.makeText(this, "Examen finalizado", Toast.LENGTH_LONG).show()
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)

        })


    }

    override fun onBpResultError() {

        ocultarOverlay()
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Error, porfavor intente denuevo", Toast.LENGTH_LONG).show()
        })
        enMedicion=false
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
        })
    }

    override fun onLeakError(p0: Int) {
       ocultarOverlay()
        println("ERROR")
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Error, porfavor intente denuevo", Toast.LENGTH_LONG).show()
        })
        enMedicion=false
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
        })
    }

    override fun onLeadError() {
        ocultarOverlay()
        BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
        botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Error, porfavor intente denuevo", Toast.LENGTH_LONG).show()
        })
        enMedicion=false
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
        })
    }

    override fun onBpError() {
        ocultarOverlay()
        BleManager.getInstance().stopMeasure(MeasureType.TYPE_BP,this)
        botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Error, porfavor intente denuevo", Toast.LENGTH_LONG).show()
        })
        enMedicion=false
        this@PresionSanguinea_2.runOnUiThread(java.lang.Runnable {
            botonMedicion?.setImageResource(R.drawable.iniciar_medicion)
        })
    }
}