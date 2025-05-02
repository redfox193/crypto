package com.cryptott.api

import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class CurrencyApi {
    companion object {
        fun fetchCryptoRateFromAPI(currency: String): Double {
            val url = URL("https://min-api.cryptocompare.com/data/price?fsym=${currency}&tsyms=USD")
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)

            val rate = jsonObject.getDouble("USD")
            return rate
        }

        fun fetchHistoricalDataFromAPI(currency: String, period: String): List<Pair<Long, Double>> {
            val limit = when (period) {
                "1h" -> 60
                "24h" -> 24*60
                else -> 24*7
            }

            val url = when (period) {
                "1h" -> URL("https://min-api.cryptocompare.com/data/v2/histominute?fsym=$currency&tsym=USD&limit=$limit")
                "24h" -> URL("https://min-api.cryptocompare.com/data/v2/histominute?fsym=$currency&tsym=USD&limit=$limit")
                else -> URL("https://min-api.cryptocompare.com/data/v2/histohour?fsym=$currency&tsym=USD&limit=$limit")
            }

            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)
            val data = jsonObject.getJSONObject("Data").getJSONArray("Data")

            return (0 until data.length()).map { i ->
                val item = data.getJSONObject(i)
                val timestamp = item.getLong("time") * 1000
                val price = item.getDouble("close")
                timestamp to price
            }
        }
    }
}