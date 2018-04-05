package com.vap.whistler.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.vap.whistler.R
import com.vap.whistler.model.*
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants
import com.vap.whistler.utils.WhistlerFirebase
import kotlinx.android.synthetic.main.activity_group_info.*
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics


class GroupInfoActivity : BaseActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var groupItem: MyGroupItem
    private lateinit var recyclerAdapter: GroupInfoAdapter
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.logEvent("check_group_info", null)
        val str = intent.getStringExtra(WhistlerConstants.Intent.GROUP_ITEM)
        if (str != null) {
           groupItem = Gson().fromJson(str, MyGroupItem::class.java)
        } else {
            finish()
            return
        }
        initActionBar()
        initView()
    }

    private fun initActionBar() {
        supportActionBar!!.title = groupItem.name
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (WhistlerFirebase.getFirebaseCurrentUser().uid == groupItem.admin) {
            menu!!.add(0, 3541, 0, "Delete").setIcon(R.drawable.delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, 3542, 0, "Edit").setIcon(R.drawable.edit)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        } else {
            menu!!.add(0, 3543, 0, "Leave").setIcon(R.drawable.exit)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            3541 -> {
                deleteGroup()
                true
            }
            3542 -> {
                editGroup()
                true
            }
            3543 -> {
                leaveGroup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteGroup() {

        val alert = AlertDialog.Builder(this)
        alert.setMessage("Are you sure you want to delete this group?. All the members of the group will be removed. Think Twice")
        alert.setTitle("Delete group")

        alert.setPositiveButton("Yes") { _, _ ->
            Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/delete_group/${groupItem._id}")
                    .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                        val (response, _) = result
                        if (response != null && response.error == null) {
                            finish()
                        } else {
                            Toast.makeText(this@GroupInfoActivity, "Unable to delete group. Please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
        }

        alert.setNegativeButton("No") { _, _ -> }

        alert.show()



    }

    private fun editGroup() {
        val alert = AlertDialog.Builder(this)
        val edittext = EditText(this)
        alert.setMessage("Please enter the new group name")
        alert.setTitle("Edit group")
        edittext.hint = "Group name"
        alert.setView(edittext)

        alert.setPositiveButton("Ok") { _, _ ->
            if (edittext.text.isNotBlank()) {
                showBaseProgressBar("Loading", false)
                Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/edit_group/${groupItem._id}/${edittext.text}/${groupItem.icon}")
                        .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                            val (response, _) = result
                            if (response != null && response.error == null) {
                                finish()
                                firebaseAnalytics.logEvent("edit_group", null)
                            } else {
                                firebaseAnalytics.logEvent("edit_group_error", null)
                                Toast.makeText(this@GroupInfoActivity, "Unable to edit group. Please try again", Toast.LENGTH_SHORT).show()
                            }
                            hideBaseProgressBar()
                        }
            } else {
                firebaseAnalytics.logEvent("edit_group_empty", null)
                Toast.makeText(this@GroupInfoActivity, "Please enter a group name", Toast.LENGTH_SHORT).show()
            }
        }

        alert.setNegativeButton("Cancel") { _, _ -> }

        alert.show()
    }

    private fun leaveGroup() {
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Are you sure you want to leave this group?")
        alert.setTitle("Leave group")

        alert.setPositiveButton("Yes") { _, _ ->

            Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/leave_group/${groupItem._id}")
                    .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                        val (response, _) = result
                        if (response != null && response.error == null) {
                            finish()
                            firebaseAnalytics.logEvent("leave_group", null)
                        } else {
                            firebaseAnalytics.logEvent("leave_group_error", null)
                            Toast.makeText(this@GroupInfoActivity, "Unable to leave group. Please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
        }

        alert.setNegativeButton("No") { _, _ -> }

        alert.show()
    }

    private fun initView() {
        recyclerAdapter = GroupInfoAdapter(ArrayList(), this)
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
        Utils.Others.buttonEffect(inviteFriendsButton, "#157201")
        inviteFriendsButton.setOnClickListener {
            firebaseAnalytics.logEvent("invite_friends", null)
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Because Guessing Is Fun! Join ${WhistlerFirebase.getFirebaseCurrentUser().displayName}'s group @GUESSBUZZ and Predict Scores for T20 Cricket Matches this SUMMER. Win Exciting Prizes. " +
                    "\n\nGroup ID: ${groupItem.groupId.toUpperCase()} \nJoin code: ${groupItem.joinCode} \n\nDownload the app now at https://guessbuzz.in")
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
    }

    override fun onRefresh() {
        loadRecyclerViewData()
    }

    private fun loadRecyclerViewData() {
        swipe_container.isRefreshing = true
        Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/get_everyone_form_group/${groupItem._id}/${Utils.Match.getCurrentMatch().key}")
                .header(Utils.Fuel.autoHeader()).responseObject(GroupInfoResponse.Deserializer()) { _, _, result ->
            val (response, _) = result
            if (response != null && response.error == null) {
                recyclerAdapter.items = response.groupMembers!!
            } else {
                //error
            }
            swipe_container.isRefreshing = false
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    fun itemClicked(position: Int) {
        val g: GroupInfoItem = recyclerAdapter.items[position]
        startActivity(Intent(this, UserPredictionReportActivity::class.java)
                .putExtra(WhistlerConstants.Intent.GROUP_INFO_ITEM, Gson().toJson(g)))

    }

    fun itemLongClicked(position: Int): Boolean {
        val g: GroupInfoItem = recyclerAdapter.items[position]
        if (g.uid == WhistlerFirebase.getFirebaseCurrentUser().uid) {
            val alert = AlertDialog.Builder(this)
            alert.setMessage("You are the admin, you cant leave the group")
            alert.setTitle("Oops!")

            alert.setPositiveButton("Yes") { _, _ ->

            }

            alert.setNegativeButton("No") { _, _ -> }

            alert.show()
            return true
        } else {
            val alert = AlertDialog.Builder(this)
            alert.setMessage("Are you sure you want to remove ${g.name}?")
            alert.setTitle("Remove member?")

            alert.setPositiveButton("Yes") { _, _ ->

                Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/remove_member/${groupItem._id}/${g.uid}")
                        .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                            val (response, _) = result
                            if (response != null && response.error == null) {
                                firebaseAnalytics.logEvent("remove_group_member", null)
                            } else {
                                firebaseAnalytics.logEvent("remove_group_member_error", null)
                                Toast.makeText(this@GroupInfoActivity, "Unable to remove member from group. Please try again", Toast.LENGTH_SHORT).show()
                            }
                        }
            }

            alert.setNegativeButton("No") { _, _ -> }

            alert.show()
            return true
        }
    }

    class GroupInfoAdapter(var items: List<GroupInfoItem>, val context: GroupInfoActivity) : RecyclerView.Adapter<GroupInfoAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById<View>(R.id.name) as TextView
            var current: TextView = view.findViewById(R.id.current) as TextView
            var overall: TextView = view.findViewById(R.id.overall) as TextView
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): GroupInfoAdapter.ViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_info_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = items[position].name
            holder.overall.text = items[position].over_all_points.toString()
            holder.current.text = items[position].total_for_match.toString()
            holder.itemView.setOnLongClickListener({
                context.itemLongClicked(position)
            })
            holder.itemView.setOnClickListener {
                context.itemClicked(position)
            }
        }


        override fun getItemCount() = items.size
    }
}
