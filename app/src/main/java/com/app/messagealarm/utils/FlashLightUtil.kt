package com.app.messagealarm.utils

import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import java.lang.RuntimeException


class FlashLightUtil {


    companion object {

        private val TAG: String = FlashLightUtil::class.java.simpleName
        private var mCamera: Camera? = null
        private var parameters: Camera.Parameters? = null
        private var camManager: CameraManager? = null
        private var isFlashOn = false
        private var isBlinking = false

        private fun turnOn(context: Context) {
            isFlashOn = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    var cameraId: String? = null
                    if (camManager != null) {
                        cameraId = camManager!!.cameraIdList[0]
                        camManager!!.setTorchMode(cameraId, true)
                    }
                } catch (e: CameraAccessException) {
                    isFlashOn = false
                    Log.e(TAG, e.toString())
                }
            } else {
                try {
                    mCamera = Camera.open()
                    parameters = mCamera?.parameters
                    parameters?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                    mCamera?.parameters = parameters
                    mCamera?.startPreview()
                } catch (e: RuntimeException) {
                    isFlashOn = false
                }
            }
        }

        /**
         * @Notes: If user phone has front flash, then the front flash will beep
         * if don't have the front flash then the back flash will beep
         * Right now it's just turn on the back flash
         */
        fun startBlinkingFlash(context: Context) {
            isBlinking = true
            while (isBlinking) {
                if (isFlashOn) {
                    turnOff(context)
                } else {
                    turnOn(context)
                }
                try {
                    Thread.sleep(150)
                } catch (e: InterruptedException) {

                }
            }
        }

        fun stopBlinkingFlash(context: Context) {
            isBlinking = false
            turnOff(context)
        }

        private fun turnOff(context: Context) {
            isFlashOn = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val cameraId: String
                    camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    if (camManager != null) {
                        cameraId =
                            camManager!!.cameraIdList[0] // Usually front camera is at 0 position.
                        camManager!!.setTorchMode(cameraId, false)
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            } else {
                mCamera = Camera.open()
                parameters = mCamera?.parameters
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                mCamera?.parameters = parameters
                mCamera?.stopPreview()
            }
        }
    }


}