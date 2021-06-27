package com.app.messagealarm.ui.onboarding

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.app.messagealarm.R
import com.app.messagealarm.ui.onboarding.fragments.*
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
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
       //initViewPager()
        showVideoTutorial()
        setListener()
    }

    private fun setListener(){
        btn_skip?.setOnClickListener {
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_TUTORIAL_SHOW, true)
            dismiss()
        }

        btn_finish?.setOnClickListener {
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_TUTORIAL_SHOW, true)
            dismiss()
        }
    }

    private fun showVideoTutorial(){
        //start full sound
        val mobilemode =
            context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        mobilemode!!.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
        val path = "android.resource://" + requireActivity().packageName.toString() + "/" + R.raw.video_tutorial
        quick_start_video?.setVideoURI(Uri.parse(path))
        quick_start_video?.setOnPreparedListener {
            it.setScreenOnWhilePlaying(true)
        }
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        quick_start_video?.start()
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}