package com.vap.whistler.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.vap.whistler.R
import com.vap.whistler.model.GroupInfoItem
import com.vap.whistler.model.UserPredictionItem
import com.vap.whistler.model.UserPredictionResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import kotlinx.android.synthetic.main.activity_user_prediction_report.*

class UserPredictionReportActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var recyclerAdapter: UserPredictionAdapter
    private lateinit var predictionMatchKey: String
    private lateinit var uid: String
    private lateinit var title: String
    private var isLast: Boolean = false
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setContentView(R.layout.activity_user_prediction_report)
        if (intent.getStringExtra(WhistlerConstants.Intent.GROUP_INFO_ITEM) == null) {
            predictionMatchKey = intent.getStringExtra(WhistlerConstants.Intent.MATCH_KEY)
            uid = intent.getStringExtra(WhistlerConstants.Intent.UID)
            title = intent.getStringExtra(WhistlerConstants.Intent.TITLE)
            firebaseAnalytics.logEvent("check_match_report_last", null)
        } else {
            val str: String = intent.getStringExtra(WhistlerConstants.Intent.GROUP_INFO_ITEM)
            val groupInfoItem = Gson().fromJson(str, GroupInfoItem::class.java)
            predictionMatchKey = Utils.Match.getCurrentMatch().key
            uid = groupInfoItem.uid
            title = groupInfoItem.name
            firebaseAnalytics.logEvent("check_match_report", null)
        }

        isLast = intent.getBooleanExtra(WhistlerConstants.Intent.IS_LAST, false)
        loadAd()

        initActionBar()
        initView()
    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun initActionBar() {
        supportActionBar!!.title = title
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun initView() {
        recyclerAdapter = UserPredictionAdapter(ArrayList(), this)
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        swipe_container.setOnRefreshListener(this)
        swipe_container.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark)
        swipe_container.post({
            swipe_container.isRefreshing = true
            loadRecyclerViewData()
        })
        loadRecyclerViewData()
        if (isLast) {
            other_matches.visibility = View.GONE
        } else {
            other_matches.visibility = View.VISIBLE
            other_matches.text = "$title's Other Matches"
            Utils.Others.buttonEffect(other_matches, "#175ed1")
            other_matches.setOnClickListener {
                startActivity(Intent(this, UserMatchsReportActivity::class.java)
                        .putExtra(WhistlerConstants.Intent.UID, uid))
            }
        }
    }

    override fun onRefresh() {
        loadRecyclerViewData()
    }

    private fun loadRecyclerViewData() {
        swipe_container.isRefreshing = true
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/prediction/user_prediction/$uid/$predictionMatchKey")
                .header(Utils.Fuel.autoHeader()).responseObject(UserPredictionResponse.Deserializer()) { _, _, result ->
                    val (response, _) = result
                    if (response != null && response.error == null) {
                        recyclerAdapter.items = response.userPrediction!!
                    } else {
                        //error
                    }
                    swipe_container.isRefreshing = false
                    recyclerAdapter.notifyDataSetChanged()
                }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class UserPredictionAdapter(var items: List<UserPredictionItem>, val context: UserPredictionReportActivity) : RecyclerView.Adapter<UserPredictionAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var over: TextView = view.findViewById<View>(R.id.over) as TextView
            var runs: TextView = view.findViewById(R.id.runs) as TextView
            var predicted: TextView = view.findViewById(R.id.predicted) as TextView
            var points: TextView = view.findViewById(R.id.points) as TextView
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): UserPredictionAdapter.ViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_predict_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.over.text = items[position].over
            holder.runs.text = items[position].runs
            holder.predicted.text = items[position].predicted
            holder.points.text = items[position].points
        }


        override fun getItemCount() = items.size
    }
}
