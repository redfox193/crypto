package com.cryptott.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test


class CurrencyApiTest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchCryptoRateFromAPI returns correct rate for valid response`() {
        val testCurrency = "BTC"
        val testRate = 50000.0
        val jsonResponse = JSONObject().apply {
            put("USD", testRate)
        }.toString()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
        )

        val result = CurrencyApi.fetchCryptoRateFromAPI(testCurrency)

        assert(result != null)
    }

    @Test
    fun `fetchHistoricalDataFromAPI returns correct data for 1h period`() {
        val testCurrency = "BTC"
        val testPeriod = "1h"
        val testData = listOf(
            Pair(1000L, 50000.0),
            Pair(2000L, 50100.0)
        )

        val jsonResponse = JSONObject().apply {
            put("Data", JSONObject().apply {
                put("Data", JSONObject().apply {
                    put("time", testData[0].first / 1000)
                    put("close", testData[0].second)
                })
                put("Data", JSONObject().apply {
                    put("time", testData[1].first / 1000)
                    put("close", testData[1].second)
                })
            })
        }.toString()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
        )

        val result = CurrencyApi.fetchHistoricalDataFromAPI(testCurrency, testPeriod)

        assert(result != null)
    }

    @Test
    fun `fetchHistoricalDataFromAPI returns correct data for 24h period`() {
        val testCurrency = "ETH"
        val testPeriod = "24h"
        val testData = listOf(
            Pair(1000L, 2000.0),
            Pair(2000L, 2010.0)
        )

        val jsonResponse = JSONObject().apply {
            put("Data", JSONObject().apply {
                put("Data", JSONObject().apply {
                    put("time", testData[0].first / 1000)
                    put("close", testData[0].second)
                })
                put("Data", JSONObject().apply {
                    put("time", testData[1].first / 1000)
                    put("close", testData[1].second)
                })
            })
        }.toString()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
        )

        val result = CurrencyApi.fetchHistoricalDataFromAPI(testCurrency, testPeriod)

        assert(result != null)
    }

    @Test
    fun `fetchHistoricalDataFromAPI returns correct data for 7d period`() {
        val testCurrency = "SOL"
        val testPeriod = "7d"
        val testData = listOf(
            Pair(1000L, 100.0),
            Pair(2000L, 105.0)
        )

        val jsonResponse = JSONObject().apply {
            put("Data", JSONObject().apply {
                put("Data", JSONObject().apply {
                    put("time", testData[0].first / 1000)
                    put("close", testData[0].second)
                })
                put("Data", JSONObject().apply {
                    put("time", testData[1].first / 1000)
                    put("close", testData[1].second)
                })
            })
        }.toString()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
        )

        val result = CurrencyApi.fetchHistoricalDataFromAPI(testCurrency, testPeriod)

        assert(result != null)
    }
}