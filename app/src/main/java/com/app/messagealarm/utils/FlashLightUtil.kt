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
                if(mCameraId == "1"){
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(!isFlashOn){
                                instance.setTorchMode(mCameraId, true)
                                isFlashOn = true
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }catch (e: CameraAccessException){
                e.printStackTrace()
            }
        }

        fun stopBlinkingFlash(context: Context){
            try {
                val instance = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val mCameraId: String = instance.cameraIdList[0]
                if(mCameraId == "1"){
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(isFlashOn){
                                instance.setTorchMode(mCameraId, false)
                                isFlashOn = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }catch (e: CameraAccessException){
                e.printStackTrace()
            }
        }
    }


}