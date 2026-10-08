package com.example.miprimeraaplicacion.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.miprimeraaplicacion.CargandoDialog
import com.example.miprimeraaplicacion.R
import com.example.miprimeraaplicacion.SimulacionRepository
import com.example.miprimeraaplicacion.models.Lectura
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormularioLecturaActivity : AppCompatActivity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val cargando by lazy { CargandoDialog(this) }
    private lateinit var nombre: Spinner
    private lateinit var temperatura: EditText
    private lateinit var humedad: EditText
    private lateinit var fecha: EditText
    private lateinit var automatico: SwitchMaterial
    private lateinit var manualEncendido: SwitchMaterial

    private lateinit var resultado: TextView
    private var editarId: String? = null
    private var ocupado = false
    private var cambiado = false
    private var configurando = false
    private val nombres = arrayOf("Invernadero 1", "Invernadero 2", "Invernadero 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_lectura)
        nombre = findViewById(R.id.spInvernaderoForm)
        temperatura = findViewById(R.id.edtTemperatura)
        humedad = findViewById(R.id.edtHumedad)
        fecha = findViewById(R.id.edtFechaHora)
        automatico = findViewById(R.id.swAutomaticoForm)
        manualEncendido = findViewById(R.id.swVentiladorForm)
        resultado = findViewById(R.id.txtEstadoVentiladorForm)
        nombre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        editarId = intent.getStringExtra("lecturaId")

        if (editarId == null) {
            val repo = SimulacionRepository(this)
            val estado = repo.obtenerEstado()
            nombre.setSelection(estado.invernaderoIndice)
            temperatura.setText(estado.temperatura.toString())
            humedad.setText(estado.humedad.toString())
            fecha.setText(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            automatico.isChecked = estado.modoAutomatico
            manualEncendido.isChecked = estado.ventiladorManualEncendido
            actualizarEstado()
            findViewById<TextView>(R.id.txtTituloFormulario).text = "Nueva medición"
        } else {
            findViewById<TextView>(R.id.txtTituloFormulario).text = "Editar medición"
            bloquearFormulario(true)
            cargando.mostrar("Cargando medición...")
            db.collection("mediciones_invernadero").document(editarId!!).get()
                .addOnSuccessListener { doc ->
                    cargando.ocultar()
                    val item = doc.toObject(Lectura::class.java)
                    if (item == null) {
                        Toast.makeText(this, "No se encontró la medición", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        configurando = true
                        nombre.setSelection(nombres.indexOf(item.invernadero).coerceAtLeast(0))
                        temperatura.setText(item.temperatura.toString())
                        humedad.setText(item.humedad.toString())
                        fecha.setText(item.fechaHora)
                        automatico.isChecked = item.modoAutomatico
                        manualEncendido.isChecked = item.ventiladorEncendido
                        actualizarEstado()
                        configurando = false
                        cambiado = false
                        bloquearFormulario(false)
                    }
                }.addOnFailureListener {
                    cargando.ocultar()
                    Toast.makeText(this, "Error de lectura: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    finish()
                }
        }
        listOf(temperatura, humedad, fecha).forEach { campo ->
            campo.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!configurando) cambiado = true
                    actualizarEstado()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        automatico.setOnCheckedChangeListener { _, _ ->
            if (!configurando) cambiado = true
            actualizarEstado()
        }
        manualEncendido.setOnCheckedChangeListener { _, _ ->
            if (!configurando) cambiado = true
            actualizarEstado()
        }
        nombre.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!configurando) cambiado = true
            }
        }
        findViewById<View>(R.id.btnGuardarLectura).setOnClickListener { guardar() }
        findViewById<View>(R.id.btnCancelarForm).setOnClickListener { salir() }
        findViewById<View>(R.id.btnAtrasForm).setOnClickListener { salir() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { salir() }
        })
    }

    private fun bloquearFormulario(b: Boolean) {
        findViewById<View>(R.id.btnGuardarLectura).isEnabled = !b
        nombre.isEnabled = !b
        temperatura.isEnabled = !b
        humedad.isEnabled = !b
        fecha.isEnabled = !b
        automatico.isEnabled = !b
        if (b) manualEncendido.isEnabled = false else actualizarEstado()
    }

    private fun actualizarEstado() {
        if (!::automatico.isInitialized || !::manualEncendido.isInitialized || !::resultado.isInitialized) return
        manualEncendido.isEnabled = !automatico.isChecked && !ocupado
        val t = temperatura.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val h = humedad.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val encendido = if (automatico.isChecked) t > 15.0 || h > 50.0 else manualEncendido.isChecked
        resultado.text = "Ventilador: ${if (encendido) "Encendido" else "Apagado"} (${if (automatico.isChecked) "Automático" else "Manual"})"
    }

    private fun guardar() {
        if (ocupado) return
        val t = temperatura.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val h = humedad.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val f = fecha.text.toString().trim()
        var valido = true
        if (t == null || !t.isFinite() || t !in 0.0..40.0) {
            temperatura.error = "Ingresa temperatura entre 0 y 40 °C"
            valido = false
        }
        if (h == null || !h.isFinite() || h !in 0.0..100.0) {
            humedad.error = "Ingresa humedad entre 0 y 100 %"
            valido = false
        }
        if (f.isBlank()) {
            fecha.error = "La fecha es obligatoria"
            valido = false
        }
        if (!valido) return
        val encendido = if (automatico.isChecked) t!! > 15.0 || h!! > 50.0 else manualEncendido.isChecked
        val datos = hashMapOf<String, Any>(
            "invernadero" to nombres[nombre.selectedItemPosition.coerceIn(0, 2)],
            "temperatura" to t!!,
            "humedad" to h!!,
            "fechaHora" to f,
            "modoAutomatico" to automatico.isChecked,
            "ventiladorEncendido" to encendido
        )
        ocupado = true
        bloquearFormulario(true)
        cargando.mostrar("Guardando medición...")
        val coleccion = db.collection("mediciones_invernadero")
        val tarea = editarId?.let { coleccion.document(it).update(datos) } ?: coleccion.add(datos)
        tarea.addOnSuccessListener {
            cargando.ocultar()
            cambiado = false
            Toast.makeText(this, "Medición guardada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            cargando.ocultar()
            ocupado = false
            bloquearFormulario(false)
            Toast.makeText(this, "Error al guardar: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun salir() {
        if (ocupado) return
        if (!cambiado) { finish(); return }
        AlertDialog.Builder(this)
            .setTitle("Cambios sin guardar")
            .setMessage("¿Salir sin guardar los cambios?")
            .setNegativeButton("Seguir editando", null)
            .setPositiveButton("Salir") { _, _ -> finish() }
            .show()
    }
    override fun onDestroy() {
        cargando.ocultar()
        super.onDestroy()
    }
}
