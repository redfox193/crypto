package com.cryptott.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cryptott.model.TransactionModel
import com.cryptott.R

class TransactionAdapter : ListAdapter<TransactionModel, TransactionAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFrom: TextView = itemView.findViewById(R.id.tvFrom)
        val tvTo: TextView = itemView.findViewById(R.id.tvTo)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)

        if(transaction.currencyFrom == "USD") {
            holder.tvFrom.text = "${String.format("%.2f", transaction.amountFrom)} ${transaction.currencyFrom}"
            holder.tvTo.text = "${String.format("%.6f", transaction.amountTo)} ${transaction.currencyTo}"
        }
        else {
            holder.tvFrom.text = "${String.format("%.6f", transaction.amountFrom)} ${transaction.currencyFrom}"
            holder.tvTo.text = "${String.format("%.2f", transaction.amountTo)} ${transaction.currencyTo}"
        }
        holder.tvDate.text = transaction.datetime
    }

    class DiffCallback : DiffUtil.ItemCallback<TransactionModel>() {
        override fun areItemsTheSame(oldItem: TransactionModel, newItem: TransactionModel) =
            oldItem.datetime == newItem.datetime

        override fun areContentsTheSame(oldItem: TransactionModel, newItem: TransactionModel) =
            oldItem == newItem
    }
}