package com.cryptott

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.cryptott.db.DBHelper

class CryptoApp : Application() {
    companion object {
        private lateinit var instance: CryptoApp
        fun getDBHelper(): DBHelper = instance.dbHelper
    }

    lateinit var dbHelper: DBHelper

    override fun onCreate() {
        super.onCreate()
        instance = this
        dbHelper = DBHelper(this)
    }
}