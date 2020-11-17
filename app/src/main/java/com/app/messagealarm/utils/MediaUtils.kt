package com.app.messagealarm.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.ui.notifications.FloatingNotification.Companion.notifyMute
import com.app.messagealarm.utils.SharedPrefUtils.write
import java.io.IOException


class MediaUtils {

    companion object {

        var mediaPlayer: MediaPlayer? = null

        fun playAlarm(
            isJustVibrate:Boolean,
            isVibrate: Boolean,
            context: Context,
            mediaPath: String?,
            isLastIndex: Boolean,
            packageName: String,
            appName: String
        ) {

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
                    val runnable = Runnable(){
                        mediaPlayer = MediaPlayer()
                        mediaPlayer!!.reset()
                        if(mediaPath != null){
                            mediaPlayer!!.setDataSource(mediaPath)
                        }else{
                            val afd = context.resources.openRawResourceFd(com.app.messagealarm.R.raw.default_ringtone)
                            mediaPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                        }
                        if(!isJustVibrate || (!isJustVibrate && !isVibrate)){
                            mediaPlayer!!.setVolume(1.0f, 1.0f)
                        }else{
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
            }catch (e: Exception){
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
            //stop playBack
            stopPlayBackAfterDone(isLastIndex, context, packageName, appName)
        }

        private fun stopVibration() {
            Thread(Runnable {
                VibratorUtils.stopVibrate()
            }).start()
        }

        private fun stopPlayBackAfterDone(isLastIndex: Boolean, context: Context, packageName:String, appName:String) {
            try{
                //here 30 is not static it will be from setting page, the values will be 1, 2, 3, or Full song
                while (true) {
                        val totalPlayBack = (mediaPlayer!!.currentPosition / 1000).toInt()
                        if (totalPlayBack == 30) {
                            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                                if(isLastIndex){
                                    mediaPlayer!!.stop()
                                    mediaPlayer!!.release()
                                    mediaPlayer = null
                                    FloatingNotification.showMissedAlarmNotification(
                                        context,
                                        packageName,
                                        appName
                                    )
                                }else{
                                    mediaPlayer!!.stop()
                                }
                                stopVibration()
                                break
                            }
                        }
                }
            }catch (e: IllegalStateException){

            }catch (e: NullPointerException){

            }
        }

        fun stopAlarm() {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                write(Constants.PreferenceKeys.IS_STOPPED, true)
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
                stopVibration()
                write(Constants.PreferenceKeys.IS_MUTED, true)
                notifyMute(true)
            }
        }


        fun isPlaying(): Boolean {
            return if(mediaPlayer != null){
                mediaPlayer!!.isPlaying
            }else{
                false
            }
        }


        fun getDurationOfMediaFle(path:String) : Int{
            val uri: Uri = Uri.parse(path)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(BaseApplication.getBaseApplicationContext(), uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr!!.toInt() / 1000
        }
    }
}