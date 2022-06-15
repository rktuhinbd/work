package com.app.messagealarm.utils

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build


class FlashLightUtil{


    companion object{

       private var isFlashOn = false

        fun startBlinkingFlash(context: Context) {
            try {
                val instance = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val mCameraId: String = instance.cameraIdList[0]
                /**
                 * @Notes: If user phone has front flash, then the front flash will beep
                 * if don't have the front flash then the back flash will beep
                 * Right now it's just turn on the back flash
                 */
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(!isFlashOn){
                                isFlashOn = true
                                instance.setTorchMode(0.toString(), true)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            }catch (e: CameraAccessException){
                e.printStackTrace()
            }
        }

        fun stopBlinkingFlash(context: Context){
            try {
                val instance = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

                /**
                 * Need to stop the flash based on which flash was beeping ( Front ? Back)
                 */
                val mCameraId: String = instance.cameraIdList[0]
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(isFlashOn){
                                instance.setTorchMode(0.toString(), false)
                                isFlashOn = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            }catch (e: CameraAccessException){
                e.printStackTrace()
            }
        }
    }


}