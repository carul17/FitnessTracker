package com.example.callum_arul_myruns5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var start: StartFragment
    private lateinit var history: HistoryFragment
    private lateinit var settings: SettingsFragment
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var myMyFragmentStateAdapter: FragmentAdapter
    private lateinit var fragments: ArrayList<Fragment>
    private val tabTitles = arrayOf("Start", "History", "Settings") //Tab titles
    private lateinit var tabConfigurationStrategy: TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Util.checkPermissions(this)

        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tab)

        start = StartFragment()
        history = HistoryFragment()
        settings = SettingsFragment()

        fragments = ArrayList()
        fragments.add(start)
        fragments.add(history)
        fragments.add(settings)

        myMyFragmentStateAdapter = FragmentAdapter(this, fragments)
        viewPager2.adapter = myMyFragmentStateAdapter

        tabConfigurationStrategy = TabConfigurationStrategy {
                tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position] }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}