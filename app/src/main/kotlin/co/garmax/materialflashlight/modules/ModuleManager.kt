package co.garmax.materialflashlight.modules

import timber.log.Timber
import java.util.*

/**
 * Work with hardware light modules
 */
class ModuleManager {

    interface OnStateChangedListener {
        fun stateChanged(isRunning: Boolean)
    }

    // If light session started (During session light can be turned on or turned off)
    private var mIsRunning: Boolean = false

    // Used to detect if module try to start or stop because it can take a while
    private var mIsProcessing: Boolean = false

    private var mOnStateChangedListeners: WeakHashMap<OnStateChangedListener, Int>? = null

    var module: ModuleBase ? = null

    fun turnOff() {

        // Exit if in transition state
        if(mIsProcessing) return

        if (!isAvailable()) {
            Timber.w("turnOff() called when module not available")
            return
        }

        module!!.turnOff()
    }

    fun turnOn() {
        // Exit if in transition state
        if(mIsProcessing) return

        if (!isAvailable()) {
            Timber.w("turnOn() called when module not available")
            return
        }

        module!!.turnOn()
    }

    fun start() {

        // Exit if in transition state
        if(mIsProcessing) return

        mIsProcessing = true

        module!!.start()

        if (module!!.isAvailable()) {
            mIsRunning = true

            // Notify listeners
            if (mOnStateChangedListeners != null) {
                for (listener in mOnStateChangedListeners!!.keys) {
                    listener?.stateChanged(mIsRunning)
                }
            }
        } else {
            Timber.e("Module not started")
        }

        mIsProcessing = false
    }

    fun stop() {

        // Exit if in transition state
        if(mIsProcessing) return

        // Turn off light
        turnOff()

        mIsProcessing = true

        mIsRunning = false

        // Notify listeners
        if (mOnStateChangedListeners != null) {
            for (listener in mOnStateChangedListeners!!.keys) {
                listener?.stateChanged(mIsRunning)
            }
        }

        if (isAvailable()) {
            module!!.stop()
        } else {
            Timber.w("stop() called when module not available")
        }

        mIsProcessing = false
    }

    fun isRunning(): Boolean {
        return mIsRunning
    }

    fun isSupported(): Boolean {
        return module!!.isSupported()
    }

    fun isAvailable(): Boolean {
        return module!!.isAvailable()
    }

    fun setBrightnessVolume(volume: Int) {
        if (!isAvailable()) {
            Timber.w("setBrightnessVolume() called when module not available")
            return
        }

        module!!.setBrightnessVolume(volume)
    }

    fun addOnStateChangedListener(onStateChangedListener: OnStateChangedListener) {

        if(mOnStateChangedListeners == null) {
            mOnStateChangedListeners = WeakHashMap()
        }

        mOnStateChangedListeners?.put(onStateChangedListener, mOnStateChangedListeners?.size)
    }

    fun removeOnStateChangedListener(onStateChangedListener: OnStateChangedListener) {
        mOnStateChangedListeners?.remove(onStateChangedListener)
    }

    fun checkPermissions(): Boolean {
        return module!!.checkPermissions()
    }
}
