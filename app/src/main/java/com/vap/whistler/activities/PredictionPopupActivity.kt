package com.vap.whistler.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Window
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.vap.whistler.R
import com.vap.whistler.model.ScoreBoardResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import kotlinx.android.synthetic.main.activity_prediction_popup.*

class PredictionPopupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_prediction_popup)
        val teamBatting: String? = intent.getStringExtra(WhistlerConstants.Intent.TEAM_BATTING)
        val overIntent: Int = intent.getIntExtra(WhistlerConstants.Intent.OVER, -1)
        over.setText("$overIntent")
        Utils.Others.buttonEffect(predictButton, "#2d85b5")
        predictButton.setOnClickListener {
            if (overIntent != -1 && teamBatting != null) {
                if (prediction.text.isNotBlank() ) {
                    showBaseProgressBar("Loading", false)
                    Fuel.get(WhistlerConstants.Server.BASE_URL + "/prediction/predict/${Utils.Match.getCurrentMatch().key}/$teamBatting/$overIntent/${prediction.text}")
                            .header(Utils.Fuel.autoHeader()).responseObject(ScoreBoardResponse.Deserializer()) { _, _, result ->
                        val (response, _) = result
                        if (response != null && response.error == null) {
                            finish()
                        } else if (response != null && response.error!!.code == 401) {
                            AlertDialog.Builder(this).apply {
                                setTitle("Oops!")
                                setMessage("Sorry!!.. This over has started, so predict the next one")
                                setPositiveButton("Ok", { _, _ ->
                                    finish()
                                })
                                create()
                                show()
                            }
                        } else {
                            AlertDialog.Builder(this).apply {
                                setTitle("Error")
                                setMessage("Something went wrong. Please try again.")
                                setPositiveButton("Ok", { _, _ ->
                                })
                                create()
                                show()
                            }
                        }
                        hideBaseProgressBar()
                    }
                } else {
                    Toast.makeText(this, "Please enter your prediction", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
