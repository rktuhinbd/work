package com.app.messagealarm.utils

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.device.DeviceListener
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import java.lang.IllegalStateException
import java.lang.NullPointerException


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
            try {
                //audio playing logic should be playing for
                exoPlayer = SimpleExoPlayer.Builder(context)
                    .build()
                var mediaItem: MediaItem? = null
                mediaItem = if (mediaPath != null) {
                    MediaItem.fromUri(mediaPath)
                } else {
                    MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(R.raw.crush))
                }
                exoPlayer!!.setMediaItem(mediaItem)
                exoPlayer!!.prepare()
                exoPlayer!!.setForegroundMode(true)
                exoPlayer!!.playWhenReady = true
                Thread(Runnable {
                    //vibrate
                    if (isVibrate) {
                        VibratorUtils.startVibrate(BaseApplication.getBaseApplicationContext())
                    }
                }).start()
                stopPlayBackAfterDone()

            } catch (e: NullPointerException) {
              e.printStackTrace()
            } catch (e: IllegalStateException) {
               e.printStackTrace()
            }

        }

        private fun stopPlayBackAfterDone() {
            //here 30 is not static it will be from setting page, the values will be 1, 2, 3, or Full song
            while (true) {
                val totalPlayBack = (exoPlayer!!.currentPosition / 1000).toInt()
                if (totalPlayBack == 20) {
                    if (exoPlayer!!.isPlaying) {
                        exoPlayer!!.playWhenReady = false
                        VibratorUtils.stopVibrate()
                        break
                    }
                }
            }
        }


        fun stopAlarm() {
            if (exoPlayer != null && isPlaying()) {
                exoPlayer!!.playWhenReady = false
                VibratorUtils.stopVibrate()
            }
        }

        fun isPlaying(): Boolean {
            return exoPlayer != null && exoPlayer!!.isPlaying
        }
    }
}