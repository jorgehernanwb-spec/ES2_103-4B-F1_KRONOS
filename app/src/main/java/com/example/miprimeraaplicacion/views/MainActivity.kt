package com.example.miprimeraaplicacion.views
import com.example.miprimeraaplicacion.R


// Importa la clase Intent para permitir la navegación y comunicación entre Activities (pantallas)
import android.content.Intent
// Importa Bundle para manejar el estado guardado de la Activity en su ciclo de vida
import android.os.Bundle
// Importa View para recibir la vista que disparó un evento de clic
import android.view.View
import android.widget.Button
// Importa el componente visual CheckBox
import android.widget.CheckBox
// Importa el componente visual EditText para campos de entrada de texto
import android.widget.EditText
// Importa Patterns para utilizar patrones estándar de validación como direcciones de email
import android.util.Patterns
// Importa Toast para mostrar notificaciones flotantes temporales en pantalla
import android.widget.Toast
// Importa la función para habilitar el diseño de borde a borde (edge-to-edge)
import androidx.activity.enableEdgeToEdge
// Importa la clase base para actividades con soporte de compatibilidad hacia atrás
import androidx.appcompat.app.AppCompatActivity
// Importa utilidades para manejar vistas y compatibilidad de diseño
import androidx.core.view.ViewCompat
// Importa el manejador de márgenes del sistema (barras de estado y navegación)
import androidx.core.view.WindowInsetsCompat

// FIREBASE: IMPORTACIONES DE FIREBASE AUTHENTICATION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.miprimeraaplicacion.CargandoDialog

// Declaración de la clase principal de la pantalla de Login que hereda de AppCompatActivity
class MainActivity : AppCompatActivity() {

    // FIREBASE: Instancia principal de Firebase Authentication
    private lateinit var auth: FirebaseAuth
    private lateinit var cargando: CargandoDialog

    // Control para evitar llamadas concurrentes de inicio de sesión
    private var isLoggingIn: Boolean = false

    // Variable de estado a nivel de clase para rastrear si la contraseña está visible (false por defecto)
    var mostrandoPassword: Boolean = false
    // Contador a nivel de clase para registrar la cantidad de intentos fallidos de inicio de sesión
    var intentosFallidos: Int = 0

    // Método que se ejecuta al crearse la pantalla (primer paso del ciclo de vida)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama a la implementación original de la clase padre para inicializar la actividad
        super.onCreate(savedInstanceState)
        // Habilita el modo de pantalla completa que se extiende detrás de las barras del sistema
        enableEdgeToEdge()
        // Asocia y "dibuja" el diseño XML (activity_main.xml) en esta Activity
        setContentView(R.layout.activity_main)
        // Ajusta los márgenes (padding) de la vista raíz para no solaparse con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Obtiene las dimensiones de las barras del sistema (status bar y navigation bar)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplica padding a la vista con el tamaño exacto de las barras del sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Retorna los insets procesados
            insets
        }

        // FIREBASE: INICIALIZACIÓN DE LA INSTANCIA DE FIREBASE AUTH
        auth = Firebase.auth
        cargando = CargandoDialog(this)

        cargarPreferenciasRecordarme()
    }

    // Carga el correo guardado si la casilla Recordarme estaba marcada previamente
    private fun cargarPreferenciasRecordarme() {
        val prefs = getSharedPreferences("kronos_login_prefs", MODE_PRIVATE)
        val recordar = prefs.getBoolean("recordar", false)
        if (recordar) {
            val emailGuardado = prefs.getString("email", "") ?: ""
            val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
            val chkRecordarme = findViewById<CheckBox>(R.id.chkRecordarme)
            edtUsuario.setText(emailGuardado)
            chkRecordarme.isChecked = true
        }
    }

    // Método público ejecutado automáticamente al hacer clic en el botón btnMostrarPassword (vía android:onClick)
    fun onMostrarPasswordClick(view: View) {
        // Busca y obtiene la referencia del campo de texto de contraseña por su ID
        val edtPassword = findViewById<EditText>(R.id.edtPassword)

        // Evalúa si la contraseña actualmente está oculta (mostrandoPassword == false)
        if (!mostrandoPassword) {
            // Cambia el tipo de entrada a texto visible (desenmascara la contraseña)
            edtPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            // Actualiza el estado indicando que ahora la contraseña está visible
            mostrandoPassword = true
        } else {
            // Cambia el tipo de entrada a texto oculto / contraseña (enmascara con puntos)
            edtPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            // Actualiza el estado indicando que ahora la contraseña está oculta
            mostrandoPassword = false
        }
        // Mueve el cursor al final del texto para no reiniciar la posición de escritura
        edtPassword.setSelection(edtPassword.text.length)
    }

    // Método público ejecutado automáticamente al hacer clic en el botón Ingresar (vía android:onClick)
    fun onIngresarClick(view: View) {
        // Evita solicitudes de login duplicadas si ya hay una petición en curso
        if (isLoggingIn) return

        // Busca y obtiene la referencia de los controles
        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val chkRecordarme = findViewById<CheckBox>(R.id.chkRecordarme)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)

        // Extrae el texto ingresado en el campo usuario y contraseña
        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()
        val recordar = chkRecordarme.isChecked

        // Bandera para controlar si todos los campos cumplen las validaciones requeridas
        var esValido = true

        // Valida si el campo usuario está vacío
        if (usuario.isEmpty()) {
            edtUsuario.error = "Ingresa tu email de usuario"
            esValido = false
        // Valida si el formato de usuario no coincide con un correo electrónico válido
        } else if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            edtUsuario.error = "Ingresa un email válido (ej: usuario@correo.com)"
            esValido = false
        } else {
            edtUsuario.error = null
        }

        // Valida si el campo contraseña está vacío
        if (password.isEmpty()) {
            edtPassword.error = "Ingresa tu contraseña"
            esValido = false
        // Valida si la contraseña tiene menos de 6 caracteres
        } else if (password.length < 6) {
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            esValido = false
        } else {
            edtPassword.error = null
        }

        // Si alguna validación falló
        if (!esValido) {
            intentosFallidos++
            return
        }

        // Bloquea nuevos envíos y desactiva el botón mientras se comunica con Firebase
        isLoggingIn = true
        btnIngresar.isEnabled = false

        // Muestra la animación de reloj de arena de KRONOS
        cargando.mostrar("Verificando credenciales...")

        // Autentica contra Firebase Authentication
        auth.signInWithEmailAndPassword(usuario, password)
            .addOnCompleteListener(this) { task ->
                // Oculta SIEMPRE el diálogo de carga y rehabilita el botón (tanto en éxito como en error)
                cargando.ocultar()
                isLoggingIn = false
                btnIngresar.isEnabled = true

                if (task.isSuccessful) {
                    // Gestión de Recordarme: solo se persiste el email, jamás la contraseña en texto plano
                    val prefs = getSharedPreferences("kronos_login_prefs", MODE_PRIVATE)
                    if (recordar) {
                        prefs.edit()
                            .putBoolean("recordar", true)
                            .putString("email", usuario)
                            .apply()
                    } else {
                        prefs.edit()
                            .putBoolean("recordar", false)
                            .remove("email")
                            .apply()
                    }

                    // FIREBASE: INICIO DE SESIÓN EXITOSO EN FIREBASE
                    val firebaseUser = auth.currentUser
                    val emailUsuario = firebaseUser?.email ?: usuario

                    Toast.makeText(this, "FIREBASE: Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    // Navega a BienvenidaActivity pasando el email verificado
                    val intent = Intent(this, BienvenidaActivity::class.java)
                    intent.putExtra("usuario", emailUsuario)
                    startActivity(intent)
                } else {
                    // FIREBASE: FALLÓ EL INICIO DE SESIÓN EN FIREBASE
                    intentosFallidos++
                    val mensajeError = task.exception?.localizedMessage ?: "Error al autenticar usuario"
                    Toast.makeText(this, "FIREBASE: Error de autenticación: $mensajeError", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Método público ejecutado automáticamente al hacer clic en el botón Limpiar (vía android:onClick)
    fun onLimpiarClick(view: View) {
        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val chkRecordarme = findViewById<CheckBox>(R.id.chkRecordarme)

        // Limpia el texto y errores visuales
        edtUsuario.setText("")
        edtPassword.setText("")
        edtUsuario.error = null
        edtPassword.error = null
        chkRecordarme.isChecked = false

        // Limpia la preferencia guardada de Recordarme
        val prefs = getSharedPreferences("kronos_login_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    // Método público ejecutado automáticamente al hacer clic en el botón de Registrarse (vía android:onClick)
    fun onIrARegistroClick(view: View) {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        cargando.ocultar()
    }
}