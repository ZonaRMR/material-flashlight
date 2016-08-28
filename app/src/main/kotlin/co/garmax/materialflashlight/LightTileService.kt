package co.garmax.materialflashlight

import android.annotation.SuppressLint
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import co.garmax.materialflashlight.modules.ModuleManager
import javax.inject.Inject

@SuppressLint("NewApi")
class LightTileService : TileService(), ModuleManager.OnStateChangedListener {

    @Inject lateinit var mModuleManager: ModuleManager
    @Inject lateinit var mLightController: LightController

    override fun onCreate() {
        super.onCreate()

        (application as CustomApplication).applicationComponent.inject(this)
    }

    override fun onClick() {
        super.onClick()

        // switch from active to passive based on tiles current state
        when (qsTile.state) {
            Tile.STATE_ACTIVE -> {
                setCurrentState(Tile.STATE_INACTIVE)
                mLightController.stop()
            }
            Tile.STATE_INACTIVE -> {
                setCurrentState(Tile.STATE_ACTIVE)
                mLightController.start()
            }
            Tile.STATE_UNAVAILABLE -> {
            }
        }
    }

    private fun setCurrentState(state: Int) {
        val tile = qsTile
        qsTile.state = state
        when (state) {
            Tile.STATE_ACTIVE -> {
                tile.icon = Icon.createWithResource(applicationContext, R.drawable.ic_quick_settings_active)
            }
            Tile.STATE_INACTIVE -> {
                tile.icon = Icon.createWithResource(applicationContext, R.drawable.ic_quick_settings_inactive)
            }
            Tile.STATE_UNAVAILABLE -> {
                tile.icon = Icon.createWithResource(applicationContext, R.drawable.ic_quick_settings_unavailable)
            }
        }

        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()

        if (mModuleManager.isRunning()) setCurrentState(Tile.STATE_ACTIVE)
        else if (!mModuleManager.isSupported()) setCurrentState(Tile.STATE_UNAVAILABLE)
        else setCurrentState(Tile.STATE_INACTIVE)

        mModuleManager.addOnStateChangedListener(this)
    }

    override fun onStopListening() {
        super.onStopListening()
        mModuleManager.addOnStateChangedListener(this)
    }

    override fun stateChanged(isRunning: Boolean) {
        setCurrentState(if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE)
    }

}