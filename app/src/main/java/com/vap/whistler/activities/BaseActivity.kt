package com.vap.whistler.activities

import android.support.v7.app.AppCompatActivity
import android.app.ProgressDialog
import android.os.Bundle


/**
 * Copyright 2018 (C) Whistler
 * Created on: 02/04/18
 * Author: Kavin Varnan
 */
open class BaseActivity : AppCompatActivity() {

    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage("Loading...")
    }

    fun showBaseProgressBar(text: String, isCancellable: Boolean) {
        mProgressDialog!!.setMessage(text)
        mProgressDialog!!.setCancelable(isCancellable)
        mProgressDialog!!.show()
    }

    fun hideBaseProgressBar() {
        mProgressDialog!!.dismiss()
    }
}