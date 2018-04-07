package com.vap.whistler.fragments


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel

import com.vap.whistler.R
import com.vap.whistler.activities.SettingsActivity
import com.vap.whistler.model.*
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import com.vap.whistler.utils.WhistlerFirebase
import kotlinx.android.synthetic.main.fragment_live_main.*
import java.util.*
import kotlin.concurrent.timerTask
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.vap.whistler.MainApplication
import com.vap.whistler.activities.PredictionPopupActivity
import kotlin.collections.ArrayList


class LiveMainFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var customActionBarTitle: TextView
    private lateinit var customActionBarImageOne: ImageView
    private lateinit var customActionBarImageTwo: ImageView
    private lateinit var leftText: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerAdapter: PredictionAdapter
    private lateinit var adView: AdView
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_live_main, container, false)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        initCustomActionBar(view.findViewById(R.id.custom_action_bar))
        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        adView = view.findViewById(R.id.adView)
        loadAd()
        initFragmentActionBar()
        initView()
        return view
    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun initView() {
        recyclerAdapter = PredictionAdapter(ArrayList(), null, context!!)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark)
        swipeRefreshLayout.post({
            swipeRefreshLayout.isRefreshing = true
            loadRecyclerViewData(true)
        })
        loadRecyclerViewData(true)

        val params = recyclerView.layoutParams as ViewGroup.LayoutParams
        val paramsParent = swipeRefreshLayout.layoutParams as ViewGroup.LayoutParams
        params.height = paramsParent.height
        recyclerView.layoutParams = params
    }

    override fun onRefresh() {
        loadRecyclerViewData(true)
    }

    private fun loadRecyclerViewData(showRefreshIcon: Boolean) {
        if (WhistlerFirebase.isUserLoggedIn()) {
            swipeRefreshLayout.isRefreshing = showRefreshIcon
            Fuel.get(WhistlerConstants.Server.BASE_URL + "/prediction/my_prediction_table/${Utils.Match.getCurrentMatch().key}")
                    .header(Utils.Fuel.autoHeader()).responseObject(PredictPointsResponse.Deserializer()) { _, _, result ->
                        val (response, _) = result
                        if (response != null && response.error == null) {
                            recyclerAdapter.items = response.predictPointsTableData!!
                            recyclerAdapter.teamBatting = response.teamBatting
                            firebaseAnalytics.logEvent("fetch_prediction_table", null)
                        } else {
                            firebaseAnalytics.logEvent("fetch_prediction_table_error", null)
                        }
                        swipeRefreshLayout.isRefreshing = false
                        recyclerAdapter.notifyDataSetChanged()
                    }
        } else {
            stopTimer()
        }
    }

    private var timer: Timer? = null
    private var secondsTimer: Timer? = null
    private var updatedHowManySecondsAgo: Int = 0
    override fun onResume() {
        super.onResume()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timerTask{
            if(activity != null) {
                activity!!.runOnUiThread {
                    getScoreCardFromServer()
                    loadRecyclerViewData(false)
                }
            }
        }, 0, 5000)
        secondsTimer = Timer()
        timer!!.scheduleAtFixedRate(timerTask{
            incrementTimerUpdatedLabel()
        }, 0, 1000)
    }

    private fun incrementTimerUpdatedLabel() {
        updatedHowManySecondsAgo += 1
        if (activity != null && updatedAt != null) {
            try {
                activity!!.runOnUiThread {
                    try {
                        updatedAt.text = "Updated $updatedHowManySecondsAgo seconds ago"
                    } catch (err: Exception) {
                        firebaseAnalytics.logEvent("caught_crash_123", null)
                    }
                }
            } catch (err: Exception) {
                firebaseAnalytics.logEvent("caught_crash_243", null)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    private fun initCustomActionBar(custom_action_bar: View?) {
        customActionBarTitle = custom_action_bar!!.findViewById(R.id.title_text)
        customActionBarImageOne = custom_action_bar.findViewById(R.id.image_one)
        customActionBarImageTwo = custom_action_bar.findViewById(R.id.image_two)
        leftText = custom_action_bar.findViewById(R.id.left_text)
    }

    private fun initFragmentActionBar() {
        leftText.text = "RULES"
        leftText.visibility = View.VISIBLE
        customActionBarTitle.text = "Live"
        if (Utils.Match.getHappeningMatches().size > 1) {
            customActionBarImageTwo.visibility = View.VISIBLE
        } else {
            customActionBarImageTwo.visibility = View.INVISIBLE
        }
        customActionBarImageTwo.setOnClickListener { imageTwoClicked() }
        customActionBarImageOne.setOnClickListener { imageOneClicked() }
        leftText.setOnClickListener {
            firebaseAnalytics.logEvent("rules_action_bar", null)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guessbuzz.in/rules.html"))
            startActivity(browserIntent)
        }
    }

    private fun imageOneClicked() {
        if (activity != null) {
            activity!!.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    private fun imageTwoClicked() {
        switchMatch()
    }

    private fun switchMatch() {
        if (Utils.Match.getHappeningMatches().size > 1) {
            val alert = AlertDialog.Builder(context!!)
            alert.setMessage("Want to see this match?")
            alert.setTitle(getOtherMatchName())
            alert.setPositiveButton("Yes") { _, _ ->
                if (Utils.Match.getCurrentMatch().key == Utils.Match.getHappeningMatches()[0].key) {
                    Utils.Match.updateCurrentMatch(Utils.Match.getHappeningMatches()[1])
                } else {
                    Utils.Match.updateCurrentMatch(Utils.Match.getHappeningMatches()[0])
                }
                recyclerAdapter.items = ArrayList()
                recyclerAdapter.notifyDataSetChanged()
                getScoreCardFromServer()
                loadRecyclerViewData(true)

            }
            alert.setNegativeButton("No") { _, _ -> }
            alert.show()
        }
    }

    private fun getOtherMatchName(): String {
        return if (Utils.Match.getCurrentMatch().key == Utils.Match.getHappeningMatches()[0].key) {
            Utils.Match.getHappeningMatches()[1].short_name
        } else {
            Utils.Match.getHappeningMatches()[0].short_name
        }
    }

    private fun getScoreCardFromServer() {
        if (WhistlerFirebase.isUserLoggedIn()) {
            Fuel.get(WhistlerConstants.Server.BASE_URL + "/runs/score_board/${Utils.Match.getCurrentMatch().key}").header(Utils.Fuel.autoHeader()).responseObject(ScoreBoardResponse.Deserializer()) { _, _, result ->
                val (response, _) = result
                if (response != null && response.error == null) {
                    updateScoreBoard(response.scoreBoard!!)
                    firebaseAnalytics.logEvent("fetch_scorecard", null)
                } else {
                    firebaseAnalytics.logEvent("fetch_scorecard_error", null)
                }
            }
        } else {
            stopTimer()
        }
    }

    private fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
        }
        if(secondsTimer != null) {
            secondsTimer!!.cancel()
            secondsTimer!!.purge()
        }

    }

    private fun updateScoreBoard(scoreBoard: ScoreBoard) {
        if (updatedAt == null) return
        updatedHowManySecondsAgo = 0
        if (scoreBoard.showUpdated) {
            updatedAt.visibility = View.VISIBLE
        } else {
            updatedAt.visibility = View.INVISIBLE
        }
        teamName.text = scoreBoard.teamShortName
        inningsNumber.text = scoreBoard.inningsNumber
        scoreWickets.text = scoreBoard.runsWickets
        overNumber.text = scoreBoard.overNumber

        pshipLabel.text = scoreBoard.pShipLabel
        pshipData.text = scoreBoard.pShipData
        crrLabel.text = scoreBoard.crrLabel
        crrData.text = scoreBoard.crrData
        rrrLabel.text = scoreBoard.rrrLabel
        rrrData.text = scoreBoard.rrrData
        currentStatus.text = scoreBoard.matchInfo
        currentStatus.isSelected = true
        batsmanOneName.text = scoreBoard.batsmanNameOne
        batsmanOneRun.text = scoreBoard.batsmanRunsOne
        batsmanOneBalls.text = scoreBoard.batsmanBallsOne
        batsmanOne4s.text = scoreBoard.batsman4sOne
        batsmanOne6s.text = scoreBoard.batsman6sOne
        batsmanOneSr.text = scoreBoard.batsmanSROne
        batsmanTwoName.text = scoreBoard.batsmanNameTwo
        batsmanTwoRuns.text = scoreBoard.batsmanRunsTwo
        batsmanTwoBalls.text = scoreBoard.batsmanBallsTwo
        batsmanTwo4s.text = scoreBoard.batsman4sTwo
        batsmanTwo6s.text = scoreBoard.batsman6sTwo
        batsmanTwoSr.text = scoreBoard.batsmanSRTwo
        bowlerName.text = scoreBoard.bowlerName
        bowlerOver.text = scoreBoard.bowlerOver
        bowlerMaiden.text = scoreBoard.bowlerMaiden
        bowlerRuns.text = scoreBoard.bowlerRuns
        bowlerWickets.text = scoreBoard.bowlerWickets
        bowlerEco.text = scoreBoard.bowlerEconomy

        customActionBarTitle.text = scoreBoard.title

    }

    class PredictionAdapter(var items: List<PredictPointsItemArr>, var teamBatting: String?, var context: Context) : RecyclerView.Adapter<PredictionAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var overs: TextView = view.findViewById<View>(R.id.overs) as TextView
            var runs: TextView = view.findViewById(R.id.runs) as TextView
            var prediction: TextView = view.findViewById(R.id.prediction) as TextView
            var points: TextView = view.findViewById(R.id.points) as TextView
            var predictButton: TextView = view.findViewById(R.id.predictButton) as Button
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): PredictionAdapter.ViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.predict_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.overs.text = items[position].over.label
            val density = MainApplication.getGlobalContext().resources.displayMetrics.density
            if (items[position].over.whiteText) {
                holder.overs.setTextColor(Color.WHITE)
                val shape2 = GradientDrawable()
                shape2.shape = GradientDrawable.RECTANGLE
                shape2.cornerRadii = floatArrayOf(16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density)
                shape2.setColor(Color.parseColor(items[position].over.colorHex))
                holder.overs.background = shape2
            } else {
                holder.overs.setTextColor(Color.BLACK)
                holder.overs.setBackgroundColor(Color.TRANSPARENT)
            }

            holder.runs.text = items[position].runs.label
            if (items[position].runs.whiteText) {
                holder.runs.setTextColor(Color.WHITE)
                val shape3 = GradientDrawable()
                shape3.shape = GradientDrawable.RECTANGLE
                shape3.cornerRadii = floatArrayOf(16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density)
                shape3.setColor(Color.parseColor(items[position].runs.colorHex))
                holder.runs.background = shape3
            } else {
                holder.runs.setTextColor(Color.BLACK)
                holder.runs.setBackgroundColor(Color.TRANSPARENT)
            }


            holder.prediction.text = items[position].predicted.label
            if (items[position].predicted.whiteText) {
                holder.prediction.setTextColor(Color.WHITE)
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.RECTANGLE
                shape.cornerRadii = floatArrayOf(16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density)
                shape.setColor(Color.parseColor(items[position].predicted.colorHex))
                holder.prediction.background = shape
            } else {
                holder.prediction.setTextColor(Color.BLACK)
                holder.prediction.setBackgroundColor(Color.TRANSPARENT)
            }


            holder.points.text = items[position].points.label
            if (items[position].points.whiteText) {
                holder.points.setTextColor(Color.WHITE)
                val shape1 = GradientDrawable()
                shape1.shape = GradientDrawable.RECTANGLE
                shape1.cornerRadii = floatArrayOf(16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density, 16 * density)
                shape1.setColor(Color.parseColor(items[position].points.colorHex))
                holder.points.background = shape1
            } else {
                holder.points.setTextColor(Color.BLACK)
                holder.points.setBackgroundColor(Color.TRANSPARENT)
            }


            if(items[position].predictButton.clickable) {
                holder.predictButton.isEnabled = true
                val shape1 = GradientDrawable()
                shape1.shape = GradientDrawable.RECTANGLE
                shape1.cornerRadii = floatArrayOf(10 * density, 10 * density, 10 * density, 10 * density, 10 * density, 10 * density, 10 * density, 10 * density)
                shape1.setColor(Color.parseColor("#007BFA"))
                holder.predictButton.background = shape1
                Utils.Others.buttonEffect(holder.predictButton, "#175ed1")
                holder.predictButton.setTextColor(Color.WHITE)
                holder.predictButton.setOnClickListener {
                    if (teamBatting != null) {
                        val over: Int = position + 1
                        context.startActivity(Intent(context, PredictionPopupActivity::class.java)
                                .putExtra(WhistlerConstants.Intent.OVER, over)
                                .putExtra(WhistlerConstants.Intent.TEAM_BATTING, teamBatting!!))
                    }
                }
            } else {
                holder.predictButton.isEnabled = false
                val shape1 = GradientDrawable()
                shape1.shape = GradientDrawable.RECTANGLE
                shape1.cornerRadii = floatArrayOf(10 * density, 10 * density, 10 * density, 10 * density, 10 * density, 10 * density, 10 * density, 10 * density)
                shape1.setColor(Color.TRANSPARENT)
                holder.predictButton.setTextColor(Color.parseColor("#c4c4c4"))
                holder.predictButton.background = shape1
            }

        }


        override fun getItemCount() = items.size
    }

}