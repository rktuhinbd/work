package com.app.messagealarm.ui.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.app.messagealarm.R
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.layout_dialog_onboarding.*


class OnboardingDialog : DialogFragment(){


    private lateinit var firebaseAnalytics: FirebaseAnalytics

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
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
    }



    @SuppressLint("SetTextI18n")
    private fun showVideoTutorial(){
         startVideo()
        /**
         * btn skip listener
         */
        btn_skip?.setOnClickListener {
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_VIDEO_SHOWED, true)
            if(btn_skip?.text?.toString().equals("SKIP")){
                val bundle = Bundle()
                bundle.putString("skipped_video", "yes")
                firebaseAnalytics.logEvent("video_view_status", bundle)
                //user skipped the video, say you can always see it from setting
                Toasty.info(requireActivity(), "You can always see the video from Setting!").show()
                dismiss()
            }else if(btn_skip?.text?.toString().equals("CLOSE")){
                Toasty.success(requireActivity(), "Thanks for watching!").show()
                val bundle = Bundle()
                bundle.putString("closed_video", "yes")
                firebaseAnalytics.logEvent("video_view_status", bundle)
                //close the video
                dismiss()
            }
        }

        btn_reload?.setOnClickListener {
            startVideo()
        }
    }

    private fun startVideo(){
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
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            it.setScreenOnWhilePlaying(true)
        }
        quick_start_video?.setOnCompletionListener {
            val bundle = Bundle()
            bundle.putString("video_finished", "yes")
            firebaseAnalytics.logEvent("video_view_status", bundle)
            btn_skip?.text = "CLOSE"
            quick_start_video.stopPlayback()
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        quick_start_video?.start()
    }


    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }else{
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels
            if(height > 2000){
                dialog?.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }else{
                if(width < 1000){
                    dialog?.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }else{
                    dialog?.window?.setLayout(
                        width - 100,
                        height - 115
                    )
                }
            }
        }
    }
}