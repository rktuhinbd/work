package com.app.messagealarm.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import java.util.concurrent.TimeUnit

class VibratorUtils {
    companion object{

        var vibrator:Vibrator? = null

        fun startVibrate(context: Context){
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator!!.vibrate(VibrationEffect.createWaveform(ConverterUtils.genVibratorPattern(1.0f,
                    2000), 1))
            } else {
                vibrator!!.vibrate(ConverterUtils.genVibratorPattern(1.0f,
                    2000), 1)
            }
        }

        fun stopVibrate(){
                if(vibrator != null){
                    vibrator!!.cancel()
                }
        }

    }
}