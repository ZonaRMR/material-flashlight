package co.garmax.materialflashlight

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import co.garmax.materialflashlight.modes.ModeBase
import co.garmax.materialflashlight.modules.ModuleBase

class Preferences(internal var mContext: Context) {

    private var mSharedPreferences: SharedPreferences

    init {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    var isKeepScreenOn: Boolean
        get() = mSharedPreferences.getBoolean(KEEP_SCREEN_ON, false)
        set(enabled) = mSharedPreferences.edit().putBoolean(KEEP_SCREEN_ON, enabled).apply()

    var isAutoTurnOn: Boolean
        get() = mSharedPreferences.getBoolean(AUTO_TURN_ON, false)
        set(enabled) = mSharedPreferences.edit().putBoolean(AUTO_TURN_ON, enabled).apply()

    var mode: Int
        get() = mSharedPreferences.getInt(MODE, ModeBase.MODE_TORCH)
        set(mode) = mSharedPreferences.edit().putInt(MODE, mode).apply()

    var module: Int
        get() = mSharedPreferences.getInt(MODULE, ModuleBase.MODULE_CAMERA_FLASHLIGHT)
        set(mode) = mSharedPreferences.edit().putInt(MODULE, mode).apply()

    companion object {
        private const val KEEP_SCREEN_ON = "keep_screen_on"
        private const val MODE = "mode"
        private const val MODULE = "module"
        private const val AUTO_TURN_ON = "auto_turn_on"
    }
}
