package com.example.ciudades.domain.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ciudades.domain.models.Ciudad


/* Aqui añadimos tantas tablas como queramos separado por comas -> [Contacto::class, Departamento::class] */
@Database(entities = [Ciudad::class], version = 1)
/* Para una base de datos diferente debemos cambiar el nombre  */
abstract class MyDataBase : RoomDatabase() {
    abstract fun daoCiudades(): DaoCiudad

    companion object {
        // Aquí escribimos el nombre de nuestra base de datos. Debemos tener cuidado ya que deben ser nombres únicos para cada id (com.estech.appcontactos)
        const val DBNAME = "appCiudades"

        @Volatile
        private var INSTANCE: MyDataBase? = null

        fun getDatabase(context: Context): MyDataBase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(MyDataBase::class) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDataBase::class.java,
                    DBNAME
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}