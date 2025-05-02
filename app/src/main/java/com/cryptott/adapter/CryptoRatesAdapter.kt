package com.cryptott.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cryptott.CryptoRatesActivity
import com.cryptott.CurrencyDetailActivity
import com.cryptott.R
import com.cryptott.logic.WalletManager
import com.cryptott.model.CryptoRate

class CryptoRatesAdapter : RecyclerView.Adapter<CryptoRatesAdapter.CryptoRateViewHolder>() {

    var cryptoRates: List<CryptoRate> = emptyList()

    fun updateRates(newRates: List<CryptoRate>) {
        cryptoRates = newRates
        notifyDataSetChanged()
    }

    fun updateBalance() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoRateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crypto_rate, parent, false)
        return CryptoRateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoRateViewHolder, position: Int) {
        holder.bind(cryptoRates[position])
    }

    override fun getItemCount(): Int = cryptoRates.size

    inner class CryptoRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cryptoName: TextView = itemView.findViewById(R.id.cryptoName)
        private val cryptoSymbol: TextView = itemView.findViewById(R.id.cryptoSymbol)
        private val cryptoRate: TextView = itemView.findViewById(R.id.cryptoRate)

        fun bind(cryptoRate: CryptoRate) {
            cryptoName.text = cryptoRate.symbol
            cryptoSymbol.text = String.format("%.6f", WalletManager.getWalletBalance().getOrDefault(cryptoRate.symbol, 0.0))
            if(cryptoRate.rate == 0.0)
                this.cryptoRate.text = "..."
            else
                this.cryptoRate.text = "$${String.format("%,.2f", cryptoRate.rate)}"

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, CurrencyDetailActivity::class.java).apply {
                    putExtra("symbol", cryptoRate.symbol)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}