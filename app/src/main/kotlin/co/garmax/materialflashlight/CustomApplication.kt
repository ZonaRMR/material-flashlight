package co.garmax.materialflashlight

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import co.garmax.materialflashlight.appwidgets.WidgetProviderButton
import co.garmax.materialflashlight.appwidgets.WidgetProviderButtonModules
import co.garmax.materialflashlight.dagger.ApplicationComponent
import co.garmax.materialflashlight.dagger.ApplicationModule
import co.garmax.materialflashlight.dagger.ContextModule
import co.garmax.materialflashlight.dagger.DaggerApplicationComponent
import timber.log.Timber

class CustomApplication : Application() {

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule())
                .contextModule(ContextModule(applicationContext))
                .build()

        Timber.tag("garmax")
        Timber.plant(Timber.DebugTree())
    }

    /**
     * Update all widgets if state changed
     */
    fun updateWidgets() {

        val widgetProviders = arrayOf(WidgetProviderButton::class.java,
                WidgetProviderButtonModules::class.java)

        for (widgetProviderClass in widgetProviders) {
            val idsWidgetButton = AppWidgetManager.getInstance(this).
                    getAppWidgetIds(ComponentName(this, widgetProviderClass))

            val intentButton = Intent(this, widgetProviderClass)
            intentButton.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intentButton.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsWidgetButton)
            sendBroadcast(intentButton)
        }
    }
}
