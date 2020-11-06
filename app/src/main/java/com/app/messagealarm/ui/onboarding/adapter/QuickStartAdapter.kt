package com.app.messagealarm.ui.onboarding.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter

class QuickStartAdapter(fm : FragmentManager, val fragmentList:ArrayList<Fragment>) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    override fun getCount(): Int {
       return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
      return fragmentList[position]
    }

}