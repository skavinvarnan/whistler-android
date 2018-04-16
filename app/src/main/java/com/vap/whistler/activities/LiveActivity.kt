package com.vap.whistler.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.vap.whistler.R
import kotlinx.android.synthetic.main.activity_live.*
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.support.v4.app.FragmentPagerAdapter
import com.vap.whistler.fragments.GroupsMainFragment
import com.vap.whistler.fragments.LeaderboardMainFragment
import com.vap.whistler.fragments.LiveMainFragment
import com.vap.whistler.fragments.ScheduleMainFragment
import android.support.v4.view.ViewPager.OnPageChangeListener
import com.vap.whistler.utils.WhistlerFirebase
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.vap.whistler.utils.WhistlerConstants
import com.vap.whistler.utils.WhistlerSharedPreference


class LiveActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    val scheduleFragment: Fragment = ScheduleMainFragment()
    val liveFragment: Fragment = LiveMainFragment()
    val leaderboardFragment: Fragment = LeaderboardMainFragment()
    val groupsFragment: Fragment = GroupsMainFragment()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_schedule -> {
                viewPager.currentItem = 0
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_live -> {
                viewPager.currentItem = 1
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_leaderboard -> {
                viewPager.currentItem = 2
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
                viewPager.currentItem = 3
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        viewPager.adapter = MyPageAdapter(supportFragmentManager)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        disableShiftMode(navigation)
        navigation.selectedItemId = R.id.navigation_live
        viewPager.currentItem = 1
        viewPagerListener()
    }

    private fun viewPagerListener() {
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when(position) {
                    0 -> {
                        firebaseAnalytics.logEvent("schedule", null)
                        navigation.selectedItemId = R.id.navigation_schedule
                    }
                    1 -> {
                        firebaseAnalytics.logEvent("live", null)
                        navigation.selectedItemId = R.id.navigation_live
                    }
                    2 -> {
                        firebaseAnalytics.logEvent("leaderboard", null)
                        navigation.selectedItemId = R.id.navigation_leaderboard
                    }
                    3 -> {
                        firebaseAnalytics.logEvent("groups", null)
                        navigation.selectedItemId = R.id.navigation_groups
                    }
                }

            }
        })
    }

    private inner class MyPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return 4
        }

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    return scheduleFragment
                }
                1 -> {
                    return liveFragment
                }
                2 -> {
                    return leaderboardFragment
                }
                3 -> {
                    return groupsFragment
                }
            }
            return LiveMainFragment()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun disableShiftMode(view: BottomNavigationView) {
        val menuView = view.getChildAt(0) as BottomNavigationMenuView
        try {
            val shiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                item.setShiftingMode(false)
                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: NoSuchFieldException) {
            Log.e("BNVHelper", "Unable to get shift mode field", e)
        } catch (e: IllegalAccessException) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e)
        }

    }

    override fun onResume() {
        super.onResume()
        if (!WhistlerFirebase.isUserLoggedIn()) {
            startActivity(Intent(this@LiveActivity, LoginActivity::class.java))
            finish()
        } else {
            WhistlerFirebase.getFirebaseCurrentUser().getIdToken(true).addOnCompleteListener({
                try {
                    if (it.result.token != null) {
                        WhistlerSharedPreference.updateSharedPreference(WhistlerConstants.SP.ACCESS_TOKEN, it.result.token!!)
                    }
                } catch (err: Exception) {

                }
            })
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
