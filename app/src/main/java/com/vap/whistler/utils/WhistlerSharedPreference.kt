package com.vap.whistler.utils

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.vap.whistler.MainApplication


/**
 * Copyright 2018 (C) Whistler
 * Created on: 02/04/18
 * Author: Kavin Varnan
 */
class WhistlerSharedPreference {
    companion object {
        private var sharedPreferences: SharedPreferences? = null

        private fun initializeSharedPreference() {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainApplication.getGlobalContext())
        }

        fun updateSharedPreference(key: String, value: String): Boolean {
            if (sharedPreferences == null) {
                initializeSharedPreference()
            }

            val editor = sharedPreferences!!.edit()
            editor.putString(key, value)
            return editor.commit()
        }

        fun updateSharedPreference(key: String, value: Int): Boolean {
            if (sharedPreferences == null) {
                initializeSharedPreference()
            }

            val editor = sharedPreferences!!.edit()
            editor.putInt(key, value)
            return editor.commit()
        }

        fun updateSharedPreference(key: String, value: Boolean): Boolean {
            if (sharedPreferences == null) {
                initializeSharedPreference()
            }

            val editor = sharedPreferences!!.edit()
            editor.putBoolean(key, value)
            return editor.commit()
        }

        fun getSharedPreferenceValue(key: String): String {
            if (sharedPreferences == null) {
                initializeSharedPreference()
            }

            return sharedPreferences!!.getString(key, null)
        }

        fun getSharedPreferenceValueInt(key: String): Int {
            if (sharedPreferences == null) {
                initializeSharedPreference()
            }

            return sharedPreferences!!.getInt(key, 0)
        }

        fun getSharedPreferenceBoolean(key: String): Boolean {
            if (sharedPreferences == null) {
                initializeSharedPreference()
            }

            return sharedPreferences!!.getBoolean(key, false)
        }


        fun getSharedPreference(): SharedPreferences? {
            if (sharedPreferences == null) {
                initializeSharedPreference()
            }
            return sharedPreferences
        }

    }

}