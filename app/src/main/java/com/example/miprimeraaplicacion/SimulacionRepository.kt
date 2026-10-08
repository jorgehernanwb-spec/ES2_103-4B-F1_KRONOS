package com.example.miprimeraaplicacion

import android.content.Context
import android.content.SharedPreferences

data class EstadoInvernadero(
    val invernaderoIndice: Int,
    val nombreInvernadero: String,
    val temperatura: Float,
    val humedad: Float,
    val modoAutomatico: Boolean,
    val ventiladorManualEncendido: Boolean,
    val ventiladorActivo: Boolean
)

/**
 * Repositorio para gestionar y persistir los datos de simulación IoT
 * compartidos entre las pantallas de KRONOS.
 */
class SimulacionRepository(context: Context) {

    private val prefsSimulacion: SharedPreferences = context.getSharedPreferences(
        "kronos_simulacion_prefs",
        Context.MODE_PRIVATE
    )

    private val prefsPreferencias: SharedPreferences = context.getSharedPreferences(
        "preferencias_cronos",
        Context.MODE_PRIVATE
    )

    /**
     * Obtiene el índice del invernadero seleccionado (0, 1 o 2),
     * reutilizando el valor guardado por PreferenciasActivity.
     */
    fun obtenerIndiceInvernaderoSeleccionado(): Int {
        val indice = prefsPreferencias.getInt("invernadero_seleccionado", 0)
        return if (indice in 0..2) indice else 0
    }

    /**
     * Devuelve el nombre legible del invernadero según su índice.
     */
    fun obtenerNombreInvernadero(indice: Int = obtenerIndiceInvernaderoSeleccionado()): String {
        return "Invernadero ${indice + 1}"
    }

    /**
     * Obtiene el estado actual de simulación para el invernadero indicado.
     * Si no existen datos previos, utiliza los valores iniciales predeterminados:
     * - Temperatura: 24 °C
     * - Humedad: 65 %
     * - Modo: Automático (true)
     * - Estado manual inicial: Apagado (false)
     */
    fun obtenerEstado(indice: Int = obtenerIndiceInvernaderoSeleccionado()): EstadoInvernadero {
        val prefijo = "inv_${indice}_"
        val temperatura = prefsSimulacion.getFloat("${prefijo}temperatura", 24.0f)
        val humedad = prefsSimulacion.getFloat("${prefijo}humedad", 65.0f)
        val modoAutomatico = prefsSimulacion.getBoolean("${prefijo}modo_auto", true)
        val ventiladorManual = prefsSimulacion.getBoolean("${prefijo}vent_manual", false)

        // Reglas de negocio para el estado del ventilador:
        // Automático: encendido si temperatura > 15 °C o humedad > 50 %
        // Manual: estado fijado manualmente por el usuario
        val ventiladorActivo = if (modoAutomatico) {
            temperatura > 15.0f || humedad > 50.0f
        } else {
            ventiladorManual
        }

        return EstadoInvernadero(
            invernaderoIndice = indice,
            nombreInvernadero = obtenerNombreInvernadero(indice),
            temperatura = temperatura,
            humedad = humedad,
            modoAutomatico = modoAutomatico,
            ventiladorManualEncendido = ventiladorManual,
            ventiladorActivo = ventiladorActivo
        )
    }

    /**
     * Guarda el estado completo de simulación para un invernadero específico.
     */
    fun guardarEstado(
        indice: Int,
        temperatura: Float,
        humedad: Float,
        modoAutomatico: Boolean,
        ventiladorManual: Boolean
    ) {
        val prefijo = "inv_${indice}_"
        prefsSimulacion.edit()
            .putFloat("${prefijo}temperatura", temperatura)
            .putFloat("${prefijo}humedad", humedad)
            .putBoolean("${prefijo}modo_auto", modoAutomatico)
            .putBoolean("${prefijo}vent_manual", ventiladorManual)
            .apply()
    }

    /**
     * Permite actualizar exclusivamente los parámetros de control de ventilación.
     */
    fun actualizarVentilacion(indice: Int, modoAutomatico: Boolean, ventiladorManual: Boolean) {
        val prefijo = "inv_${indice}_"
        prefsSimulacion.edit()
            .putBoolean("${prefijo}modo_auto", modoAutomatico)
            .putBoolean("${prefijo}vent_manual", ventiladorManual)
            .apply()
    }
}
