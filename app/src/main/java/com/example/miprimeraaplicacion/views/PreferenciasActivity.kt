
package com.example.miprimeraaplicacion.views

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.example.miprimeraaplicacion.R

class PreferenciasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferencias)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Conectar los controles del XML con Kotlin
        val swAlertas = findViewById<MaterialSwitch>(R.id.swNotificaciones)
        val spInvernadero = findViewById<Spinner>(R.id.spIdioma)

        // Guardar preferencias en el dispositivo
        val preferencias = getSharedPreferences(
            "preferencias_cronos",
            MODE_PRIVATE
        )

        // Recuperar estado anterior del Switch
        swAlertas.isChecked = preferencias.getBoolean(
            "alertas_activadas",
            false
        )

        // Cargar las opciones del invernadero
        val invernaderos = resources.getStringArray(
            R.array.idiomas
        )

        // Adaptador con texto blanco para legibilidad óptima en el tema oscuro
        val adaptador = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            invernaderos
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                view.textSize = 15f
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                view.setBackgroundColor(android.graphics.Color.parseColor("#1B2D26"))
                view.setPadding(24, 20, 24, 20)
                return view
            }
        }

        adaptador.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spInvernadero.adapter = adaptador

        // Recuperar invernadero seleccionado
        val posicionGuardada = preferencias.getInt(
            "invernadero_seleccionado",
            0
        )

        if (posicionGuardada in invernaderos.indices) {
            spInvernadero.setSelection(posicionGuardada)
        }

        // Guardar cambios del Switch
        swAlertas.setOnCheckedChangeListener { _, activado ->
            preferencias.edit()
                .putBoolean("alertas_activadas", activado)
                .apply()

            Toast.makeText(
                this,
                if (activado) "Alertas de temperatura activadas" else "Alertas de temperatura desactivadas",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Guardar el invernadero seleccionado
        spInvernadero.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    preferencias.edit()
                        .putInt("invernadero_seleccionado", position)
                        .apply()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                }
            }
    }

    // Regresa a BienvenidaActivity finalizando esta actividad
    fun onVolverClick(view: View) {
        finish()
    }
}
