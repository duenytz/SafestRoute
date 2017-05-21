package com.dtz.safestroute.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import com.dtz.clinic.action.creators.RouteActionsCreator
import com.dtz.safestroute.R
import com.dtz.safestroute.net.gson.RedAreas
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import io.reactivex.Flowable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Created by duenytz on 05/17/17.
 */
@Singleton //
class GoogleMapUtil @Inject constructor(private val context: Context, private val actionsCreator: RouteActionsCreator) :
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    companion object {
        private val INITIAL_ZOOM = 15.0f
    }

    private var firstTimeLocation: Boolean = false
    private var locationRequest: LocationRequest? = null
    private var lastLocation: Location? = null
    private var googleMap: GoogleMap? = null
    private var googleApiClient: GoogleApiClient? = null
    private var lastDestination: LatLng? = null

    init {
        firstTimeLocation = true
    }

    fun start() {
        googleApiClient?.connect()
    }

    fun stop() {
        stopLocationUpdates()
        googleApiClient?.disconnect()
    }

    fun destroy() {
        firstTimeLocation = true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.night_style_json))
        this.googleMap!!.uiSettings.isMapToolbarEnabled = false
        this.googleMap!!.uiSettings.isZoomControlsEnabled = false
        this.googleMap!!.uiSettings.isCompassEnabled = false
        this.googleMap!!.uiSettings.isMyLocationButtonEnabled = true
        this.googleMap!!.isMyLocationEnabled = true
        this.googleMap!!.setOnMapClickListener { latLng ->
            if (lastLocation != null) {
                actionsCreator.getRoute(String.format(Locale.US, "geo!%f,%f", lastLocation!!.latitude, lastLocation!!.longitude),
                        String.format(Locale.US, "geo!%f,%f", latLng.latitude, latLng.longitude))
                lastDestination = latLng
            }
        }
        initGoogleApiClient()
    }

    fun initGoogleApiClient() {
        googleApiClient = googleApiClient ?: GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        googleApiClient?.connect()
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onConnected(bundle: Bundle?) {
        requestSettings()
    }

    fun requestSettings() {
        if (locationRequest == null) {
            locationRequest = LocationRequest.create()
            locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest?.interval = (30 * 1000).toLong()
            locationRequest?.fastestInterval = (5 * 1000).toLong()
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true)
        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result1 ->
            val status = result1.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> startLocationUpdates()
            }
        }
    }

    fun startLocationUpdates() {
        if (!googleApiClient?.isConnected!!) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
    }

    fun stopLocationUpdates() {
        if (!googleApiClient?.isConnected!!) {
            return
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
    }

    override fun onLocationChanged(location: Location?) {
        this.lastLocation = location
        if (lastLocation != null && firstTimeLocation) {
            this.googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngFromMyLocation, INITIAL_ZOOM))
            firstTimeLocation = false
        }
        actionsCreator.checkLocationSafety(LatLng(lastLocation?.latitude!!, lastLocation?.longitude!!))
    }

    private val latLngFromMyLocation: LatLng
        get() = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)

    fun drawRegularRoute(routeShape: List<LatLng>) {
        googleMap?.clear()
        drawRoute(routeShape, ContextCompat.getColor(context, R.color.mapsRegularRoute), 9, 1f)
    }

    fun drawSaferRoute(routeShape: List<LatLng>) {
        drawRoute(routeShape, ContextCompat.getColor(context, R.color.mapsSaferRoute), 6, 2f)
    }

    fun drawRoute(routeShape: List<LatLng>, color: Int, stroke: Int, zIndex: Float) {
        val polylineOptions = PolylineOptions().width(dpToPx(stroke).toFloat()).color(color).geodesic(true).zIndex(zIndex)
        var latLng: LatLng = latLngFromMyLocation
        Flowable.fromIterable(routeShape)
                .doOnNext {
                    polylineOptions.add(it)
                    latLng = it
                }
                .doOnComplete {
                    googleMap?.addPolyline(polylineOptions)
                    googleMap?.addMarker(MarkerOptions().position(latLng))
                }
                .subscribe()
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun drawRedAreas(redAreas: List<RedAreas.RedArea>) {
        Flowable.fromIterable(redAreas)
                .map { it.getMiddlePoint() }
                .doOnNext {
                    googleMap?.addGroundOverlay(GroundOverlayOptions()
                            .image(getDrawableDescriptor())
                            .position(it, (80 * 2).toFloat())
                            .zIndex(3f))
                }
                .subscribe()
    }

    private var redAreaBitmap: Bitmap? = null

    fun getDrawableDescriptor(): BitmapDescriptor {
        if (redAreaBitmap == null) {
            redAreaBitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(redAreaBitmap)
            val shape = ContextCompat.getDrawable(context, R.drawable.map_circle)
            shape.setBounds(0, 0, redAreaBitmap?.width!!, redAreaBitmap?.height!!)
            shape.draw(canvas)
        }
        return BitmapDescriptorFactory.fromBitmap(redAreaBitmap)
    }

    fun getSaferRoute(avoidAreasString: String) {
        actionsCreator.getSaferRoute(String.format(Locale.US, "geo!%f,%f", lastLocation!!.latitude, lastLocation!!.longitude),
                String.format(Locale.US, "geo!%f,%f", lastDestination!!.latitude, lastDestination!!.longitude), avoidAreasString)
    }
}
