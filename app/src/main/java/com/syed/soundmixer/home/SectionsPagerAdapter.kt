package com.syed.soundmixer.home

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.syed.soundmixer.merge.MergeFragment
import com.syed.soundmixer.play.PlayFragment
import com.syed.soundmixer.rec.RecordingFragment
import com.syed.soundmixer.saved_sounds.SavedSoundsFragment
import com.syed.soundmixer.search.SearchFragment


class SectionsPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val fragments = listOf(
        SearchFragment(),
        SavedSoundsFragment(),
        RecordingFragment(),
        MergeFragment(),
        PlayFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFragment(position: Int, fragment: Fragment) {
        (fragments as MutableList)[position] = fragment
        notifyDataSetChanged()
    }
}

