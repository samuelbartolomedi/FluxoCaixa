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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fluxocaixa.R
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerDetalhe: Spinner
    private lateinit var etValor: EditText
    private lateinit var tvData: TextView
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
        tvData = findViewById(R.id.tvData)
        btnLancamento = findViewById(R.id.btnLancamento)
        btnVerLancamentos = findViewById(R.id.btnVerLancamentos)
        btnSaldo = findViewById(R.id.btnSaldo)

        val tipoAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("Crédito", "Débito"))
        spinnerTipo.adapter = tipoAdapter

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

        tvData.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                tvData.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
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

    private fun realizarLancamento() {
        val tipo = spinnerTipo.selectedItem.toString()
        val detalhe = spinnerDetalhe.selectedItem.toString()
        val valor = etValor.text.toString().toDouble()
        val dataLancamento = tvData.text.toString()

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
            tvData.text = "Data lançamento:"
        } else {
            Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show()
        }
    }
}