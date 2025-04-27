package com.example.fluxocaixa

import DBHelper
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerDetalhe: Spinner
    private lateinit var etValor: EditText
    private lateinit var datePicker: DatePicker
    private lateinit var btnLancamento: Button
    private lateinit var btnVerLancamentos: Button
    private lateinit var btnSaldo: Button

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        spinnerTipo = findViewById(R.id.spinnerTipo)
        spinnerDetalhe = findViewById(R.id.spinnerDetalhe)
        etValor = findViewById(R.id.etValor)
        datePicker = findViewById(R.id.datePicker)
        btnLancamento = findViewById(R.id.btnLancamento)
        btnVerLancamentos = findViewById(R.id.btnVerLancamentos)
        btnSaldo = findViewById(R.id.btnSaldo)

        // Spinner Tipo
        val tipos = arrayOf("Crédito", "Débito")
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapterTipo

        // Spinner Detalhe (dinâmico com base em Crédito ou Débito)
        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val tipoSelecionado = tipos[position]
                val detalhes = if (tipoSelecionado == "Crédito") {
                    arrayOf("Salário", "Extras")
                } else {
                    arrayOf("Alimentação", "Transporte", "Saúde", "Moradia")
                }
                val adapterDetalhe = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, detalhes)
                adapterDetalhe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDetalhe.adapter = adapterDetalhe
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Lançar
        btnLancamento.setOnClickListener {
            val tipo = spinnerTipo.selectedItem.toString()
            val detalhe = spinnerDetalhe.selectedItem.toString()
            val valor = etValor.text.toString().toDouble()
            val data = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"
            dbHelper.inserirLancamento(tipo, detalhe, valor, data)
        }

        // Ver Lançamentos
        btnVerLancamentos.setOnClickListener {
            val lancamentos = dbHelper.getLancamentos()
            val builder = StringBuilder()
            while (lancamentos.moveToNext()) {
                val tipo = lancamentos.getString(lancamentos.getColumnIndex("tipo"))
                val detalhe = lancamentos.getString(lancamentos.getColumnIndex("detalhe"))
                val valor = lancamentos.getDouble(lancamentos.getColumnIndex("valor"))
                val data = lancamentos.getString(lancamentos.getColumnIndex("data"))
                builder.append("Tipo: $tipo - $data - $detalhe - R$$valor\n")
            }
            lancamentos.close()
            AlertDialog.Builder(this)
                .setTitle("Lançamentos")
                .setMessage(builder.toString())
                .setPositiveButton("OK", null)
                .show()
        }

        // Calcular Saldo
        btnSaldo.setOnClickListener {
            val saldo = dbHelper.calcularSaldo()
            AlertDialog.Builder(this)
                .setTitle("Saldo")
                .setMessage("Saldo atual: R$$saldo")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}