package com.example.ciudades.utils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.SupportMapFragment

// Hereda de SupportMapFragment, el mapa de google
class MapaScroll : SupportMapFragment() {
    // Añade un listener del constructor. Este listener se crea aquí para luego llamarlo desde fuera
    private var listenerMapa : OnTouchListener? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        val layout: View = super.onCreateView(inflater, container, savedInstanceState)
        val fragmentLayout = TouchableWrapper(requireActivity())

        fragmentLayout.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        (layout as ViewGroup).addView(
            fragmentLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        return layout
    }

    // Esta función sirve para asignar el listener ya que lo tenemos como privado
    fun setListener(listener: OnTouchListener?){
        listenerMapa = listener
    }

    // Una interfaz que sirve para obligar a implementar el listener (setListener)
    interface OnTouchListener {
        fun onTouch()
    }

    /* El listener lo que hace es capturar la acción de cuando hacemos arriaba o abajo en el mapa.
       Ayuda a solucionar el conflicto que se crea entre el scroll del mapa y el scroll del scrollView. El móvil no sabe
       a que tipo de scroll nos estamos refiriendo.
       Al crear una clase que hereda de SupportMapFragment, heredamos de esta manera todas las funciones del mapa y lo que le
       añade es otra clase propia que lo que intenta es capturar el evento de arriba o abajo.

       PROCEDIMIENTO:
         1. Creamos una clase que hereda de TouchableWrapper
         2. Donde queremos que se ejecute el listener, nos vamos a ir al xml del fragment del mapa que queramos y le añadimos un listener
            en el apartado name="view". Ej: android:name="com.example.ciudades.utils.MapaScroll" (borramos el de supportmapfragment que tenemos colocado)
         3. En el listener del mapa, llamamos a la función setListener(listener) que hemos creado en el fragment del mapa.

                // Carga el mapa en un fragment
                val mapFragment = (childFragmentManager.findFragmentById(R.id.map)) as MapaScroll
                mapFragment.setListener(object : MapaScroll.OnTouchListener {
                    override fun onTouch() {
                        binding.scrollView.requestDisallowInterceptTouchEvent(true)
                    }
                })
        mapFragment.getMapAsync(this)
       */
    inner class TouchableWrapper(context: Context?) : FrameLayout(context!!) {
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    listenerMapa?.let { listenerMapa!!.onTouch() }
                }
            }
            return super.dispatchTouchEvent(event)
        }
    }
}