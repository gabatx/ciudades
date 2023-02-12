package com.example.ciudades.domain.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.ciudades.domain.models.Ciudad

@Dao
interface DaoCiudad {

    /*Dame todos las ciudades*/
    @Query("SELECT * FROM ciudad order by distancia asc")
    fun todasLasCiudades(): LiveData<List<Ciudad>>

    /* Añade una ciudad */
    @Insert
    suspend fun nuevaCiudad(ciudad: Ciudad)

    @Query("DELETE FROM ciudad")
    suspend fun borrarTodasCiudades()

    /* Selecciona por nombre */
    @Query("SELECT * FROM ciudad WHERE nombre LIKE '%' || :nombre || '%'")
    fun buscarPorNombre(nombre: String): LiveData<List<Ciudad>>

    // Actualiza una ciudad
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun actualizarCiudad(ciudad: Ciudad)

    // Borra una ciudad
    @Delete
    fun borrarCiudad(ciudad: Ciudad?)
}