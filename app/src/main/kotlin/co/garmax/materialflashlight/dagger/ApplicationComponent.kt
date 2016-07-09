package co.garmax.materialflashlight.dagger

import co.garmax.materialflashlight.modes.ModeService
import co.garmax.materialflashlight.ui.MainActivity
import co.garmax.materialflashlight.ui.ScreenModuleActivity
import co.garmax.materialflashlight.appwidgets.WidgetProviderButton
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(ApplicationModule::class, ContextModule::class))
@Singleton
interface ApplicationComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(customService: ModeService)
    fun inject(screenModuleActivity: ScreenModuleActivity)
    fun inject(widgetProvider: WidgetProviderButton)
}