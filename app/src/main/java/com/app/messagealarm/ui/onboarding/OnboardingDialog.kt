package com.app.messagealarm.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.app.messagealarm.R
import com.app.messagealarm.ui.onboarding.adapter.QuickStartAdapter
import com.app.messagealarm.ui.onboarding.fragments.*
import kotlinx.android.synthetic.main.layout_dialog_onboarding.*

class OnboardingDialog : DialogFragment(){


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_dialog_onboarding, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewPager()
        setListener()
    }

    private fun setListener(){
        btn_skip?.setOnClickListener {
            dismiss()
        }
        btn_next?.setOnClickListener {
            quick_start_pager?.currentItem?.plus(1)?.let { it1 ->
                quick_start_pager?.setCurrentItem(
                    it1, true)
            }
        }

        btn_finish?.setOnClickListener {
            dismiss()
        }
    }


    private fun initViewPager(){
        val fragmentList = ArrayList<Fragment>()
        fragmentList.add(QuickStartFirstFragment())
        fragmentList.add(QuickStartSecondFragment())
        fragmentList.add(QuickStartThirdFragment())
        fragmentList.add(QuickStartFourFragment())
        fragmentList.add(QuickStartFiveFragment())
       val quickStartAdapter= QuickStartAdapter(childFragmentManager, fragmentList)
        quick_start_pager?.adapter = quickStartAdapter
        dots_indicator?.setViewPager(quick_start_pager)

        //listener
        quick_start_pager?.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                    if(position == ((quick_start_pager?.adapter as QuickStartAdapter).count - 1)){
                        btn_next?.visibility = View.INVISIBLE
                        btn_finish?.visibility = View.VISIBLE
                    }else{
                        btn_next?.visibility = View.VISIBLE
                        btn_finish?.visibility = View.INVISIBLE
                    }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }



    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}