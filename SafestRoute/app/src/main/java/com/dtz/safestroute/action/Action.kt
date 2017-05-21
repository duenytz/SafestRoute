package com.dtz.clinic.action

import android.annotation.SuppressLint

@SuppressLint("UseSparseArrays")
class Action internal constructor(val type: String, private val data: HashMap<Int, Any>?) {

    fun getByKey(key: Int): Any {
        return data!![key]!!
    }

    class Builder {
        private var type: String? = null
        private var data: HashMap<Int, Any>? = null

        fun with(type: String): Builder {
            this.type = type
            this.data = HashMap<Int, Any>()
            return this
        }

        fun bundle(key: Int, value: Any?): Builder {
            if (value == null) {
                throw IllegalArgumentException("Value may not be null.")
            }
            data!!.put(key, value)
            return this
        }

        fun build(): Action {
            return Action(type!!, data)
        }
    }

    companion object {

        fun type(type: String): Builder {
            return Builder().with(type)
        }
    }
}
