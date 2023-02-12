package com.example.ciudades.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ciudades.R
import com.example.ciudades.databinding.FragmentConfigCiudadBinding
import com.example.ciudades.domain.models.Ciudad
import com.example.ciudades.ui.activity.MainActivity
import com.example.ciudades.utils.MapaScroll
import com.example.ciudades.utils.OFuncionesVarias
import com.example.ciudades.utils.OMapa
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng


class ConfigCiudadFragment : Fragment(), OnMapReadyCallback {

    lateinit var binding: FragmentConfigCiudadBinding
    private val viewModelCiudad: ViewModelCiudad by activityViewModels()
    lateinit var adapter: ArrayAdapter<String>
    lateinit var mapa: GoogleMap // Para utilizar map en funciones que está fuera de onMapReady

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Esto para decirle a la actividad que el menu lo va a poner el fragment
        setHasOptionsMenu(true)
        binding = FragmentConfigCiudadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Paso los datos de la ciudad seleccionada a la variable de configuración. Se utilizará en el listener del botón añadir o actualizar
        viewModelCiudad.configCiudad.value = viewModelCiudad.ciudadSeleccionada.value

        // Dependiendo del origen cambia el texto de los botones
        preparaConfigCiudad()

        // Actualiza la imagen seleccionada de la galería
        viewModelCiudad.imageUri.observe(viewLifecycleOwner) {
            binding.imagenMiniatura.setImageURI(it)
            viewModelCiudad.configCiudad.value?.Imagen = it.toString()
        }


        // Carga el mapa en un fragment
        val mapFragment = (childFragmentManager.findFragmentById(R.id.map)) as MapaScroll
        mapFragment.setListener(object : MapaScroll.OnTouchListener {
            override fun onTouch() {
                binding.scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })
        mapFragment.getMapAsync(this)

        // Botón que importa una imagen de la galeria
        binding.botonAniadirImagen.setOnClickListener {
            // Llamar a tomarFoto() de MainActivity
            (activity as MainActivity).tomarImagenGaleria()
        }

        // Botón cámara
        binding.botonCamara.setOnClickListener {
            (activity as MainActivity).tomarImagenCamara()
        }

        // Botón que activa la ubicación actual
        binding.botonUbicacionActual.setOnClickListener {
            activaUbicacionActual()
        }

        //  Añadir o modificar una ciudad
        binding.botonAniadirCiudad.setOnClickListener {

            val configCiudad = viewModelCiudad.configCiudad.value

            configCiudad?.nombre = binding.textInputeditCiudad.text.toString()
            configCiudad?.pais = binding.textInputeditPais.text.toString()
            configCiudad?.Latitud = viewModelCiudad.configCiudad.value!!.Latitud
            configCiudad?.Longitud = viewModelCiudad.configCiudad.value!!.Longitud
            configCiudad?.Imagen = Uri.parse(configCiudad?.Imagen).toString()
            configCiudad?.Visitada = binding.switchVisitado.isChecked


            if (validaCiudad(viewModelCiudad.configCiudad.value!!)) {
                // Asigno la distancia a la ciudad
                configCiudad?.distancia = viewModelCiudad.calculaDistanciaUbicacionActualDestino(viewModelCiudad.configCiudad.value!!)
                // Dependiendo del origen, añado o modifico la ciudad
                when (viewModelCiudad.origen.value) {
                    "nuevaCiudad" -> {
                        viewModelCiudad.nuevaCiudad(viewModelCiudad.configCiudad.value!!)
                        mensajeConfirmacion()
                    }
                    "Editar"      -> {
                        viewModelCiudad.actualizarCiudad(viewModelCiudad.configCiudad.value!!)
                        mensajeConfirmacion()
                    }
                }
                OFuncionesVarias.ocultarTeclado(requireActivity())
            }
        }
    } // Fin onViewCreated

    //------------------   ADAPTACIÓN DEL FRAGMENT  ------------------------------------------------------------

    private fun preparaConfigCiudad() {

        when (viewModelCiudad.origen.value) {
            "nuevaCiudad" -> {
                reiniciaConfigCiudad()

            }
            "Editar"      -> {
                rellenarDatosEditar()
            }
        }
        introduceCoordenadas()
    }

    private fun introduceCoordenadas() {
        viewModelCiudad.latitudVariable.value = viewModelCiudad.configCiudad.value!!.Latitud
        viewModelCiudad.longitudVariable.value = viewModelCiudad.configCiudad.value!!.Longitud
        // Introduce las coordenadas cuando se añade el market
        viewModelCiudad.latitudVariable.observe(viewLifecycleOwner) {
            binding.latitudMarcador.text = it.toString()
            viewModelCiudad.configCiudad.value!!.Latitud = it
        }
        viewModelCiudad.longitudVariable.observe(viewLifecycleOwner) {
            binding.longitudMarcador.text = it.toString()
            viewModelCiudad.configCiudad.value!!.Longitud = it
        }
    }

    private fun reiniciaConfigCiudad() {
        viewModelCiudad.configCiudad.value = Ciudad("", "", 0.0, 0.0, "", false)
        // Introduzco la imagen por defecto que irá en la miniatura al iniciares el fragment
        viewModelCiudad.imageUri.value = Uri.parse("android.resource://com.example.ciudades/drawable/paris")
    }

    private fun rellenarDatosEditar() {
        // Cambiar la etiqueta del fragment
        (activity as MainActivity).supportActionBar?.title = "Modificar ciudad"

        // Rellenar los datos de la ciudad en el fragment
        val configCiudad = viewModelCiudad.configCiudad.value

        binding.textInputeditCiudad.setText(configCiudad?.nombre)
        binding.textInputeditPais.setText(configCiudad?.pais)
        binding.imagenMiniatura.setImageURI(Uri.parse(configCiudad?.Imagen))
        binding.switchVisitado.isChecked = configCiudad?.Visitada ?: false
        binding.latitudMarcador.text = configCiudad?.Latitud.toString()
        binding.longitudMarcador.text = configCiudad?.Longitud.toString()
        binding.botonAniadirCiudad.text = "Actualizar"
        // Introduzco la imagen de la ciudad seleccionada que irá en la miniatura al iniciares el fragment
        viewModelCiudad.imageUri.value = Uri.parse(configCiudad?.Imagen)
    }

    //------------------   MAPA  ------------------------------------------------------------

    override fun onMapReady(map: GoogleMap) {
        mapa = map // Le damos valor al mapa

        OMapa.configurarMapa(map, GoogleMap.MAP_TYPE_HYBRID)
        OMapa.aniadirMarcadorPulsar(map, viewModelCiudad)
        OMapa.actualizaPosicionArrastrar(map, viewModelCiudad)
        if (viewModelCiudad.origen.value == "Editar") {
            OMapa.muestraMarcadorCiudadDetalle(map,
                viewModelCiudad.configCiudad.value!!.Latitud,
                viewModelCiudad.configCiudad.value!!.Longitud)
        }
    }

    //--------------------------   VALIDACIÓN   ----------------------------------------

    private fun validaCiudad(nuevaCiudad: Ciudad): Boolean {
        if (camposIncompletos(nuevaCiudad)) {
            Toast.makeText(requireContext(), "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        if (existe(nuevaCiudad) && viewModelCiudad.origen.value == "nuevaCiudad") {
            Toast.makeText(requireContext(), "La ciudad ya existe", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun existe(nuevaCiudad: Ciudad): Boolean {
        return viewModelCiudad.listaCiudades.value?.filter { it.nombre == nuevaCiudad.nombre }!!.isNotEmpty()
    }

    private fun camposIncompletos(nuevaCiudad: Ciudad): Boolean {
        return (
                nuevaCiudad.nombre.isEmpty() ||
                        nuevaCiudad.pais.isEmpty() ||
                        nuevaCiudad.Imagen.isEmpty() ||
                        nuevaCiudad.Latitud == 0.0 ||
                        nuevaCiudad.Longitud == 0.0
                )
    }

    //--------------------------   MENSAJES (ALERTDIALOG)   ----------------------------------------

    private fun mensajeConfirmacion() {
        // Confirma que está toro correcto
        val dialog = AlertDialog.Builder(requireContext(), R.style.temaAlertDialog)
        dialog.setTitle("Éxito")
        if (viewModelCiudad.origen.value == "nuevaCiudad") {
            dialog.setMessage("Ciudad añadida correctamente")
        } else {
            dialog.setMessage("Ciudad editada correctamente")
        }
        dialog.setPositiveButton("Aceptar") { _, _ ->
            findNavController().navigate(R.id.action_app_bar_Config_Ciudad_to_app_bar_Listado_Ciudades)
        }
        // Listener que se activa al hacer click fuera del dialog
        dialog.setOnCancelListener {
            findNavController().navigate(R.id.action_app_bar_Config_Ciudad_to_app_bar_Listado_Ciudades)
        }
        dialog.show()
    }

    //--------------------------   MENÚ   ----------------------------------------

    // Infla en menú superior que hemos creado en res/menu/menu_superior
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_superior_nueva_ciudad, menu)
        // El inflado del menu (res/menu/menu_superior)
        val item = menu.findItem(R.id.app_bar_search_buscar_ciudad)
        val searchView = item?.actionView as SearchView
        // El listener del buscador
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Se muestra el listView
                binding.listViewCiudades.visibility = View.VISIBLE

                OFuncionesVarias.ocultaTecladoTrasEscribir(requireContext(), binding.listViewCiudades)

                // FALTA POR TIEMPO IMPLEMENTAR CON RETROFIT OBTENER LOS RESULTADOS DE LAS CIUDADES DE UNA API
                // Y RELLENAR LOS CAMPOS
                val arrayCiudades = arrayListOf(
                    "Sevilla",
                    "Madrid",
                    "Barcelona",
                    "Valencia",
                    "Zaragoza",
                    "Bilbao",
                    "Cadiz",
                )

                // Mostrar el resultado de la búsqueda en el listview
                adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    arrayCiudades
                )
                binding.listViewCiudades.adapter = adapter
                // Listener del listview
                binding.listViewCiudades.setOnItemClickListener { _, _, position, _ ->
                    // Se oculta el listview
                    binding.listViewCiudades.visibility = View.GONE

                    println(arrayCiudades[position])
                    // Acción deseada
                    Toast.makeText(
                        requireContext(),
                        "Ciudad seleccionada: ${arrayCiudades[position]}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                Toast.makeText(requireContext(), "Resultado búsqueda", Toast.LENGTH_SHORT).show()
                //adapter.filter.filter(query)
                return true
            }

            // Da el resultado de la búsqueda en cada pulsación de tecla
            override fun onQueryTextChange(newText: String?): Boolean {
                //Toast.makeText(requireContext(), "Resultado búsqueda al cambiar", Toast.LENGTH_SHORT).show()
                //adapter.filter.filter(newText)
                return true
            }
        })
    }

    //------------------   PERMISOS  ------------------------------------------------------------

    // -------- UBICACIÓN ------------

    val ubicacionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.containsValue(false)) {
            Toast.makeText(
                requireContext(),
                "No has aceptado los permisos",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            activaUbicacionActual()
        }

        it.forEach { mapa ->
            when (mapa.key) {
                Manifest.permission.ACCESS_FINE_LOCATION   -> {
                    if (mapa.value) {
                        Log.d("Permisos", "Permiso ACCESS_FINE_LOCATION concedido")
                    }
                }
                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    if (mapa.value) {
                        Log.d("Permisos", "Permiso ACCESS_COARSE_LOCATION concedido")
                    }
                }
            }
        }
    }


    private fun activaUbicacionActual() {
        if (
            ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            //mapa.isMyLocationEnabled = true  // Activa el punto de ubicación actual

            // Obtiene la ubicación actual
            val ubicacionActual = LatLng(
                viewModelCiudad.ubicacionActual.value!!.latitude,
                viewModelCiudad.ubicacionActual.value!!.longitude
            )

            OMapa.centrarMapa(ubicacionActual, mapa)
            OMapa.dibujaMarcador(ubicacionActual, mapa, viewModelCiudad)
        } else {
            ubicacionPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        }
    }
}


