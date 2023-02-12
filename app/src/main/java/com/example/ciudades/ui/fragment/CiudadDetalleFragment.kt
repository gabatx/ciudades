package com.example.ciudades.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.ciudades.R
import com.example.ciudades.databinding.FragmentCiudadDetalleBinding
import com.example.ciudades.domain.models.Ciudad
import com.example.ciudades.utils.OMapa
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class CiudadDetalleFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentCiudadDetalleBinding
    private val viewModelCiudad: ViewModelCiudad by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Esto para decirle a la actividad que el menu lo va a poner el fragment
        setHasOptionsMenu(true)
        binding = FragmentCiudadDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carga el mapa en un fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapDetalleCiudad) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Rellenar los datos en el layout
        rellenarDatos(viewModelCiudad.ciudadSeleccionada.value)
    }

    // Rellenar los datos en el layout
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    private fun rellenarDatos(ciudadSeleccionada: Ciudad?) {
        binding.nombreCiudadDetalle.text = ciudadSeleccionada?.nombre
        binding.nombrePaisDetalle.text = ciudadSeleccionada?.pais
        // Comprobar que la imagen está en el directorio público
        if (ciudadSeleccionada?.Imagen != null) {
            binding.imagenDetalle.setImageURI(Uri.parse(ciudadSeleccionada.Imagen))
        } else {
            binding.imagenDetalle.setImageResource(R.drawable.paris)
        }
        binding.distanciaDetalle.text = viewModelCiudad.ciudadSeleccionada.value?.let {
            viewModelCiudad.calculaDistanciaUbicacionActualDestino(it)
        }.toString() + " km"

        if (ciudadSeleccionada!!.Visitada) {
            binding.textoVisitadoDetalle.text = "Visitada"
        } else {
            binding.iconoVisitadaDetalle.backgroundTintList = ColorStateList.valueOf(Color.GRAY) // Colorea el fondo del icono
            binding.textoVisitadoDetalle.text = "No visitada"
        }
    }

    // Función para cargar el menu
    override fun onMapReady(map: GoogleMap) {
        OMapa.configurarMapa(map, GoogleMap.MAP_TYPE_NORMAL)
        OMapa.muestraMarcadorCiudadDetalle(map,
            viewModelCiudad.ciudadSeleccionada.value!!.Latitud,
            viewModelCiudad.ciudadSeleccionada.value!!.Longitud)
    }

    // Infla el menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_superior_detalle_ciudad, menu)
    }

    // La función listener al pulsar un item del toolbar superior
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.botonEditarCiudad -> {
                viewModelCiudad.origen.value = "Editar"
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_ciudadDetalleFragment_to_app_bar_Config_Ciudad)
            }
            R.id.botonBorrarCiudad -> confirmarBorrado()
        }
        return super.onOptionsItemSelected(item)
    }

    // Función para confirmar el borrado de la ciudad
    private fun confirmarBorrado() {
        val builder = AlertDialog.Builder(requireContext(), R.style.temaAlertDialog)
        builder.setTitle("Borrar ciudad")
        builder.setMessage("¿Estás seguro de que quieres borrar la ciudad?")
        builder.setPositiveButton("Sí") { _, _ ->
            // Borrar la ciudad
            viewModelCiudad.borrarCiudad(viewModelCiudad.ciudadSeleccionada.value!!)
            // Volver hacia atrás
            Navigation.findNavController(binding.root).navigateUp()
        }
        builder.setNegativeButton("No") { _, _ ->
            Toast.makeText(requireContext(), "Ciudad no borrada", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }
}
