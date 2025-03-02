package com.example.smartfridge_app_finalproject.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.SupermarketChains
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import android.util.Log

import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar

class SuperMarketFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var searchInput: AutoCompleteTextView
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private val markers = mutableListOf<Marker>()

    // Search radius in meters (20 km)
    private val searchRadius = 20000.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }
        placesClient = Places.createClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return inflater.inflate(R.layout.fragment_super_market, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.supermarket_TIET_search)
        setupSearchDropdown()

        val mapFragment = childFragmentManager.findFragmentById(R.id.supermarket_MAP_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupSearchDropdown() {
        val chains = SupermarketChains.chains.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            chains
        )

        searchInput.setAdapter(adapter)
        searchInput.inputType = 0

        searchInput.setOnItemClickListener { _, _, position, _ ->
            val selectedChain = chains[position]
            searchSupermarkets(selectedChain)
        }
    }



    private fun searchSupermarkets(chainName: String) {
        currentLocation?.let { location ->
            // נקה מרקרים קיימים
            clearMarkers()

            // שדות המקום שאנחנו רוצים לאחזר
            val placeFields = listOf(
                Place.Field.DISPLAY_NAME,
                Place.Field.LOCATION,
//                Place.Field.ADDRESS
            )

            // הגדר תחום חיפוש סביב המיקום הנוכחי
            val locationBias = RectangularBounds.newInstance(
                LatLng(location.latitude - 0.2, location.longitude - 0.2),
                LatLng(location.latitude + 0.2, location.longitude + 0.2)
            )

            // בנה בקשת חיפוש אוטומטי
            val autocompleteRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery("$chainName")
                .setLocationBias(locationBias)
                .build()

            placesClient.findAutocompletePredictions(autocompleteRequest)
                .addOnSuccessListener { response ->
                    // אם אין תוצאות
                    if (response.autocompletePredictions.isEmpty()) {
                        showNoResultsMessage()
                        return@addOnSuccessListener
                    }

                    // עבור על כל התוצאות
                    response.autocompletePredictions.forEach { prediction ->
                        // בקשה לפרטים מלאים של המקום
                        val fetchPlaceRequest = FetchPlaceRequest.builder(
                            prediction.placeId,
                            placeFields
                        ).build()

                        placesClient.fetchPlace(fetchPlaceRequest)
                            .addOnSuccessListener { placeResponse ->
                                val place = placeResponse.place

                                // חשב מרחק
                                val results = FloatArray(1)
                                Location.distanceBetween(
                                    location.latitude,
                                    location.longitude,
                                    place.latLng!!.latitude,
                                    place.latLng!!.longitude,
                                    results
                                )

                                // הוסף מרקר רק אם במרחק המבוקש
                                if (results[0] <= searchRadius) {
                                    addMarkerToMap(place)
                                }
                            }
                            .addOnCompleteListener {
                                // לאחר סיום כל החיפושים
                                if (markers.isEmpty()) {
                                    showNoResultsMessage()
                                } else {
                                    // התמקד במרקרים
                                    val builder = LatLngBounds.Builder()
                                    markers.forEach { builder.include(it.position) }
                                    val bounds = builder.build()
                                    googleMap?.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(bounds, 100)

                                    )
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SupermarketSearch", "Error in autocomplete search", exception)
                    showErrorMessage(exception.message ?: "Error searching for supermarkets")
                }
        } ?: showLocationError()
    }

    private fun addMarkerToMap(place: Place) {
        place.latLng?.let { latLng ->
            val marker = googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(place.name)
                    .snippet(place.address)
            )
            marker?.let { markers.add(it) }
        }
    }

    private fun clearMarkers() {
        markers.forEach { it.remove() }
        markers.clear()
    }

    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = it
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                    )

                    // Add marker for current location
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("מיקומך הנוכחי")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                } ?: showLocationError()
            }
        } else {
            requestLocationPermission()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isMyLocationButtonEnabled = true
        }

        if (checkLocationPermission()) {
            setupMapWithLocation()
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }

        // Set up info window click listener for navigation
        map.setOnInfoWindowClickListener { marker ->
            // Handle navigation to the selected supermarket
            marker.position?.let { position ->
                val uri = "google.navigation:q=${position.latitude},${position.longitude}"
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(uri)
                )
                startActivity(intent)
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            Constants.PermissionRequest.LOCATION_PERMISSION_REQUEST
        )
    }

    private fun setupMapWithLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            e.printStackTrace()
            showErrorMessage("Permission denied: Cannot enable location")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.PermissionRequest.LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    setupMapWithLocation()
                    getCurrentLocation()
                } else {
                    showErrorMessage("Location permission is required")
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showLocationError() {
        showErrorMessage("Could not get current location")
    }

    private fun showNoResultsMessage() {
        showErrorMessage("לא נמצאו סניפים בטווח של 20 ק\"מ")
    }
}