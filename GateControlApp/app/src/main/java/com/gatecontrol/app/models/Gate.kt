package com.gatecontrol.app.models

import android.net.Uri

data class Gate( var name:String,  var wifiName: String?,  var wifiPassword:String?,
     var voltage:Int?,  var urlImage:Uri, var state:String = "close"){



    fun validateGate():String?{
        if (this.name.trim().isEmpty() || this.wifiName!!.trim().isEmpty() || this.voltage!!<0){
            return "Fields cannot be empty"
        }
        return null
    }

    override fun toString(): String {
        return "Gate(name='$name', wifiName='$wifiName', wifiPassword='$wifiPassword', voltage=$voltage, urlImage=$urlImage)"
    }
}

