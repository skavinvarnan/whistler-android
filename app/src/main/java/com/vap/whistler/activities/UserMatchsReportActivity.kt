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
import com.vap.whistler.R
import com.vap.whistler.model.MatchReportItem
import com.vap.whistler.model.MatchReportResponse
import com.vap.whistler.model.UserPredictionResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import kotlinx.android.synthetic.main.activity_user_prediction_report.*

class UserMatchsReportActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var recyclerAdapter: UserMatchReportAdapter
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_matchs_report)
        if(intent.getStringExtra(WhistlerConstants.Intent.UID) == null) {
            finish()
            return
        }
        uid = intent.getStringExtra(WhistlerConstants.Intent.UID)
        loadAd()
        initActionBar()
        initView()
    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun initActionBar() {
        supportActionBar!!.title = "All Matches"
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun initView() {
        recyclerAdapter = UserMatchReportAdapter(ArrayList(), this)
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

    }

    override fun onRefresh() {
        loadRecyclerViewData()
    }

    private fun loadRecyclerViewData() {
        swipe_container.isRefreshing = true
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/match/all_match_points/$uid")
                .header(Utils.Fuel.autoHeader()).responseObject(MatchReportResponse.Deserializer()) { _, _, result ->
                    val (response, _) = result
                    if (response != null && response.error == null) {
                        recyclerAdapter.items = response.allMatches!!
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

    fun itemClicked(position: Int) {
        startActivity(Intent(this, UserPredictionReportActivity::class.java)
                .putExtra(WhistlerConstants.Intent.IS_LAST, true)
                .putExtra(WhistlerConstants.Intent.UID, uid)
                .putExtra(WhistlerConstants.Intent.MATCH_KEY, recyclerAdapter.items[position].key)
                .putExtra(WhistlerConstants.Intent.TITLE, recyclerAdapter.items[position].match))
    }

    class UserMatchReportAdapter(var items: List<MatchReportItem>, val context: UserMatchsReportActivity) : RecyclerView.Adapter<UserMatchReportAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById<View>(R.id.name) as TextView
            var points: TextView = view.findViewById(R.id.points) as TextView
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): UserMatchReportAdapter.ViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.leaderboard_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = items[position].match
            holder.points.text = items[position].points
            holder.itemView.setOnClickListener {
                context.itemClicked(position)
            }
        }


        override fun getItemCount() = items.size
    }
}
