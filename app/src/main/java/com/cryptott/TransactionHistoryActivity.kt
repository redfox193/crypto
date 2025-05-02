package com.cryptott

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cryptott.adapter.TransactionAdapter
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.StringUtils

class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var symbol: String
    private lateinit var adapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvCurrency: TextView
    private lateinit var tvBought: TextView
    private lateinit var tvSold: TextView
    private lateinit var tvProfit: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        tvCurrency = findViewById(R.id.tvCurrency)
        tvBought = findViewById(R.id.tvBought)
        tvSold = findViewById(R.id.tvSold)
        tvProfit = findViewById(R.id.tvProfit)

        symbol = intent.getStringExtra("symbol") ?: run {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            adapter = this@TransactionHistoryActivity.adapter
        }
    }

    private fun loadData() {
        val dbHelper = (application as CryptoApp).dbHelper

        val stats = dbHelper.getTransactionStats(symbol)
        tvCurrency.text = symbol
        tvBought.text = "${getString(R.string.bought)}: ${String.format("%.6f", stats.currencyBought)}"
        tvSold.text = "${getString(R.string.sold)}: ${String.format("%.6f", stats.currencySold)}"
        tvProfit.text = "${getString(R.string.profit)}: ${String.format("%.2f", Math.round(stats.profit * 100.0) / 100.0)}$"

        val transactions = dbHelper.getTransactionsOrderedByDatetime(symbol)
        adapter.submitList(transactions)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}