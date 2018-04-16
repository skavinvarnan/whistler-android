package com.vap.whistler.activities

import android.annotation.SuppressLint
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.squareup.picasso.Picasso

import com.vap.whistler.R
import com.vap.whistler.model.PlayerInfo
import com.vap.whistler.model.ScoreBoard
import com.vap.whistler.utils.WhistlerConstants
import kotlinx.android.synthetic.main.activity_squad.*

class SquadActivity : AppCompatActivity() {
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var scoreBoard: ScoreBoard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_squad)

        if (intent.getStringExtra(WhistlerConstants.Intent.SCORE_BOARD) != null) {
            scoreBoard = Gson().fromJson(intent.getStringExtra(WhistlerConstants.Intent.SCORE_BOARD), ScoreBoard::class.java)
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Squad"
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        tabs.getTabAt(0)!!.text = scoreBoard.teamAShortName
        tabs.getTabAt(1)!!.text = scoreBoard.teamBShortName

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

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(position, scoreBoard)
        }

        override fun getCount(): Int {
            return 2
        }
    }

    class PlaceholderFragment : Fragment() {

        private lateinit var recyclerView: RecyclerView
        private lateinit var recyclerAdapter: SquadAdapter

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_squad, container, false)
            val position = arguments!!.getInt(ARGSECTIONNUMBER)
            val scoreBoard = Gson().fromJson(arguments!!.getString(WhistlerConstants.Intent.SCORE_BOARD), ScoreBoard::class.java)
            recyclerView = rootView.findViewById(R.id.recycler_view)
            recyclerAdapter = SquadAdapter(ArrayList())
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = recyclerAdapter
                addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            }


            if (position == 0) {
                recyclerAdapter.items = scoreBoard.squadA
            } else {
                recyclerAdapter.items = scoreBoard.squadB
            }
            recyclerAdapter.notifyDataSetChanged()
            return rootView
        }

        companion object {
            const val ARGSECTIONNUMBER = "ARGSECTIONNUMBER"
            fun newInstance(sectionNumber: Int, scoreBoard: ScoreBoard): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARGSECTIONNUMBER, sectionNumber)
                args.putString(WhistlerConstants.Intent.SCORE_BOARD, Gson().toJson(scoreBoard))
                fragment.arguments = args
                return fragment
            }
        }
    }

    class SquadAdapter(var items: List<PlayerInfo>) : RecyclerView.Adapter<SquadAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var icon: ImageView = view.findViewById(R.id.icon) as ImageView
            var text: TextView = view.findViewById(R.id.text) as TextView
        }


        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): SquadAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.squad_item, parent, false)

            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Picasso.get().load("http://img.litzscore.com/user/${items[position].key}_paint.png")
                    .placeholder(R.drawable.empty_paint)
                    .error(R.drawable.empty_paint).into(holder.icon)
            holder.text.text = items[position].name
            if (items[position].isCaptain) {
                holder.text.text = "${items[position].name} (C)"
            }

            if (items[position].isKeeper) {
                holder.text.text = "${items[position].name} (Wk)"
            }

            if (items[position].isCaptain && items[position].isCaptain) {
                holder.text.text = "${items[position].name} (C & Wk)"
            }

        }

        override fun getItemCount() = items.size
    }
}
