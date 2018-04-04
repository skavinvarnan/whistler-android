package com.vap.whistler.utils

import com.google.gson.Gson
import com.vap.whistler.model.HappeningMatches
import com.vap.whistler.model.ScheduleItem

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
        }
    }
}