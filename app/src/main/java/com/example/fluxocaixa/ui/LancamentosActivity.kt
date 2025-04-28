package com.example.fluxocaixa.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.fluxocaixa.R
import com.example.fluxocaixa.data.DBHelper

class LancamentosActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lancamentos)

        listView = findViewById(R.id.listViewTransactions)
        dbHelper = DBHelper(this)

        val transactions = dbHelper.getLancamentos()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            transactions.map {
                "${it.tipo} - ${it.dataLancamento} - ${it.detalhe} - R$ %.2f".format(it.valor)
            }
        )
        listView.adapter = adapter
    }
}
