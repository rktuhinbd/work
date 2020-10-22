package com.app.messagealarm.utils

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.RawResourceDataSource


class ExoPlayerUtils {

    companion object {

        var exoPlayer: SimpleExoPlayer? = null

        public fun playAudio(isVibrate: Boolean, context: Context, mediaPath: String?) {
            val mobilemode =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            mobilemode?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
                //code that runs in main
                //audio playing logic should be playing for
                val defaultRingtoneUri =
                    RingtoneManager.getActualDefaultRingtoneUri(
                        BaseApplication.getBaseApplicationContext(),
                        RingtoneManager.TYPE_RINGTONE
                    )

                exoPlayer = SimpleExoPlayer.Builder(context)
                    .build()
                var mediaItem: MediaItem? = null
                mediaItem = if (mediaPath != null) {
                    MediaItem.fromUri(mediaPath)
                } else {
                    MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(R.raw.default_ringtone))
                }
                exoPlayer!!.setMediaItem(mediaItem)
                exoPlayer!!.prepare()
                exoPlayer!!.setForegroundMode(true)
                exoPlayer!!.playWhenReady = true
                //start vibraton
                //startVibration(isVibrate)
                stopPlayBackAfterDone()
        }

        private fun startVibration(isVibrate: Boolean) {
            Thread(Runnable {
                //vibrate
                if (isVibrate) {
                    val once = Once()
                    once.run(Runnable {
                        VibratorUtils.startVibrate(BaseApplication.getBaseApplicationContext())
                    })
                }
            }).start()
        }

        private fun stopVibration() {
            Thread(Runnable {
                VibratorUtils.stopVibrate()
            }).start()
        }

        private fun stopPlayBackAfterDone() {
            //here 30 is not static it will be from setting page, the values will be 1, 2, 3, or Full song
            while (true) {
                val totalPlayBack = (exoPlayer!!.currentPosition / 1000).toInt()
                if (totalPlayBack == 30) {
                    if (exoPlayer!!.isPlaying) {
                        exoPlayer!!.playWhenReady = false
                        // stopVibration()
                        break
                    }
                }
            }
        }


        fun stopAlarm() {
            if (exoPlayer != null && isPlaying()) {
                exoPlayer!!.playWhenReady = false
                //stopVibration()
            }
        }

        fun isPlaying(): Boolean {
            return exoPlayer != null && exoPlayer!!.isPlaying
        }
    }
}