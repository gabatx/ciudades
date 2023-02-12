package com.example.ciudades.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.lifecycle.*
import com.example.ciudades.AppCiudades
import com.example.ciudades.domain.models.Ciudad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelCiudad(aplicacion : Application): ViewModel() {


    val repositorio = (aplicacion as AppCiudades).repositorio

    val imageUri = MutableLiveData<Uri>()
    var listaCiudades: LiveData<List<Ciudad>>
    var nombreBuscar = MutableLiveData<String>()
    val ciudadSeleccionada = MutableLiveData<Ciudad>()
    val configCiudad = MutableLiveData<Ciudad>()
    var origen = MutableLiveData<String>()
    val latitudVariable = MutableLiveData<Double>()
    val longitudVariable = MutableLiveData<Double>()
    val toast = MutableLiveData<String>()
    val ubicacionActual = MutableLiveData<Location>()



    // FUNCIONES HACIA EL REPOSITORIO
    init{
        // No se utiliza .value porque el Live data ya lo está obteniendo el repositorio. Se utilizará cuando cambiemos el valor
        listaCiudades = repositorio.todasLasCiudades
    }

    fun borrarTodasLasCiudades() {
        viewModelScope.launch {
            repositorio.borrarTodasLasCiudades()
        }
    }

    fun nuevaCiudad(ciudad: Ciudad) {
        viewModelScope.launch {
            repositorio.nuevaCiudad(ciudad)
        }
    }


    // FUNCIÓN MyViewModelFactory que ayuda
    class MyViewModelFactory(val aplicacion: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Application::class.java)
                .newInstance(aplicacion)
        }
    }

    fun buscarPorNombre(nombre: String) : LiveData<List<Ciudad>> {
        return repositorio.buscarPorNombre(nombre)
    }

    fun actualizarCiudad(ciudad: Ciudad) {
        CoroutineScope(Dispatchers.IO).launch {
            repositorio.actualizarCiudad(ciudad)
        }
    }

    fun borrarCiudad(value: Ciudad?) {
        CoroutineScope(Dispatchers.IO).launch {
            repositorio.borrarCiudad(value)
        }
    }

    // FUNCIÓN QUE CALCULA LA DISTANCIA ENTRE DOS CIUDADES
    @SuppressLint("SetTextI18n")
    fun calculaDistanciaUbicacionActualDestino(ciudad: Ciudad): Double {
        // distanceTo o distanceBetween
        val distancia = ubicacionActual.value?.distanceTo(Location(LocationManager.GPS_PROVIDER).apply {
            latitude = ciudad.Latitud
            longitude = ciudad.Longitud
        }) ?: 0.0

        val resultadoString = String.format("%.3f", distancia.toDouble().div(1000))
        val resultadoDouble = resultadoString.split(",").joinTo(StringBuffer(), ".")
        return resultadoDouble.toString().toDouble()
    }
}