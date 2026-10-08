
package com.example.miprimeraaplicacion

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.TextView

class CargandoDialog(private val activity: Activity) {

    private var dialog: Dialog? = null

    fun mostrar(mensaje: String = "Cargando...") {
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
            if (dialog?.isShowing == true) return@runOnUiThread

            val nuevoDialog = Dialog(activity)
            nuevoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            nuevoDialog.setContentView(R.layout.dialog_cargando)
            nuevoDialog.window?.setBackgroundDrawableResource(
                android.R.color.transparent
            )
            nuevoDialog.setCancelable(false)

            val reloj = nuevoDialog.findViewById<TextView>(
                R.id.txtRelojArena
            )

            nuevoDialog.findViewById<TextView>(
                R.id.txtMensajeCarga
            ).text = mensaje

            val giro = RotateAnimation(
                0f, 180f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )

            giro.duration = 900
            giro.repeatCount = Animation.INFINITE
            giro.repeatMode = Animation.REVERSE

            reloj.startAnimation(giro)

            nuevoDialog.setOnDismissListener {
                reloj.clearAnimation()
            }

            dialog = nuevoDialog
            nuevoDialog.show()
        }
    }

    fun ocultar() {
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                dialog = null
                return@runOnUiThread
            }
            try {
                if (dialog?.isShowing == true) {
                    dialog?.dismiss()
                }
            } catch (_: Exception) {
            } finally {
                dialog = null
            }
        }
    }
}
