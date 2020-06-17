package project.inflabnet.mytest.maps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.inflabnet.infsocial.maps.PlacesRootClass
import project.inflabnet.mytest.R
import project.inflabnet.mytest.maps.model.NomesPlacesEnderecos
import project.inflabnet.mytest.mesas.activity.MembrosAdapter
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.alert_foto_item.*
import kotlinx.android.synthetic.main.alert_foto_item.view.*
import project.inflabnet.mytest.BuildConfig
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

class MapsActivity : AppCompatActivity() {

    //places
    var placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)
    lateinit var placesClient: PlacesClient
    internal var placeId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        //places
        requestPermission()
        initPlaces()
        setupPlacesAutocomplete()
        setupCurrentPlace()
    }

    //places abaixo
    private fun requestPermission() {
        Dexter.withActivity(this)
            .withPermissions(Arrays.asList(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION

            ))
            .withListener(object:MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MapsActivity,"A permissão é necessária", Toast.LENGTH_LONG).show()
                }
            }).check()

    }

    private fun initPlaces() {
        
        Places.initialize(this, getString(R.string.places_api_key))
        placesClient = Places.createClient(this)
    }

    private fun setupPlacesAutocomplete() {
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.fragment_place) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(placeFields)
        autocompleteFragment.view?.setBackgroundColor(resources.getColor(R.color.colorAccent))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            autocompleteFragment.view?.outlineAmbientShadowColor
        }

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                Toast.makeText(this@MapsActivity, "" + p0.address, Toast.LENGTH_LONG).show()
                val addressUri = Uri.parse("geo:0,0?q=$p0.address")
                val intent = Intent(Intent.ACTION_VIEW, addressUri)
                // Achar uma activity para o intent e abrir
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Log.d("ImplicitIntents", "Can't handle this intent!")
                }
            }
            override fun onError(p0: Status) {
                Toast.makeText(this@MapsActivity, "" + p0.statusMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupCurrentPlace() {
        val request = FindCurrentPlaceRequest.builder(placeFields).build()

            if (ActivityCompat.checkSelfPermission(
                    this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this,"Permissão necessária para seguir",Toast.LENGTH_SHORT).show()
            }

            val placeResponse = placesClient.findCurrentPlace(request)

            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val response = task.result
                    placeId = response!!.placeLikelihoods[0].place.id!!
                    val likehoods = StringBuilder("")
                    val toReturn: ArrayList<NomesPlacesEnderecos> = ArrayList()
                    txtMeuLocal.text = StringBuilder(response.placeLikelihoods[0].place.address!!)

                    for (placeLikelihood in response.placeLikelihoods){
                        val n = placeLikelihood.place.name
                        val e = placeLikelihood.place.address
                        var r = placeLikelihood.place.rating
                        val p = round(placeLikelihood.likelihood*100).roundToInt()
                        val id = placeLikelihood.place.id
                        if (r == null){
                            r = 0.0
                        }
                            val localNew = NomesPlacesEnderecos(n,e,r,p,id)
                            toReturn.add(localNew)
                            likehoods.append(
                                String.format(
                                    "Local '%s' parece estar próximo: %d%%",
                                    placeLikelihood.place.name,
                                    round(placeLikelihood.likelihood*100).roundToInt()))
                                .append("\n")
                        }
                    setupRV(toReturn)
                    //edt_place_likelihoods.setText(likehoods.toString())
                    }
                else
                {
                    Toast.makeText(this, "Local não encontrado", Toast.LENGTH_LONG).show()
                }
            }

    }

    private fun setupRV(toReturn:ArrayList<NomesPlacesEnderecos>) {
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        rvLocais.layoutManager = linearLayoutManager
        rvLocais.scrollToPosition(toReturn.size)
        rvLocais.adapter = PlacesNamesAdapter(toReturn){
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("O que gostaria de ver?")
                .setCancelable(false)
                .setPositiveButton("Localização"){_, _ ->
                    val addressUri = Uri.parse("geo:0,0?q=${it.endereco}")
                    val intent = Intent(Intent.ACTION_VIEW, addressUri)

                    // Achar avtivity que abra a intent
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Log.d("ImplicitIntents", "Can't handle this intent!")
                    }

                }
                .setNegativeButton("Foto") { _, _ ->
                    verFotoDoLocal(it)

                }
                .setNeutralButton("Cancelar") {_, _ ->
                    Toast.makeText(this,"Operação cancelada",Toast.LENGTH_SHORT).show()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Locais Interessantes?")
            alert.show()

        }
    }

    private fun verFotoDoLocal(placeId: NomesPlacesEnderecos?) {
        if (TextUtils.isEmpty(placeId!!.placeId)) {
            Toast.makeText(this@MapsActivity, "Place ID não pode ser nulo", Toast.LENGTH_SHORT)
                .show()
        }
        getFoto(placeId)
    }

    private fun getFoto(placeId: NomesPlacesEnderecos) {
        val placeRequest = FetchPlaceRequest.builder(
            placeId.placeId!!,
            Arrays.asList(Place.Field.PHOTO_METADATAS,
                Place.Field.LAT_LNG)).build()

        placesClient.fetchPlace(placeRequest).addOnSuccessListener { fetchPlaceResponse ->
            val place = fetchPlaceResponse.place

            try {
                val photoMetaData = place.photoMetadatas!![0]
                //Criar a requesição
                val photoRequest = FetchPhotoRequest.builder(photoMetaData).build()
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
                    val bitmap = fetchPhotoResponse.bitmap
                    val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_foto_item, null)
                    val mBuilder = AlertDialog.Builder(this)
                        .setView(mDialogView)
                        .setTitle("${placeId.nome}\n${placeId.endereco}")

                    val  mAlertDialog = mBuilder.show()
                    mAlertDialog.imageViewFoto.setImageBitmap(bitmap)
                    mDialogView.btnRetornar.setOnClickListener {
                        mAlertDialog.dismiss()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAlertDialog.create()
                    }else
                        Toast.makeText(this,"Versão do Android não suportada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception){
                Toast.makeText(this,"Foto não disponível para esse local",Toast.LENGTH_SHORT).show()
            }
        }
    }

}