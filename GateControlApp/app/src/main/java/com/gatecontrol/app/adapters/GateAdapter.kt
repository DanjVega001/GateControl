package com.gatecontrol.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gatecontrol.app.R
import com.gatecontrol.app.models.Gate

class GateAdapter(private val gateList: List<Gate>) : RecyclerView.Adapter<GateViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GateViewHolder(layoutInflater.inflate(R.layout.item_gate_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return gateList.size
    }

    override fun onBindViewHolder(holder: GateViewHolder, position: Int) {
        holder.render(gate = gateList[position])
    }

}