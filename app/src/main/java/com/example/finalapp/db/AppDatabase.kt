package com.example.finalapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
// Definición de la base de datos que contiene la tabla de compras.
@Database(entities = [Registro::class], version = 1 )
abstract class AppDatabase : RoomDatabase() {
    abstract fun registroDao():RegistroDao

    companion object {
    //  asegura que sea actualizada la propiedad automaticamnte.
    @Volatile private var BASE_DATOS : AppDatabase? = null
        fun getInstance(contexto: Context):AppDatabase {
        // synchronized previene el acceso de múltiples threads de manera simultánea.
        return BASE_DATOS ?: synchronized(this) {
            Room.databaseBuilder(
                contexto.applicationContext,
                AppDatabase::class.java,
                "RegistroBD.bd" // nombre de base datos de la app.
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { BASE_DATOS = it } } }
        }
    }
