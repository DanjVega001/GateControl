package com.gatecontrol.app.models

data class User ( var name:String,  var email:String) {

     var userId:String = ""
     var twoStepAuthEnabled:Boolean = false


    override fun toString(): String {
        return "User(name='$name', email='$email')"
    }
}