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
import com.vap.whistler.model.ScheduleItem
import com.vap.whistler.model.ScheduleResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import com.vap.whistler.utils.WhistlerFirebase
import com.vap.whistler.utils.WhistlerSharedPreference

class ScheduleMainFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var customActionBarTitle: TextView
    private lateinit var customActionBarImageOne: ImageView
    private lateinit var customActionBarImageTwo: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerAdapter: ScheduleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_schedule_main, container, false)
        initCustomActionBar(view.findViewById(R.id.custom_action_bar))
        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        initFragmentActionBar()
        initView()
        return view
    }

    private fun initView() {
        recyclerAdapter = ScheduleAdapter(ArrayList())
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

    private fun loadRecyclerViewData() {
        swipeRefreshLayout.isRefreshing = true
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/match/schedule").header(Utils.Fuel.autoHeader()).responseObject(ScheduleResponse.Deserializer()) { req, res, result ->
            val (response, err) = result
            if (response != null && response.error == null) {
                recyclerAdapter.scheduleItems = response.schedule!!
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
        customActionBarTitle.text = "Schedule"
        customActionBarImageOne.visibility = View.INVISIBLE
        customActionBarImageTwo.visibility = View.INVISIBLE
        customActionBarImageOne.setOnClickListener { imageOneClicked() }
        customActionBarImageTwo.setOnClickListener { imageTwoClicked() }
    }

    private fun imageOneClicked() {

    }

    private fun imageTwoClicked() {

    }

    override fun onRefresh() {
        loadRecyclerViewData()
    }

    class ScheduleAdapter(var scheduleItems: List<ScheduleItem>) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var topLeft: TextView = view.findViewById<View>(R.id.topLeft) as TextView
            var topRight: TextView = view.findViewById(R.id.topRight) as TextView
            var centerTop: TextView = view.findViewById(R.id.centerTop) as TextView
            var centerBottom: TextView = view.findViewById(R.id.centerBottom) as TextView
            var bottomLeft: TextView = view.findViewById(R.id.bottomLeft) as TextView
            var bottomRight: TextView = view.findViewById(R.id.bottomRight) as TextView
            var teamOne: ImageView = view.findViewById(R.id.teamOne) as ImageView
            var teamTwo: ImageView = view.findViewById(R.id.teamTwo) as ImageView
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ScheduleAdapter.ViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.schedule_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.topLeft.text = scheduleItems[position].displayDate
            holder.topRight.text = scheduleItems[position].venue
            holder.bottomLeft.text = scheduleItems[position].team_a_name
            holder.bottomRight.text = scheduleItems[position].team_b_name
            holder.centerBottom.text = scheduleItems[position].displayTime
            holder.centerTop.text = "VS"
            holder.teamOne.setImageResource(getImageForTeam(scheduleItems[position].team_a))
            holder.teamTwo.setImageResource(getImageForTeam(scheduleItems[position].team_b))
        }

        private fun getImageForTeam(team: String): Int {
            when(team) {
                "mi" -> return R.drawable.mi
                "csk" -> return R.drawable.csk
                "rcb" -> return R.drawable.rcb
                "rr" -> return R.drawable.rr
                "kkr" -> return R.drawable.kkr
                "srh" -> return R.drawable.srh
                "dd" -> return R.drawable.dd
                "kxip" -> return R.drawable.kxp
            }
            return R.drawable.tbd
        }


        override fun getItemCount() = scheduleItems.size
    }

}
