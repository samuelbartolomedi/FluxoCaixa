import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "fluxo_caixa.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE LANCAMENTO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT,
                detalhe TEXT,
                valor REAL,
                data TEXT
            );
        """
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS LANCAMENTO")
        onCreate(db)
    }

    fun inserirLancamento(tipo: String, detalhe: String, valor: Double, data: String) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("tipo", tipo)
            put("detalhe", detalhe)
            put("valor", valor)
            put("data", data)
        }
        db.insert("LANCAMENTO", null, contentValues)
        db.close()
    }

    fun getLancamentos(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM LANCAMENTO", null)
    }

    @SuppressLint("Range")
    fun calcularSaldo(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT tipo, valor FROM LANCAMENTO", null)
        var saldo = 0.0

        while (cursor.moveToNext()) {
            val tipo = cursor.getString(cursor.getColumnIndex("tipo"))
            val valor = cursor.getDouble(cursor.getColumnIndex("valor"))
            saldo += if (tipo == "Crédito") valor else -valor
        }
        cursor.close()
        return saldo
    }
}
