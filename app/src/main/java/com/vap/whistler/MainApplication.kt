package com.vap.whistler

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds

/**
 * Copyright 2018 (C) Whistler
 * Created on: 02/04/18
 * Author: Kavin Varnan
 */

class MainApplication: Application() {
    companion object {
        private var context: Context? = null

        fun getGlobalContext(): Context {
            return context!!
        }

        fun setGlobalContext(contextIn: Context) {
            context = contextIn
        }
    }

    override fun onCreate() {
        super.onCreate()
        MainApplication.setGlobalContext(applicationContext)
        MobileAds.initialize(this, "ca-app-pub-7846555754762077~9566832943")
    }
}