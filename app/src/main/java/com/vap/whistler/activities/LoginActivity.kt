package com.vap.whistler.activities

import android.os.Bundle
import com.vap.whistler.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.Html
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.vap.whistler.model.GenericResponse
import com.vap.whistler.model.HappeningMatches
import com.vap.whistler.model.ScheduleItem
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import com.vap.whistler.utils.WhistlerFirebase
import com.vap.whistler.utils.WhistlerSharedPreference
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {


    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        textView.setText(Html.fromHtml("By clicking Sign In, you agree to our <font color='blue'>Terms of Service</font> and that you have read our <font color='blue'>Privacy Policy</font>."), TextView.BufferType.SPANNABLE)
        initGoogleSignInAndFirebase()
        textView.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Select")
                setMessage("Which one would you like to read")
                setPositiveButton("Terms", { _, _ ->
                    firebaseAnalytics.logEvent("terms", null)
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/terms.html"))
                    startActivity(browserIntent)
                })
                setNegativeButton("Privacy", { _, _ ->
                    firebaseAnalytics.logEvent("privacy", null)
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/privacy.html"))
                    startActivity(browserIntent)
                })
                create()
                show()
            }
        }
    }

    private fun initGoogleSignInAndFirebase() {
        sign_in_button.setSize(SignInButton.SIZE_STANDARD)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()

        sign_in_button.setOnClickListener {
            signIn()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                unableToSignIn()
            }

        }
    }

    private fun unableToSignIn() {
        signInError()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        showBaseProgressBar("Loading", false)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        mAuth.currentUser!!.getIdToken(true).addOnCompleteListener({
                            if (it.result.token != null) {
                                WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.ACCESS_TOKEN, it.result.token!!)

                                Fuel.get(WhistlerConstants.Server.BASE_URL + "/user/init/${WhistlerFirebase.getFirebaseCurrentUser().displayName}/${WhistlerFirebase.getFirebaseCurrentUser().email}")
                                        .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                                            val (response, _) = result
                                            if (response != null && response.error == null) {
                                                readyToProceedToLanding()
                                            } else {
                                                signInError()
                                                firebaseAnalytics.logEvent("user_init_server_error", null)
                                            }
                                        }

                            } else {
                                unableToSignIn()
                                firebaseAnalytics.logEvent("unable_to_sign_in", null)
                            }
                        })
                    } else {
                        signInError()
                        firebaseAnalytics.logEvent("f_sign_in_error", null)
                    }
                }
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

                            hideBaseProgressBar()
                            startActivity(Intent(this@LoginActivity, LiveActivity::class.java))
                            finish()
                        } else {
                            thereAreNoMatchesHappeningCurrently()
                        }
                    } else {
                        thereAreNoMatchesHappeningCurrently()
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

                            hideBaseProgressBar()
                            startActivity(Intent(this@LoginActivity, LiveActivity::class.java))
                            finish()
                        } else {
                            signInError()
                        }
                    } else {
                        signInError()
                    }
                }
    }

    private fun signInError() {
        hideBaseProgressBar()
        Snackbar.make(findViewById(R.id.main_layout), "Unable to signin please try again.", Snackbar.LENGTH_SHORT).show()
    }


    private fun signIn() {
        firebaseAnalytics.logEvent("sign_in_clicked", null)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }
}
