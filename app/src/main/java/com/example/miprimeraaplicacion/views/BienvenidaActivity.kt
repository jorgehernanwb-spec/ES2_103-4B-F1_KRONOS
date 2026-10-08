package com.example.miprimeraaplicacion.views

import com.example.miprimeraaplicacion.R
import com.example.miprimeraaplicacion.SimulacionRepository
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class BienvenidaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var simulacionRepo: SimulacionRepository

    private var emailUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        db = Firebase.firestore
        simulacionRepo = SimulacionRepository(this)

        // Email recibido mediante Intent con la clave "usuario"
        emailUsuario = intent.getStringExtra("usuario") ?: auth.currentUser?.email
        val txtBienvenida = findViewById<TextView>(R.id.txtBienvenida)
        txtBienvenida.text = "Bienvenido, ${emailUsuario ?: "Usuario"}"

        // Intentar obtener el nombre del perfil registrado en Firestore sin bloquear la UI
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre")
                    if (!nombre.isNullOrBlank()) {
                        txtBienvenida.text = "Bienvenido, $nombre"
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresca los valores del invernadero y la simulación al volver de otra pantalla
        actualizarPanelSimulacion()
    }

    private fun actualizarPanelSimulacion() {
        val estado = simulacionRepo.obtenerEstado()

        val txtInvernadero = findViewById<TextView>(R.id.txtInvernaderoSeleccionado)
        val txtTemperatura = findViewById<TextView>(R.id.txtTemperatura)
        val txtHumedad = findViewById<TextView>(R.id.txtHumedad)
        val txtEstadoVentilador = findViewById<TextView>(R.id.txtEstadoVentilador)
        val txtModoVentilacion = findViewById<TextView>(R.id.txtModoVentilacion)
        val txtReglaVentilador = findViewById<TextView>(R.id.txtReglaVentilador)

        txtInvernadero.text = "📍 Monitoreando: ${estado.nombreInvernadero}"
        txtTemperatura.text = String.format(Locale.getDefault(), "%.1f °C", estado.temperatura)
        txtHumedad.text = String.format(Locale.getDefault(), "%.1f %%", estado.humedad)

        if (estado.ventiladorActivo) {
            txtEstadoVentilador.text = "Encendido"
            txtEstadoVentilador.setTextColor(android.graphics.Color.parseColor("#73D99A"))
        } else {
            txtEstadoVentilador.text = "Apagado"
            txtEstadoVentilador.setTextColor(android.graphics.Color.parseColor("#A3B7AC"))
        }

        txtModoVentilacion.text = if (estado.modoAutomatico) "Modo: Automático" else "Modo: Manual"
        txtReglaVentilador.text = if (estado.modoAutomatico) {
            "Regla auto: activo si Temp > 15 °C o Hum > 50 %"
        } else {
            "Control manual: estado fijado por el operador"
        }
    }

    fun onControlarVentilacionClick(view: View) {
        val intent = Intent(this, VentilacionActivity::class.java)
        startActivity(intent)
    }

    fun onVerLecturasClick(view: View) {
        val intent = Intent(this, ListaActivity::class.java)
        startActivity(intent)
    }

    fun onPreferenciasClick(view: View) {
        val intent = Intent(this, PreferenciasActivity::class.java)
        intent.putExtra("usuario", emailUsuario)
        startActivity(intent)
    }

    fun onCerrarSesionClick(view: View) {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
