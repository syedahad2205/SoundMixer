package com.syed.soundmixer.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.syed.soundmixer.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        binding.viewPager.adapter = sectionsPagerAdapter

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Search"
                1 -> "Files"
                2 -> "Rec"
                3 -> "Merge"
                4 -> "Play"
                else -> null
            }
        }.attach()
    }

    fun updatePlayFragment(fragment: Fragment) {
        val adapter = binding.viewPager.adapter as SectionsPagerAdapter
        adapter.updateFragment(4, fragment)
    }

    fun updateMergeFragment(fragment: Fragment) {
        val adapter = binding.viewPager.adapter as SectionsPagerAdapter
        adapter.updateFragment(3, fragment)
    }

}
