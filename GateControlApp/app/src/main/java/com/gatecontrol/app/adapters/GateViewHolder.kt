package com.gatecontrol.app.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gatecontrol.app.R
import com.gatecontrol.app.models.Gate
import com.squareup.picasso.Picasso

class GateViewHolder(view: View): ViewHolder(view){

    private val nameGate:TextView = view.findViewById(R.id.txtNameGateHome)
    private val stateGate:TextView = view.findViewById(R.id.txtCurrentState)
    private val imgGate:ImageView = view.findViewById(R.id.imgGateHome)

    fun render(gate: Gate){
        nameGate.text = gate.name
        stateGate.text = gate.state
        Picasso.get().load(gate.urlImage).into(imgGate)
    }
}