package com.vap.whistler.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel

import com.vap.whistler.R
import com.vap.whistler.model.LeaderBoardItem
import com.vap.whistler.model.LeaderBoardResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants

class LeaderboardMainFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var customActionBarTitle: TextView
    private lateinit var customActionBarImageOne: ImageView
    private lateinit var customActionBarImageTwo: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerAdapter: LeaderboardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_leaderboard_main, container, false)
        initCustomActionBar(view.findViewById(R.id.custom_action_bar))
        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        initFragmentActionBar()
        initView()
        return view
    }

    private fun initView() {
        recyclerAdapter = LeaderboardAdapter(ArrayList())
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
            loadRecyclerViewData()
        })
        loadRecyclerViewData()
    }

    override fun onRefresh() {
        loadRecyclerViewData()
    }

    private fun loadRecyclerViewData() {
        swipeRefreshLayout.isRefreshing = true
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/match/leader_board/${Utils.Match.getCurrentMatch().key}")
                .header(Utils.Fuel.autoHeader()).responseObject(LeaderBoardResponse.Deserializer()) { _, _, result ->
            val (response, _) = result
            if (response != null && response.error == null) {
                recyclerAdapter.items = response.leaderBoard!!
            } else {
                //error
            }
            swipeRefreshLayout.isRefreshing = false
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun initCustomActionBar(custom_action_bar: View?) {
        customActionBarTitle = custom_action_bar!!.findViewById(R.id.title_text)
        customActionBarImageOne = custom_action_bar.findViewById(R.id.image_one)
        customActionBarImageTwo = custom_action_bar.findViewById(R.id.image_two)
    }

    private fun initFragmentActionBar() {
        customActionBarTitle.text = "Top 50 for ${Utils.Match.getCurrentMatch().related_name}"
        customActionBarImageOne.visibility = View.GONE
        customActionBarImageTwo.visibility = View.GONE
        customActionBarImageOne.setOnClickListener { imageOneClicked() }
        customActionBarImageTwo.setOnClickListener { imageTwoClicked() }
    }

    private fun imageOneClicked() {

    }

    private fun imageTwoClicked() {

    }

    override fun onResume() {
        super.onResume()
    }

    class LeaderboardAdapter(var items: List<LeaderBoardItem>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById<View>(R.id.name) as TextView
            var points: TextView = view.findViewById(R.id.points) as TextView
        }


        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): LeaderboardAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.leaderboard_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = items[position].name
            holder.points.text = "${items[position].total_for_match}"
        }

        override fun getItemCount() = items.size
    }

}