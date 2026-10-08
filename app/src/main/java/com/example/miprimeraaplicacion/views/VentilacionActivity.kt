package com.example.miprimeraaplicacion.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.example.miprimeraaplicacion.R
import com.example.miprimeraaplicacion.SimulacionRepository
import java.util.Locale

class VentilacionActivity : AppCompatActivity() {

    private lateinit var simulacionRepo: SimulacionRepository
    private var indiceInvernadero: Int = 0

    private var temperaturaActual: Float = 24.0f
    private var humedadActual: Float = 65.0f
    private var modoAutomatico: Boolean = true
    private var ventiladorManualEncendido: Boolean = false

    private lateinit var txtInvernadero: TextView
    private lateinit var txtModoActual: TextView
    private lateinit var txtEstadoVentilador: TextView
    private lateinit var txtDetalleRegla: TextView
    private lateinit var swModoOperacion: MaterialSwitch
    private lateinit var txtDescripcionModo: TextView
    private lateinit var btnAlternarVentilador: Button
    private lateinit var txtValorTemperaturaSlider: TextView
    private lateinit var sliderTemperatura: Slider
    private lateinit var txtValorHumedadSlider: TextView
    private lateinit var sliderHumedad: Slider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ventilacion)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        simulacionRepo = SimulacionRepository(this)
        indiceInvernadero = simulacionRepo.obtenerIndiceInvernaderoSeleccionado()

        // Vincular componentes de la interfaz
        txtInvernadero = findViewById(R.id.txtInvernaderoVentilacion)
        txtModoActual = findViewById(R.id.txtModoActualVentilacion)
        txtEstadoVentilador = findViewById(R.id.txtEstadoVentilador)
        txtDetalleRegla = findViewById(R.id.txtDetalleReglaVentilador)
        swModoOperacion = findViewById(R.id.swModoOperacion)
        txtDescripcionModo = findViewById(R.id.txtDescripcionModo)
        btnAlternarVentilador = findViewById(R.id.btnAlternarVentilador)
        txtValorTemperaturaSlider = findViewById(R.id.txtValorTemperaturaSlider)
        sliderTemperatura = findViewById(R.id.sliderTemperatura)
        txtValorHumedadSlider = findViewById(R.id.txtValorHumedadSlider)
        sliderHumedad = findViewById(R.id.sliderHumedad)

        txtInvernadero.text = "📍 ${simulacionRepo.obtenerNombreInvernadero(indiceInvernadero)}"

        // Cargar estado persistente inicial
        val estado = simulacionRepo.obtenerEstado(indiceInvernadero)
        temperaturaActual = estado.temperatura
        humedadActual = estado.humedad
        modoAutomatico = estado.modoAutomatico
        ventiladorManualEncendido = estado.ventiladorManualEncendido

        // Configurar valores en los controles
        val tempClamp = temperaturaActual.coerceIn(0.0f, 40.0f)
        sliderTemperatura.value = tempClamp
        txtValorTemperaturaSlider.text = String.format(Locale.getDefault(), "%.1f °C", tempClamp)

        val humClamp = humedadActual.coerceIn(0.0f, 100.0f)
        sliderHumedad.value = humClamp
        txtValorHumedadSlider.text = String.format(Locale.getDefault(), "%.1f %%", humClamp)

        swModoOperacion.isChecked = modoAutomatico

        // Configurar listeners
        sliderTemperatura.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                temperaturaActual = value
                txtValorTemperaturaSlider.text = String.format(Locale.getDefault(), "%.1f °C", value)
                recalcularYActualizar()
            }
        }

        sliderHumedad.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                humedadActual = value
                txtValorHumedadSlider.text = String.format(Locale.getDefault(), "%.1f %%", value)
                recalcularYActualizar()
            }
        }

        swModoOperacion.setOnCheckedChangeListener { _, isChecked ->
            modoAutomatico = isChecked
            recalcularYActualizar()
        }

        btnAlternarVentilador.setOnClickListener {
            if (!modoAutomatico) {
                ventiladorManualEncendido = !ventiladorManualEncendido
                recalcularYActualizar()
            }
        }

        // Aplicar estado y calcular el ventilador
        recalcularYActualizar()
    }

    private fun recalcularYActualizar() {
        // Regla automática: temperatura > 15 °C || humedad > 50 %
        // Regla manual: ventiladorManualEncendido
        val ventiladorActivo = if (modoAutomatico) {
            temperaturaActual > 15.0f || humedadActual > 50.0f
        } else {
            ventiladorManualEncendido
        }

        // Estado visual del ventilador
        if (ventiladorActivo) {
            txtEstadoVentilador.text = "Encendido"
            txtEstadoVentilador.setTextColor(Color.parseColor("#73D99A"))
        } else {
            txtEstadoVentilador.text = "Apagado"
            txtEstadoVentilador.setTextColor(Color.parseColor("#A3B7AC"))
        }

        // Indicador de modo y detalle
        txtModoActual.text = if (modoAutomatico) "Modo: Automático" else "Modo: Manual"
        swModoOperacion.text = if (modoAutomatico) "Modo Automático" else "Modo Manual"

        if (modoAutomatico) {
            txtDescripcionModo.text = "En modo automático el estado del ventilador responde a las variables ambientales."
            txtDetalleRegla.text = "Auto: activo si Temp > 15 °C o Hum > 50 %"
            btnAlternarVentilador.isEnabled = false
            btnAlternarVentilador.alpha = 0.45f
            btnAlternarVentilador.text = "CONTROL MANUAL BLOQUEADO (MODO AUTO)"
            btnAlternarVentilador.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#34483D"))
        } else {
            txtDescripcionModo.text = "En modo manual el operador controla el encendido/apagado independientemente de las mediciones."
            txtDetalleRegla.text = "Manual: fijado por el operador"
            btnAlternarVentilador.isEnabled = true
            btnAlternarVentilador.alpha = 1.0f

            if (ventiladorManualEncendido) {
                btnAlternarVentilador.text = "APAGAR VENTILADOR (MANUAL)"
                btnAlternarVentilador.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8C2B2B"))
            } else {
                btnAlternarVentilador.text = "ENCENDER VENTILADOR (MANUAL)"
                btnAlternarVentilador.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#358655"))
            }
        }

        // Persistir inmediatamente el estado en SimulacionRepository
        simulacionRepo.guardarEstado(
            indice = indiceInvernadero,
            temperatura = temperaturaActual,
            humedad = humedadActual,
            modoAutomatico = modoAutomatico,
            ventiladorManual = ventiladorManualEncendido
        )
    }

    fun onVolverClick(view: View) {
        finish()
    }
}
