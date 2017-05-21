package com.dtz.clinic.action.creators

import com.dtz.clinic.dispatcher.Dispatcher
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

abstract class ActionsCreator(protected var dispatcher: Dispatcher) {

    protected fun execute(single: Single<*>, success: Consumer<Any>, error: Consumer<Throwable>) {
        single.subscribeOn(Schedulers.io()).subscribe(success, error)
    }

    protected fun execute(any: Any, success: Consumer<Any>) {
        Single.just(any).subscribeOn(Schedulers.io()).subscribe(success)
    }
}
