package project.inflabnet.mytest.mesas.activity

import android.content.Context
import android.content.Intent
import project.inflabnet.mytest.R
import project.inflabnet.mytest.mesas.adapter.MesaAdapter
import project.inflabnet.mytest.mesas.model.MembrosMesa
import project.inflabnet.mytest.mesas.model.Mesa
import project.inflabnet.mytest.mesas.model.MesaData
import project.inflabnet.mytest.mesas.model.User
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_mesa.*
import kotlinx.android.synthetic.main.pin_dialog.*
import kotlinx.android.synthetic.main.pin_dialog.okBtnPin
import kotlinx.android.synthetic.main.pin_dialog_verifica.*

class MesaActivity : AppCompatActivity() {

    //Firebase references
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mesa)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        //mDatabaseReference = mDatabase!!.reference.child("mesas")
        mDatabaseReference = mDatabase!!.reference
        criarMesa()
        btnCriarMesa.setOnClickListener { cadastrarMesa() }
        ACTVMesas.setOnClickListener { setACTVMesas() }
    }



    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onResume() {
        super.onResume()
        setACTVMesas()
        //ACTVMesas.setText("")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun setACTVMesas(){
        //autocomplete
        note_list_progress.visibility = View.VISIBLE
        val toReturn: ArrayList<Mesa> = arrayListOf()
        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (data in dataSnapshot.children) {
                    val mesaData = data.getValue<Mesa>(Mesa::class.java)
                    //unwrap
                    val mesa = mesaData?.let { it } ?: continue
                    mesa.let { toReturn.add(it) }
                }
                note_list_progress.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        mDatabaseReference?.child("Mesas")?.addValueEventListener(postListener)

        val adapter = ArrayAdapter<Mesa>(this, android.R.layout.simple_expandable_list_item_1, toReturn)
        ACTVMesas.setAdapter(adapter)
        ACTVMesas.threshold = 1
        ACTVMesas.text.toString()

        ACTVMesas.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as Mesa

            var txtPin: String? = null
            note_list_progress.visibility = View.VISIBLE
            //Toast.makeText(this, "${data.nameMesa} clicked", Toast.LENGTH_SHORT).show()
            //ao clicar ira para a tela da comanda da mesa - antes pedirá o pin
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val toReturn: ArrayList<Mesa> = ArrayList()
                    for(data in dataSnapshot.children){
                        val mesaData = data.getValue(Mesa::class.java)
                        //unwrap
                        val mesa = mesaData?.let { it } ?: continue
                        toReturn.add(mesa)
                    }
                    //sort so newest at bottom
                    toReturn.sortBy { mesa ->
                        mesa.timestamp
                    }
                    val mDialogView = LayoutInflater.from(this@MesaActivity).inflate(R.layout.pin_dialog_verifica,rootView,false )
                    val mBuilder = AlertDialog.Builder(this@MesaActivity)
                        .setView(mDialogView)
                        .setTitle("Digite o PIN para a Mesa:\n \u27A2 ${selectedItem.nameMesa}")
                    val  mAlertDialog = mBuilder.show()
                    mAlertDialog.okBtnPin.setOnClickListener {
                        txtPin = mAlertDialog.edtPinVerificar.text.toString()
                        toReturn.forEach {

                            if(txtPin == it.pin.toString() && it.nameMesa == selectedItem.nameMesa){
                                mAlertDialog.dismiss()
                                val intt = Intent(this@MesaActivity, ContaActivity::class.java)
                                val mesaData = MesaData(selectedItem.nameMesa,selectedItem.proprietarioMesa.toString())
                                intt.putExtra("mesa",mesaData)
                                startActivity(intt)
                            }
                        }
                        mAlertDialog.edtPinVerificar.setText("")
                        mAlertDialog.edtPinVerificar.hint = "Pin errado. Tente novamente"
                        mAlertDialog.edtPinVerificar.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.red))

                    }
                    mAlertDialog.okCancelaPin.setOnClickListener {
                        mAlertDialog.dismiss()
                        Toast.makeText(this@MesaActivity,"Operação Cancelada",Toast.LENGTH_SHORT).show()
                    }
                    note_list_progress.visibility = View.GONE

                }
                override fun onCancelled(databaseError: DatabaseError) {
                    //log error
                }
            }
            mDatabaseReference?.child("Mesas")?.addValueEventListener(postListener)


        }
        // Fecha o autocomplite
        ACTVMesas.setOnDismissListener {
            //Toast.makeText(applicationContext, "Sugestões fechadas", Toast.LENGTH_SHORT).show()
        }
        // Listener do layout
        rootLL.setOnClickListener {
            val text = ACTVMesas.text
            Toast.makeText(applicationContext, "Entrado : $text", Toast.LENGTH_SHORT).show()
        }
        // Listender para mudança de foco
        ACTVMesas.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (b) {
                // Display the suggestion dropdown on focus
                ACTVMesas.showDropDown()
            }
        }
    }

    private fun cadastrarMesa(){
        if ((etNomeMesa.text.toString().contains(" ")) || (etNomeMesa.text.toString().contains("!")) || (etNomeMesa.text.toString().contains("@"))
            || (etNomeMesa.text.toString().contains("#"))  || (etNomeMesa.text.toString().contains("$"))
            || (etNomeMesa.text.toString().contains("%")) || (etNomeMesa.text.toString().contains("%"))
            || (etNomeMesa.text.toString().contains("&"))  || (etNomeMesa.text.toString().contains("*"))
            || (etNomeMesa.text.toString().contains(".")) || (etNomeMesa.text.toString().contains(",")))
        {
                Toast.makeText(this, "Por favor, não utilize símbolos nem espaçoes para o nome da mesa", Toast.LENGTH_SHORT).show()
        } else{
            if (etNomeMesa.text.toString().isNotEmpty()){
                gravarMesa()
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(btnCriarMesa.windowToken, 0)
            }else{
                Toast.makeText(this, "Por favor, escolha um nome para mesa!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //pegar usuário e email
    private fun pegaUser(): User{
        //pegar o usuário
        val userEmail = mAuth?.currentUser?.email
        var user: String? = null

        if (userEmail != null) {
            if (userEmail.contains("@")) {
                user =
                    userEmail.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            } else {
                user = userEmail
            }
        }
        return User(user,userEmail)
    }

    //insere dados no firebase
    private fun gravarMesa() {
        //pegar PIN mesa
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.pin_dialog,rootView,false )
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Escolha um PIN para Mesa: ${etNomeMesa.text}")
        val  mAlertDialog = mBuilder.show()
        mAlertDialog.okBtnPin.setOnClickListener {
            val pinTxt = mAlertDialog.edtPin.text.toString()
            val pinTxtConf = mAlertDialog.edtPinConf.text.toString()
            if(pinTxt!= pinTxtConf){
                    Toast.makeText(this,"Pins não correspondem",Toast.LENGTH_SHORT).show()
                }else{
                val user = pegaUser()
                val timestamp = System.currentTimeMillis().toString()
                //inserir mesa
                mDatabaseReference?.child("Mesas")
                    ?.child(timestamp)
                    ?.setValue(Mesa("${etNomeMesa.text}",user.name,timestamp,pinTxt.toInt()))
                //limpar o campo
                etNomeMesa.setText("")
                    mAlertDialog.dismiss()
            }

        }

    }
    //mostrar mesas no RV
    private fun criarMesa(){
        //array de mesas para o adapter do RV
        note_list_progress.visibility = View.VISIBLE
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val toReturn: ArrayList<Mesa> = ArrayList()
                for(data in dataSnapshot.children){
                    val mesaData = data.getValue<Mesa>(Mesa::class.java)
                    //unwrap
                    val mesa = mesaData?.let { it } ?: continue
                    toReturn.add(mesa)
                }
                //sort so newest at bottom
                toReturn.sortBy { mesa ->
                    mesa.timestamp
                }
                note_list_progress.visibility = View.GONE
                setupMesaAdapter(toReturn)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        mDatabaseReference?.child("Mesas")?.addValueEventListener(postListener)
    }

    private fun setupMesaAdapter(data: ArrayList<Mesa>){
        mesaRecyclerView.clearOnChildAttachStateChangeListeners()
        val linearLayoutManager = LinearLayoutManager(this)
        mesaRecyclerView.layoutManager = linearLayoutManager
        mesaRecyclerView.adapter = MesaAdapter(data, this::act)
        //scroll to bottom
        mesaRecyclerView.scrollToPosition(data.size - 1)
    }

    private fun act (data : Mesa) : Unit {
        var txtPin: String? = null
        note_list_progress.visibility = View.VISIBLE
        //Toast.makeText(this, "${data.nameMesa} clicked", Toast.LENGTH_SHORT).show()
        //ao clicar ira para a tela da comanda da mesa - antes pedirá o pin
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val toReturn: ArrayList<Mesa> = ArrayList()
                for(data in dataSnapshot.children){
                    val mesaData = data.getValue(Mesa::class.java)
                    //unwrap
                    val mesa = mesaData?.let { it } ?: continue
                    toReturn.add(mesa)
                }
                //sort so newest at bottom
                toReturn.sortBy { mesa ->
                    mesa.timestamp
                }
                val mDialogView = LayoutInflater.from(this@MesaActivity).inflate(R.layout.pin_dialog_verifica,rootView,false )
                val mBuilder = AlertDialog.Builder(this@MesaActivity)
                    .setView(mDialogView)
                    .setTitle("Digite o PIN para a Mesa:\n \u27A2 ${data.nameMesa}")
                val  mAlertDialog = mBuilder.show()
                mAlertDialog.okBtnPin.setOnClickListener {
                    txtPin = mAlertDialog.edtPinVerificar.text.toString()
                    toReturn.forEach {

                        if(txtPin == it.pin.toString() && it.nameMesa == data.nameMesa){
                            mAlertDialog.dismiss()
                            val intt = Intent(this@MesaActivity, ContaActivity::class.java)
                            val mesaData = MesaData(data.nameMesa,data.proprietarioMesa.toString())
                            intt.putExtra("mesa",mesaData)
                            startActivity(intt)
                        }
                    }
                    mAlertDialog.edtPinVerificar.setText("")
                    mAlertDialog.edtPinVerificar.hint = "Pin errado. Tente novamente"
                    mAlertDialog.edtPinVerificar.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.red))
                }
                mAlertDialog.okCancelaPin.setOnClickListener {
                    mAlertDialog.dismiss()
                    Toast.makeText(this@MesaActivity,"Operação Cancelada",Toast.LENGTH_SHORT).show()
                }
                note_list_progress.visibility = View.GONE

            }
            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        mDatabaseReference?.child("Mesas")?.addListenerForSingleValueEvent(postListener)


    }

}