package com.example.ciudades

import android.app.Application
import com.example.ciudades.domain.room.MyDataBase
import com.example.ciudades.domain.room.Repositorio

class AppCiudades: Application() {
    val dataBase by lazy { MyDataBase.getDatabase(this)}
    val repositorio by lazy { Repositorio(dataBase.daoCiudades()) }
}