package project.inflabnet.mytest.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.inflabnet.infsocial.maps.PlacesRootClass
import project.inflabnet.mytest.R
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import kotlin.properties.Delegates

class MapaDeLocais : AppCompatActivity(), OnMapReadyCallback {

    //mapa
    lateinit var context: Context
    lateinit var mMap: GoogleMap
    var nomeTipo: String? = null

    //pegar localização atual
    private val PERMISSION_ID = 30
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var txtLat by Delegates.notNull<Double>()
    var txtLon by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_de_locais)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        nomeTipo = intent.getStringExtra("nomeTipo")


    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {



                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        txtLat = location.latitude
                        txtLon = location.longitude
                        val key: String? = resources.getString(R.string.google_maps_key)
                        HitApi(this,txtLat, txtLon,10000,nomeTipo!!, key!!).execute()
                    }
                }
            } else {
                Toast.makeText(this, "Localização desligada. O GPS deve estar ligado para seguir", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            txtLat = mLastLocation.latitude
            txtLon = mLastLocation.longitude
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //pegar localização atual
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

    }
    private inner class HitApi: AsyncTask<Void, Void, String> {
        var context : Context? = null
        var lat : Double? = null
        var lng : Double? = null
        var radius : Int? = null
        var type : String? = null
        var key : String? = null

        constructor(context: Context,lat: Double,lng: Double,radius: Int,type: String, key : String) {
            this.context = context
            this.lat = lat
            this.lng = lng
            this.radius = radius
            this.type = type
            this.key = key
        }
        override fun doInBackground(vararg params: Void?): String {

            return GooglePlacesApi().getPlacesJson(context as Context,lat as  Double,lng as Double,radius as Int,type as String,key as String)

        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val gson = GsonBuilder().create()
            val root = gson.fromJson(result, PlacesRootClass::class.java)
            addMarkers(root)
        }
    }

    fun addMarkers(root: PlacesRootClass){
        for (result  in root.results){
            val p  = LatLng(result.geometry.location.lat, result.geometry.location.lng)
            mMap.addMarker(MarkerOptions().position(p).title(result.name))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(txtLat, txtLon)))
        val cam = mMap.cameraPosition
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(txtLat, txtLon),12f,0f,0f)))
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
    }
}
