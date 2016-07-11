package co.garmax.materialflashlight.modules

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager

/**
 * Module for device screen
 */
class ScreenModule(context: Context) : ModuleBase(context) {

    var mPreviousScreenBrightness: Int = 0
    var mPreviousBrightnessMode: Int = 0

    override fun turnOn() {
        setBrightnessVolume(100)
    }

    override fun turnOff() {
        setBrightnessVolume(0)
    }

    override fun setBrightnessVolume(percent: Int) {
        val intent = Intent(ACTION_SCREEN_MODULE)
        intent.putExtra(EXTRA_BRIGHTNESS_PERCENT, percent)

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun isSupported(): Boolean {
        return true
    }

    override fun start() {
        // Save initial values
        mPreviousScreenBrightness = Settings.System.getInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS)
        mPreviousBrightnessMode = Settings.System.getInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE)

        // Set system values
        Settings.System.putInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, 255)
        Settings.System.putInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
    }

    override fun stop() {

        // Restore sytem values
        Settings.System.putInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, mPreviousScreenBrightness)
        Settings.System.putInt(context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE, mPreviousBrightnessMode)
    }

    override fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {

            val grantIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            grantIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(grantIntent)

            return false
        }

        return true
    }

    companion object {
        const val ACTION_SCREEN_MODULE = "action_screen_module"
        const val EXTRA_BRIGHTNESS_PERCENT = "extra_brightness_percent"
    }
}