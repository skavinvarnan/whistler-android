package com.vap.whistler.activities

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.vap.whistler.R
import android.support.v7.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_new_join_group.*
import com.github.kittinunf.fuel.Fuel
import com.vap.whistler.model.GenericResponse
import com.vap.whistler.utils.Utils
import com.vap.whistler.utils.WhistlerConstants


class NewJoinGroupActivity : AppCompatActivity() {

    private lateinit var recyclerAdapter: NewJoinAdapter

    private var imageArray: IntArray = intArrayOf(R.drawable.batman, R.drawable.cat, R.drawable.clown, R.drawable.cool,
        R.drawable.crazy, R.drawable.devil, R.drawable.hypnotized, R.drawable.minion,
        R.drawable.ninja, R.drawable.pirate_cat, R.drawable.shocked, R.drawable.wink )

    private var imageArrayString: Array<String> = arrayOf("batman", "cat", "clown", "cool",
            "crazy", "devil", "hypnotized", "minion",
            "ninja", "pirate_cat", "shocked", "wink" )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_join_group)
        supportActionBar!!.title = "Add / Join Group"
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initView()
    }

    private fun initView() {
        recyclerAdapter = NewJoinAdapter(imageArray, 0, this)
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@NewJoinGroupActivity, 4)
            adapter = recyclerAdapter
        }
        recyclerAdapter.notifyDataSetChanged()
        createGroup.setOnClickListener {
            if (groupName.text.isNotBlank()) {
                Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/create_group/${groupName.text}/${imageArrayString[recyclerAdapter.selectedItem]}")
                        .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                            val (response, _) = result
                            if (response != null && response.error == null) {
                                finish()
                            } else {
                                Snackbar.make(findViewById(R.id.main_layout), "Unable to create group. Please try again", Snackbar.LENGTH_SHORT).show()
                            }
                        }

            } else {
                Snackbar.make(findViewById(R.id.main_layout), "Please enter the group name and click on create group button", Snackbar.LENGTH_SHORT).show()
            }
        }

        joinButton.setOnClickListener {
            if (groupId.text.isNotBlank() && joinCode.text.isNotBlank()) {
                Fuel.get(WhistlerConstants.Server.BASE_URL + "/group/join_group/${groupId.text.toString().toLowerCase()}/${joinCode.text}")
                        .header(Utils.Fuel.autoHeader()).responseObject(GenericResponse.Deserializer()) { _, _, result ->
                            val (response, _) = result
                            if (response != null && response.error == null) {
                                finish()
                            } else if (response != null && response.error!!.code == 404) {
                                Snackbar.make(findViewById(R.id.main_layout), "Group not found. Check if you have entered proper Group ID and Joincode", Snackbar.LENGTH_SHORT).show()
                            }
                        }
            } else {
                Snackbar.make(findViewById(R.id.main_layout), "Please enter the group ID and join code. You can get this from group admin", Snackbar.LENGTH_SHORT).show()
            }
        }

        Utils.Others.buttonEffect(joinButton, "#175ed1")
        Utils.Others.buttonEffect(createGroup, "#175ed1")

    }

    fun imageChanged() {
        selected_image.setImageResource(imageArray[recyclerAdapter.selectedItem])
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

    class NewJoinAdapter(private var items: IntArray, var selectedItem: Int, val context: NewJoinGroupActivity) : RecyclerView.Adapter<NewJoinAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var image: ImageView = view.findViewById(R.id.image) as ImageView
            var parent: FrameLayout = view.findViewById(R.id.parent) as FrameLayout
        }


        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): NewJoinAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.emoji_recycle_item, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.image.setImageResource(items[position])
            holder.image.setOnClickListener {
                selectedItem = position
                this.notifyDataSetChanged()
            }
            if (selectedItem == position) {
                holder.parent.setBackgroundColor(Color.parseColor("#c4c4c4"))
                context.imageChanged()
            } else {
                holder.parent.setBackgroundColor(Color.TRANSPARENT)
            }
        }


        override fun getItemCount() = items.size
    }
}
