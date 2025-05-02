package com.cryptott

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cryptott.adapter.CryptoRatesAdapter
import com.cryptott.api.CurrencyApi
import com.cryptott.logic.WalletManager
import com.cryptott.model.CryptoRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CryptoRatesActivity : AppCompatActivity() {
    private lateinit var walletBalance: TextView
    private lateinit var addMoneyButton: Button
    private lateinit var cryptoRatesRecyclerView: RecyclerView
    private lateinit var adapter: CryptoRatesAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val symbols = listOf("BTC", "ETH", "TON", "BNB", "DOGE", "USDT", "PI")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WalletManager.initialize(applicationContext)

        setContentView(R.layout.activity_crypto_rates)

        walletBalance = findViewById(R.id.walletBalance)
        addMoneyButton = findViewById(R.id.addMoneyButton)
        cryptoRatesRecyclerView = findViewById(R.id.cryptoRatesRecyclerView)

        setupRecyclerView()
        setupAddMoneyButton()
        fetchCryptoRates()

        WalletManager.walletLiveData.observe(this) { wallet ->
            updateWalletBalanceViews(wallet)
            adapter.updateBalance()
        }
    }

    private fun updateWalletBalanceViews(wallet: Map<String, Double>) {
        walletBalance.text = "$${String.format("%.2f", WalletManager.getWalletBalance()["USD"])}"
    }

    private fun setupRecyclerView() {
        adapter = CryptoRatesAdapter()
        cryptoRatesRecyclerView.layoutManager = LinearLayoutManager(this)
        cryptoRatesRecyclerView.adapter = adapter
    }

    private fun setupAddMoneyButton() {
        addMoneyButton.setOnClickListener {
            WalletManager.add(0.01)
        }
    }

    private fun fetchCryptoRates() {
        CoroutineScope(Dispatchers.IO).launch {
            var rates: List<CryptoRate>
            Log.d("check", "rates fetched")
            try {
                rates = symbols.map { symbol ->
                    val rate = CurrencyApi.fetchCryptoRateFromAPI(symbol)
                    CryptoRate(symbol, rate)
                }
            } catch (e: Exception) {
                rates = symbols.map { symbol ->
                    CryptoRate(symbol, 0.0)
                }
            }
            withContext(Dispatchers.Main) {
                adapter.updateRates(rates)
            }
        }
    }

    private val fetchRatesRunnable = object : Runnable {
        override fun run() {
            fetchCryptoRates()
            handler.postDelayed(this, 10_000L)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(fetchRatesRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(fetchRatesRunnable)
    }
} 