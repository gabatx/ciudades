package com.example.ciudades.utils

import android.content.Context
import android.widget.ListView


object OFuncionesVarias {

    fun ocultarTeclado(activity: androidx.fragment.app.FragmentActivity) {

        // Si el teclado está visible, lo ocultamos
        if (activity.window.currentFocus != null) {
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.window.currentFocus!!.windowToken, 0)
        }
    }

    // Oculta el teclado después de escribir
    fun ocultaTecladoTrasEscribir(context: Context, listView: ListView) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(listView.windowToken, 0)
    }
}