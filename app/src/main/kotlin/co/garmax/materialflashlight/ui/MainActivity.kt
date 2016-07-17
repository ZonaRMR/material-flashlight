package co.garmax.materialflashlight.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import butterknife.Bind
import butterknife.ButterKnife
import butterknife.OnClick
import co.garmax.materialflashlight.*
import co.garmax.materialflashlight.modes.ModeBase
import co.garmax.materialflashlight.modes.ModeService
import co.garmax.materialflashlight.modules.ModuleBase
import co.garmax.materialflashlight.modules.ModuleManager
import javax.inject.Inject

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
        ModuleManager.OnStateChangedListener {

    @Bind(R.id.image_appbar)
    lateinit var mImageAppbar: ImageView
    @Bind(R.id.switch_keep_screen_on)
    lateinit var mSwitchKeepScreenOn: SwitchCompat
    @Bind(R.id.switch_auto_turn_on)
    lateinit var mSwitchAutoTurnOn: SwitchCompat
    @Bind(R.id.radio_torch)
    lateinit var mRadioTorch: RadioButton
    @Bind(R.id.radio_interval_strobe)
    lateinit var mRadioIntervalStrobe: RadioButton
    @Bind(R.id.radio_sound_strobe)
    lateinit var mRadioSoundStrobe: RadioButton
    @Bind(R.id.radio_camera_flashlight)
    lateinit var mRadioCameraFlashlight: RadioButton
    @Bind(R.id.radio_screen)
    lateinit var mRadioScreen: RadioButton
    @Bind(R.id.fab)
    lateinit var mFab: FloatingActionButton
    @Bind(R.id.text_version)
    lateinit var mTextVersion: TextView

    @Inject
    lateinit var mLightController: LightController
    @Inject
    lateinit var mModuleManager: ModuleManager
    @Inject
    lateinit var mPreferences: Preferences

    private lateinit var mAnimatedDrawableDay: AnimatedVectorDrawableCompat
    private lateinit var mAnimatedDrawableNight: AnimatedVectorDrawableCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        (application as CustomApplication).applicationComponent.inject(this)

        mAnimatedDrawableDay = AnimatedVectorDrawableCompat.create(this, R.drawable.avc_appbar_day)
                as AnimatedVectorDrawableCompat
        mAnimatedDrawableNight = AnimatedVectorDrawableCompat.create(this, R.drawable.avc_appbar_night)
                as AnimatedVectorDrawableCompat

        setupLayout()

        mModuleManager.addOnStateChangedListener(this)

        // Handle auto turn on
        if (savedInstanceState == null && mPreferences.isAutoTurnOn) {
            mLightController.start()
        }
        // Restore state on recreation
        else {
            setState(mModuleManager.isRunning(), false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mModuleManager.removeOnStateChangedListener(this@MainActivity)
    }

    private fun setupLayout() {

        // Restore settings
        mSwitchKeepScreenOn.isChecked = mPreferences.isKeepScreenOn
        mSwitchAutoTurnOn.isChecked = mPreferences.isAutoTurnOn

        // Set module
        updateModule()

        // Set mode
        updateMode()

        // Set keep screen on
        updateKeepScreenOn()

        mRadioSoundStrobe.setOnCheckedChangeListener(this)
        mRadioIntervalStrobe.setOnCheckedChangeListener(this)
        mRadioTorch.setOnCheckedChangeListener(this)
        mSwitchKeepScreenOn.setOnCheckedChangeListener(this)
        mSwitchAutoTurnOn.setOnCheckedChangeListener(this)
        mRadioCameraFlashlight.setOnCheckedChangeListener(this)
        mRadioScreen.setOnCheckedChangeListener(this)
        mTextVersion.text = getString(R.string.text_version, BuildConfig.VERSION_NAME)
    }

    private fun updateKeepScreenOn() {

        mFab.keepScreenOn = mSwitchKeepScreenOn.isChecked
    }

    private fun updateModule() {
        when (mPreferences.module) {
            ModuleBase.MODULE_CAMERA_FLASHLIGHT -> {
                mRadioCameraFlashlight.isChecked = true
            }
            ModuleBase.MODULE_SCREEN -> {
                mRadioScreen.isChecked = true
            }
        }
    }

    private fun updateMode() {
        when (mPreferences.mode) {
            ModeBase.MODE_INTERVAL_STROBE -> {
                mRadioIntervalStrobe.isChecked = true
            }
            ModeBase.MODE_TORCH -> {
                mRadioTorch.isChecked = true
            }
            ModeBase.MODE_SOUND_STROBE -> {
                mRadioSoundStrobe.isChecked = true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updateModule()
    }

    override fun stateChanged(isRunning: Boolean) {
        setState(isRunning, true)
    }

    private fun setState(turnedOn: Boolean, animated: Boolean) {
        runOnUiThread(
                {
                    if (turnedOn) {
                        // Fab image
                        mFab.setImageResource(R.drawable.ic_power_on)

                        // Appbar image
                        if (animated) {
                            mImageAppbar.setImageDrawable(mAnimatedDrawableDay)
                            mAnimatedDrawableDay.start()
                        } else {
                            mImageAppbar.setImageResource(R.drawable.vc_appbar_day)
                        }
                    } else {
                        // Fab image
                        mFab.setImageResource(R.drawable.ic_power_off)

                        // Appbar image
                        if (animated) {
                            mImageAppbar.setImageDrawable(mAnimatedDrawableNight)

                            mAnimatedDrawableNight.start()
                        } else {
                            mImageAppbar.setImageResource(R.drawable.vc_appbar_night)
                        }
                    }
                }
        )
    }

    @OnClick(R.id.fab, R.id.layout_keep_screen_on, R.id.layout_auto_turn_on)
    internal fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {

                // Turn off light
                if (mModuleManager.isRunning()) {
                    mLightController.stop()
                }
                // Turn on light
                else {
                    mLightController.start()
                }
            }

            R.id.layout_keep_screen_on ->
                mSwitchKeepScreenOn.toggle()

            R.id.layout_auto_turn_on ->
                mSwitchAutoTurnOn.toggle()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.switch_keep_screen_on -> {
                mPreferences.isKeepScreenOn = isChecked
                updateKeepScreenOn()
            }
            R.id.switch_auto_turn_on -> {
                mPreferences.isAutoTurnOn = isChecked
            }
            R.id.radio_sound_strobe -> {
                if (isChecked) changeMode(ModeBase.MODE_SOUND_STROBE)
            }
            R.id.radio_interval_strobe -> {
                if (isChecked) changeMode(ModeBase.MODE_INTERVAL_STROBE)
            }
            R.id.radio_torch -> {
                if (isChecked) changeMode(ModeBase.MODE_TORCH)
            }
            R.id.radio_camera_flashlight ->
                if (isChecked) mLightController.changeModule(ModuleBase.MODULE_CAMERA_FLASHLIGHT)

            R.id.radio_screen ->
                if (isChecked) mLightController.changeModule(ModuleBase.MODULE_SCREEN)
        }
    }

    private fun changeMode(mode: Int) {
        mPreferences.mode = mode

        // Start new mode if in running state
        if (mModuleManager.isRunning()) mLightController.start()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        // Stop service if user exit with back button
        if (mModuleManager.isRunning()) {
            ModeService.setMode(this, ModeBase.MODE_OFF)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null && intent.getBooleanExtra(EXTRA_FINISH, false)) {
            finish()
        }
    }

    companion object {
        const val EXTRA_FINISH = "extra_finish"
    }
}
