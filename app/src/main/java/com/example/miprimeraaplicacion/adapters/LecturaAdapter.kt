package com.example.miprimeraaplicacion.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miprimeraaplicacion.R
import com.example.miprimeraaplicacion.models.Lectura
import java.util.Locale

class LecturaAdapter(
    private val onEditarClick: (Lectura) -> Unit,
    private val onEliminarClick: (Lectura) -> Unit
) : RecyclerView.Adapter<LecturaAdapter.ViewHolder>() {
    private var elementos = listOf<Lectura>()

    fun actualizarLista(nuevos: List<Lectura>) {
        elementos = nuevos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_lectura, parent, false)
    )

    override fun getItemCount(): Int = elementos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.mostrar(elementos[position])

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val nombre: TextView = item.findViewById(R.id.txtInvernaderoItem)
        private val fecha: TextView = item.findViewById(R.id.txtFechaHoraItem)
        private val temperatura: TextView = item.findViewById(R.id.txtTemperaturaItem)
        private val humedad: TextView = item.findViewById(R.id.txtHumedadItem)
        private val ventilador: TextView = item.findViewById(R.id.txtVentiladorItem)
        private val modo: TextView = item.findViewById(R.id.txtModoItem)
        private val eliminar: ImageButton = item.findViewById(R.id.btnEliminar)

        fun mostrar(m: Lectura) {
            nombre.text = m.invernadero
            fecha.text = m.fechaHora
            temperatura.text = String.format(Locale.getDefault(), "Temperatura: %.1f °C", m.temperatura)
            humedad.text = String.format(Locale.getDefault(), "Humedad: %.1f %%", m.humedad)
            ventilador.text = if (m.ventiladorEncendido) "Ventilador: Encendido" else "Ventilador: Apagado"
            ventilador.setTextColor(android.graphics.Color.parseColor(if (m.ventiladorEncendido) "#73D99A" else "#FFB1A9"))
            modo.text = if (m.modoAutomatico) "Automático" else "Manual"
            itemView.setOnClickListener { onEditarClick(m) }
            eliminar.setOnClickListener { onEliminarClick(m) }
        }
    }
}
