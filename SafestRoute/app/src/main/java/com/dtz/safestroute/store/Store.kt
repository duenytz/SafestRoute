package com.dtz.clinic.store

import com.dtz.clinic.action.Action
import com.dtz.clinic.action.Keys
import com.dtz.clinic.dispatcher.Dispatcher
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.Subject
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

abstract class Store(dispatcher: Dispatcher, val bus: EventBus) {
    init {
        dispatcher.actionBus.subscribe({ onActionReceived(it) }, { handleError(it) })
    }

    abstract fun onActionReceived(action: Action)

    protected fun handleError(action: Action) {
        handleError(action.getByKey(Keys.ROUTE) as Throwable)
    }

    protected fun handleError(throwable: Throwable) {
        Timber.e(throwable)
    }

    protected fun emitStoreChange(event: Any) {
        Subject.create<Any> { bus.post(event) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

}
