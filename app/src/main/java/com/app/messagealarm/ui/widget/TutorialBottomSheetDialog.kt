package com.app.messagealarm.ui.widget

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.AndroidUtils
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.app.messagealarm.utils.isVisibile
import com.example.loadinganimation.LoadingAnimation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.DefaultPlayerUiController
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.layout_dialog_onboarding.*
import pl.droidsonroids.gif.GifImageView


class TutorialBottomSheetDialog(val activity: Activity) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        try {
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            val behavior: BottomSheetBehavior<*> =
                BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
            behavior.isDraggable = false
            val layoutParams = bottomSheet.layoutParams
            val windowHeight = getWindowHeight()
            if (layoutParams != null) {
                layoutParams.height = windowHeight
            }
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        } catch (e: NullPointerException) {

        }
    }

    private fun getWindowHeight(): Int { // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay
            .getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }


    private fun showYoutubePlayer(v:View){
        val youtubeView = v.findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        val gifView = v.findViewById<GifImageView>(R.id.gif_no_internet)
        val retryButton = v.findViewById<MaterialButton>(R.id.button_retry)
        val loadingAnimation = v.findViewById<LoadingAnimation>(R.id.progress_bar_video_loading)
        loadingAnimation.visibility = View.VISIBLE

        gifView?.visibility = View.GONE
        retryButton?.visibility = View.GONE
        youtubeView?.visibility = View.VISIBLE
        youtubeView?.enterFullScreen()


        youtubeView?.addYouTubePlayerListener(object : YouTubePlayerListener{
            override fun onApiChange(youTubePlayer: YouTubePlayer) {

            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {

            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                requireActivity().runOnUiThread {
                    Toasty.error(requireActivity(), error.name).show()
                }
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {

            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate
            ) {

            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
                    DefaultPlayerUiController(youtubeView, youTubePlayer)
                removeErrorViews(v)
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {

            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    if(v.findViewById<GifImageView>(R.id.gif_no_internet).isVisibile() && duration > 1){
                        removeErrorViews(v)
                    }
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {

            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {

            }

        })

    }

    private fun removeErrorViews(v:View){
        val youtubeView = v.findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        val gifView = v.findViewById<GifImageView>(R.id.gif_no_internet)
        val retryButton = v.findViewById<MaterialButton>(R.id.button_retry)
        v.findViewById<LoadingAnimation>(R.id.progress_bar_video_loading)
            .visibility = View.GONE
        gifView?.visibility = View.GONE
        retryButton?.visibility = View.GONE
        youtubeView?.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.layout_bottom_sheet_tutorial, container, false)
        val skipButton = v.findViewById<TextView>(R.id.btn_skip)
        handlePlayerWithInternet(v)
        val youtubeView = v.findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        lifecycle.addObserver(youtubeView)
        skipButton.setOnClickListener {
            dismiss()
        }
        return v
    }

    private fun handlePlayerWithInternet(v:View){
        if (AndroidUtils.isOnline(requireActivity())) {
            //show youtube player
            showYoutubePlayer(v)
        }else{
            //show internet error
            internetError(v)
        }
    }

    private fun internetError(v:View){
        Toasty.error(requireActivity(),
            "Please check your internet!").show()
        val youtubeView = v.findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        val gifView = v.findViewById<GifImageView>(R.id.gif_no_internet)
        val retryButton = v.findViewById<MaterialButton>(R.id.button_retry)
        val loadingAnimation = v.findViewById<LoadingAnimation>(R.id.progress_bar_video_loading)
        loadingAnimation.visibility = View.GONE

        youtubeView?.visibility = View.GONE
        gifView?.visibility = View.VISIBLE
        retryButton?.visibility = View.VISIBLE

        retryButton?.setOnClickListener {
                handlePlayerWithInternet(v)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).state =
                BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
            BottomSheetBehavior.from(bottomSheet).isHideable = true
            setupFullHeight(dia)
        }

        return bottomSheetDialog
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_VIDEO_SHOWED)){
            (activity as AlarmApplicationActivity).changeStateOfSpeedDial()
        }
    }


}