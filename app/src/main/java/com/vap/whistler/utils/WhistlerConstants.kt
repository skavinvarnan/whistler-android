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
            const val BASE_URL: String  = "https://api.guessbuzz.in:7325/api"
        }
    }

    class SP {
        companion object {
            const val ACCESS_TOKEN: String = "ACCESS_TOKEN"
            const val HAPPENING_MATCH: String = "HAPPENING_MATCH"
            const val CURRENT_MATCH: String = "CURRENT_MATCH"
        }
    }

    class Intent {
        companion object {
            const val OVER: String = "OVER"
            const val TEAM_BATTING: String = "TEAM_BATTING"
            const val GROUP_ITEM: String = "GROUP_ITEM"
            const val UID: String = "UID"
            const val GROUP_INFO_ITEM: String = "GROUP_INFO_ITEM"
            const val IS_LAST: String = "IS_LAST"
            const val MATCH_KEY: String = "MATCH_KEY"
            const val TITLE: String = "TITLE"
            const val SCORE_BOARD: String = "SCORE_BOARD"
            const val URL: String = "URL"
        }
    }

    class AdMob {
        companion object {

            const val APP_ID: String = "ca-app-pub-7846555754762077~9566832943"
            const val LIVE: String = "ca-app-pub-7846555754762077/4067094253"
            const val LEADER_BOARD: String = "ca-app-pub-7846555754762077/6170513416"
            const val MATCH_REPORT: String = "ca-app-pub-7846555754762077/4843676226"
            const val ALL_MATCHES: String = "ca-app-pub-7846555754762077/7680758403"
            const val GROUPS: String = "ca-app-pub-7846555754762077/6420002846"
            const val TEST_AD_ID: String = "ca-app-pub-3940256099942544/6300978111"
        }
    }
}