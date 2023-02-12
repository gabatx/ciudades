package com.example.ciudades.domain.room

import androidx.lifecycle.LiveData
import com.example.ciudades.domain.models.Ciudad


class Repositorio (val daoCiudad: DaoCiudad){

    suspend fun borrarTodasLasCiudades() {
        daoCiudad.borrarTodasCiudades()
    }



    suspend fun nuevaCiudad(ciudad: Ciudad) {
        daoCiudad.nuevaCiudad(ciudad)
    }

    fun buscarPorNombre(nombre: String): LiveData<List<Ciudad>> {
        return daoCiudad.buscarPorNombre(nombre)
    }

    suspend fun actualizarCiudad(ciudad: Ciudad) {
        daoCiudad.actualizarCiudad(ciudad)
    }

    fun borrarCiudad(ciudad: Ciudad?) {
        daoCiudad.borrarCiudad(ciudad)
    }

    // FUNCIONES HACIA DAO
    val todasLasCiudades = daoCiudad.todasLasCiudades()

}


