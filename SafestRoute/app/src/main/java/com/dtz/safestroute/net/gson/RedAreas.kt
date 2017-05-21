package com.dtz.safestroute.net.gson

import com.google.android.gms.maps.model.LatLng

/**
 * Created by duenytz on 5/20/17.
 */

class RedAreas(val areas: List<RedAreas.RedArea>) {
    inner class RedArea constructor(val lat0: Double, val lon0: Double, val lat1: Double, val lon1: Double) {
        fun getMiddlePoint(): LatLng {
            return LatLng((lat0 + lat1) / 2, (lon0 + lon1) / 2)
        }
    }
}
