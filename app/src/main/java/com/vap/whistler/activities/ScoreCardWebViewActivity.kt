package com.vap.whistler.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.vap.whistler.R
import com.vap.whistler.utils.WhistlerConstants
import kotlinx.android.synthetic.main.activity_score_card_web_view.*
import android.webkit.WebView
import android.webkit.WebChromeClient



class ScoreCardWebViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_card_web_view)
        if (intent.getStringExtra(WhistlerConstants.Intent.URL) != null) {
            webview.loadUrl(intent.getStringExtra(WhistlerConstants.Intent.URL))
        } else {
            finish()
        }
        showBaseProgressBar("Loading...", false)
        webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) {
                    hideBaseProgressBar()
                }
            }
        }
    }
}
