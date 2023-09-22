package com.example.finalapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// Acceso a datos de la entidad creada anteriormente a la cual se le hara algunas consultas y operaciones en la bd.
@Dao
interface RegistroDao {

    @Query("SELECT * FROM Registro ORDER BY orden")
    fun getAllRegistros(): List<Registro>

    @Query("SELECT COUNT(*) FROM Registro")
    fun contarRegistros(): Int

    @Insert
    suspend fun insertarRegistro(registro: Registro): Long

    @Update
    suspend fun actualizarRegistro(registro: Registro)

    @Delete
    suspend fun eliminarRegistro(registro: Registro)
}