package com.app.messagealarm.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri

class MediaUtils {
    companion object {

        var mediaPlayer: MediaPlayer? = null

        fun playAlarm(context: Context, mediaPath:String?) {
            val mobilemode =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            mobilemode!!.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            );
            mediaPlayer = if(mediaPath != null){
                MediaPlayer.create(context, Uri.parse(mediaPath))
            }else{
                MediaPlayer.create(context, com.app.messagealarm.R.raw.crush)
            }
            mediaPlayer!!.setVolume(1.0f, 1.0f)
            mediaPlayer!!.isLooping = true
            mediaPlayer!!.start()
        }

        fun stopAlarm() {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
            }
        }

        fun isPlaying() : Boolean{
            return mediaPlayer != null && mediaPlayer!!.isPlaying
        }
    }
}