package com.dtz.safestroute.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import com.dtz.clinic.store.RouteStore
import com.dtz.safestroute.App
import com.dtz.safestroute.R
import com.dtz.safestroute.event.OnRedAreasCalculated
import com.dtz.safestroute.event.OnRouteError
import com.dtz.safestroute.event.OnRouteReceived
import com.dtz.safestroute.event.OnSaferRouteReceived
import com.dtz.safestroute.util.GoogleMapUtil
import com.duenytz.fluxtest.view.extension.snack
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_map.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject


class MapActivity : AppCompatActivity() {
    val REQUEST_CODE_ASK_PERMISSIONS = 0

    @Inject lateinit var bus: EventBus
    @Inject lateinit var googleMapUtil: GoogleMapUtil
    @Inject lateinit var store: RouteStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        setContentView(R.layout.activity_map)

        checkPermissions()
    }

    override fun onStart() {
        googleMapUtil.start()
        bus.register(this)
        super.onStart()
    }

    override fun onStop() {
        bus.unregister(this)
        googleMapUtil.stop()
        super.onStop()
    }

    override fun onDestroy() {
        googleMapUtil.destroy()
        super.onDestroy()
    }

    private fun checkPermissions() {
        val missingPermissions = ArrayList<String>()
        val REQUIRED_SDK_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE)
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (missingPermissions.isEmpty()) {
            setupMap()
            return
        }
        val permissions = missingPermissions.toTypedArray()
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                for (index in permissions.indices.reversed()) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index] + "' not granted, exiting", Toast.LENGTH_LONG).show()
                        finish()
                        return
                    }
                }
                setupMap()
            }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(googleMapUtil)
    }

    @Subscribe fun onEvent(event: OnRedAreasCalculated) {
        googleMapUtil.drawRedAreas(event.involvedRedAreas)
        if (!event.notify) {
            googleMapUtil.getSaferRoute(event.avoidAreasString)
            return
        }
        SimpleDialogFragment.createBuilder(this, supportFragmentManager)
                .setMessage(R.string.dangerous_zone_warning_text)
                .setPositiveButtonText(R.string.ok_btn_text)
                .show()
        (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(500)
    }

    @Subscribe fun onEvent(event: OnRouteReceived) {
        googleMapUtil.drawRegularRoute(event.routeShape)
    }

    @Subscribe fun onEvent(event: OnRouteError) {
        mainContainer.snack(R.string.route_error) {}
    }

    @Subscribe fun onEvent(event: OnSaferRouteReceived) {
        googleMapUtil.drawSaferRoute(event.routeShape)
    }
}
