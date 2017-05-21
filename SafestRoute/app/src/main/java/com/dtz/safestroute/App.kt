package com.dtz.safestroute

import android.app.Application
import com.dtz.clinic.dagger.component.AppComponent
import com.dtz.clinic.dagger.component.DaggerAppComponent
import com.dtz.clinic.dagger.module.AppModule
import com.dtz.clinic.dagger.module.NetModule
import timber.log.Timber

/**
 * Created by duenytz on 5/17/17.
 */
class App : Application() {

    companion object {
        @JvmStatic lateinit var component: AppComponent
            private set
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        component = createComponent()
    }

    private fun createComponent(): AppComponent {
        return DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule())
                .build()
    }
}