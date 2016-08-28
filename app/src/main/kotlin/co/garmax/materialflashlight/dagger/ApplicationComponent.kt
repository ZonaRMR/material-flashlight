package co.garmax.materialflashlight.dagger

import co.garmax.materialflashlight.LightTileService
import co.garmax.materialflashlight.modes.ModeService
import co.garmax.materialflashlight.ui.MainActivity
import co.garmax.materialflashlight.ui.ScreenModuleActivity
import co.garmax.materialflashlight.appwidgets.WidgetProviderButton
import co.garmax.materialflashlight.appwidgets.WidgetProviderButtonModules
import co.garmax.materialflashlight.ui.PermissionsActivity
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(ApplicationModule::class, ContextModule::class))
@Singleton
interface ApplicationComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(customService: ModeService)
    fun inject(screenModuleActivity: ScreenModuleActivity)
    fun inject(permissionActivity: PermissionsActivity)
    fun inject(widgetProvider: WidgetProviderButton)
    fun inject(widgetProvider: WidgetProviderButtonModules)
    fun inject(tileService: LightTileService)
}