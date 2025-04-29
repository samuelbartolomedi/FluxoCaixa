package com.example.fluxocaixa.ui

import com.example.fluxocaixa.data.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fluxocaixa.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerDetalhe: Spinner
    private lateinit var etValor: EditText
    private lateinit var etDataLancamento: EditText
    private lateinit var btnLancamento: Button
    private lateinit var btnVerLancamentos: Button
    private lateinit var btnSaldo: Button

    private val detalhesCredito = arrayOf("Salário", "Extras")
    private val detalhesDebito = arrayOf("Alimentação", "Transporte", "Saúde", "Moradia")

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        spinnerTipo = findViewById(R.id.spinnerTipo)
        spinnerDetalhe = findViewById(R.id.spinnerDetalhe)
        etValor = findViewById(R.id.etValor)
        btnLancamento = findViewById(R.id.btnLancamento)
        btnVerLancamentos = findViewById(R.id.btnVerLancamentos)
        btnSaldo = findViewById(R.id.btnSaldo)
        etDataLancamento = findViewById(R.id.etDataLancamento)

        val tipoAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("Crédito", "Débito"))
        spinnerTipo.adapter = tipoAdapter

        val detalheAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf(""))
        spinnerDetalhe.adapter = detalheAdapter

        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val detalhes = if (position == 0) detalhesCredito else detalhesDebito
                val detalheAdapter =
                    ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, detalhes)
                spinnerDetalhe.adapter = detalheAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnLancamento.setOnClickListener {
            realizarLancamento()
        }

        btnVerLancamentos.setOnClickListener {
            startActivity(Intent(this, LancamentosActivity::class.java))
        }

        btnSaldo.setOnClickListener {
            mostrarSaldo();
        }

        etDataLancamento.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendario = Calendar.getInstance()
        val datePicker = DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                calendario.set(Calendar.YEAR, year)
                calendario.set(Calendar.MONTH, month)
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataSelecionada = formatoData.format(calendario.time)
                etDataLancamento.setText(dataSelecionada)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun mostrarSaldo(){
        val saldo = dbHelper.calcularSaldo()
        AlertDialog.Builder(this)
            .setTitle("Saldo")
            .setMessage("Saldo atual: R$$saldo")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("FluxoCaixaPreferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            val tipoSelecionado = spinnerTipo.selectedItem.toString()
            val detalheSelecionado = spinnerDetalhe.selectedItem.toString()
            val valorDigitado = etValor.text.toString()
            val dataSelecionada = etDataLancamento.text.toString()

            putString("TIPO", tipoSelecionado)
            putString("DETALHE", detalheSelecionado)
            putString("VALOR", valorDigitado)
            putString("DATA", dataSelecionada)

            apply()
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("FluxoCaixaPreferences", MODE_PRIVATE)

        val tipo = sharedPreferences.getString("TIPO", "")
        val detalhe = sharedPreferences.getString("DETALHE", "")
        val valor = sharedPreferences.getString("VALOR", "")
        val data = sharedPreferences.getString("DATA", "")

        if (!tipo.isNullOrEmpty()) {
            val tipoAdapter = spinnerTipo.adapter
            for (i in 0 until tipoAdapter.count) {
                if (tipoAdapter.getItem(i).toString() == tipo) {
                    spinnerTipo.setSelection(i)
                    break
                }
            }
        }

        if (!detalhe.isNullOrEmpty()) {
            val detalheAdapter = spinnerDetalhe.adapter
            for (i in 0 until detalheAdapter.count) {
                if (detalheAdapter.getItem(i).toString() == detalhe) {
                    spinnerDetalhe.setSelection(i)
                    break
                }
            }
        }

        etValor.setText(valor)
        etDataLancamento.setText(data)
    }

    private fun realizarLancamento() {
        val tipo = spinnerTipo.selectedItem.toString()
        val detalhe = spinnerDetalhe.selectedItem.toString()
        val valor = etValor.text.toString().toDouble()
        val dataLancamento = etDataLancamento.text.toString()

        if (valor == null || dataLancamento.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
            return
        }

        val lancamento = Lancamento(
            tipo = tipo,
            detalhe = detalhe,
            valor = valor,
            dataLancamento = dataLancamento
        )

        val suceso = dbHelper.inserirLancamento(lancamento)
        if (suceso) {
            Toast.makeText(this, "Lançamento salvo com sucesso!", Toast.LENGTH_SHORT).show()
            etValor.text.clear()
            etDataLancamento.text.clear();
        } else {
            Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show()
        }
    }
}