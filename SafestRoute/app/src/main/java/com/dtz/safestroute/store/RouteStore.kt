package com.dtz.clinic.store

import android.content.Context
import android.location.Location
import com.dtz.clinic.action.Action
import com.dtz.clinic.action.Actions
import com.dtz.clinic.action.Keys
import com.dtz.clinic.dispatcher.Dispatcher
import com.dtz.safestroute.event.OnRedAreasCalculated
import com.dtz.safestroute.event.OnRouteError
import com.dtz.safestroute.event.OnRouteReceived
import com.dtz.safestroute.event.OnSaferRouteReceived
import com.dtz.safestroute.net.gson.RedAreas
import com.dtz.safestroute.net.gson.RouteResponse
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.toSingle
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Created by duenytz on 2/5/16.
 */
@Singleton
class RouteStore @Inject constructor(dispatcher: Dispatcher, bus: EventBus, val context: Context, val gson: Gson) : Store(dispatcher, bus) {
    //    var field = ObservableField<String>()

    private var nearestUnMatchLat: Double? = null
    private var nearestUnMatchLng: Double? = null
    private var redAreas: RedAreas? = null

    override fun onActionReceived(action: Action) {
        when (action.type) {
            Actions.ROUTE_RECEIVED -> {
                val route = action.getByKey(Keys.ROUTE) as RouteResponse
                processRoute(route, Consumer {
                    emitStoreChange(OnRouteReceived(it))
                    getInvolvedRedAreas(it, false, 500)
                })
            }
            Actions.SAFER_ROUTE_RECEIVED -> {
                val route = action.getByKey(Keys.ROUTE) as RouteResponse
                processRoute(route, Consumer {
                    emitStoreChange(OnSaferRouteReceived(it))
                })
            }
            Actions.CHECK_CURRENT_LOCATION -> {
                val latLng = action.getByKey(Keys.LOCATION) as LatLng
                val list = ArrayList<LatLng>()
                list.add(latLng)
                getInvolvedRedAreas(list, true, 100)
            }
            Actions.ROUTE_ERROR -> {
                handleError(action)
                emitStoreChange(OnRouteError())
            }
            Actions.SAFER_ROUTE_ERROR -> {
                handleError(action)
                emitStoreChange(OnRouteError())
            }
        }
    }

    private fun processRoute(route: RouteResponse, consumer: Consumer<List<LatLng>>) {
        Flowable.fromIterable(route.response.route[0].shape)
                .concatMap {
                    Flowable.fromArray(it.split(","))
                            .concatMapIterable { it }
                            .map { it.toDouble() }
                            .toList()
                            .map { LatLng(it[0], it[1]) }
                            .toFlowable()
                }
                .toList()
                .subscribe(consumer)
    }

    private fun getInvolvedRedAreas(routeShape: List<LatLng>, notify: Boolean, radio: Int) {
        nearestUnMatchLat = Double.MAX_VALUE
        nearestUnMatchLng = Double.MAX_VALUE
        redAreas = redAreas ?: loadRedAreas()
        val involvedRedAreas: MutableList<RedAreas.RedArea> = ArrayList()
        routeShape.asSequence()
                .flatMap { checkRedAreasForLatLng(it, radio).asSequence() }
                .filterNot { involvedRedAreas.contains(it) }
                .filter { involvedRedAreas.size < 20 }
                .map {
                    involvedRedAreas.add(it)
                    "${it.lat0},${it.lon0};${it.lat1},${it.lon1}"
                }
                .fold("") { whole, next -> if (whole.isEmpty()) next else "$whole!$next" }
                .toSingle()
                .subscribe(Consumer {
                    if (involvedRedAreas.isNotEmpty()) {
                        emitStoreChange(OnRedAreasCalculated(involvedRedAreas, it, notify))
                    }
                })
    }

    private fun checkRedAreasForLatLng(latLng: LatLng, radio: Int): List<RedAreas.RedArea> {
        return Flowable.fromIterable(redAreas!!.areas)
                .map { Pair(it, it.getMiddlePoint()) }
                .filter { isRoutePointNearToRedArea(latLng, it.second, radio) }
                .map { it.first }
                .toList()
                .blockingGet()
    }

    fun isRoutePointNearToRedArea(routePoint: LatLng, redAreaCenter: LatLng, minDistanceConsidered: Int): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(routePoint.latitude, routePoint.longitude, redAreaCenter.latitude, redAreaCenter.longitude, results)
        val isRedAreaNear = results[0] < minDistanceConsidered

        val latDifference = routePoint.latitude - redAreaCenter.latitude
        val lngDifference = routePoint.longitude - redAreaCenter.longitude

        if (!isRedAreaNear) {
            if (Math.abs(latDifference) > Math.abs(lngDifference)) {
                nearestUnMatchLat = redAreaCenter.latitude
            } else {
                nearestUnMatchLng = redAreaCenter.longitude
            }
        }
        return isRedAreaNear
    }

    private fun loadRedAreas(): RedAreas {
        val rawFile = context.assets.open("red_areas.txt")
        val reader = BufferedReader(InputStreamReader(rawFile, "UTF8"))
        return gson.fromJson(reader, RedAreas::class.java)
    }
}


