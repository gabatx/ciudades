package com.example.ciudades.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.ciudades.R
import com.example.ciudades.databinding.FragmentMapaCiudadesBinding
import com.example.ciudades.utils.OMapa
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment


class MapaCiudadesFragment : Fragment(), OnMapReadyCallback {

    lateinit var binding: FragmentMapaCiudadesBinding
    private val viewModelCiudad: ViewModelCiudad by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMapaCiudadesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carga el mapa en un fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Cuando el mapa está listo carga las funcionalidades
    override fun onMapReady(map: GoogleMap) {
        OMapa.configurarMapa(map, GoogleMap.MAP_TYPE_HYBRID)
        OMapa.todosMarcadoresMapa(map, viewModelCiudad)
        OMapa.listenerMarcadoresMapa(map, viewModelCiudad, binding.root)
    }
}
