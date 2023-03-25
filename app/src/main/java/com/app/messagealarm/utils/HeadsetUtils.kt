package com.app.messagealarm.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.app.messagealarm.BaseApplication


class HeadsetUtils {
    companion object {
        private lateinit var audioManager: AudioManager

        fun isHeadsetConnected(): Boolean {
            var isConnected = false
            if (!::audioManager.isInitialized) {
                audioManager =
                    BaseApplication.getBaseApplicationContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (device in audioDevices) {
                    val deviceType = device.type
                    if (deviceType == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || deviceType == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                        isConnected = true
                        break
                    } else if (deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                        isConnected = true
                        break
                    }
                }
            } else {
                isConnected = audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn
            }
            return isConnected
        }
    }
}
