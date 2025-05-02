package com.cryptott.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cryptott.logic.WalletManager
import com.cryptott.model.TransactionModel
import com.cryptott.model.TransactionStatsModel


class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "CryptoWallet.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_TRANSACTIONS = "transactions"

        private const val COLUMN_ID = "id"
        private const val COLUMN_CURRENCY_FROM = "currencyFrom"
        private const val COLUMN_CURRENCY_TO = "currencyTo"
        private const val COLUMN_AMOUNT_FROM = "amountFrom"
        private const val COLUMN_AMOUNT_TO = "amountTo"
        private const val COLUMN_DATETIME = "datetime"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CURRENCY_FROM TEXT NOT NULL,
                $COLUMN_CURRENCY_TO TEXT NOT NULL,
                $COLUMN_AMOUNT_FROM REAL NOT NULL,
                $COLUMN_AMOUNT_TO REAL NOT NULL,
                $COLUMN_DATETIME DATETIME NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

    fun addTransaction(transaction: TransactionModel): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CURRENCY_FROM, transaction.currencyFrom)
            put(COLUMN_CURRENCY_TO, transaction.currencyTo)
            put(COLUMN_AMOUNT_FROM, transaction.amountFrom)
            put(COLUMN_AMOUNT_TO, transaction.amountTo)
            put(COLUMN_DATETIME, transaction.datetime)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    fun getTransactionsOrderedByDatetime(currency: String): List<TransactionModel> {
        val transactions = mutableListOf<TransactionModel>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_CURRENCY_FROM = ? OR $COLUMN_CURRENCY_TO = ?
            ORDER BY $COLUMN_DATETIME DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(currency, currency))

        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor))
        }
        cursor.close()
        return transactions
    }

    fun getTransactionStats(currency: String): TransactionStatsModel {
        val db = readableDatabase

        val boughtQuery = """
            SELECT SUM($COLUMN_AMOUNT_FROM), SUM($COLUMN_AMOUNT_TO)
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_CURRENCY_TO = ?
        """.trimIndent()
        val boughtCursor = db.rawQuery(boughtQuery, arrayOf(currency))
        val boughtAmount = if (boughtCursor.moveToFirst()) boughtCursor.getDouble(0) else 0.0
        val boughtAmountCrypto = if (boughtCursor.moveToFirst()) boughtCursor.getDouble(1) else 0.0
        boughtCursor.close()

        val soldQuery = """
            SELECT SUM($COLUMN_AMOUNT_TO), SUM($COLUMN_AMOUNT_FROM)
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_CURRENCY_FROM = ?
        """.trimIndent()
        val soldCursor = db.rawQuery(soldQuery, arrayOf(currency))
        val soldAmount = if (soldCursor.moveToFirst()) soldCursor.getDouble(0) else 0.0
        val soldAmountCrypto = if (soldCursor.moveToFirst()) soldCursor.getDouble(1) else 0.0
        soldCursor.close()

        return TransactionStatsModel(
            currency = currency,
            currencyBought = boughtAmountCrypto,
            currencySold = soldAmountCrypto,
            profit = soldAmount - boughtAmount
        )
    }

    private fun cursorToTransaction(cursor: Cursor): TransactionModel {
        return TransactionModel(
            currencyFrom = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY_FROM)),
            currencyTo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY_TO)),
            amountFrom = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT_FROM)),
            amountTo = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT_TO)),
            datetime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME))
        )
    }
}