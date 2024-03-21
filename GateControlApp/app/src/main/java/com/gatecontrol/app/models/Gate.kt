package com.gatecontrol.app.models

import android.net.Uri

class Gate(private var name:String, private var wifiName: String, private var wifiPassword:String,
    private var voltage:Int, private var urlImage:Uri){

    fun getName():String = this.name

    fun getWifiName():String = this.wifiName

    fun getVoltage():Int = this.voltage

    fun getUrlImage():Uri = this.urlImage

    fun validateGate():String?{
        if (this.name.trim().isEmpty() || this.wifiName.trim().isEmpty() || this.voltage<0){
            return "Fields cannot be empty"
        }
        return null
    }

    override fun toString(): String {
        return "Gate(name='$name', wifiName='$wifiName', wifiPassword='$wifiPassword', voltage=$voltage, urlImage=$urlImage)"
    }
}

