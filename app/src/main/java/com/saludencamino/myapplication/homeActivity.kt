package com.saludencamino.myapplication

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.text.TextUtils

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.linktop.DeviceType
import com.linktop.MonitorDataTransmissionManager
import com.linktop.infs.OnBleConnectListener
import com.linktop.whealthService.BleDevManager
import com.mintti.visionsdk.ble.BleDevice
import com.mintti.visionsdk.ble.BleManager
import com.mintti.visionsdk.ble.callback.IBleConnectionListener
import com.mintti.visionsdk.ble.callback.IBleScanCallback
import org.json.JSONObject

import java.util.*

import android.widget.*
import java.text.SimpleDateFormat
import android.bluetooth.BluetoothAdapter
import android.os.*
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.mintti.visionsdk.ble.callback.IDeviceBatteryCallback
import kotlinx.coroutines.delay
import kotlin.concurrent.timer


class homeActivity : AppCompatActivity(),IBleConnectionListener,Handler.Callback, MonitorDataTransmissionManager.OnServiceBindListener,OnBleConnectListener,IDeviceBatteryCallback{

    private val deviceList = mutableListOf<BleDevice>()
    private var mBleDevice: BleDevice? = null
    private var oldBleDevice: BluetoothDevice? = null
    private var isScanning: Boolean = false;
    private var mHandler: Handler? = null;
    private var botonConectar: ImageButton? = null;
    private var nombreTextView: TextView? = null;
    private var imageView: ImageView? = null;
    protected var mHcService: HcService? = null
    private var bateria: TextView? = null

    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()
        actualizarGui()
        if (mBleDevice != null) {
            BleManager.getInstance().getDeviceBattery(this)
        }else if (oldBleDevice != null){
            var bat = MonitorDataTransmissionManager.getInstance().batteryValue
            println("$bat%")
            this@homeActivity.runOnUiThread(java.lang.Runnable {
                bateria?.text = "Bateria: $bat%"
            })

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {


            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (mBluetoothAdapter == null) {
                this@homeActivity.runOnUiThread(java.lang.Runnable {
                    android.widget.Toast.makeText(
                        this,
                        "Su dispositivo no soporta bluetooth.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                })
            } else if (!mBluetoothAdapter.isEnabled) {
                this@homeActivity.runOnUiThread(java.lang.Runnable {
                    android.widget.Toast.makeText(
                        this,
                        "Debe habilitar el bluetooth.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                })
            } else {
                // Bluetooth is enabled
            }


        //绑定服务，
        // 类型是 HealthMonitor（HealthMonitor健康检测仪），
        MonitorDataTransmissionManager.getInstance().bind(
            DeviceType.HealthMonitor, this,
            this
        )
        BleManager.getInstance().init(this);


        requestPermission()
        mHandler = Handler(getMainLooper());
        super.onCreate(savedInstanceState)
        BleManager.getInstance().addConnectionListener(this)

        var mBleDevManager = BleDevManager()
        mBleDevManager.initHC(this)

        setContentView(R.layout.activity_home)
        botonConectar = findViewById(R.id.botonConectar)
        nombreTextView = findViewById(R.id.nombreView)
        imageView = findViewById<ImageView>(R.id.imageView)
        bateria = findViewById(R.id.bateria)


        val prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )

        val nombreUsuario = prefs.getString("nombreUsuario","NAN")

        nombreTextView?.text="Hola $nombreUsuario"

        /*var foto = prefs.getString("FotoUsuario","NAN")
        if(foto != "NAN"){
            foto = foto?.substring(foto.indexOf(",")+1)
            println(foto);
            val imageBytes = Base64.decode(foto,  Base64.DEFAULT)
            println(imageBytes)
            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            imageView?.setImageBitmap(image)
        }*/
        actualizarGui()

    }

    fun iniciarBusqueda(view: View){
        if(ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                ){

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
                {
                    this@homeActivity.runOnUiThread(java.lang.Runnable {
                        android.widget.Toast.makeText(this, "Debe habilitar la busqueda de dispositivos", android.widget.Toast.LENGTH_LONG).show()
                    })
                }



        }else {

            /*val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            //if (mBluetoothAdapter == null) {
                this@homeActivity.runOnUiThread(java.lang.Runnable {
                    android.widget.Toast.makeText(this, "Su dispositivo no soporta bluetooth.", android.widget.Toast.LENGTH_SHORT).show()
                })
            } else if (!mBluetoothAdapter.isEnabled) {
                this@homeActivity.runOnUiThread(java.lang.Runnable {
                    android.widget.Toast.makeText(this, "Debe habilitar el bluetooth.", android.widget.Toast.LENGTH_SHORT).show()
                })
            } else {
                // Bluetooth is enabled

*/
                if (isScanning == true && oldBleDevice == null && mBleDevice == null) {
                    isScanning = false
                    botonConectar?.setImageResource(R.drawable.conectar)
                    BleManager.getInstance().stopScan()
                    MonitorDataTransmissionManager.getInstance().scan(false);
                    return
                }

                if (oldBleDevice != null || mBleDevice != null) {
                    if (oldBleDevice != null) {
                        MonitorDataTransmissionManager.getInstance().disConnectBle()
                        botonConectar?.setImageResource(R.drawable.conectar)
                        this@homeActivity.runOnUiThread(java.lang.Runnable {
                            Toast.makeText(this, "Desconectado", Toast.LENGTH_LONG).show()
                            bateria?.text=""
                        })

                        (this.application as App).version = 0
                        oldBleDevice = null
                    }
                    if (mBleDevice != null) {
                        BleManager.getInstance().stopScan()
                        BleManager.getInstance().disconnect()
                        this@homeActivity.runOnUiThread(java.lang.Runnable {
                            Toast.makeText(this, "Desconectado", Toast.LENGTH_LONG).show()
                            bateria?.text=""
                        })
                    }
                } else {
                    if (!isScanning) {
                        botonConectar?.setImageResource(R.drawable.buscando)
                        isScanning = true
                        MonitorDataTransmissionManager.getInstance().scan(true);
                        MonitorDataTransmissionManager.getInstance()
                            .setOnBleConnectListener(this)
                        startScan();

                    } else {
                        isScanning = false
                        botonConectar?.setImageResource(R.drawable.conectar)
                        MonitorDataTransmissionManager.getInstance().scan(false);
                        BleManager.getInstance().stopScan();

                    }

                    /*AlertDialog.Builder(this)
                        .setTitle("Version de Amanda")
                        .setMessage("Seleccione su versión de Amanda")
                        .setNegativeButton("1.0") { dialog, which ->

                            if (!isScanning) {
                                botonConectar?.setImageResource(R.drawable.buscando)
                                isScanning = true
                                MonitorDataTransmissionManager.getInstance().scan(true);
                                MonitorDataTransmissionManager.getInstance()
                                    .setOnBleConnectListener(this)

                            } else {
                                isScanning = false
                                botonConectar?.setImageResource(R.drawable.conectar)
                                MonitorDataTransmissionManager.getInstance().scan(false);

                            }

                        }
                        .setPositiveButton(
                            "2.0"
                        ) { dialog, which ->

                            if (!isScanning) {
                                botonConectar?.setImageResource(R.drawable.buscando)
                                isScanning = true
                                startScan();
                            } else {
                                isScanning = false
                                botonConectar?.setImageResource(R.drawable.desconectar)
                                BleManager.getInstance().stopScan();
                            }
                        }
                        .show()*/
                }
            //}
        }
    }
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }
    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    fun actualizarGui(){
        val prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )

        //Revisar estados!
        if(prefs.getBoolean("DatosCapturados",false)){
            //temperatura
            val temperatura = prefs.getFloat("temperatura", -1F)
            if(temperatura != -1F) {
                findViewById<ImageButton>(R.id.imageButton8)?.setImageResource(R.drawable.temperatura_listo)
            }else{
                findViewById<ImageButton>(R.id.imageButton8)?.setImageResource(R.drawable.temperatura)
            }

            //Saturacion
            val spo2 = prefs.getFloat("saturacion_spo2",-1F)
            if(spo2 != -1F){
                findViewById<ImageButton>(R.id.imageButton9)?.setImageResource(R.drawable.saturacion_listo)
            }else{
                findViewById<ImageButton>(R.id.imageButton9)?.setImageResource(R.drawable.sangre2)
            }
            //Glucosa
            val glucosa = prefs.getFloat("glucosa",-1F)
            if(glucosa != -1F){
                findViewById<ImageButton>(R.id.imageButton10)?.setImageResource(R.drawable.glucosa_listo)
            }else{
                findViewById<ImageButton>(R.id.imageButton10)?.setImageResource(R.drawable.glucosa)
            }
            //Presion
            val sis = prefs.getInt("presion_sistolica",-1)
            val dias = prefs.getInt("presion_diastolica",-1)

            if(sis != -1 || dias != -1){
                findViewById<ImageButton>(R.id.imageButton7)?.setImageResource(R.drawable.presion_listo)
            }else{
                findViewById<ImageButton>(R.id.imageButton7)?.setImageResource(R.drawable.presion)
            }

            //ECG
            val rrmin = prefs.getInt("rrmin",-1)
            val rrmax = prefs.getInt("rrmax",-1)
            if(rrmin != -1 || rrmax != -1){
                findViewById<ImageButton>(R.id.imageButton11)?.setImageResource(R.drawable.ecg_listo)
            }else{
                findViewById<ImageButton>(R.id.imageButton11)?.setImageResource(R.drawable.ecg)
            }


        }
    }
    private fun startOldScan() {
        isScanning = true
        BleManager.getInstance().startScan(object : IBleScanCallback {

            override fun onScanResult(bleDevice: BleDevice) {
                if (TextUtils.isEmpty(bleDevice.name) || TextUtils.isEmpty(bleDevice.mac)
                    || !bleDevice.name.startsWith("HC02")
                ) {
                    return
                }
                var hasDevice = false
                for (device in deviceList) {
                    if (device.mac == bleDevice.mac) {
                        hasDevice = true
                        device.rssi = bleDevice.rssi
                        mBleDevice = device
                    }
                }
                if (!hasDevice) {
                    deviceList.add(bleDevice)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                isScanning = false
                BleManager.getInstance().stopScan();
                println("FAILED")
            }
        })
    }

    private fun startScan() {
        isScanning = true
        BleManager.getInstance().startScan(object : IBleScanCallback {

            override fun onScanResult(bleDevice: BleDevice) {
                if (TextUtils.isEmpty(bleDevice.name) || TextUtils.isEmpty(bleDevice.mac)
                    || bleDevice.name != "Mintti-Vision"
                ) {
                    return
                }
                var hasDevice = false
                for (device in deviceList) {
                    if (device.mac == bleDevice.mac) {
                        hasDevice = true
                        device.rssi = bleDevice.rssi
                        BleManager.getInstance().connect(device)
                        mBleDevice = device
                    }
                }
                if (!hasDevice) {
                    deviceList.add(bleDevice)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                isScanning = false
                BleManager.getInstance().stopScan();
                println("FAILED")
            }
        })
    }
    override fun onServiceBind() {
        println("LALAS")
    }

    override fun onServiceUnbind() {
        println("LALOS")
    }


    fun mensajeSinConexion(){
        this@homeActivity.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Debe conectar el dispositivo primero", Toast.LENGTH_LONG).show()
        })
    }


    fun presion(view: View) {
        val intent = Intent(this,PresionSanguinea::class.java)
        if(mBleDevice !=null || oldBleDevice != null){
            startActivity(intent)
        }else{
            mensajeSinConexion()
        }
        // Do something in response to button click
    }
    fun temperatura(view: View) {
        val intent = Intent(this,TemperaturaCorporal::class.java)
        if(mBleDevice !=null || oldBleDevice != null){
            startActivity(intent)
        }else{
            mensajeSinConexion()
        }

        // Do something in response to button click
    }

    fun logout(view: View) {

        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("Seguro que desea cerrar sesión")
            .setNegativeButton("Cancelar") { dialog, which ->
            }
            .setPositiveButton("Seguir"){dialog,which->
                val prefs = getSharedPreferences(
                    "com.saludencamino.myapplication", Context.MODE_PRIVATE
                )

                prefs.edit().remove("sesion_iniciada").commit()
                val intent = Intent(this,MainActivity::class.java)

                    startActivity(intent)
            }
            .show()
        // Do something in response to button click
    }
    fun saturacion(view: View) {
        val intent = Intent(this,Saturacion::class.java)
        if(mBleDevice !=null || oldBleDevice != null){
            startActivity(intent)
        }else{
            mensajeSinConexion()
        }
        // Do something in response to button click
    }
    fun glucosa(view: View) {
        val intent = Intent(this,glucosa::class.java)
        if(mBleDevice !=null || oldBleDevice != null){
            startActivity(intent)
        }else{
            mensajeSinConexion()
        }
        // Do something in response to button click
    }
    fun ecg(view: View) {
        val intent = Intent(this,ecg::class.java)
        if(mBleDevice !=null || oldBleDevice != null){
            startActivity(intent)
        }else{
            mensajeSinConexion()
        }
        // Do something in response to button click
    }

    override fun onConnectSuccess(p0: String?) {
        BleManager.getInstance().stopScan()
        botonConectar?.setImageResource(R.drawable.desconectar)
        this@homeActivity.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Conectado correctamente", Toast.LENGTH_LONG).show()
        })
        isScanning=false
        (this.application as App).version = 2
        Handler(Looper.getMainLooper()).postDelayed(
            {
                print("Ejecutando weas")
                BleManager.getInstance().getDeviceBattery(this)
            },
            2000 // value in milliseconds
        )




    }


    override fun onConnectFailed(p0: String?, p1: Int) {
        BleManager.getInstance().stopScan()
        mBleDevice=null
        this@homeActivity.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Error al conectar intente nuevamente", Toast.LENGTH_LONG).show()
        })

    }

    override fun onDisconnected(p0: String?, p1: Boolean, p2: Int) {
        botonConectar?.setImageResource(R.drawable.conectar)
        this@homeActivity.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this, "Desconectado", Toast.LENGTH_LONG).show()
        })
        (this.application as App).version = 0
        mBleDevice = null
        isScanning=false

    }

    override fun onServicesDiscovered(p0: String?, p1: MutableList<BluetoothGattService>?) {

    }

    override fun handleMessage(p0: Message): Boolean {
        println("$p0 Mensjae")
        return true
    }

    override fun onBLENoSupported() {
        println("Hola")
    }

    override fun onOpenBLE() {
        println("Hola")
    }

    override fun onBleState(p0: Int) {
        println(p0)
        if(p0 == 101){


            if(oldBleDevice ==null){
                if(mBleDevice == null){
                    botonConectar?.setImageResource(R.drawable.conectar)
                    this@homeActivity.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(this, "No se encontro dispositivo", Toast.LENGTH_LONG).show()
                    })
                }

            }else{
                this@homeActivity.runOnUiThread(java.lang.Runnable {
                    bateria?.text = ""
                    Toast.makeText(this, "Desconectado", Toast.LENGTH_LONG).show()
                })
            }

            (this.application as App).version = 0
            oldBleDevice = null
            isScanning = false
        }
        if(p0 == 104){
            oldBleDevice = MonitorDataTransmissionManager.getInstance().bluetoothDevice
            MonitorDataTransmissionManager.getInstance().scan(false)
            botonConectar?.setImageResource(R.drawable.desconectar)
            isScanning=false

            print(MonitorDataTransmissionManager.getInstance().batteryValue)
            (this.application as App).version = 1
            this@homeActivity.runOnUiThread(java.lang.Runnable {
                Toast.makeText(this, "Conectado correctamente", Toast.LENGTH_LONG).show()
            })



            Handler(Looper.getMainLooper()).postDelayed(
                {
                    print("Ejecutando weas2")
                    var bat = MonitorDataTransmissionManager.getInstance().batteryValue
                    println("$bat%")
                    this@homeActivity.runOnUiThread(java.lang.Runnable {
                        bateria?.text = "Bateria: $bat%"
                    })
                },
                3000 // value in milliseconds
            )

        }
    }



    fun enviarDatos(view: View){
        val prefs = getSharedPreferences(
            "com.saludencamino.myapplication", Context.MODE_PRIVATE
        )

        //Hay Datos?

        if(!prefs.getBoolean("DatosCapturados",false)){
            this@homeActivity.runOnUiThread(java.lang.Runnable {
                Toast.makeText(this, "No hay datos para enviar", Toast.LENGTH_LONG).show()
            })


            return;
        }

        //Temperatura
        val temperatura = prefs.getFloat("temperatura", -1F)
        val tempObj = JSONObject();

        if(temperatura > -1F){
            tempObj.put("temperatura",temperatura);
        }else{
            tempObj.put("temperatura",JSONObject.NULL);
        }

        tempObj.put("horarioMedicion",SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()).toPattern());


        //Saturacion
        val spo2 = prefs.getFloat("saturacion_spo2",-1F)
        val hr1 = prefs.getInt("saturacion_corazon",-1)



        val satObj = JSONObject()
        if(spo2 > -1F){
            satObj.put("saturacion",spo2)
        }else{
            satObj.put("saturacion",JSONObject.NULL)
        }
        if(hr1 > -1F){
            satObj.put("ritmoCardiaco",hr1)
        }else{
            satObj.put("ritmoCardiaco",JSONObject.NULL)
        }
        satObj.put("horarioMedicion",SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()).toPattern());
        //glucosa
        val glucosa = prefs.getFloat("glucosa",-1F)
        val ayuna = prefs.getBoolean("ayuna",false)


        val glucObj = JSONObject()
        if(glucosa > -1F){
            glucObj.put("mgd",glucosa)
        }else{
            glucObj.put("mgd",JSONObject.NULL)
        }
        if(ayuna){
            glucObj.put("ayuna","true")
        }else{
            glucObj.put("ayuna","false")
        }

        glucObj.put("diabetes","false")

        glucObj.put("horarioMedicion",SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()).toPattern());

        //presion
        val sis = prefs.getInt("presion_sistolica",-1)
        val dias = prefs.getInt("presion_diastolica",-1)
        val hr2 = prefs.getInt("presion_ritmo",-1)



        val presTemp = JSONObject();

        if(sis > -1){
            presTemp.put("SBP",sis)
        }else{
            presTemp.put("SBP",JSONObject.NULL)
        }
        if(dias > -1){
            presTemp.put("DBP",dias)
        }else{
            presTemp.put("DBP",JSONObject.NULL)
        }

        if(hr2 > -1){
            presTemp.put("ritmoCardiaco",hr2)
        }else{
            presTemp.put("ritmoCardiaco",JSONObject.NULL)
        }

        presTemp.put("horarioMedicion",SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()).toPattern());



        //ecg
        val rrmin = prefs.getInt("rrmin",-1)
        val rrmax = prefs.getInt("rrmax",-1)
        val hrv = prefs.getInt("hrv",-1)
        val rr = prefs.getInt("rr",-1)
        val hr3 = prefs.getInt("hr",-1)
        val duration = prefs.getInt("duration",0)
        val mood = prefs.getInt("mood",0)

        val ecgObj = JSONObject();

        if(rrmax > -1){
            ecgObj.put("RRmax",rrmax)
        }else{
            ecgObj.put("RRmax",JSONObject.NULL)
        }
        if(rrmin>-1){
            ecgObj.put("RRmin",rrmin)
        }else{
            ecgObj.put("RRmin",JSONObject.NULL)
        }

        if(hrv > -1){
            ecgObj.put("HRV",hrv)
        }else{
            ecgObj.put("HRV",JSONObject.NULL)
        }
        if(rr > -1){
            ecgObj.put("respirationRate",rr)
        }else{
            ecgObj.put("respirationRate",JSONObject.NULL)
        }
        if(hr3 > -1){
            ecgObj.put("ritmoCardiaco",hr3)
        }else{
            ecgObj.put("ritmoCardiaco",JSONObject.NULL)
        }
        ecgObj.put("mood",mood)
        ecgObj.put("duration", duration)
        ecgObj.put("horarioMedicion",SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()).toPattern());


        val userId = prefs.getLong("idUsuario",-1)
        if(userId == -1L){
            this@homeActivity.runOnUiThread(java.lang.Runnable {
                Toast.makeText(this, "Error interno!", Toast.LENGTH_LONG).show()
            })
            return;
        }
        val body = JSONObject();
        body.put("medicionTemperatura",tempObj)
        body.put("medicionPresion",presTemp)
        body.put("medicionOxigenacion",satObj)
        body.put("medicionGlucosa",glucObj)
        body.put("medicionElectro",ecgObj)
        body.put("idUsuario",userId)



        println(body.toString())
        val server = Server();
        if(server.saveData(this,body)){
            this@homeActivity.runOnUiThread(java.lang.Runnable {
                Toast.makeText(this, "Mediciones Recibidas Conforme.", Toast.LENGTH_LONG).show();
            })

           prefs.edit().remove("DatosCapturados").commit()
           prefs.edit().remove("temperatura").commit();
           prefs.edit().remove("saturacion_spo2").commit();
           prefs.edit().remove("saturacion_corazon").commit();
           prefs.edit().remove("glucosa").commit();
           prefs.edit().remove("presion_sistolica").commit();
           prefs.edit().remove("presion_diastolica").commit();
           prefs.edit().remove("presion_ritmo").commit();
           prefs.edit().remove("rrmin").commit();
           prefs.edit().remove("rrmax").commit();
           prefs.edit().remove("hrv").commit();
           prefs.edit().remove("rr").commit();
           prefs.edit().remove("hr").commit();
            findViewById<ImageButton>(R.id.imageButton8)?.setImageResource(R.drawable.temperatura)
            findViewById<ImageButton>(R.id.imageButton9)?.setImageResource(R.drawable.sangre2)
            findViewById<ImageButton>(R.id.imageButton10)?.setImageResource(R.drawable.glucosa)
            findViewById<ImageButton>(R.id.imageButton7)?.setImageResource(R.drawable.presion)
            findViewById<ImageButton>(R.id.imageButton11)?.setImageResource(R.drawable.ecg)
       }else{
            this@homeActivity.runOnUiThread(java.lang.Runnable {
                Toast.makeText(this, "Error interno", Toast.LENGTH_LONG).show();
            })
       }
    }
    override fun onUpdateDialogBleList() {
        println("Hola")
    }

    override fun onDeviceBattery(p0: Int) {
        this@homeActivity.runOnUiThread(java.lang.Runnable {
            bateria?.text = "Bateria: $p0%"
        })

    }
}

