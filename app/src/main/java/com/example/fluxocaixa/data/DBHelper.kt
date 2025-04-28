package com.example.fluxocaixa.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "fluxo_caixa.db", null, 1) {
    companion object {
        private const val DATABASE_NAME = "fluxo_caixa.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "LANCAMENTO"
        private const val COL_ID = "_id"
        private const val COL_TIPO = "tipo"
        private const val COL_DETALHE = "detalhe"
        private const val COL_VALOR = "valor"
        private const val COL_DATA = "data_lancamento"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TIPO TEXT,
                $COL_DETALHE TEXT,
                $COL_VALOR REAL,
                $COL_DATA TEXT
            );
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun inserirLancamento(lancamento: Lancamento): Boolean {
        val db = writableDatabase
        db.isOpen();
        val contentValues = ContentValues().apply {
            put(COL_TIPO, lancamento.tipo)
            put(COL_DETALHE, lancamento.detalhe)
            put(COL_VALOR, lancamento.valor)
            put(COL_DATA, lancamento.dataLancamento)
        }
        val resultado = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return resultado != -1L
    }

    fun getLancamentos(): List<Lancamento> {
        val listLancamento = mutableListOf<Lancamento>()
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val lancamento = Lancamento(
                    _id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO)),
                    detalhe = cursor.getString(cursor.getColumnIndexOrThrow(COL_DETALHE)),
                    valor = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_VALOR)),
                    dataLancamento = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA))
                )
                listLancamento.add(lancamento)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listLancamento
    }

    fun calcularSaldo(): Double {
        var saldo = 0.0

        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_TIPO, $COL_VALOR FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
                saldo += if (tipo == "Crédito") valor else -valor
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return saldo
    }
}
