package com.example.ubimap

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import com.example.ubimap.persistence.room.AppDatabase


class OpenStreetMapActivity : AppCompatActivity() {
    private val TAG = "yaOpenStreetMapActivity"
    private lateinit var map: MapView
    lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_street_map)

        Log.d(TAG, "onCreate: The activity is being created.");

        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location")

        // Display open street map
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(18.0)

        if (location != null) {
            Toast.makeText(this, "Start route on Location["+location.altitude+"]["+location.latitude+"]["+location.longitude+"]", Toast.LENGTH_SHORT).show()
            val startPoint = GeoPoint(location.latitude, location.longitude)
            map.controller.setCenter(startPoint)
            addMarker(startPoint, "My current location")
        }else{
            Toast.makeText(this, "Creating route without starting point...", Toast.LENGTH_SHORT).show()
        }

        // read default route from csv file
        val (coords, names) = readLocationsFromCsv(this, "routes.csv")
        addMarkersAndRoute(map, coords, names)


        // Load data coordinates from room db
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "coordinates").build()
        lifecycleScope.launch(Dispatchers.IO) {
            val locations = database.locationDao().getAllLocations() // Asume que este método devuelve una lista de tus entidades
            val locationsCoords = locations.map { GeoPoint(it.latitude, it.longitude) }
            withContext(Dispatchers.Main) {
                addMarkers(map, locationsCoords)
            }
        }
    }

    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        map.overlays.add(marker)
        map.invalidate() // Reload map
    }

    fun addMarkers(mapView: MapView, locationsCoords: List<GeoPoint>) {

        for (location in locationsCoords) {
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Marker at ${location.latitude}, ${location.longitude}"
            marker.icon = ContextCompat.getDrawable(this, com.google.android.material.R.drawable.ic_m3_chip_checked_circle)
            mapView.overlays.add(marker)
        }
        mapView.invalidate() // Refresh the map to display the new markers
    }

    /**
     * Default route read from csv file
     */
    fun addMarkersAndRoute(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {
        if (locationsCoords.size != locationsNames.size) {
            Log.e("addMarkersAndRoute", "Locations and names lists must have the same number of items.")
            return
        }

        val route = Polyline()
        route.setPoints(locationsCoords)
        route.color = ContextCompat.getColor(this, R.color.teal_700)
        mapView.overlays.add(route)

        for (location in locationsCoords) {
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            val locationIndex = locationsCoords.indexOf(location)
            marker.title = "Marker at ${locationsNames[locationIndex]} ${location.latitude}, ${location.longitude}"
            marker.icon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.ic_menu_compass)
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    fun readLocationsFromCsv(context: Context, fileName: String): Pair<List<GeoPoint>, List<String>> {
        val coords = mutableListOf<GeoPoint>()
        val names = mutableListOf<String>()
        try {
            context.assets.open(fileName).bufferedReader().forEachLine { line ->
                val (name, latitude, longitude) = line.split(";")
                coords.add(GeoPoint(latitude.toDouble(), longitude.toDouble()))
                names.add(name)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading CSV file", e)
        }
        return Pair(coords, names)
    }


}
