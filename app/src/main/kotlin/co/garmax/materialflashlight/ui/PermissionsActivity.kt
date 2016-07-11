package co.garmax.materialflashlight.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import co.garmax.materialflashlight.CustomApplication
import co.garmax.materialflashlight.LightController
import co.garmax.materialflashlight.modes.ModeBase
import co.garmax.materialflashlight.modes.ModeService
import co.garmax.materialflashlight.modules.ModuleManager
import javax.inject.Inject

class PermissionsActivity : AppCompatActivity() {

    @Inject
    lateinit var mLightController: LightController
    @Inject
    lateinit var mModuleManager: ModuleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as CustomApplication).applicationComponent.inject(this)

        if(intent != null) {
            val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, RC_CHECK_PERMISSION)
            val permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS_ARRAY)

            ActivityCompat.requestPermissions(this, permissions, requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_START_LIGHT) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLightController.start()
            } else if (mModuleManager.isRunning()) {
                ModeService.setMode(this, ModeBase.MODE_OFF)
            }
        }

        finish()
    }

    companion object {

        const val RC_START_LIGHT = 0
        const val RC_CHECK_PERMISSION = 1

        private const val EXTRA_PERMISSIONS_ARRAY = "extra_permissions_array"
        private const val EXTRA_REQUEST_CODE = "extra_request_code"

        fun startActivity(context: Context, permissions: Array<String>, requestCode: Int) {
            val intent = Intent(context, PermissionsActivity::class.java)

            intent.putExtra(EXTRA_PERMISSIONS_ARRAY, permissions)
            intent.putExtra(EXTRA_REQUEST_CODE, requestCode)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        }
    }
}
