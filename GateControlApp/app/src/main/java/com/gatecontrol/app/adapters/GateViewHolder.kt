package com.gatecontrol.app.adapters

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gatecontrol.app.R
import com.gatecontrol.app.fragments.HomeFragmentDirections
import com.gatecontrol.app.models.Gate
import com.squareup.picasso.Picasso

class GateViewHolder(private val view: View): ViewHolder(view){

    private val nameGate:TextView = view.findViewById(R.id.txtNameGateHome)
    private val stateGate:TextView = view.findViewById(R.id.txtCurrentState)
    private val imgGate:ImageView = view.findViewById(R.id.imgGateHome)
    private val btnEditGate:ImageButton = view.findViewById(R.id.btnEditeGateHome)

    fun render(gate: Gate){
        nameGate.text = gate.name
        stateGate.text = gate.state
        Picasso.get().load(gate.urlImage).into(imgGate)
        btnEditGate.setOnClickListener {
            goEditGate(gate.gateId)
        }
    }

    private fun goEditGate(gateId:String){

        view.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddGateFragment(
            gateId = gateId))
    }
}