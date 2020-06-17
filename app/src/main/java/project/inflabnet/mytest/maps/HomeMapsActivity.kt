package project.inflabnet.mytest.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import project.inflabnet.mytest.HomeActivity
import project.inflabnet.mytest.R
import project.inflabnet.mytest.login.LoginActivity
import project.inflabnet.mytest.maps.model.NomesPlacesTraducao
import project.inflabnet.mytest.maps.model.UserLocation
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home_maps.*
import kotlinx.android.synthetic.main.escolhe_tipo.view.*

class HomeMapsActivity : AppCompatActivity() {

    private val PERMISSION_ID = 30
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mAuth: FirebaseAuth? = null

    //Firebase references para conexão
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_maps)

        MobileAds.initialize(this) {}
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                //.setTestDeviceIds(Arrays.asList("275AE6C1B126313586B3368295FE5B80"))
                .build()
        )

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        ad_view_maps.loadAd(adRequest)

        txtLatitude.visibility = View.GONE
        txtLongitude.visibility = View.GONE
        textView22.visibility = View.GONE
        textView20.visibility = View.GONE
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference

        btnHabilitarLocalizacao.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Gostaria de compartilhar sua localização?")
                .setCancelable(false)
                .setPositiveButton("Sim"){_, _ ->
                    setLocationOn()
                    Toast.makeText(this, "Localização compartilhada", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Não") { _, _ ->
                    setLocationOff()
                    Toast.makeText(this,"Localização não será mais compartilhada",Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton("Cancelar") {_, _ ->
                    Toast.makeText(this,"Operação cancelada",Toast.LENGTH_SHORT).show()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Compartilhar Localização?")
            alert.show()
        }

        btnSugestaoLocais.setOnClickListener {
            val listPlace: List<NomesPlacesTraducao> = listOf(
                NomesPlacesTraducao(
                    "bar",
                    "Bares"
                ),
                NomesPlacesTraducao(
                    "bakery",
                    "Padarias"
                ),
                NomesPlacesTraducao(
                    "night_club",
                    "Casas Noturnas"
                ),
                NomesPlacesTraducao(
                    "cafe",
                    "Cafés"
                ),
                NomesPlacesTraducao(
                    "restaurant",
                    "Restaurantes"
                ),
                NomesPlacesTraducao(
                    "convenience_store",
                    "Lojas de Conveniência"
                ),
                NomesPlacesTraducao(
                    "shopping_mall",
                    "Shoppings"
                )
                )
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.escolhe_tipo, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Qual o tipo de lugar?")
            val  mAlertDialog = mBuilder.show()
            val linearLayoutManager = LinearLayoutManager(applicationContext)
                mDialogView.rcRecycleV.layoutManager = linearLayoutManager
                mDialogView.rcRecycleV.scrollToPosition(listPlace.size)
                mDialogView.rcRecycleV.adapter = PlacesAdapter(listPlace){
                    Toast.makeText(this,it.nome,Toast.LENGTH_SHORT).show()
                    val selectedItem= it.name
                    val intt = Intent(this, MapaDeLocais::class.java)
                        intt.putExtra("nomeTipo",selectedItem)
                        startActivity(intt)
                }

            mDialogView.btnVoltar.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }

        btnProcurarLocal.setOnClickListener {
            val intt = Intent(this, MapsActivity::class.java)
            startActivity(intt)
        }

        btnVoltarMenu.setOnClickListener {
            val intt = Intent(this, HomeActivity::class.java)
            startActivity(intt)
        }
    }

    private fun setLocationOff() {
        val dbRefe = mDatabaseReference!!
        val userFBase = pegarUser()?.replace(".","")
        dbRefe.child("UserLocations").child(userFBase!!).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this,"Localização não está sendo compartilhada",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLocationOn() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        txtLatitude.text = location.latitude.toString()
                        txtLongitude.text = location.longitude.toString()
                        txtLatitude.visibility = View.VISIBLE
                        txtLongitude.visibility = View.VISIBLE
                        textView22.visibility = View.VISIBLE
                        textView20.visibility = View.VISIBLE
                        //update firebase
                        var userFBase = pegarUser()
                          if (userFBase != null) {
                              userFBase = userFBase.replace(".","")
                        }
                        //atiualizar firebase com o nome do user e location (lat e lon)
                        //referencia do caminho
                        val dbRefe = mDatabaseReference!!
                        //gerar a key
                        val locTimestamp = System.currentTimeMillis().toString()
                        //montar o objeto
                        val userLocation =
                            UserLocation(
                                userFBase!!,
                                "${txtLatitude.text}, ${txtLongitude.text}",
                                locTimestamp
                            )
                        dbRefe.child("UserLocations").child(userFBase).setValue(userLocation)

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
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
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
        //de qq forma tem que voltar uma casinha caso peça permissão
        txtLatitude.text = "Para compartilhar a localização, aperte o botão acima"
        txtLatitude.visibility = View.VISIBLE
        val intt = Intent(this, HomeActivity::class.java)
        startActivity(intt)
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 30000
        mLocationRequest.fastestInterval = 15000
        mLocationRequest.numUpdates = 10
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            txtLatitude.text = mLastLocation.latitude.toString()
            txtLongitude.text = mLastLocation.longitude.toString()
        }
    }

    private fun pegarUser(): String?{
        //pegar o usuário
        var user: String? = null
        val userEmail = mAuth?.currentUser?.email
        //val user: String
        if (userEmail != null) {
            if (userEmail.contains("@")) {
                user =
                    userEmail.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            } else {
                user = userEmail
            }
        }else {
            val intt = Intent(this, LoginActivity::class.java)
            startActivity(intt)
        }
        return user
    }
}
