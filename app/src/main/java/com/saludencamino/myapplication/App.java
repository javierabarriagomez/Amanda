package com.saludencamino.myapplication;

import android.app.Application;
import android.content.res.Configuration;

import com.linktop.MonitorDataTransmissionManager;
import com.linktop.whealthService.BleDevManager;

import lib.linktop.sev.CssServerApi;


public class App extends Application {

    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int ver) {
        version = ver;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        MonitorDataTransmissionManager.isDebug(true);
        CssServerApi.init(this, "zydb_", true);
        //TODO Create a BleDevManager object
        BleDevManager mBleDevManager = new BleDevManager();
        mBleDevManager.initHC(this);
        // Required initialization logic here!
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
