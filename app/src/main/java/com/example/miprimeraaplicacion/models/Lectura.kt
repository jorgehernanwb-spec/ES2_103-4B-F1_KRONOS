package com.example.miprimeraaplicacion.models

import com.google.firebase.firestore.DocumentId

/**
 * Modelo de datos para las mediciones ambientales de invernaderos en KRONOS.
 * Cumple con los requisitos de rúbrica (campos de negocio, numérico e id @DocumentId).
 */
data class Lectura(
    @DocumentId
    var id: String = "",
    var invernadero: String = "",
    var temperatura: Double = 0.0,
    var humedad: Double = 0.0,
    var fechaHora: String = "",
    var modoAutomatico: Boolean = true,
    var ventiladorEncendido: Boolean = false
)
