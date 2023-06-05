package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.PermissionUtils
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.Locale

const val REQUEST_LOCATION_PERMISSION = 1

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var map: GoogleMap? = null
    private var currentPointOfInterest: PointOfInterest? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        binding.btnSaveLocation.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }

    private fun onLocationSelected() {
        currentPointOfInterest?.let {
            _viewModel.updatePOI(it)
        }
    }

    override fun onMapReady(map: GoogleMap) {

        this.map = map

        _viewModel.selectedPOI.value?.let { poi ->
            updateMapToLocation(poi)
        }

        setMapStyle(map)

        setMapClick(map)

        setPoiClick(map)

        enableMyLocation()
    }


    private fun setMapClick(map: GoogleMap?) {
        map?.setOnMapClickListener { latLng ->
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val scope = CoroutineScope(Dispatchers.Main)
                scope.launch {
                    val addresses = withContext(Dispatchers.IO) {
                        fetchLocationOnClick(geocoder, latLng)
                    }
                    val address: String = addresses[0].getAddressLine(0)
                    updateMapToLocation(PointOfInterest(latLng, "", address))
                }
            } else {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null && addresses.size > 0) {
                    val address: String = addresses[0].getAddressLine(0)
                    updateMapToLocation(PointOfInterest(latLng, "", address))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchLocationOnClick(
        geocoder: Geocoder,
        latLng: LatLng
    ): List<Address> = suspendCancellableCoroutine { continuation ->
        geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        ) { addresses ->
            continuation.resume(addresses) {
                Timber.d(it)
            }
        }
    }

    private fun updateMapToLocation(poi: PointOfInterest) {
        binding.btnSaveLocation.visibility = View.VISIBLE
        currentPointOfInterest = poi
        map?.clear()
        val marker = map?.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
        )
        //Add circular radius around the marker
        map?.addCircle(
            com.google.android.gms.maps.model.CircleOptions()
                .center(poi.latLng)
                .radius(150.0)
                .strokeWidth(1f)
                .strokeColor(R.color.colorAccent)
                .fillColor(R.color.colorAccent)
        )
        marker?.showInfoWindow()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.google_map_style)
            )
            if (success) {
                Timber.d("Style parsing success.")
            } else {
                Timber.d("Style parsing failed.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun setPoiClick(map: GoogleMap?) {
        map?.setOnPoiClickListener { poi ->
            updateMapToLocation(poi)
        }
    }


    private fun isPermissionGranted(): Boolean {
        val context = requireContext()
        return PermissionUtils.isGranted(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) && PermissionUtils.isGranted(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map?.isMyLocationEnabled = true
            fetchUserLocationAndMoveCamera()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchUserLocationAndMoveCamera() {
        if (isPermissionGranted()) {
            //Get user current location and move camera
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map?.animateCamera(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            15f
                        )
                    )
                }
            }
        } else {
            requestPermissions()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.settings) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        BuildConfig.APPLICATION_ID,
                        null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }.show()
            }
        }
    }
}