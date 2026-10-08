package com.example.miprimeraaplicacion.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miprimeraaplicacion.CargandoDialog
import com.example.miprimeraaplicacion.R
import com.example.miprimeraaplicacion.adapters.LecturaAdapter
import com.example.miprimeraaplicacion.models.Lectura
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ListaActivity : AppCompatActivity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: LecturaAdapter
    private lateinit var mensaje: TextView
    private var registro: ListenerRegistration? = null
    private val cargando by lazy { CargandoDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        mensaje = findViewById(R.id.txtEstadoLista)
        adapter = LecturaAdapter(
            onEditarClick = { dato ->
                startActivity(Intent(this, FormularioLecturaActivity::class.java).putExtra("lecturaId", dato.id))
            },
            onEliminarClick = { dato ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar medición")
                    .setMessage("¿Eliminar la medición de ${dato.invernadero} del ${dato.fechaHora}?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Eliminar") { _, _ -> eliminar(dato) }
                    .show()
            }
        )
        findViewById<RecyclerView>(R.id.rvLecturas).apply {
            layoutManager = LinearLayoutManager(this@ListaActivity)
            adapter = this@ListaActivity.adapter
        }
        findViewById<View>(R.id.fabAgregarLectura).setOnClickListener {
            startActivity(Intent(this, FormularioLecturaActivity::class.java))
        }
        findViewById<View>(R.id.btnVolverLista).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAtrasLista).setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        mensaje.text = "Cargando mediciones..."
        mensaje.visibility = View.VISIBLE
        registro = db.collection("mediciones_invernadero").addSnapshotListener { snapshot, error ->
            if (error != null) {
                mensaje.text = "Error al cargar mediciones: ${error.localizedMessage}"
                mensaje.visibility = View.VISIBLE
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val lista = snapshot.documents.mapNotNull { documento ->
                    try { documento.toObject(Lectura::class.java) } catch (_: Exception) { null }
                }.sortedByDescending { it.fechaHora }
                adapter.actualizarLista(lista)
                mensaje.text = "Todavía no hay mediciones. Pulsa Agregar medición."
                mensaje.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onStop() {
        registro?.remove()
        registro = null
        cargando.ocultar()
        super.onStop()
    }

    private fun eliminar(dato: Lectura) {
        if (dato.id.isBlank()) return
        cargando.mostrar("Eliminando medición...")
        db.collection("mediciones_invernadero").document(dato.id).delete()
            .addOnSuccessListener {
                cargando.ocultar()
                Toast.makeText(this, "Medición eliminada", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                cargando.ocultar()
                Toast.makeText(this, "Error al eliminar: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
