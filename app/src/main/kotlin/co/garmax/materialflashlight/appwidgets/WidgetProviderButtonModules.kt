package co.garmax.materialflashlight.appwidgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import co.garmax.materialflashlight.CustomApplication
import co.garmax.materialflashlight.R
import co.garmax.materialflashlight.modules.ModuleBase

class WidgetProviderButtonModules : WidgetProviderButton() {

    protected val ACTIONE_WIDGET_MODE_SCREEN_CLICK =
            "co.garmax.materialflashlight.action.WIDGET_MODE_SCREEN_CLICK"
    protected val ACTION_WIDGET_MODE_FLASHLIGHT_CLICK =
            "co.garmax.materialflashlight.action.WIDGET_MODE_FLASHLIGHT_CLICK"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        (context.applicationContext as CustomApplication).applicationComponent.inject(this)

        val N = appWidgetIds.size

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (i in 0..N - 1) {
            val appWidgetId = appWidgetIds[i]

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views = RemoteViews(context.packageName, R.layout.view_widget_button_modules)

            // Set on intent to handle onclick
            views.setOnClickPendingIntent(R.id.button_widget,
                    getPendingSelfIntent(context, ACTION_WIDGET_BUTTON_CLICK))
            views.setOnClickPendingIntent(R.id.button_widget_module_flash,
                    getPendingSelfIntent(context, ACTION_WIDGET_MODE_FLASHLIGHT_CLICK))
            views.setOnClickPendingIntent(R.id.button_widget_module_screen,
                    getPendingSelfIntent(context, ACTIONE_WIDGET_MODE_SCREEN_CLICK))

            // Set button image according to current state
            if (mModuleManager.isRunning()) {
                setWidgetImage(context, views, R.id.button_widget, R.drawable.ic_widget_button_on)
            } else {
                setWidgetImage(context, views, R.id.button_widget, R.drawable.ic_widget_button_off)
            }

            // Set module
            if(mPreferences.module == ModuleBase.MODULE_CAMERA_FLASHLIGHT) {
                setWidgetImage(context, views, R.id.button_widget_module_flash, R.drawable.ic_module_flashlight_white)
                setWidgetImage(context, views, R.id.button_widget_module_screen, R.drawable.ic_module_screen_accent)
            } else {
                setWidgetImage(context, views, R.id.button_widget_module_flash, R.drawable.ic_module_flashlight_accent)
                setWidgetImage(context, views, R.id.button_widget_module_screen, R.drawable.ic_module_screen_white)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if(context == null) return

        (context.applicationContext as CustomApplication).applicationComponent.inject(this)

        // Change module and update widgets
        if (ACTION_WIDGET_MODE_FLASHLIGHT_CLICK.equals(intent?.action)) {

            if(mPreferences.module != ModuleBase.MODULE_CAMERA_FLASHLIGHT) {

                mLightController.changeModule(ModuleBase.MODULE_CAMERA_FLASHLIGHT)

                (context.applicationContext as CustomApplication).updateWidgets()
            }
        } else if (ACTIONE_WIDGET_MODE_SCREEN_CLICK.equals(intent?.action)) {

            if(mPreferences.module != ModuleBase.MODULE_SCREEN) {

                mLightController.changeModule(ModuleBase.MODULE_SCREEN)

                (context.applicationContext as CustomApplication).updateWidgets()
            }
        }
    }

}
