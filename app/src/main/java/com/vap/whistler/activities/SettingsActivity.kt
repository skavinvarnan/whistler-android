package com.vap.whistler.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.vap.whistler.R
import kotlinx.android.synthetic.main.activity_settings.*
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth


class SettingsActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.logEvent("settings", null)
        supportActionBar!!.title = "Settings"
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        rules.setOnClickListener {
            firebaseAnalytics.logEvent("rules_settings", null)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/rules.html"))
            startActivity(browserIntent)
        }

        faq.setOnClickListener {
            firebaseAnalytics.logEvent("faq_settings", null)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/faq.html"))
            startActivity(browserIntent)
        }

        privacy.setOnClickListener {
            firebaseAnalytics.logEvent("privacy_settings", null)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/privacy.html"))
            startActivity(browserIntent)
        }

        terms.setOnClickListener {
            firebaseAnalytics.logEvent("terms_settings", null)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/terms.html"))
            startActivity(browserIntent)
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                firebaseAnalytics.logEvent("logout", null)
            }
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                firebaseAnalytics.logEvent("settings_back", null)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
