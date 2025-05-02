package com.cryptott.model

data class TransactionStatsModel(
    val currency: String,
    val currencyBought: Double,
    val currencySold: Double,
    val profit: Double
)