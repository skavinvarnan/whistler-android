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
import com.vap.whistler.model.GenericResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import com.vap.whistler.utils.WhistlerFirebase
import com.vap.whistler.utils.WhistlerSharedPreference
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {


    private lateinit var mAuth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        textView.setText(Html.fromHtml("By clicking Sign In, you agree to our <font color='blue'>Terms of Service</font> and that you have read our <font color='blue'>Privacy Policy</font>."), TextView.BufferType.SPANNABLE)
        initGoogleSignInAndFirebase()
        textView.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Select")
                setMessage("Which one would you like to read")
                setPositiveButton("Terms", { _, _ ->
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/terms.html"))
                    startActivity(browserIntent)
                })
                setNegativeButton("Privacy", { _, _ ->
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

                        Fuel.get(WhistlerConstants.Server.BASE_URL + "/user/init/${WhistlerFirebase.getFirebaseCurrentUser().displayName}/${WhistlerFirebase.getFirebaseCurrentUser().email}")
                                .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                                    val (response, _) = result
                                    if (response != null && response.error == null) {
                                        mAuth.currentUser!!.getIdToken(true).addOnCompleteListener({
                                            if (it.result.token != null) {
                                                WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.ACCESS_TOKEN, it.result.token!!)
                                                readyToProceedToLanding()
                                            } else {
                                                unableToSignIn()
                                            }
                                        })
                                    } else {
                                        signInError()
                                    }
                                }

                    } else {
                        signInError()
                    }
                }
    }

    private fun readyToProceedToLanding() {
        hideBaseProgressBar()
        startActivity(Intent(this@LoginActivity, LiveActivity::class.java))
        finish()
    }

    private fun signInError() {
        hideBaseProgressBar()
        Snackbar.make(findViewById(R.id.main_layout), "Unable to signin please try again.", Snackbar.LENGTH_SHORT).show()
    }


    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }
}
