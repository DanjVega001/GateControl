package com.gatecontrol.app.models

class User (private var name:String, private var email:String) {

    private var userId:String = ""
    private var twoStepAuthEnabled:Boolean = false

    fun getName():String = this.name

    fun getEmail():String = this.email

    fun getUserId():String = this.userId

    fun isTwoStepAuthEnabled():Boolean = this.twoStepAuthEnabled

    fun setName(name: String) {
        this.name = name
    }

    fun setTwoStepAuthEnabled(value: Boolean) {
        this.twoStepAuthEnabled = value
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    override fun toString(): String {
        return "User(name='$name', email='$email')"
    }
}