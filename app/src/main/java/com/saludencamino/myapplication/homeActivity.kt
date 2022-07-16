package com.saludencamino.myapplication

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.Toast
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


class homeActivity : AppCompatActivity(),IBleConnectionListener,Handler.Callback, MonitorDataTransmissionManager.OnServiceBindListener,OnBleConnectListener{

    private val deviceList = mutableListOf<BleDevice>()
    private var mBleDevice: BleDevice? = null
    private var oldBleDevice: BluetoothDevice? = null
    private var isScanning: Boolean = false;
    private var mHandler: Handler? = null;
    private var botonConectar: Button? = null;
    protected var mHcService: HcService? = null




    override fun onCreate(savedInstanceState: Bundle?) {

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
    }

    fun iniciarBusqueda(view: View){

        if(oldBleDevice != null || mBleDevice !=null){
            if(oldBleDevice != null){
                MonitorDataTransmissionManager.getInstance().disConnectBle()
                botonConectar?.text ="Conectar"
                Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show()
                (this.application as App).version = 0
                oldBleDevice = null
            }
            if(mBleDevice !=null){
                BleManager.getInstance().stopScan()
                BleManager.getInstance().disconnect()
            }
        }else{



            AlertDialog.Builder(this)
                .setTitle("Version de Amanda")
                .setMessage("Seleccione su versión de Amanada")
                .setNegativeButton("1.0") { dialog, which ->

                    if(!isScanning){
                        botonConectar?.text = "Dejar de buscar"
                        isScanning=true
                        MonitorDataTransmissionManager.getInstance().scan(true);
                        MonitorDataTransmissionManager.getInstance().setOnBleConnectListener(this)

                    }else{
                        isScanning=false
                        botonConectar?.text = "Conectar"
                        MonitorDataTransmissionManager.getInstance().scan(false);

                    }

                }
                .setPositiveButton(
                    "2.0"
                ) { dialog, which ->

                    if(!isScanning){
                        botonConectar?.text = "Dejar de buscar"
                        isScanning=true
                        startScan();
                    }else{
                        isScanning=false
                        botonConectar?.text = "Conectar"
                        BleManager.getInstance().stopScan();
                    }


                }
                .show()
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

    override fun onServiceUnbind() {}


    fun mensajeSinConexion(){
        Toast.makeText(this, "Debe conectar el dispositivo primero", Toast.LENGTH_SHORT).show()
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
        botonConectar?.text = "Desconectar"
        Toast.makeText(this, "Conectado correctamente", Toast.LENGTH_SHORT).show()
        isScanning=false
        (this.application as App).version = 2
    }

    override fun onConnectFailed(p0: String?, p1: Int) {
        BleManager.getInstance().stopScan()
        mBleDevice=null
        Toast.makeText(this, "Error al conectar intente nuevamente", Toast.LENGTH_SHORT).show()

    }

    override fun onDisconnected(p0: String?, p1: Boolean, p2: Int) {
        botonConectar?.text ="Conectar"
        Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show()
        (this.application as App).version = 0
        mBleDevice = null
    }

    override fun onServicesDiscovered(p0: String?, p1: MutableList<BluetoothGattService>?) {

    }

    override fun handleMessage(p0: Message): Boolean {
        println(p0)
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
            botonConectar?.text ="Conectar"
            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show()
            (this.application as App).version = 0
            oldBleDevice = null
        }
        if(p0 == 104){
            oldBleDevice = MonitorDataTransmissionManager.getInstance().bluetoothDevice
            MonitorDataTransmissionManager.getInstance().scan(false)
            botonConectar?.text = "Desconectar"
            isScanning=false
            (this.application as App).version = 1

        }
    }

    override fun onUpdateDialogBleList() {
        println("Hola")
    }
}

