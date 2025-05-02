package com.cryptott.model

data class TransactionModel(
    val currencyFrom: String,
    val currencyTo: String,
    val amountFrom: Double,
    val amountTo: Double,
    val datetime: String
)
