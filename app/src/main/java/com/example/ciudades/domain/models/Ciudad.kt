package com.example.ciudades.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ciudad")
data class Ciudad(
        var nombre: String,
        var pais: String,
        var Latitud: Double,
        var Longitud: Double,
        var Imagen: String,
        var Visitada: Boolean,
        var distancia: Double = 0.0
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
