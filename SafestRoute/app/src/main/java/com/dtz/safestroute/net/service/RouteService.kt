package com.dtz.clinic.net.service

import com.dtz.safestroute.net.gson.RouteResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by dxsier on 5/12/17.
 */
interface RouteService {

    @GET("calculateroute.json?mode=shortest;pedestrian&routeattributes=sh")
    fun calculateRoute(@Query("app_id") appId: String, @Query("app_code") appCode: String, @Query("waypoint0") waypoint0: String,
                       @Query("waypoint1") waypoint1: String, @Query("avoidareas") avoidAreas: String?): Single<RouteResponse>
}