package com.dtz.clinic.dispatcher

import com.dtz.clinic.action.Action
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Dispatcher @Inject constructor(val actionBus: PublishSubject<Action>) {

    fun dispatch(action: String, vararg data: Any) {
        if (action.isEmpty()) {
            actionBus.onError(IllegalArgumentException("Type must not be empty"))
            return
        }

        if (data.size % 2 != 0) {
            actionBus.onError(IllegalArgumentException("Data must be a valid list of key,value pairs"))
            return
        }

        val actionBuilder = Action.type(action)
        var i = 0
        while (i < data.size) {
            val key = data[i++] as Int
            val value = data[i++]
            actionBuilder.bundle(key, value)
        }
        actionBus.onNext(actionBuilder.build())
    }
}
