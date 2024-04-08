package com.gatecontrol.app.adapters

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gatecontrol.app.R
import com.gatecontrol.app.fragments.HomeFragmentDirections
import com.gatecontrol.app.models.Gate
import com.gatecontrol.app.network.GateApiCliente
import com.gatecontrol.app.network.GateApiService
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.checkerframework.checker.units.qual.C



class GateViewHolder(private val view: View) : ViewHolder(view) {

    private val nameGate: TextView = view.findViewById(R.id.txtNameGateHome)
    private val stateGate: TextView = view.findViewById(R.id.txtCurrentState)
    private val imgGate: ImageView = view.findViewById(R.id.imgGateHome)
    private val btnEditGate: ImageButton = view.findViewById(R.id.btnEditeGateHome)
    private val btnOpenGate: Button = view.findViewById(R.id.btnOpenGate)
    private val btnCloseGate: Button = view.findViewById(R.id.btnCloseGate)
    private val service: GateApiService = GateApiCliente.getInstance()
    private val backActiveGate = ContextCompat.getDrawable(view.context, R.drawable.btn_active_gate)
    private val backInactiveGate = ContextCompat.getDrawable(view.context, R.drawable.btn_inactive_gate)



    fun render(gate: Gate, userID: String) {
        nameGate.text = gate.name
        stateGate.text = gate.state
        if (gate.state == "open"){
            btnCloseGate.background = backInactiveGate
            btnOpenGate.background = backActiveGate
        }else{
            btnCloseGate.background = backActiveGate
            btnOpenGate.background = backInactiveGate
        }
        Picasso.get().load(gate.urlImage).into(imgGate)
        manageGate(gate.gateId, userID)
    }

    private fun manageGate(gateID: String, userID: String) {
        val txtState:TextView = view.findViewById(R.id.txtCurrentState)

        btnOpenGate.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                btnCloseGate.isEnabled = false
                btnOpenGate.isEnabled = false
                val data = openGate(gateID, userID)
                handleGateOperationResult(data)
                btnCloseGate.background = backInactiveGate
                btnOpenGate.background = backActiveGate
                txtState.text = "open"
            }
        }

        btnCloseGate.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                btnCloseGate.isEnabled = false
                btnOpenGate.isEnabled = false
                val data = closeGate(gateID, userID)
                handleGateOperationResult(data)
                btnCloseGate.background = backActiveGate
                btnOpenGate.background = backInactiveGate
                txtState.text = "close"
            }
        }

        btnEditGate.setOnClickListener {
            goEditGate(gateID)
        }
    }

    private fun handleGateOperationResult(data: Map<String, Any>) {
        val message = data["message"] as? String
        val error = data["error"] as? String
        if (message != null) {
            Toast.makeText(view.context, message, Toast.LENGTH_LONG).show()
        } else if (error != null) {
            Toast.makeText(view.context, error, Toast.LENGTH_LONG).show()
        }
        btnCloseGate.isEnabled = true
        btnOpenGate.isEnabled = true
    }

    private suspend fun openGate(gateID: String, userID: String): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            val res = service.openGate(userID, gateID)
            res.body() ?: emptyMap()
        }
    }

    private suspend fun closeGate(gateID: String, userID: String): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            val res = service.closeGate(userID, gateID)
            res.body() ?: emptyMap()
        }
    }

    private fun goEditGate(gateId: String) {
        view.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddGateFragment(gateId))
    }
}
