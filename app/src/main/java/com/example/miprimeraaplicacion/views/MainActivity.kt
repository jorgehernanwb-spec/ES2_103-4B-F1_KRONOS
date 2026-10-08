package com.example.miprimeraaplicacion.views
import com.example.miprimeraaplicacion.R


// Importa la clase Intent para permitir la navegación y comunicación entre Activities (pantallas)
import android.content.Intent
// Importa Bundle para manejar el estado guardado de la Activity en su ciclo de vida
import android.os.Bundle
// Importa View para recibir la vista que disparó un evento de clic
import android.view.View
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
        // Busca y obtiene la referencia del campo de texto de usuario por su ID
        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        // Busca y obtiene la referencia del campo de texto de contraseña por su ID
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        // Busca y obtiene la referencia del checkbox "Recordarme" por su ID
        val chkRecordarme = findViewById<CheckBox>(R.id.chkRecordarme)

        // Extrae el texto ingresado en el campo usuario y lo convierte a String eliminando espacios perimetrales
        val usuario = edtUsuario.text.toString().trim()
        // Extrae el texto ingresado en el campo contraseña y lo convierte a String
        val password = edtPassword.text.toString()
        // Obtiene el estado booleano del CheckBox (true si está marcado, false si no)
        val recordar = chkRecordarme.isChecked

        // Bandera para controlar si todos los campos cumplen las validaciones requeridas
        var esValido = true

        // Valida si el campo usuario está vacío
        if (usuario.isEmpty()) {
            // Muestra mensaje de error flotante directo sobre el campo usuario
            edtUsuario.error = "Ingresa tu email de usuario"
            // Marca la validación general como falsa
            esValido = false
        // Valida si el formato de usuario no coincide con un correo electrónico válido
        } else if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            // Muestra error indicando que el formato de correo es incorrecto
            edtUsuario.error = "Ingresa un email válido (ej: usuario@correo.com)"
            // Marca la validación general como falsa
            esValido = false
        } else {
            // Limpia cualquier error visual previo en el campo usuario
            edtUsuario.error = null
        }

        // Valida si el campo contraseña está vacío
        if (password.isEmpty()) {
            // Muestra mensaje de error flotante directo sobre el campo contraseña
            edtPassword.error = "Ingresa tu contraseña"
            // Marca la validación general como falsa
            esValido = false
        // Valida si la contraseña tiene menos de 6 caracteres
        } else if (password.length < 6) {
            // Muestra mensaje de error indicando la longitud mínima requerida
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            // Marca la validación general como falsa
            esValido = false
        } else {
            // Limpia cualquier error visual previo en el campo contraseña
            edtPassword.error = null
        }

        // Si alguna validación falló
        if (!esValido) {
            // Incrementa en 1 el contador de intentos fallidos
            intentosFallidos++
        } else {
            // FIREBASE: INICIO DE AUTENTICACIÓN CON FIREBASE AUTH
            // Intenta iniciar sesión con el correo y contraseña provistos en Firebase
            cargando.mostrar("Verificando credenciales...")
            auth.signInWithEmailAndPassword(usuario, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // FIREBASE: INICIO DE SESIÓN EXITOSO EN FIREBASE
                        val firebaseUser = auth.currentUser
                        val emailUsuario = firebaseUser?.email ?: usuario

                        Toast.makeText(this, "FIREBASE: Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                        // Crea un Intent explícito indicando la pantalla actual (this) y la pantalla destino (BienvenidaActivity)
                        val intent = Intent(this, BienvenidaActivity::class.java)
                        // Empaqueta el email del usuario como un dato extra en el Intent con la clave "usuario"
                        intent.putExtra("usuario", emailUsuario)
                        // Inicia la navegación y lanza la nueva Activity
                        startActivity(intent)
                    } else {
                        // FIREBASE: FALLÓ EL INICIO DE SESIÓN EN FIREBASE
                        intentosFallidos++
                        val mensajeError = task.exception?.localizedMessage ?: "Error al autenticar usuario"
                        Toast.makeText(this, "FIREBASE: Error de autenticación: $mensajeError", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // Método público ejecutado automáticamente al hacer clic en el botón Limpiar (vía android:onClick)
    fun onLimpiarClick(view: View) {
        // Busca y obtiene la referencia del campo de texto de usuario por su ID
        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        // Busca y obtiene la referencia del campo de texto de contraseña por su ID
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        // Busca y obtiene la referencia del checkbox "Recordarme" por su ID
        val chkRecordarme = findViewById<CheckBox>(R.id.chkRecordarme)

        // Limpia el texto del campo usuario dejándolo vacío
        edtUsuario.setText("")
        // Limpia el texto del campo contraseña dejándolo vacío
        edtPassword.setText("")
        // Remueve cualquier mensaje de error visual activo en el campo usuario
        edtUsuario.error = null
        // Remueve cualquier mensaje de error visual activo en el campo contraseña
        edtPassword.error = null
        // Desmarca la casilla de verificación de recordarme
        chkRecordarme.isChecked = false
    }

    // Método público ejecutado automáticamente al hacer clic en el botón de Registrarse (vía android:onClick)
    fun onIrARegistroClick(view: View) {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }
}