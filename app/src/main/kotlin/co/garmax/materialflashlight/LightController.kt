package co.garmax.materialflashlight

import android.content.Context
import android.content.Intent
import co.garmax.materialflashlight.modes.ModeBase
import co.garmax.materialflashlight.modes.ModeService
import co.garmax.materialflashlight.modules.FlashModule
import co.garmax.materialflashlight.modules.ModuleBase
import co.garmax.materialflashlight.modules.ModuleManager
import co.garmax.materialflashlight.modules.ScreenModule
import co.garmax.materialflashlight.ui.ScreenModuleActivity
import timber.log.Timber

/**
 * Implemented global features to control light
 * Called from different places like widget, activity
 */
class LightController(val mContext: Context, val mModuleManager: ModuleManager, val mPreferences: Preferences) {

    fun changeModule(module: Int) {

        // Exit if current module is set already
        // It can be changed from widget and updated onResume
        if(module == mPreferences.module) return

        val isRunning = mModuleManager.isRunning()

        if (isRunning) {
            mModuleManager.stop()
        }

        mPreferences.module = module

        if (module == ModuleBase.MODULE_CAMERA_FLASHLIGHT) {
            mModuleManager.module = FlashModule(mContext)
        } else if (module == ModuleBase.MODULE_SCREEN) {
            mModuleManager.module = ScreenModule(mContext)
        } else {
            throw IllegalArgumentException("Unknown module type " + module)
        }

        // Try to turn on if was running and turn off if can't do that
        if (isRunning && !start()) stop()
    }

    fun stop() {

        if (mModuleManager.isRunning()) mModuleManager.stop()

        ModeService.setMode(mContext, ModeBase.MODE_OFF)
    }

    fun start(): Boolean {
        val mode = mPreferences.mode
        val module = mPreferences.module
//TODO implement in separated activity
        // Exit if we don't have permission for the module
       // if (!mModuleManager.checkPermissions(MainActivity.RC_MODULE_PERMISSIONS, mContext)) return false

        // Exit if we don't have permission for sound strobe mode
      //  if (mode == ModeBase.MODE_SOUND_STROBE &&
      //          !SoundStrobeMode.checkPermissions(MainActivity.RC_MODE_PERMISSIONS, mContext)) return false

        // Start activity for screen module
        if (module == ModuleBase.MODULE_SCREEN) {

            // Start activity
            val intent = Intent(mContext, ScreenModuleActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(intent)
        }

        Timber.d("Started mode %s; module %s", mode, module)

        ModeService.setMode(mContext, mode)

        return true
    }
}
