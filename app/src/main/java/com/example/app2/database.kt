package com.example.app2

import com.example.app2.Registro
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "registros.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE registros (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT, " +
                    "mensaje TEXT, " +
                    "hora TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS registros")
        onCreate(db)
    }

    fun insertarRegistro(registro: Registro) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", registro.nombre)
            put("mensaje", registro.mensaje)
            put("hora", registro.hora)
        }
        db.insert("registros", null, values)
    }

    fun obtenerRegistros(): List<Registro> {
        val lista = mutableListOf<Registro>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM registros ORDER BY id ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val item = Registro(
                    nombre = cursor.getString(1),
                    mensaje = cursor.getString(2),
                    hora = cursor.getString(3)
                )
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}
