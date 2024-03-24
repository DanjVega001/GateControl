package com.gatecontrol.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.gatecontrol.app.R
import com.gatecontrol.app.models.Gate
import com.gatecontrol.app.network.GateApiCliente
import com.gatecontrol.app.network.GateApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GateAdapter(private val gateList: List<Gate>, private val userID:String) : RecyclerView.Adapter<GateViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_gate_layout, parent, false)
        return GateViewHolder(view)
    }

    override fun getItemCount(): Int {
        return gateList.size
    }

    override fun onBindViewHolder(holder: GateViewHolder, position: Int) {
        holder.render(gate = gateList[position], userID)
    }



}