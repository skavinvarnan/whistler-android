package com.vap.whistler.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Copyright 2018 (C) Whistler
 * Created on: 02/04/18
 * Author: Kavin Varnan
 */


class WhistlerFirebase {
    companion object {
        fun getFirebaseCurrentUser(): FirebaseUser {
            return FirebaseAuth.getInstance().currentUser!!
        }

        fun isUserLoggedIn(): Boolean {
            return FirebaseAuth.getInstance().currentUser != null
        }
    }
}