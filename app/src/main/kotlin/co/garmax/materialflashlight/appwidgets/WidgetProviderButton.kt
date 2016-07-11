package co.garmax.materialflashlight.appwidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.AppCompatDrawableManager
import android.widget.RemoteViews
import co.garmax.materialflashlight.CustomApplication
import co.garmax.materialflashlight.LightController
import co.garmax.materialflashlight.Preferences
import co.garmax.materialflashlight.R
import co.garmax.materialflashlight.modules.ModuleManager
import javax.inject.Inject

open class WidgetProviderButton : AppWidgetProvider() {

    protected val ACTION_WIDGET_BUTTON_CLICK = "co.garmax.materialflashlight.action.WIDGET_BUTTON_CLICK"

    @Inject
    lateinit var mLightController: LightController
    @Inject
    lateinit var mModuleManager: ModuleManager
    @Inject
    lateinit var mPreferences: Preferences

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        (context.applicationContext as CustomApplication).applicationComponent.inject(this)

        val N = appWidgetIds.size

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (i in 0..N - 1) {
            val appWidgetId = appWidgetIds[i]

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views = RemoteViews(context.packageName, R.layout.view_widget_button)

            // Set on intent to handle onclick
            views.setOnClickPendingIntent(R.id.button_widget, getPendingSelfIntent(context,
                    ACTION_WIDGET_BUTTON_CLICK))

            // Set image according to current state
            if (mModuleManager.isRunning()) {
                setWidgetImage(context, views, R.id.button_widget, R.drawable.ic_widget_button_on)
            } else {
                setWidgetImage(context, views, R.id.button_widget, R.drawable.ic_widget_button_off)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    protected fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
        // An explicit intent directed at the current class (the "self").
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    protected fun setWidgetImage(context: Context, remoteViews: RemoteViews, viewRes: Int, imageRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteViews.setImageViewResource(viewRes, imageRes)
        } else {
            var drawable = AppCompatDrawableManager.get().getDrawable(context, imageRes)

            drawable = DrawableCompat.wrap(drawable).mutate()

            if (drawable != null) {
                val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888)

                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)

                remoteViews.setImageViewBitmap(viewRes, bitmap)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if(context == null) return

        (context.applicationContext as CustomApplication).applicationComponent.inject(this)

        if (ACTION_WIDGET_BUTTON_CLICK.equals(intent?.action)) {

            if (mModuleManager.isRunning()) {

                mLightController.stop()

            } else {

                mLightController.start()

            }
        }
    }

}
