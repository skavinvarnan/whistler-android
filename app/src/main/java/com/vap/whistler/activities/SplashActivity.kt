package com.vap.whistler.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.vap.whistler.R
import com.vap.whistler.model.GenericResponse
import com.vap.whistler.model.HappeningMatches
import com.vap.whistler.model.ScheduleItem
import com.vap.whistler.model.ScheduleResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import com.vap.whistler.utils.WhistlerFirebase
import com.vap.whistler.utils.WhistlerSharedPreference

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
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
                .header(Utils.Fuel.autoHeader()).responseString() { _, _, result ->
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun showError() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
