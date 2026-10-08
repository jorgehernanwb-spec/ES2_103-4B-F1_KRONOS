
package com.example.miprimeraaplicacion.views

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.example.miprimeraaplicacion.R

class PreferenciasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)

        // Conectar los controles del XML con Kotlin
        val swAlertas = findViewById<MaterialSwitch>(
            R.id.swNotificaciones
        )

        val spInvernadero = findViewById<Spinner>(
            R.id.spIdioma
        )

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

        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            invernaderos
        )

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
                if (activado)
                    "Alertas activadas"
                else
                    "Alertas desactivadas",
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

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }
    }
}
