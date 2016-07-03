@file:Suppress("DEPRECATION")

package co.garmax.materialflashlight.modules

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import timber.log.Timber
import java.io.IOException

/**
 * Module for camera LED flashlight
 */
class FlashModule(context: Context) : ModuleBase(context) {
    private var mCamera: Camera ? = null
    private var mPreviewTexture: SurfaceTexture ? = null

    override fun start() {

        mCamera = rearCamera()

        // Turn off flash by default
        if (isAvailable()) {
            turnOff()

            // We should keep refrence for texture to save from GC
            mPreviewTexture = SurfaceTexture(0)

            // Hack for some android versions
            try {
                mCamera!!.setPreviewTexture(mPreviewTexture)
            } catch (e: IOException) {
                Timber.e("Can't set preview texture")
            }
        }
    }

    override fun stop() {

        mCamera!!.release()
        mCamera = null
        mPreviewTexture = null

    }

    override fun turnOn() {
        val params = mCamera!!.parameters

        val flashModes = params.supportedFlashModes

        if (isParameterSupported(Camera.Parameters.FLASH_MODE_TORCH, flashModes)) {
            params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        } else if (isParameterSupported(Camera.Parameters.FLASH_MODE_ON, flashModes)) {
            params.flashMode = Camera.Parameters.FLASH_MODE_ON
        }

        mCamera!!.parameters = params
        mCamera!!.startPreview()
    }

    override fun turnOff() {

        val params = mCamera!!.parameters
        params.flashMode = Camera.Parameters.FLASH_MODE_OFF
        mCamera!!.parameters = params
        mCamera!!.stopPreview()
    }

    override fun isAvailable(): Boolean {
        try {
            if (mCamera != null) {
                // Try to get parameters to check if instance is available
                mCamera?.parameters

                return true
            }
        } catch(e: Exception) {
            Timber.e("Camera instance not available", e)
        }

        // Reset camera instance if not available
        mCamera = null

        return false
    }

    override fun isSupported(): Boolean {
        // If flash supported by camera
        if (isAvailable()) {
            val flashModes = mCamera!!.parameters.supportedFlashModes
            return isParameterSupported(Camera.Parameters.FLASH_MODE_TORCH, flashModes) ||
                    isParameterSupported(Camera.Parameters.FLASH_MODE_ON, flashModes)
        }
        // Return true and will check by flash modes
        // because hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) do not work correctly
        else {
            return true
        }
    }

    override fun setBrightnessVolume(percent: Int) {
        if (percent >= 50) {
            turnOn()
        } else {
            turnOff()
        }
    }

    fun rearCamera(): Camera? {
        var cameraId = -1
        val info = Camera.CameraInfo()

        for (i in 0..Camera.getNumberOfCameras() - 1) {
            Camera.getCameraInfo(i, info)

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                break
            }
        }

        if (cameraId < 0) {
            Timber.w("Wrong camera id %d", cameraId)
            return null
        }

        var cameraResult: Camera ? = null

        try {
            cameraResult = Camera.open(cameraId)
        } catch (e: Exception) {
            Timber.e(e, "Exception when open camera %d", cameraId)
        }

        return cameraResult
    }

    fun isParameterSupported(value: String, supported: List<String>?): Boolean {
        return supported != null && supported.indexOf(value) >= 0
    }

    override fun checkPermissions(requestCode: Int, activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), requestCode)

            return false
        }

        return true
    }
}