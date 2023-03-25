package com.app.messagealarm.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import java.util.concurrent.TimeUnit

class VibratorUtils {
    companion object{

        var totalPlayBack = 0
        var vibrator:Vibrator? = null

        fun startVibrate(context: Context, duration:Int){
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator!!.vibrate(VibrationEffect.createWaveform(ConverterUtils.genVibratorPatternNew(1.0f,
                    duration.toLong()
                ), 0))
            } else {
                vibrator!!.vibrate(ConverterUtils.genVibratorPatternNew(1.0f,
                    duration.toLong()
                ), 0)
            }
        }


        fun stopVibrate(){
                if(vibrator != null){
                    vibrator!!.cancel()
                }
        }

    }
}