package com.dtz.clinic.dagger.module

import android.content.Context
import com.dtz.clinic.action.Action
import com.dtz.safestroute.App
import com.dtz.safestroute.BuildConfig
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module class AppModule(private val app: App) {

    @Provides @Singleton internal fun provideContext(): Context = app

    @Provides @Singleton internal fun provideEventBus(): EventBus = EventBus.builder()
            .throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus()

    @Provides @Singleton internal fun provideActionsHub(): PublishSubject<Action> = PublishSubject.create<Action>()
}
