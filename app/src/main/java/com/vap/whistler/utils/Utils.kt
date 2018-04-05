package com.vap.whistler.utils

import android.graphics.Color
import com.google.gson.Gson
import com.vap.whistler.model.HappeningMatches
import com.vap.whistler.model.ScheduleItem
import android.view.MotionEvent
import android.graphics.PorterDuff
import android.view.View



/**
 * Copyright 2018 (C) Whistler
 * Created on: 04/04/18
 * Author: Kavin Varnan
 */

class Utils {
    class Fuel {
        companion object {
            fun autoHeader(): HashMap<String, String> {
                val map: HashMap<String, String> = HashMap()
                map["uid"] = WhistlerFirebase.getFirebaseCurrentUser().uid
                map["accessToken"] = WhistlerSharedPreference.getSharedPreferenceValue(WhistlerConstants.SP.ACCESS_TOKEN)
                return map
            }
        }
    }

    class Match {
        companion object {
            fun getHappeningMatches(): List<ScheduleItem> {
                val str = WhistlerSharedPreference.getSharedPreferenceValue(WhistlerConstants.SP.HAPPENING_MATCH)
                return Gson().fromJson(str, HappeningMatches::class.java).schedule
            }

            fun getCurrentMatch(): ScheduleItem {
                val str = WhistlerSharedPreference.getSharedPreferenceValue(WhistlerConstants.SP.CURRENT_MATCH)
                return Gson().fromJson(str, ScheduleItem::class.java)
            }

            fun updateCurrentMatch(scheduleItem: ScheduleItem) {
                val str = Gson().toJson(scheduleItem)
                WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.CURRENT_MATCH, str)
            }
        }
    }

    class Others {
        companion object {
    fun buttonEffect(button: View, colorHex: String) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background.setColorFilter(Color.parseColor(colorHex), PorterDuff.Mode.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                }
            }
            false
        }
    }
        }
    }
}