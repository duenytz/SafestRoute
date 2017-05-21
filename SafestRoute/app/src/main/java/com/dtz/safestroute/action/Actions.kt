package com.dtz.clinic.action

interface Actions {
    companion object {
        const val ROUTE_RECEIVED = "ROUTE_RECEIVED"
        const val ROUTE_ERROR = "ROUTE_ERROR"
        const val SAFER_ROUTE_RECEIVED = "SAFER_ROUTE_RECEIVED"
        const val SAFER_ROUTE_ERROR = "SAFER_ROUTE_ERROR"
        const val CHECK_CURRENT_LOCATION = "CHECK_CURRENT_LOCATION"
    }
}
