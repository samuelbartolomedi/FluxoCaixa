package com.example.fluxocaixa.data

data class Lancamento(
    val _id: Int = 0,
    val tipo: String,
    val detalhe: String,
    val valor: Double,
    val dataLancamento: String
)
