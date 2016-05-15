package co.garmax.materialflashlight.dagger

import android.content.Context
import co.garmax.materialflashlight.Preferences
import co.garmax.materialflashlight.modules.ModuleManager
import dagger.Module
import dagger.Provides
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module class ApplicationModule() {

    @Provides @Singleton
    fun provideModuleManager(): ModuleManager {
        return ModuleManager()
    }

    @Provides @Singleton
    fun providePreferences(context: Context): Preferences {
        return Preferences(context);
    }

    @Provides @Singleton
    fun provideTaskThread(): ExecutorService {
        return Executors.newFixedThreadPool(1);
    }
}
