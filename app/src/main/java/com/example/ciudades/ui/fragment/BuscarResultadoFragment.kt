package com.example.ciudades.ui.fragment

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ciudades.R
import com.example.ciudades.databinding.FragmentBuscarResultadoBinding
import com.example.ciudades.ui.adapter.AdapterListaCiudades
import com.example.ciudades.ui.adapter.IVisitadoListener
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BuscarResultadoFragment : Fragment() {

    private lateinit var binding: FragmentBuscarResultadoBinding
    lateinit var adapter : AdapterListaCiudades
    val viewModelCiudad: ViewModelCiudad by activityViewModels()
    // Creamos variable LifecycleOwner
    private val lifecycleOwner by lazy{
        binding.root.context as? LifecycleOwner
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBuscarResultadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guarda el nombre del fragment actual en el ViewModel
        viewModelCiudad.origen.value = "BuscarResultadoFragment"

        configuracionReyclerView(viewModelCiudad)

        viewModelCiudad.buscarPorNombre(viewModelCiudad.nombreBuscar.value.toString()).observe(viewLifecycleOwner){ciudad ->
            adapter.actualizaresultado(ciudad.sortedBy { it.nombre })
        }
    }

    private fun configuracionReyclerView(viewModelCiudades: ViewModelCiudad) {

        val layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = AdapterListaCiudades(
            viewModelCiudades,

            object : IVisitadoListener {
                override fun listenerBotonVisitado(color: Int, boton: FloatingActionButton) {
                    cambiaFondoIcono(color, boton)
                }

                override fun asignaColorBotonVisitado(visitado: Boolean, boton: FloatingActionButton) {
                    if (visitado) {
                        cambiaFondoIcono(R.color.viajado_activado, boton)

                    } else {
                        cambiaFondoIcono(R.color.viajado_desactivado, boton)
                    }
                }
            },
            lifecycleOwner!!
        )


        binding.recyclerViewBusqueda.layoutManager = layoutManager
        binding.recyclerViewBusqueda.adapter = adapter
    }

    // CAMBIA EL ESTADO DEL BOTÓN DE VOTO
    private fun cambiaFondoIcono(color: Int, boton: FloatingActionButton) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boton.backgroundTintList = ColorStateList.valueOf(
                resources.getColorStateList(
                    color,
                    null
                ).defaultColor
            )
        }
    }
}