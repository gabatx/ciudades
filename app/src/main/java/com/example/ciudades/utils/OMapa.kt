package com.example.ciudades.utils

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation
import com.example.ciudades.R
import com.example.ciudades.viewmodel.ViewModelCiudad
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

object OMapa {

     var marker: Marker? = null

    fun configurarMapa (map: GoogleMap, tipoMapa: Int ) : GoogleMap {
        // Tipo de mapa:
        map.mapType = tipoMapa
        // Activar el tráfico
        map.isTrafficEnabled = true
        // Creamos una variable que nos permita crear ajustes de mapa
        val uiSettings = map.uiSettings
        // Todos los gestos de la aplicación se habilitan
        uiSettings.isZoomControlsEnabled = true //controles de zoom
        uiSettings.isCompassEnabled = true //mostrar la brújula
        uiSettings.isZoomGesturesEnabled = true //gestos de zoom
        uiSettings.isScrollGesturesEnabled = true //Gestos de scroll
        uiSettings.isTiltGesturesEnabled = true //Gestos de ángulo
        uiSettings.isRotateGesturesEnabled = true //Gestos de rotación
        uiSettings.isMyLocationButtonEnabled = true //botón de localización
        uiSettings.isMapToolbarEnabled = true //barra de herramientas
        uiSettings.isIndoorLevelPickerEnabled = true //selector de niveles

        return map
    }

    fun actualizaPosicionArrastrar(map: GoogleMap, viewModelCiudad: ViewModelCiudad){
        // Al arrastrar el market actualizamos la posición
        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(p0: Marker) {}
            override fun onMarkerDrag(p0: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                // Actualizamos la posición del marcador al soltar el market
                viewModelCiudad.latitudVariable.value = marker.position.latitude
                viewModelCiudad.longitudVariable.value = marker.position.longitude
            }
        })
    }

    fun aniadirMarcadorPulsar(map: GoogleMap, viewModelCiudad: ViewModelCiudad){

        map.setOnMapClickListener {
            dibujaMarcador(it, map, viewModelCiudad)   // Añado un marcador en la posición del click
            centrarMapa(it, map) // Centramos el mapa en el marcador
        }
    }

    fun dibujaMarcador(it: LatLng, map: GoogleMap, viewModelCiudad: ViewModelCiudad) {
        // Borramos el market anterior
        marker?.remove()
        // Añadimos un nuevo market
        marker = map.addMarker(
            MarkerOptions()
                .position(it)
                .draggable(true)
        )!!

        // Actualizamos la latitud y longitud
        viewModelCiudad.latitudVariable.value = it.latitude
        viewModelCiudad.longitudVariable.value = it.longitude
    }

    fun centrarMapa(it: LatLng, map: GoogleMap) {
        // ---------- Centramos el mapa en el marcador añadido ----------
        val posicionMarcador = CameraUpdateFactory.newLatLngZoom(
            LatLng(it.latitude, it.longitude),
            5f
        )
        // Te lleva a la coordenada que le digamos mediante animación y le coloca un zoom
        map.setOnMapLoadedCallback {
            map.animateCamera(posicionMarcador)
        }
    }

    fun muestraMarcadorCiudadDetalle(map: GoogleMap, latitud: Double, longitud: Double) {
        // Borramos el market anterior
        marker?.remove()

        marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(latitud, longitud))
                .draggable(false) // Desactivamos el arrastre del marker
        )!!

        map.setOnMapLoadedCallback {
            // Centramos el mapa en el marcador añadido
            val posicionMarcador = CameraUpdateFactory.newLatLngZoom(
                LatLng(latitud, longitud),
                9f
            )
            map.moveCamera(posicionMarcador)
        }
    }

    fun todosMarcadoresMapa(map: GoogleMap, viewModelCiudad: ViewModelCiudad) {

        // Creamos una zona rectangular
        val zona = LatLngBounds.builder()
        viewModelCiudad.listaCiudades.value?.forEach { lugar ->
            // Añadimos las coordenadas de cada lugar
            zona.include(LatLng(lugar.Latitud, lugar.Longitud))

            // Añadimos el marcador en la ubicación de cada lugar
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(lugar.Latitud, lugar.Longitud))
                    .title(lugar.nombre)
                    .snippet("Pulsa para más información")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }
        // Indicamos la zona rectangular
        map.setOnMapLoadedCallback { // Actúa cuando se ha cargado el mapa está cargado al 100%
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(zona.build(), 200))
        }
    }

    fun listenerMarcadoresMapa(map: GoogleMap, viewModelCiudad: ViewModelCiudad, activity: ConstraintLayout) {
        // Listener que se activa al pulsar sobre un marcador
        map.setOnInfoWindowClickListener { marker ->
            // Obtenemos el objeto del lugar
            viewModelCiudad.ciudadSeleccionada.value = viewModelCiudad.listaCiudades.value?.filter { lugar -> marker.title == lugar.nombre }?.get(0)
            // Navegamos al fragment detalle
            Navigation.findNavController(activity).navigate(R.id.action_app_bar_Mapa_Ciudades_to_ciudadDetalleFragment)
        }
    }
}