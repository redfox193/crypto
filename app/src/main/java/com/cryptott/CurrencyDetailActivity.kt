package com.cryptott

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.cryptott.api.CurrencyApi
import com.cryptott.logic.WalletManager
import com.cryptott.model.CryptoRate
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection



class CurrencyDetailActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var walletTradeBalance: TextView
    private lateinit var walletCryptoBalance: TextView
    private lateinit var currencyName: TextView
    private lateinit var currentPrice: TextView
    private lateinit var priceChart: LineChart
    private lateinit var periodChipGroup: ChipGroup
    private lateinit var chartPeriod: TextView

    private lateinit var inputBuyValue: EditText
    private lateinit var buyButton: TextView
    private lateinit var buyHint: TextView

    private lateinit var inputSellValue: EditText
    private lateinit var sellButton: TextView
    private lateinit var sellHint: TextView

    private lateinit var transactionHistory: Button

    private var symbol: String = ""
    private var period: String = ""
    private var currentRate: Double = 0.0
    private lateinit var historicalData: List<Pair<Long, Double>>

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_detail)

        symbol = intent.getStringExtra("symbol") ?: ""

        initViews()
        setupToolbar()
        setupChart()
        setupPeriodSelector()

        fetchCryptoRate()
        loadHistoricalData("24h")

        WalletManager.walletLiveData.observe(this) { wallet ->
            updateWalletBalanceViews(wallet)
        }

        inputBuyValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputText = s?.toString() ?: ""
                if (inputText.isNotEmpty() && currentRate != 0.0) {
                    try {
                        val amount = inputText.toDouble() / currentRate
                        buyHint.text = "${symbol} ${String.format("%.6f", amount)}"
                    } catch (e: NumberFormatException) {
                        buyHint.text = ""
                    }
                } else {
                    buyHint.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        inputSellValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputText = s?.toString() ?: ""
                if (inputText.isNotEmpty() && currentRate != 0.0) {
                    try {
                        val amount = inputText.toDouble() * currentRate
                        sellHint.text = "$ ${String.format("%.2f", amount)}"
                    } catch (e: NumberFormatException) {
                        sellHint.text = ""
                    }
                } else {
                    sellHint.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        buyButton.setOnClickListener{
            if(currentRate == 0.0) {
                Toast.makeText(
                    this@CurrencyDetailActivity,
                    "Unable to get load current exchange rate",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val inputText = inputBuyValue.text.toString()
            if(inputText != "") {
                val amount = inputText.toDouble()
                val res = WalletManager.buy(symbol, amount, currentRate)
                if(!res) {
                    Toast.makeText(
                        this@CurrencyDetailActivity,
                        "Not enough money",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                inputBuyValue.setText("")
            }
        }

        sellButton.setOnClickListener{
            if(currentRate == 0.0) {
                Toast.makeText(
                    this@CurrencyDetailActivity,
                    "Unable to get load current exchange rate",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val inputText = inputSellValue.text.toString()
            if(inputText != "") {
                val amount = inputText.toDouble()
                val res = WalletManager.sell(symbol, amount, currentRate)
                if(!res) {
                    Toast.makeText(
                        this@CurrencyDetailActivity,
                        "Not enough money",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                inputSellValue.setText("")
            }
        }

        transactionHistory.setOnClickListener{
            val intent = Intent(this, TransactionHistoryActivity::class.java).apply {
                putExtra("symbol", symbol)
            }
            startActivity(intent)
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        currencyName = findViewById(R.id.currencyName)
        currentPrice = findViewById(R.id.currentPrice)
        priceChart = findViewById(R.id.priceChart)
        periodChipGroup = findViewById(R.id.periodChipGroup)
        walletTradeBalance = findViewById(R.id.walletTradeBalance)
        walletCryptoBalance = findViewById(R.id.walletCryptoBalance)
        chartPeriod = findViewById(R.id.chartPeriod)
        chartPeriod = findViewById(R.id.chartPeriod)

        inputBuyValue = findViewById(R.id.intputBuyValue)
        buyButton = findViewById(R.id.buyButton)
        buyHint = findViewById(R.id.buyHint)

        inputSellValue = findViewById(R.id.inputSellValue)
        sellButton = findViewById(R.id.sellButton)
        sellHint = findViewById(R.id.sellHint)

        transactionHistory = findViewById(R.id.transactionHistory)

        currencyName.text = symbol
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun setupChart() {
        priceChart.description.isEnabled = false
        priceChart.setTouchEnabled(true)
        priceChart.isDragEnabled = true
        priceChart.setScaleEnabled(true)
        priceChart.setPinchZoom(true)
        priceChart.setDrawGridBackground(false)
        priceChart.axisRight.isEnabled = false

        val xAxis = priceChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        priceChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let { entry ->
                    val index = entry.x.toInt()
                    if (index >= 0 && index < historicalData.size) {
                        val (timestamp, value) = historicalData[index]
                        val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            .format(Date(timestamp))

                        Toast.makeText(
                            this@CurrencyDetailActivity,
                            "$date\n$${String.format("%.2f", entry.y)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@CurrencyDetailActivity,
                            "$${String.format("%.2f", entry.y)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onNothingSelected() {
                // Optional: Handle when nothing is selected
            }
        })
    }

    private fun setupPeriodSelector() {
        periodChipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip1h -> loadHistoricalData("1h")
                R.id.chip24h -> loadHistoricalData("24h")
                R.id.chip7d -> loadHistoricalData("7d")
            }
        }
    }

    private fun updateWalletBalanceViews(wallet: Map<String, Double>) {
        walletTradeBalance.text = "$${String.format("%.2f", WalletManager.getWalletBalance()["USD"])}"
        walletCryptoBalance.text = String.format("%.6f", WalletManager.getWalletBalance().getOrDefault(symbol,0.0))
    }

    fun millisecondsToDateTime(milliseconds: Long): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(Date(milliseconds))
    }

    private fun loadHistoricalData(period: String) {
        this.period = period
        CoroutineScope(Dispatchers.IO).launch {
            try {
                historicalData = CurrencyApi.fetchHistoricalDataFromAPI(symbol, period)
                withContext(Dispatchers.Main) {
                    updateChart(historicalData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun updateChart(data: List<Pair<Long, Double>>) {
        val entries = data.mapIndexed { index, (_, price) ->
            Entry(index.toFloat(), price.toFloat())
        }

        val dataSet = LineDataSet(entries, "Price, USD")
        dataSet.color = Color.parseColor("#3F51B5")
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        dataSet.mode = LineDataSet.Mode.LINEAR

        val lineData = LineData(dataSet)
        priceChart.data = lineData
        priceChart.invalidate()

        val mil_from = millisecondsToDateTime(data.first().first)
        val mil_to = millisecondsToDateTime(data.last().first)
        chartPeriod.text = "${mil_from} - ${mil_to}"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun fetchCryptoRate() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("check", "rate ${symbol} fetched")
            try {
                currentRate = CurrencyApi.fetchCryptoRateFromAPI(symbol)
            } catch (e: Exception) {
                currentRate = 0.0
            }
            withContext(Dispatchers.Main) {
                if (currentRate == 0.0)
                    currentPrice.text = "..."
                else
                    currentPrice.text = "$${String.format("%,.2f", currentRate)}"
                loadHistoricalData(period)
            }
        }
    }

    private val fetchRateRunnable = object : Runnable {
        override fun run() {
            fetchCryptoRate()
            handler.postDelayed(this, 10_000L)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(fetchRateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(fetchRateRunnable)
    }
} 