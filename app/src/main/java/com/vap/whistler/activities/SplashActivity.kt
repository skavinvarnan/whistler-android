package com.vap.whistler.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.vap.whistler.R
import com.vap.whistler.model.GenericResponse
import com.vap.whistler.model.HappeningMatches
import com.vap.whistler.model.ScheduleItem
import com.vap.whistler.utils.*


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        JavaUtils.trustEveryone()
        loadLandingOrSignIn()
    }

    private fun loadLandingOrSignIn() {
        if (WhistlerFirebase.isUserLoggedIn()) {
            getLatestAccessTokenFromFirebase()
        } else {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun getLatestAccessTokenFromFirebase() {
        WhistlerFirebase.getFirebaseCurrentUser().getIdToken(true).addOnCompleteListener({
            if (it.result.token != null) {
                WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.ACCESS_TOKEN, it.result.token!!)
                readyToProceedToLanding()
            } else {
                showError()
            }
        })

    }

    private fun readyToProceedToLanding() {

        getHappeningMatch()
    }

    private fun getHappeningMatch() {
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/match/happening_schedule")
                .header(Utils.Fuel.autoHeader()).responseString { _, _, result ->
                    val (response, err) = result
                    if (err == null) {
                        val obj: HappeningMatches = Gson().fromJson(response, HappeningMatches::class.java)
                        if (obj.schedule.isNotEmpty()) {
                            WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.HAPPENING_MATCH, response!!)
                            val currentMatch: ScheduleItem = obj.schedule[0]
                            val curMatchStr = Gson().toJson(currentMatch)
                            WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.CURRENT_MATCH, curMatchStr)
                            startActivity(Intent(this@SplashActivity, LiveActivity::class.java))
                            finish()
                        } else {
                            thereAreNoMatchesHappeningCurrently()
                        }
                    } else {
                        showError()
                    }
                }
    }

    private fun thereAreNoMatchesHappeningCurrently() {
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/match/get_some_match_to_display")
                .header(Utils.Fuel.autoHeader()).responseString { _, _, result ->
                    val (response, err) = result
                    if (err == null) {
                        val obj: HappeningMatches = Gson().fromJson(response, HappeningMatches::class.java)
                        if (obj.schedule.isNotEmpty()) {
                            WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.HAPPENING_MATCH, response!!)
                            val currentMatch: ScheduleItem = obj.schedule[0]
                            val curMatchStr = Gson().toJson(currentMatch)
                            WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.CURRENT_MATCH, curMatchStr)
                            updateCurrentUserAndProceed()
                        } else {
                            showError()
                        }
                    } else {
                        showError()
                    }
                }
    }

    private fun updateCurrentUserAndProceed() {
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/user/init/${WhistlerFirebase.getFirebaseCurrentUser().displayName}/${WhistlerFirebase.getFirebaseCurrentUser().email}")
            .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                val (response, _) = result
                if (response != null && response.error == null) {
                    startActivity(Intent(this@SplashActivity, LiveActivity::class.java))
                    finish()
                } else {
                    showError()
                }
            }
    }

    private fun showError() {
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Unable to connect with to the internet. Please check your network settings")
        alert.setTitle("No connection")
        alert.setPositiveButton("Try Again") { _, _ ->
            loadLandingOrSignIn()
        }
        alert.setNegativeButton("Close App") { _, _ ->
            finish()
        }
        alert.show()
    }
}
