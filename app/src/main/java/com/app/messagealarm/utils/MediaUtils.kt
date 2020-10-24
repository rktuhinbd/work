package com.app.messagealarm.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.app.messagealarm.BaseApplication
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.NullPointerException

class MediaUtils {
    companion object {

        var mediaPlayer: MediaPlayer? = null

        fun playAlarm(isVibrate: Boolean, context: Context, mediaPath: String?) {

            //start full sound
            Thread(Runnable {
                val mobilemode =
                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                mobilemode!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0
                );
            }).start()

            try {
                //start media player
                mediaPlayer = if (mediaPath != null) {
                    MediaPlayer.create(context, Uri.parse(mediaPath))
                } else {
                    MediaPlayer.create(context, com.app.messagealarm.R.raw.default_ringtone)
                }
                mediaPlayer!!.setVolume(1.0f, 1.0f)
                mediaPlayer!!.isLooping = true
                mediaPlayer!!.setOnPreparedListener { mediaPlayer!!.start() }
                mediaPlayer!!.prepareAsync()

            } catch (e: IllegalStateException) {

            } catch (e: NullPointerException) {

            } catch (e: IOException) {

            }
            //start vibration
            Thread(Runnable {
                //vibrate
                if (isVibrate) {
                    val once = Once()
                    once.run(Runnable {
                        VibratorUtils.startVibrate(BaseApplication.getBaseApplicationContext())
                    })
                }
            }).start()
            //stop playBack
            stopPlayBackAfterDone()
        }

        private fun stopVibration() {
            Thread(Runnable {
                VibratorUtils.stopVibrate()
            }).start()
        }

        private fun stopPlayBackAfterDone() {
            //here 30 is not static it will be from setting page, the values will be 1, 2, 3, or Full song
            while (true) {
                val totalPlayBack = (mediaPlayer!!.currentPosition / 1000).toInt()
                if (totalPlayBack == 30) {
                    if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.stop()
                        stopVibration()
                        break
                    }
                }
            }
        }

        fun stopAlarm() {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
                stopVibration()
            }
        }

        fun isPlaying(): Boolean {
            return mediaPlayer != null && mediaPlayer!!.isPlaying
        }
    }
}