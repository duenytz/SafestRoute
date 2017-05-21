package com.dtz.clinic.action.creators

import android.content.Context
import com.dtz.clinic.action.Actions
import com.dtz.clinic.action.Keys
import com.dtz.clinic.dispatcher.Dispatcher
import com.dtz.clinic.net.service.RouteService
import com.dtz.safestroute.R
import com.google.android.gms.maps.model.LatLng
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by duenytz on 06/02/16.
 */
@Singleton
class RouteActionsCreator @Inject constructor(dispatcher: Dispatcher, var context: Context, var routeService: RouteService) : ActionsCreator(dispatcher) {

    fun getRoute(waypoint0: String, waypoint1: String) {
        execute(routeService.calculateRoute(context.getString(R.string.here_app_id), context.getString(R.string.here_app_token), waypoint0, waypoint1, null),
                Consumer { dispatcher.dispatch(Actions.ROUTE_RECEIVED, Keys.ROUTE, it) },
                Consumer { dispatcher.dispatch(Actions.ROUTE_ERROR, Keys.ERROR, it) })
    }

    fun getSaferRoute(waypoint0: String, waypoint1: String, avoidAreas: String) {
        execute(routeService.calculateRoute(context.getString(R.string.here_app_id), context.getString(R.string.here_app_token), waypoint0, waypoint1, avoidAreas),
                Consumer { dispatcher.dispatch(Actions.SAFER_ROUTE_RECEIVED, Keys.ROUTE, it) },
                Consumer { dispatcher.dispatch(Actions.SAFER_ROUTE_ERROR, Keys.ERROR, it) })
    }

    fun checkLocationSafety(latLng: LatLng) {
        execute(latLng, Consumer { dispatcher.dispatch(Actions.CHECK_CURRENT_LOCATION, Keys.LOCATION, it) })
    }
}