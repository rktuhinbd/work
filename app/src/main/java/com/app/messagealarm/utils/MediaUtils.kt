package com.app.messagealarm.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.ui.notifications.FloatingNotification.Companion.notifyMute
import com.app.messagealarm.utils.SharedPrefUtils.write
import java.io.IOException


class MediaUtils {

    companion object {

       var isStopped = false
       // var isLoop = true
        var count = 0
        var mediaPlayer: MediaPlayer? = null
        var thread:Thread? = null

        fun playAlarm(
            thread: Thread,
            isJustVibrate: Boolean,
            isVibrate: Boolean,
            context: Context,
            mediaPath: String?,
            isLastIndex: Boolean,
            packageName: String,
            appName: String
        ) {
            this.thread = thread
            count = 0
            try {
                //start full sound
                val mobilemode =
                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                mobilemode!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0
                )
                val once = Once()
                val runnable = Runnable() {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer!!.reset()
                    if (mediaPath != null) {
                        mediaPlayer!!.setDataSource(mediaPath)
                    } else {
                        val afd =
                            context.resources.openRawResourceFd(com.app.messagealarm.R.raw.default_ringtone)
                        mediaPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                    }

                    if (!isJustVibrate || (!isJustVibrate && !isVibrate)) {
                        mediaPlayer!!.setVolume(1.0f, 1.0f)
                    } else {
                        mediaPlayer!!.setVolume(0f, 0f)
                    }

                    mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                            mediaPlayer!!.reset()
                            return true
                        }
                    })

                    mediaPlayer!!.setOnPreparedListener {
                        it.start()
                    }

                    mediaPlayer!!.prepare()

                }
                once.run(runnable)

            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //start vibration
            Thread(Runnable {
                //vibrate
                if (isVibrate || isJustVibrate) {
                    val once = Once()
                    once.run(Runnable {
                        VibratorUtils.startVibrate(BaseApplication.getBaseApplicationContext())
                    })
                }
            }).start()

            val onceAgain = Once()
            onceAgain.run(Runnable {
                    //stop playBack
                    stopPlayBackAfterDone(isLastIndex, context, packageName, appName)
                })

        }

        private fun stopVibration() {
            Thread(Runnable {
                VibratorUtils.stopVibrate()
            }).start()
        }

        private fun stopPlayBackAfterDone(
            isLastIndex: Boolean,
            context: Context,
            packageName: String,
            appName: String
        ) {
            try {
                /**
                 * There is a bug when stopping the media player stopping by button before it's finishes.
                 * It's starting the media player again
                 *
                 */
                //here 30 is not static it will be from setting page, the values will be 1, 2, 3, or Full song
                while (true) {
                    if(mediaPlayer != null){
                        count++
                    }else{
                        break
                    }
                    if (count == 30) {
                        count = 0
                        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                            if (isLastIndex) {
                                mediaPlayer!!.stop()
                                mediaPlayer!!.release()
                                mediaPlayer = null
                                FloatingNotification.showMissedAlarmNotification(
                                    context,
                                    packageName,
                                    appName
                                )
                                stopVibration()
                                break
                            } else {
                                stopVibration()
                                mediaPlayer!!.stop()
                                break
                            }
                        }
                    }
                    try{
                        Thread.sleep(1000)
                    }catch (e: InterruptedException){

                    }
                }
            } catch (e: IllegalStateException) {

            } catch (e: NullPointerException) {

            }
        }

        fun stopAlarm( ) {
            //first time alarm played and stopped, should ask user for review
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                write(Constants.PreferenceKeys.IS_STOPPED, true)
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
                isStopped = false
                thread!!.interrupt()
                thread = null
                stopVibration()
                write(Constants.PreferenceKeys.IS_MUTED, true)
                notifyMute(true)
            }
        }


        fun isPlaying(): Boolean {
            return try {
                if (mediaPlayer != null) {
                    mediaPlayer!!.isPlaying
                    true
                } else {
                    false
                }
            }catch (e:java.lang.IllegalStateException){
                e.printStackTrace()
                false
            }
        }


        fun getDurationOfMediaFle(path: String): Int {
            val uri: Uri = Uri.parse(path)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(BaseApplication.getBaseApplicationContext(), uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr!!.toInt() / 1000
        }
    }
}