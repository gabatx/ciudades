package com.example.ciudades.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ciudades.R
import com.example.ciudades.databinding.FragmentBuscadorCiudadBinding
import com.example.ciudades.viewmodel.ViewModelCiudad


class BuscadorCiudadFragment : Fragment() {

    lateinit var binding: FragmentBuscadorCiudadBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBuscadorCiudadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: ViewModelCiudad by activityViewModels()

        binding.botonEnviaBusqueda.setOnClickListener {

            if (binding.inputBuscarNombre.text.isNotEmpty()) {
                viewModel.nombreBuscar.value = binding.inputBuscarNombre.text.toString()
                binding.inputBuscarNombre.text.clear()
                findNavController().navigate(R.id.action_app_bar_Buscar_to_buscarResultadoFragment)
            }
        }
    }
}