package com.vap.whistler.activities

import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson

import com.vap.whistler.R
import com.vap.whistler.model.ScoreBoard
import com.vap.whistler.utils.WhistlerConstants
import kotlinx.android.synthetic.main.activity_scorecard.*
import kotlinx.android.synthetic.main.fragment_scorecard.view.*

class ScorecardActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var scoreBoard: ScoreBoard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scorecard)

        if (intent.getStringExtra(WhistlerConstants.Intent.SCORE_BOARD) != null) {
            scoreBoard = Gson().fromJson(intent.getStringExtra(WhistlerConstants.Intent.SCORE_BOARD), ScoreBoard::class.java)
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Scorecard"
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

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

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_scorecard, container, false)
            val position = arguments!!.getInt(ARGSECTIONNUMBER)
            val scoreBoard = Gson().fromJson(arguments!!.getString(WhistlerConstants.Intent.SCORE_BOARD), ScoreBoard::class.java)
            if (position == 0) {
                rootView.section_label.text = scoreBoard.inn1md
            } else {
                rootView.section_label.text = scoreBoard.inn2md
            }
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
}
