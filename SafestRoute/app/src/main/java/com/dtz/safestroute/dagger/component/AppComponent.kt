package com.dtz.clinic.dagger.component

import com.dtz.clinic.dagger.module.AppModule
import com.dtz.clinic.dagger.module.NetModule
import com.dtz.safestroute.view.MapActivity
import dagger.Component
import javax.inject.Singleton

@Singleton @Component(modules = arrayOf(AppModule::class, NetModule::class))
interface AppComponent {

    fun inject(mapActivity: MapActivity)
}
