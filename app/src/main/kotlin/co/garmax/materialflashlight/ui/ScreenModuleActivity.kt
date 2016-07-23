package co.garmax.materialflashlight.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import co.garmax.materialflashlight.CustomApplication
import co.garmax.materialflashlight.R
import co.garmax.materialflashlight.modes.ModeBase
import co.garmax.materialflashlight.modes.ModeService
import co.garmax.materialflashlight.modules.ModuleManager
import co.garmax.materialflashlight.modules.ScreenModule
import io.codetail.animation.ViewAnimationUtils
import javax.inject.Inject

/**
 * Emit light flow for screen module
 */
class ScreenModuleActivity : AppCompatActivity(), ModuleManager.OnStateChangedListener {

    @BindView(R.id.layout_content)
    lateinit var mLayoutContent: View
    @BindView(R.id.layout_light)
    lateinit var mLayoutLight: View
    @BindView(R.id.fab)
    lateinit var mFab: View

    @Inject
    lateinit var mModuleManager: ModuleManager

    var mInitialized : Boolean = false

    /**
     * Receive and handle commands from screen module
     */
    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            // Get brightness value
            val brightness = intent.getIntExtra(ScreenModule.EXTRA_BRIGHTNESS_PERCENT, 100)

            setBrightness(brightness)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_module)
        ButterKnife.bind(this)

        setupEnterAnimations()

        (application as CustomApplication).applicationComponent.inject(this)

        mModuleManager.addOnStateChangedListener(this)

        // Set max brightness
        val lp = window.attributes
        lp.screenBrightness = 1f
        window.attributes = lp
    }

    override fun onDestroy() {
        super.onDestroy()

        mModuleManager.removeOnStateChangedListener(this@ScreenModuleActivity)
    }

    override fun stateChanged(isRunning: Boolean) {

        // Exit if stopped
        if (!isRunning) finish()
    }

    private fun setupEnterAnimations() {

        mLayoutContent.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                try {
                    circularRevealShow(mFab, mLayoutContent,
                            ContextCompat.getColor(this@ScreenModuleActivity, R.color.colorAccent),
                            ContextCompat.getColor(this@ScreenModuleActivity, R.color.windowBackground),
                            object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(p0: Animator?) {}

                                override fun onAnimationEnd(p0: Animator?) {
                                    mInitialized = true
                                    mLayoutLight.visibility = View.VISIBLE
                                }

                                override fun onAnimationCancel(p0: Animator?) {}

                                override fun onAnimationStart(p0: Animator?) {
                                    mLayoutLight.visibility = View.INVISIBLE
                                }

                            })

                    return true
                } finally {
                    mLayoutContent.viewTreeObserver.removeOnPreDrawListener(this)
                }
            }
        })
    }

    private fun setupExitAnimations() {

        circularRevealHide(mLayoutContent, mFab,
                ContextCompat.getColor(this@ScreenModuleActivity, R.color.windowBackground),
                ContextCompat.getColor(this@ScreenModuleActivity, R.color.colorAccent), object : Animator.AnimatorListener {

            override fun onAnimationEnd(p0: Animator?) {
                mLayoutContent.visibility = View.INVISIBLE
                ModeService.setMode(this@ScreenModuleActivity, ModeBase.MODE_OFF)
            }

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationRepeat(p0: Animator?) {}

        })
    }

    private fun circularRevealShow(viewFrom: View, viewTo: View, colorFrom: Int, colorTo: Int,
                                   listener : Animator.AnimatorListener) {

        val fromRadius = Math.min(viewFrom.width, viewFrom.height).toFloat()
        val toRadius = Math.max(viewTo.width, viewTo.height).toFloat()

        val duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        val cx = (viewFrom.left + viewFrom.right) / 2
        val cy = (viewFrom.top + viewFrom.bottom) / 2

        val set = AnimatorSet()
        val reveal = ViewAnimationUtils.createCircularReveal(viewTo, cx, cy, fromRadius, toRadius)
        reveal.duration = duration
        reveal.interpolator = AccelerateDecelerateInterpolator()

        val color = ObjectAnimator.ofObject(viewTo, "backgroundColor",
                ArgbEvaluator(), colorFrom, colorTo)
        color.duration = duration

        set.playTogether(reveal, color)
        set.addListener(listener)
        set.start()
    }

    private fun circularRevealHide( viewFrom: View, viewTo: View, colorFrom: Int, colorTo: Int,
                                    listener : Animator.AnimatorListener) {

        val fromRadius = Math.max(viewFrom.width, viewFrom.height).toFloat()
        val toRadius = Math.max(viewTo.width, viewTo.height).toFloat()

        val duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        val cx = viewTo.right - viewTo.width / 2
        val cy = viewTo.bottom - viewTo.height / 2

        val set = AnimatorSet()
        val reveal = ViewAnimationUtils.createCircularReveal(viewFrom, cx, cy, fromRadius, toRadius)
        reveal.duration = duration
        reveal.interpolator = AccelerateDecelerateInterpolator()

        val color = ObjectAnimator.ofObject(viewFrom, "backgroundColor",
                ArgbEvaluator(), colorFrom, colorTo)
        color.duration = duration

        set.playTogether(reveal, color)
        set.addListener(listener)
        set.start()
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).
                registerReceiver(mBroadcastReceiver, IntentFilter(ScreenModule.ACTION_SCREEN_MODULE))
    }

    override fun onPause() {
        super.onPause()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver)
    }

    @OnClick(R.id.fab)
    fun onFabClick() {

        setupExitAnimations()
    }

    private fun setBrightness(percent: Int) {

        val color = Color.argb(255 * percent / 100, 0, 0, 0)

        // Change color when animation finished
        if(mInitialized) {
            mLayoutLight.setBackgroundColor(color)
        }
    }

    override fun onBackPressed() {
        // Close main activity
        // Start activity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_FINISH, true)
        startActivity(intent)

        // Stop service
        ModeService.setMode(this, ModeBase.MODE_OFF)

        // Close current activity
        super.onBackPressed()
    }
}