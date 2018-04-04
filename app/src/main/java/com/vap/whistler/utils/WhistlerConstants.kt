package com.vap.whistler.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Copyright 2018 (C) Whistler
 * Created on: 02/04/18
 * Author: Kavin Varnan
 */
class WhistlerConstants {
    class Server {
        companion object {
            const val BASE_URL: String  = "http://192.168.1.139:7325/api"
        }
    }

    class SP {
        companion object {
            const val ACCESS_TOKEN: String = "ACCESS_TOKEN"
            const val HAPPENING_MATCH: String = "HAPPENING_MATCH"
            const val CURRENT_MATCH: String = "CURRENT_MATCH"
        }
    }
}