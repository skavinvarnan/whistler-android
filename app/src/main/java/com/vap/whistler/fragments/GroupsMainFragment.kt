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
import com.vap.whistler.model.MyGroupItem
import com.vap.whistler.model.MyGroupsResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants

class GroupsMainFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var customActionBarTitle: TextView
    private lateinit var customActionBarImageOne: ImageView
    private lateinit var customActionBarImageTwo: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerAdapter: MyGroupsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_groups_main, container, false)
        initCustomActionBar(view.findViewById(R.id.custom_action_bar))
        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        initFragmentActionBar()
        initView()
        return view
    }

    private fun initView() {
        recyclerAdapter = MyGroupsAdapter(ArrayList())
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
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/list_all_groups").header(Utils.Fuel.autoHeader()).responseObject(MyGroupsResponse.Deserializer()) { _, _, result ->
            val (response, _) = result
            if (response != null && response.error == null) {
                recyclerAdapter.items = response.groups!!
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
        customActionBarTitle.text = "Groups"
        customActionBarImageTwo.visibility = View.INVISIBLE
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

    class MyGroupsAdapter(var items: List<MyGroupItem>) : RecyclerView.Adapter<MyGroupsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById<View>(R.id.name) as TextView
            var members: TextView = view.findViewById(R.id.members) as TextView
            var icon: ImageView = view.findViewById(R.id.emoji) as ImageView
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): MyGroupsAdapter.ViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.my_groups_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = items[position].name
            holder.members.text = "${items[position].members.size} Members"
            holder.icon.setImageResource(getImageForEmoji(items[position].icon))
        }

        private fun getImageForEmoji(team: String): Int {
            when(team) {
                "batman" -> return R.drawable.batman
                "cat" -> return R.drawable.cat
                "clown" -> return R.drawable.clown
                "cool" -> return R.drawable.cool
                "crazy" -> return R.drawable.crazy
                "devil" -> return R.drawable.devil
                "hypnotized" -> return R.drawable.hypnotized
                "minion" -> return R.drawable.minion
                "ninja" -> return R.drawable.ninja
                "pirate_cat" -> return R.drawable.pirate_cat
                "shocked" -> return R.drawable.shocked
                "wink" -> return R.drawable.wink
            }
            return R.drawable.tbd
        }


        override fun getItemCount() = items.size
    }

}