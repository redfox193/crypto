package com.cryptott.logic

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cryptott.CryptoApp
import com.cryptott.model.TransactionModel
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WalletManager {
    companion object {
        private const val PREFS_NAME = "WalletPrefs"
        private const val WALLET_KEY = "wallet_data"

        private val _walletLiveData = MutableLiveData<MutableMap<String, Double>>(mutableMapOf())
        val walletLiveData: LiveData<MutableMap<String, Double>> = _walletLiveData

        private lateinit var sharedPrefs: SharedPreferences
        private val gson = Gson()

        fun initialize(context: Context) {
            sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadWallet()
        }

        private fun loadWallet() {
            val json = sharedPrefs.getString(WALLET_KEY, null)
            val walletMap = if (json != null) {
                gson.fromJson(json, object : TypeToken<MutableMap<String, Double>>() {}.type)
            } else {
                mutableMapOf("USD" to 500.0)
            }
            _walletLiveData.value = walletMap
        }

        private fun saveWallet() {
            val json = gson.toJson(_walletLiveData.value)
            sharedPrefs.edit().putString(WALLET_KEY, json).apply()
        }

        fun add(amount: Double): Boolean {
            val currentWallet = _walletLiveData.value ?: return false

            currentWallet["USD"] = currentWallet["USD"]!! + amount
            _walletLiveData.postValue(currentWallet)
            saveWallet()
            return true
        }

        fun buy(currency: String, amountUsd: Double, rate: Double): Boolean {
            val amountCurrency = Math.round((amountUsd / rate) * 1000000.0) / 1000000.0

            val currentWallet = _walletLiveData.value ?: return false

            if (currentWallet["USD"] ?: 0.0 >= amountUsd) {
                currentWallet["USD"] = currentWallet["USD"]!! - amountUsd
                currentWallet[currency] = (currentWallet[currency] ?: 0.0) + amountCurrency

                _walletLiveData.postValue(currentWallet)

                saveWallet()

                val currentDateTime = LocalDateTime.now()
                val customFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val dateTimeString = currentDateTime.format(customFormat)

                CryptoApp.getDBHelper().addTransaction(TransactionModel(
                    currencyFrom = "USD",
                    currencyTo = currency,
                    amountFrom = amountUsd,
                    amountTo = amountCurrency,
                    datetime = dateTimeString
                ))

                return true
            }
            return false
        }

        fun sell(currency: String, amount: Double, rate: Double): Boolean {
            val amountUsd = amount * rate

            val currentWallet = _walletLiveData.value ?: return false

            if (currentWallet[currency] ?: 0.0 >= amount) {
                currentWallet[currency] = currentWallet[currency]!! - amount
                currentWallet["USD"] = (currentWallet["USD"] ?: 0.0) + amountUsd

                // Update LiveData
                _walletLiveData.postValue(currentWallet)

                saveWallet()

                val currentDateTime = LocalDateTime.now()
                val customFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val dateTimeString = currentDateTime.format(customFormat)

                CryptoApp.getDBHelper().addTransaction(TransactionModel(
                    currencyFrom = currency,
                    currencyTo = "USD",
                    amountFrom = amount,
                    amountTo = amountUsd,
                    datetime = dateTimeString
                ))

                return true
            }
            return false
        }

        fun getWalletBalance(): Map<String, Double> {
            return _walletLiveData.value?.toMap() ?: emptyMap()
        }
    }
}