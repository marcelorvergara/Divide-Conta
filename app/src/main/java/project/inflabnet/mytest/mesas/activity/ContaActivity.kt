package project.inflabnet.mytest.mesas.activity

import android.content.Intent
import project.inflabnet.mytest.R
import project.inflabnet.mytest.database.database.AppDatabase
import project.inflabnet.mytest.database.database.AppDatabaseService
import project.inflabnet.mytest.database.model.MesaOrc
import project.inflabnet.mytest.database.model.Orcamento
import project.inflabnet.mytest.login.LoginActivity
import project.inflabnet.mytest.maps.model.UserLocation
import project.inflabnet.mytest.mesas.adapter.ContaAdapter
import project.inflabnet.mytest.mesas.model.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_conta_chat.*
import kotlinx.android.synthetic.main.alert_compartilha_item.view.*
import kotlinx.android.synthetic.main.escolhe_membro_mesa.view.*
import java.util.*
import java.util.function.Predicate
import kotlin.collections.ArrayList

class ContaActivity : AppCompatActivity() {

    private lateinit var appDatabase : AppDatabase

    //Firebase references
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var user: String? = null
    lateinit var pathStr: String

    //já pega a lista de membros para não termos problemas de delay com resposta assíncrona
    val toMembros: MutableList<MembroLocal> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conta_chat)

        //campo dinheiro
        val locale: Locale = Locale.getDefault()
        edtValor.setLocale(locale)

        //Recebendo os Valores da activity MesaActivity
        val mesaData = intent.getSerializableExtra("mesa") as MesaData
        txtMesaConta.text = mesaData.nameMesa
        txtProprit.text = mesaData.proprietarioMesa
        pathStr = mesaData.nameMesa+"conta"
        //Toast.makeText(this,pathStr.toString(),Toast.LENGTH_SHORT).show()

        //instanciando o banco
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference

        //iniciando o banco local
        appDatabase = AppDatabaseService.getInstance(this)

        //chama o chat
        btnChatinho.setOnClickListener {
            val intt = Intent(this, MesaChatActivity::class.java)
            val mesaData = MesaData(mesaData.nameMesa,mesaData.proprietarioMesa)
            intt.putExtra("mesa",mesaData)
            startActivity(intt)
        }

        pegarUser()
        createFirebaseListener(pathStr)
        contaListener(pathStr)
        btnOk.setOnClickListener { setupSendButton(pathStr) }
        btnFinalizar.setOnClickListener { finalizar() }
        jaFinalizados()
        btnCompartilhar.setOnClickListener { enviaPerguntaAlert() }
        itemADividirListener()
        //verificar se há solicitações de compartilhamento não atendidas. Caso positivo, desabilita o botão para não compartilhar mais nada
        verificaSolicitComart()
        //já cria a lista de participantes/membros da mesa para depois ser usado na localização
        setUpMembrosAlert(mesaData.nameMesa)
        btnParticipantes.setOnClickListener {
            setUpRVMemrosLocal(toMembros)
        }
    }


    //Criar lista com os membros da mesa
    private fun setUpMembrosAlert(mesa: String) {

        val membroListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //varre a lista membros do FBase procurando o nome da mesa
                //se encontrar adiciona o membro no RV
                dataSnapshot.children.forEach{
                    if(it.getValue(MembrosMesa::class.java)?.nomeMesa.toString() == mesa ){
                        toMembros.add(MembroLocal(it.getValue(MembrosMesa::class.java)?.membro.toString().replace(".",""),"Sem Local"))
                    }
                }
                //não monta automaticamente. O RV será montado no btnParticipantes, pois iremos fazer o batimento com quem tem localização
                //para apresentar o botão somente para quem tem localização
                //setUpRVMemrosLocal(toMembros)
            }
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext, "Errroooo",Toast.LENGTH_SHORT ).show()
            }
        }
        mDatabaseReference!!.child("Membros").addValueEventListener(membroListener)
    }

    //criar o Alert Dialog com RV
    private fun setUpRVMemrosLocal(toMembrosLoc: MutableList<MembroLocal>) {

        //extraindo somente os nomes dos membros da mesa. Depois acerto isso na geração da tomembrosLoc
        val listNames: MutableList<String> = mutableListOf()
        toMembrosLoc.forEach {
            it.user?.let { it1 -> listNames.add(it1.replace(".","")) }
            //Log.i("Mesa 1", it.user)
        }

        val toMembrosLoc1: MutableList<MembroLocal> = toMembrosLoc

        //val pessoaLoc : String = it.user!!.replace(".","")
        var newtoMembrosLoc: MutableList<MembroLocal> = mutableListOf()
        var locationStr : String? = null
        val localListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //varre a lista membros do FBase procurando o nome e localização
                //se encontrar adiciona o membro no txtMembros

                dataSnapshot.children.forEach {
                    val userName = it.getValue(UserLocation::class.java)?.user?.replace(".","")
                    val locationCord = it.getValue(UserLocation::class.java)?.location
                    val theFirstBatman = listNames.find { actor -> userName.equals(actor) }
                    if(!theFirstBatman.isNullOrBlank()){
                        //Log.i("Mesa 2", theFirstBatman)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            toMembrosLoc1.removeIf { it.user == userName }
                        }
                        toMembrosLoc1.add(MembroLocal(userName,locationCord))
                    }

                }
                toMembrosLoc1.sortBy { MembroLocal ->
                    MembroLocal.user
                }
                val mDialogView = LayoutInflater.from(this@ContaActivity).inflate(R.layout.escolhe_membro_mesa, null)
                val mBuilder = AlertDialog.Builder(this@ContaActivity)
                    .setView(mDialogView)
                    .setTitle("Participantes da Mesa:\n \u27A2 ${txtMesaConta.text}")
                val  mAlertDialog = mBuilder.show()
                val linearLayoutManager = LinearLayoutManager(applicationContext)
                mDialogView.rvMembros.layoutManager = linearLayoutManager
                mDialogView.rvMembros.scrollToPosition(toMembrosLoc1.size)
                mDialogView.rvMembros.adapter = MembrosAdapter(toMembrosLoc1)
                {
                    val dialogBuilder = AlertDialog.Builder(mDialogView.context)
                    dialogBuilder.setMessage("Gostaria de localizar ${it.user}?")
                        .setCancelable(false)
                        .setPositiveButton("Sim"){_, _ ->
                            val localOnde = it.location
                            if(localOnde != null){
                                val addressUri = Uri.parse("geo:0,0?q=$localOnde")
                                val intent = Intent(Intent.ACTION_VIEW, addressUri)
                                // Find an activity to handle the intent, and start that activity.
                                if (intent.resolveActivity(packageManager) != null) {
                                    startActivity(intent)
                                } else {
                                    Log.d("ImplicitIntents", "Não é possível abrir mapa!")
                                }
                            }else{
                                Toast.makeText(applicationContext,"sem localização disponível", Toast.LENGTH_SHORT).show()
                            }
                        }

                        .setNegativeButton("Não") { _, _ ->

                        }
                        .setNeutralButton("Cancelar") {_, _ ->

                        }
                    val alert = dialogBuilder.create()
                    alert.setTitle("Localizar Membro")
                    alert.show()
                }
                mDialogView.btnVoltar.setOnClickListener {
                    mAlertDialog.dismiss()
                }


            }
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(applicationContext, "Errroooo",Toast.LENGTH_SHORT ).show()
                }
            }
            mDatabaseReference!!.child("UserLocations").addValueEventListener(localListener)

    }


//    private fun setupMembros(toMembros: MutableList<MembroLocal>) {
//        val linearLayoutManager = LinearLayoutManager(applicationContext)
//        rvMembros.layoutManager = linearLayoutManager
//        rvMembros.scrollToPosition(toMembros.size)
//        rvMembros.adapter = MembrosAdapter(toMembros){
//            val dialogBuilder = AlertDialog.Builder(this)
//            dialogBuilder.setMessage("Gostaria de localizar ${it}?")
//                .setCancelable(false)
//                .setPositiveButton("Sim"){_, _ ->
//                    val pessoa : String = it.pessoa!!.replace(".","")
//                    var locationStr : String? = null
//                    val membroListener = object : ValueEventListener {
//                        override fun onDataChange(dataSnapshot: DataSnapshot) {
//                            //varre a lista membros do FBase procurando o nome da mesa
//                            //se encontrar adiciona o membro no txtMembros
//                            dataSnapshot.children.forEach{
//                                if(it.getValue(UserLocation::class.java)?.user == pessoa )
//                                    locationStr = it.getValue(UserLocation::class.java)?.location.toString()
//                            }
//                            if(locationStr != null){
//                                val addressUri = Uri.parse("geo:0,0?q=$locationStr")
//                                val intent = Intent(Intent.ACTION_VIEW, addressUri)
//                                // Find an activity to handle the intent, and start that activity.
//                                if (intent.resolveActivity(packageManager) != null) {
//                                    startActivity(intent)
//                                } else {
//                                    Log.d("ImplicitIntents", "Não é possível abrir mapa!")
//                                }
//                            }else{
//                                Toast.makeText(applicationContext,"${pessoa} sem localização disponível", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                        override fun onCancelled(p0: DatabaseError) {
//                            Toast.makeText(applicationContext, "Errroooo",Toast.LENGTH_SHORT ).show()
//                        }
//                    }
//                    mDatabaseReference!!.child("UserLocations").addValueEventListener(membroListener)
//
//                }
//                .setNegativeButton("Não") { _, _ ->
//
//                }
//                .setNeutralButton("Cancelar") {_, _ ->
//
//                }
//            val alert = dialogBuilder.create()
//            alert.setTitle("Localizar Membro")
//            alert.show()
//        }
//
//    }

    //objetivo desabilitar botão de compartilhar caso já exista pedido pendente de autorização
    private fun verificaSolicitComart() {
        val blocListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext, "Errroooo na conexão com o banco",Toast.LENGTH_SHORT ).show()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach{
                    if ((it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.status.toString() == "Pergunta")
                            &&
                            it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.userOrigem.toString() == user
                            &&
                            it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.userDestino.toString() != user){

                            val dest = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.userDestino.toString()
                            val item = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.itemAdividir.toString()
                            val valor = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.itemValor.toString()
                            btnCompartilhar.visibility = View.GONE
                            val textParaPendencia = "${item}\n aguardando autorização de\n${dest} - valor: ${valor}"
                            txtPendencia.text = textParaPendencia
                    }else{
                        btnCompartilhar.visibility = View.VISIBLE
                        txtPendencia.text = ""
                    }
                }
            }
        }
        mDatabaseReference!!.child("itemADividir").addValueEventListener(blocListener)
    }

    //pergunta se quer compartilhar um item
    private fun enviaPerguntaAlert () {
        //apresentar o layout de pergunta
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_compartilha_item, null)
        //verifica ITENS que estão na comanda para montar o Radiogroup
        val itensDaContaListener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val contaItens: ArrayList<Conta> = ArrayList();
                for(data in dataSnapshot.children){
                    val contaData = data.getValue<Conta>(Conta::class.java)
                    //abrindo
                    val conta = contaData?.let { it } ?: continue
                    if(conta.quem == user && conta.mesa == txtMesaConta.text.toString()){
                        contaItens.add(conta)
                    }
                }
                //ordenando o mais novo no final
                contaItens.sortBy { conta ->
                    conta.timestamp
                }

                //llGroup.addView(rg)
                montaRG(contaItens,mDialogView)
                //setupAdapter(toReturn)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ContaActivity,"Erro ao conectar com o banco", Toast.LENGTH_SHORT).show()
            }
        }
        mDatabaseReference?.child("Conta")?.child(pathStr)?.addListenerForSingleValueEvent(itensDaContaListener)

        //verifica QUEM está na comanda para montar o Radiogroup
        val membrosDaContaListener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val membrosMesas: ArrayList<MembrosMesa> = ArrayList();
                for(data in dataSnapshot.children){
                    val membroData = data.getValue<MembrosMesa>(MembrosMesa::class.java)
                    //abrindo
                    val membro = membroData?.let { it } ?: continue
                    if(membro.membro != user && membro.nomeMesa == txtMesaConta.text.toString()){
                        membrosMesas.add(membro)
                    }
                }
                //ordenando o mais novo no final
                membrosMesas.sortBy { membro ->
                    membro.id
                }
                //llGroup.addView(rg)
                montaMembrosRG(membrosMesas,mDialogView)
                //setupAdapter(toReturn)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ContaActivity,"Erro ao conectar com o banco", Toast.LENGTH_SHORT).show()
            }
        }
        mDatabaseReference?.child("Membros")?.addListenerForSingleValueEvent(membrosDaContaListener)

        //builder do alertdialog com itens da comanda e membros para dividir (Pergunta)
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Compartilhar Custo de Item")
        //verificar leak memory
        val  mAlertDialog = mBuilder.show()
        mDialogView.btnPerguntar.setOnClickListener {
            //mAlertDialog.dismiss()
            //pegar os itens do user que ele quer compartilhar
            //começa a pegar os valores para inserir no FBase

            val itemSelected: RadioButton  = mDialogView.findViewById(mDialogView.rg.checkedRadioButtonId)
            val membroSelected: RadioButton = mDialogView.findViewById(mDialogView.rdGroup.checkedRadioButtonId)
            val userOrigem: String? = user
            val userDestino  = membroSelected.text.toString()
            val itemAdividir = itemSelected.text.toString()
            val mesaIAD = txtMesaConta.text.toString()
            //status: Pergunta, Aceito, NaoAceito
            val statusItem:String = "Pergunta"
            //gerar a key
            val IADTimestamp = System.currentTimeMillis().toString()
            val itemADV = itemAdividir.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val valorADV = itemAdividir.split("no valor de ".toRegex()).dropWhile { it.isEmpty() }.toTypedArray()[1]
            //Toast.makeText(this,"${itemADV} e ${valorADV}" ,Toast.LENGTH_SHORT).show()
            val itemADObj = userOrigem?.let { ItemDividirAlert(it,userDestino,itemADV,valorADV,mesaIAD,statusItem,IADTimestamp) }
            //colocar no FBase o item a dividir
            //referencia do caminho
            val dbRefe = mDatabaseReference!!.child("itemADividir")
            //montar o objeto
            dbRefe.child(IADTimestamp).setValue(itemADObj)
            mAlertDialog.dismiss()
        }
        mDialogView.btnCancelar.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }
    }
    //monta o RadioGroup dos itens da conta
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun montaRG(contaItens: ArrayList<Conta>, mDialogView: View?) {
        //val rg = RadioGroup(this)
        if (mDialogView != null) {
            mDialogView.rg.orientation = RadioGroup.VERTICAL
        }
        for(i in 0 until contaItens.size){
            val rb = RadioButton(applicationContext)
            val produto: String = "${contaItens[i].oque.toString()}, no valor de ${contaItens[i].quanto.toString()}"
            rb.text = produto
            rb.id = View.generateViewId()
            if (mDialogView != null) {
                mDialogView.rg.addView(rb)
            }
        }
    }
    //monta o RadioGroup dos membros da mesa
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun montaMembrosRG(membroNomes: ArrayList<MembrosMesa>, mDialogView: View?) {
        //val rg = RadioGroup(this)
        if (mDialogView != null) {
            mDialogView.rdGroup.orientation = RadioGroup.VERTICAL
        }
        for(i in 0 until membroNomes.size){
            val rb = RadioButton(applicationContext)
            rb.text = membroNomes[i].membro.toString()
            rb.id = View.generateViewId()
            mDialogView?.rdGroup?.addView(rb)
        }
    }

    //ficar ouvindo a classe Item A Dividir
    private fun itemADividirListener(){
        val iadListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext, "Errroooo na conexão com o banco",Toast.LENGTH_SHORT ).show()
            }
            override fun onDataChange(p0: DataSnapshot) {
                checkItensACompartilhar()
                checkItensACompartilharNegados()
            }
        }
        mDatabaseReference!!.child("itemADividir").addValueEventListener(iadListener)
    }

    //checa em ItemDividirNegado os itens negados
    private fun checkItensACompartilharNegados() {
        val dialogBuilder = AlertDialog.Builder(this@ContaActivity)
        val negadosListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext,"Erro DB",Toast.LENGTH_SHORT).show ()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach{
                    if ((it.getValue<ItemDividirNegado>(ItemDividirNegado::class.java)?.status.toString() == "Negado")
                            &&
                            it.getValue<ItemDividirNegado>(ItemDividirNegado::class.java)?.userOrigem.toString() == user
                            &&
                            it.getValue<ItemDividirNegado>(ItemDividirNegado::class.java)?.userDestino.toString() != user){

                        val id = it.getValue<ItemDividirNegado>(ItemDividirNegado::class.java)?.id.toString()
                        val solicitanteCompart = it.getValue<ItemDividirNegado>(ItemDividirNegado::class.java)?.userDestino.toString()
                        val itemACompartilhar = it.getValue<ItemDividirNegado>(ItemDividirNegado::class.java)?.itemAdividir.toString()
                        val valorIACompartilhar = it.getValue<ItemDividirNegado>(ItemDividirNegado::class.java)?.itemValor.toString()
                        dialogBuilder.setMessage("${solicitanteCompart} recusou compartilhar  ${itemACompartilhar} no valor de ${valorIACompartilhar}. Se vira aí!")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { _, _ ->
                                    //remover item dos itens a compartilhar
                                    mDatabaseReference?.child("itemADividir")?.child(id)?.removeValue()
                                    mDatabaseReference?.child("ItemDividirNegado")?.child(id)?.removeValue()
                                    verificaSolicitComart()
                                }
                        val alert = dialogBuilder.create()
                        alert.setTitle("Compartilhamento Negado!")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !isDestroyed) {
                            alert.show()
                        }
                    }
                }
            }
        }
        mDatabaseReference!!.child("ItemDividirNegado").addListenerForSingleValueEvent(negadosListener)
    }
    //chamada a partir de itemADividirListener() , verifica no banco se há requisição ("Pergunta") para dividir item
    private fun checkItensACompartilhar(){
        val alertListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //varre a lista membros do FBase procurando o nome da mesa
                //se encontrar remover o membro no Fbase
                dataSnapshot.children.forEach{
                    if(it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.mesaIAD == txtMesaConta.text.toString())  {
                        //se é pergunta
                        if ((it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.status.toString() == "Pergunta") &&
                                it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.userDestino.toString() == user) {
                            val id = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.id.toString()
                            val dialogBuilder = AlertDialog.Builder(this@ContaActivity)
                            val solicitanteCompart = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.userOrigem.toString()
                            val destinoCompart = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.userDestino.toString()
                            val itemACompartilhar = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.itemAdividir.toString()
                            val valorIACompartilhar = it.getValue<ItemDividirAlert>(ItemDividirAlert::class.java)?.itemValor.toString()
                            dialogBuilder.setMessage("${solicitanteCompart} gostaria de compartilhar o valor de ${valorIACompartilhar} referente ao item ${itemACompartilhar}. Aceita compartilhar esse item?")
                                    .setCancelable(false)
                                    .setPositiveButton("Sim") { _, _ ->
                                        //aceitou compartilhar, vai varrer as contas para pegar os itens
                                        val itemListener = object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                var contaADVSol: Conta? = null
                                                var contaADVAceitou: Conta? = null
                                                var TS1: String? = null
                                                var TS2: String? = null
                                                //val toReturn: ArrayList<Conta> = ArrayList();
                                                for (data in dataSnapshot.children) {
                                                    val contaData = data.getValue<Conta>(Conta::class.java)
                                                    val ts = data.getValue<Conta>(Conta::class.java)?.timestamp
                                                    //abrindo a bagaça
                                                    val conta = contaData?.let { it } ?: continue
                                                    //encontrando o item
                                                    if ((conta.quem == solicitanteCompart)
                                                            && (conta.quanto.toString() == valorIACompartilhar)
                                                            && (conta.oque == itemACompartilhar)
                                                            && (conta.mesa == txtMesaConta.text.toString())) {
                                                        //dividir o valor e criar "a metade" do item para cada um dos dois que dividiram o item
                                                        val itemADVDouble = conta.quanto?.toDouble()
                                                        val divValor: Double? = itemADVDouble?.div(2)
                                                        TS1 = System.currentTimeMillis().toString()
                                                        contaADVSol = Conta(solicitanteCompart,itemACompartilhar, divValor?.toInt(),TS1,txtMesaConta.text.toString())
                                                        TS2 =(System.currentTimeMillis()+1).toString()
                                                        contaADVAceitou = Conta(destinoCompart,itemACompartilhar, divValor?.toInt(),TS2,txtMesaConta.text.toString())
                                                        Toast.makeText(applicationContext, divValor.toString(), Toast.LENGTH_SHORT).show()
                                                        if (ts != null) {
                                                            mDatabaseReference?.child("Conta")?.child(pathStr)?.child(ts)?.removeValue()
                                                        }
                                                    }
                                                }
                                                //inserindo a conta dividida como duas novas contas no FBase
                                                if (TS1 != null) {
                                                    mDatabaseReference?.child("Conta")?.child(pathStr)?.child(TS1)?.setValue(contaADVSol)
                                                }
                                                if (TS2 != null) {
                                                    mDatabaseReference?.child("Conta")?.child(pathStr)?.child(TS2)?.setValue(contaADVAceitou)
                                                }
                                                //remover o item dos itens a dividir
                                                mDatabaseReference!!.child("itemADividir").child(id).removeValue()
                                            }
                                            override fun onCancelled(databaseError: DatabaseError) {
                                                Toast.makeText(applicationContext,"Erro de conexão com o DB", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        mDatabaseReference?.child("Conta")?.child(pathStr)?.addListenerForSingleValueEvent(itemListener)
                                    }
                                    .setNegativeButton("Não") { _, _ ->
                                        val itemNegado = ItemDividirNegado(solicitanteCompart,destinoCompart,itemACompartilhar,valorIACompartilhar,txtMesaConta.text.toString(),"Negado",id)
                                        mDatabaseReference?.child("itemADividir")?.child(id)?.child("status")?.setValue("Negado")
                                        mDatabaseReference?.child("ItemDividirNegado")?.child(id)?.setValue(itemNegado)
                                        Toast.makeText(this@ContaActivity, "Ok, não será dividido!", Toast.LENGTH_SHORT).show()
                                        //verificar se é melhor colocar fora do alerdialog - depois do ondatachange checar se há mais itens
                                        checkItensACompartilhar()
                                    }
                                    .setNeutralButton("Cancelar") { _, _ ->
                                        Toast.makeText(this@ContaActivity, "Operação cancelada", Toast.LENGTH_SHORT).show()
                                    }
                            val alert = dialogBuilder.create()
                            alert.setTitle("Compartilhar Item?")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !isDestroyed()){
                                alert.show()
                            }

                        }
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext, "Errroooo",Toast.LENGTH_SHORT ).show()
            }
        }
        mDatabaseReference!!.child("itemADividir").addListenerForSingleValueEvent(alertListener)
    }
    //não precisa falar. Ok pega o usuário que está logado
    private fun pegarUser(){
        //pegar o usuário
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
    }

    //finalizar a conta individual
    private fun finalizar() {

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Tem certeza que gostaria de pagar sua parte?")
                .setCancelable(false)
                .setPositiveButton("Sim"){_, _ ->
                    //segue a deleção dos itens de quem fechou a conta e a soma do total dos seus itens

                    val postListener = object : ValueEventListener {
                        var totalConta: Int = 0
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val toReturn: ArrayList<Conta> = ArrayList();
                            for(data in dataSnapshot.children){
                                val contaData = data.getValue<Conta>(Conta::class.java)
                                val ts = data.getValue<Conta>(Conta::class.java)?.timestamp
                                //abrindo
                                val conta = contaData?.let { it } ?: continue
                                //montando o array removendo os itens de quem está finalizando a comanda
                                if(conta.quem == user) {
                                    conta.let {
                                        if (ts != null) {
                                            mDatabaseReference?.child("Conta")?.child(pathStr)?.child(ts)?.removeValue()
                                            totalConta += (conta.quanto!!)
                                        }
                                    }

                                }else{
                                    toReturn.add(conta)
                                }
                            }
                            //inserir no banco local o valor que fechou
                            val getIdOrcamento = GetIdOrcamento().execute(currentMonth2()).get()
                            GuardarValorMesa().execute(MesaOrc(getIdOrcamento,txtMesaConta.text.toString(),totalConta,currentMonth2()))
                            //coloca user que saiu no txt dos que já correram
                            //txtFinalizado.append("${user} : ${totalConta}\n").toString()
                            //não deixa colocar mais produtos
                            btnOk.visibility = View.GONE
                            btnFinalizar.visibility = View.GONE
                            btnCompartilhar.visibility = View.GONE
                            //Toast.makeText(this@ContaActivity," ${totalConta}  a pagar",Toast.LENGTH_SHORT).show()
                            //criar um grupo no Fbase aPagar de quem já fechou e seus valores
                            user?.let { aPagar(it,totalConta) }
                            //ordenando o mais novo no final
                            toReturn.sortBy { conta ->
                                conta.timestamp
                            }
                            setupAdapter(toReturn)
                            //jaFinalizados() //atializa o txt
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            //log error
                        }
                    }

                    //mDatabaseReference?.child(pathStr)?.addValueEventListener(postListener)
                    mDatabaseReference?.child("Conta")?.child(pathStr)?.addListenerForSingleValueEvent(postListener)


                    //remover user do grupo membros
                    val membroListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            //varre a lista membros do FBase procurando o nome da mesa
                            //se encontrar remover o membro no Fbase
                            dataSnapshot.children.forEach{
                                if(it.getValue<MembrosMesa>(MembrosMesa::class.java)?.nomeMesa.toString() == txtMesaConta.text.toString() &&
                                        it.getValue<MembrosMesa>(MembrosMesa::class.java)?.membro.toString() == user) {
                                    val id = it.getValue<MembrosMesa>(MembrosMesa::class.java)?.id.toString()
                                    mDatabaseReference?.child("Membros")?.child(id)?.removeValue()
                                    //txtMembros.append("${it.getValue<MembrosMesa>(MembrosMesa::class.java)?.membro.toString()}\n")
                                }
                            }
                        }
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(applicationContext, "Errroooo",Toast.LENGTH_SHORT ).show()
                        }
                    }
                    mDatabaseReference!!.child("Membros").addListenerForSingleValueEvent(membroListener)

                }
                .setNegativeButton("Não") { _, _ ->
                    Toast.makeText(this,"Ok, a noite é uma criança!",Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton("Cancelar") {_, _ ->
                    Toast.makeText(this,"Operação cancelada",Toast.LENGTH_SHORT).show()
                }
        val alert = dialogBuilder.create()
        alert.setTitle("Fechar conta?")
        alert.show()
    }
    //listener para quem já finalizou

    //cria um child no Fbase "aPagar" para guardar quem já finalizou a comanda
    //função chamda depois de clicar em finalizar
    private fun aPagar(userP: String, totConta: Int) {
        val mesaP = txtMesaConta.text.toString()
        //atiualizar firebase com os que fecharam a conta
        //referencia do caminho
        val dbRefe = mDatabaseReference!!.child("aPagar")
        //gerar a key
        val apTimestamp = System.currentTimeMillis().toString()
        //montar o objeto
        val aPagarObj =fechouConta(userP,mesaP,totConta,apTimestamp)
        dbRefe.child(apTimestamp).setValue(aPagarObj)
    }

    //popular o RV com quem já saiu do grupo
    private fun jaFinalizados() {

        val japagouListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val toSairam: MutableList<String> = mutableListOf()
                //varre a lista membros do FBase procurando o nome da mesa
                //se encontrar no grupo dos aPagar adicionar recycleview rvSairam
                dataSnapshot.children.forEach{
                    if(it.getValue<fechouConta>(fechouConta::class.java)?.mesa.toString() == txtMesaConta.text.toString()) {
                        //txtFinalizado.append(" ${it.getValue<fechouConta>(fechouConta::class.java)?.user.toString()} : ${it.getValue<fechouConta>(fechouConta::class.java)?.totConta.toString()}\n").toString()
                        toSairam.add("${it.getValue<fechouConta>(fechouConta::class.java)?.user.toString()} R$ ${it.getValue<fechouConta>(fechouConta::class.java)?.totConta.toString()}")
                    }
                    setupSairam(toSairam)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext, "Errroooo",Toast.LENGTH_SHORT ).show()
            }
        }
        mDatabaseReference!!.child("aPagar").addValueEventListener(japagouListener)
    }

    private fun setupSairam(toSairam: MutableList<String>) {
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        rvSairam.layoutManager = linearLayoutManager
        rvSairam.scrollToPosition(toSairam.size)
        rvSairam.adapter = SairamAdapter(toSairam){
        }
    }

    //atualiza os membros da mesa em recycleview rvMembros
    private fun membrosLista(mesa: String) {


    }

    //ao clicar para enviar um item para comanda e entrar no grupo da mesa, caso já não esteja
    private fun setupSendButton(pathStr: String) {
        var membroExiste: String? = null
        if(checkFields()) {
            //validar se usuário já está na mesa
            val toMembrosMesa: MutableList<MembrosMesa> = mutableListOf()
            val membroListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    dataSnapshot.children.forEach{
                        if(it.getValue(MembrosMesa::class.java)?.nomeMesa.toString() == txtMesaConta.text.toString() && it.getValue(MembrosMesa::class.java)?.membro == user )
                            toMembrosMesa.add(it.getValue(MembrosMesa::class.java)!!)
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(applicationContext, "Errroooo",Toast.LENGTH_SHORT ).show()
                }
            }
            mDatabaseReference!!.child("Membros").addValueEventListener(membroListener)

            //atiualizar firebase com nome da mesa e novo membro
            val key = "${user?.replace(".","")}${txtMesaConta.text}"
            val mesa = MembrosMesa(txtMesaConta.text.toString(),user!!,key)
            mDatabaseReference!!.child("Membros").child(key).setValue(mesa)

            //enviar dados do item consumido para banco
            sendData(pathStr, edtItem.text.toString(), edtValor.text.toString())
        }
    }

    //checar os campos ao entrar um item consumido
    private fun checkFields(): Boolean{
        if (edtItem.text.toString().isEmpty() && edtValor.text.toString().isEmpty()){
            Toast.makeText(this, "Por favor, inserir um item e seu valor.", Toast.LENGTH_SHORT).show()
            return false
        }else if (edtItem.text.toString().isEmpty()){
            Toast.makeText(this, "Por favor, inserir um item.", Toast.LENGTH_SHORT).show()
            return false
        } else if (edtValor.text.toString().isEmpty()){
            Toast.makeText(this, "Por favor, colocar o valor do item", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    //envia dados para firebase
    private fun sendData(pathStr: String, item:String, valor: String){
        val itemTimestamp = System.currentTimeMillis().toString()
        val valorItem = valor.replace("R$ ","").replace(".","")
        val conta = Conta(user, item,valorItem.toInt(), itemTimestamp,txtMesaConta.text.toString())
        mDatabaseReference?.child("Conta")?.child(pathStr)?.
                child(itemTimestamp)?.
                setValue(conta)
        //limpar a entrada de dados
        edtItem.setText("")
        edtValor.setText("")
    }
    //listener para itens da comanda
    private fun createFirebaseListener(pathStr : String){
        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val toReturn: ArrayList<Conta> = ArrayList();
                for(data in dataSnapshot.children){
                    val contaData = data.getValue<Conta>(Conta::class.java)
                    //abrindo
                    val conta = contaData?.let { it } ?: continue
                    toReturn.add(conta)
                }
                //ordenando o mais novo no final
                toReturn.sortBy { conta ->
                    conta.timestamp
                }
                setupAdapter(toReturn)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        mDatabaseReference?.child("Conta")?.child(pathStr)?.addValueEventListener(postListener)

    }
    //listener para valor da conta pessoal
    private fun contaListener(pathStr: String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val toReturn: ArrayList<Conta> = ArrayList();
                for(data in dataSnapshot.children){
                    val contaData = data.getValue<Conta>(Conta::class.java)
                    //abrindo
                    val conta = contaData?.let { it } ?: continue
                    //montando o array
                    toReturn.add(conta)
                }
                //ordenando o mais novo no final
                toReturn.sortBy { conta ->
                    conta.timestamp
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        mDatabaseReference?.child("Conta")?.child(pathStr)?.addValueEventListener(postListener)
    }
    //colocar os Txts de valores da conta
    private fun setupTxtView(data: ArrayList<Conta>){
        val orcStr: String?
        orcStr = try{
            GetOrcamento().execute(currentMonth2()).get().valor.toString()
        }catch (e: Exception){
            "500000.0"
        }

        val orcamento = orcStr.toDouble()
        var totEu: Int = 0
        val tot = data.sumBy { conta ->
            conta.quanto!!.toInt()
        }
        for (conta in data){
            if(conta.quem == user){
                totEu += conta.quanto!!
            }
        }
        txtTotConta.text = tot.toString()

        if (totEu == 0){
            txtTotEu.visibility = View.GONE
            txtTotEuText.visibility = View.GONE
			textView35.visibility = View.GONE
        }else {
            txtTotEu.visibility = View.VISIBLE
            txtTotEuText.visibility = View.VISIBLE
			textView35.visibility = View.VISIBLE
            txtTotEu.text = totEu.toString()
        }
		val totEu2 = totEu.toDouble()
        val percentage = (totEu2/ orcamento) *100.0
        //Toast.makeText(this,"Percentagem: ${percentage}",Toast.LENGTH_SHORT).show()
        //Toast.makeText(this,"orcamento: ${orcamento}",Toast.LENGTH_SHORT).show()
        if (percentage < 20.0){
            txtTotEu.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
            txtTotEuText.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
            textView35.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
        }else if (percentage < 30.0){
            txtTotEu.setTextColor(ContextCompat.getColor(applicationContext, R.color.yellow))
            txtTotEuText.setTextColor(ContextCompat.getColor(applicationContext, R.color.yellow))
            textView35.setTextColor(ContextCompat.getColor(applicationContext, R.color.yellow))
        }else{
            txtTotEu.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
            txtTotEuText.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
            textView35.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
        }
    }

    //mostrar os dados e deletar no evento toque
    private fun setupAdapter(data: ArrayList<Conta>) {
        val linearLayoutManager = LinearLayoutManager(this)
        rvConta.layoutManager = linearLayoutManager
        //scroll to bottom
        rvConta.scrollToPosition(data.size - 1)
        rvConta.adapter = ContaAdapter(data) {
            val txtTitulo = "${it.oque} de ${it.quem} no valor de ${it.quanto}?"
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Tem certeza que gostaria de deletar $txtTitulo ?")
                    .setCancelable(false)
                    .setPositiveButton("Sim"){_, _ ->
                        //segue a deleção do item
                        removeItem(it)
                        Toast.makeText(this, "${it.oque} removido da comanda", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Não") { _, _ ->
                        Toast.makeText(this,"${it.oque} não foi removido",Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("Cancelar") {_, _ ->
                        Toast.makeText(this,"Operação cancelada",Toast.LENGTH_SHORT).show()
                    }
            val alert = dialogBuilder.create()
            alert.setTitle("Deletar Item da Comanda")
            alert.show()
        }
        setupTxtView(data)
    }

    //remove item na comanda
    private fun removeItem(item: Conta){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val toReturn: ArrayList<Conta> = ArrayList();

                for(data in dataSnapshot.children){
                    val contaData = data.getValue<Conta>(Conta::class.java)
                    //abrindo
                    val conta = contaData?.let { it } ?: continue
                    //montando o array
                    if(item.timestamp != conta.timestamp) {
                        toReturn.add(conta)
                    }else{
                        conta.timestamp?.let { mDatabaseReference?.child("Conta")?.child(pathStr)?.child(it)
                                ?.removeValue() }
                        Toast.makeText(applicationContext," ${conta.oque} removido",Toast.LENGTH_SHORT).show()
                    }
                }
                //ordenando o mais novo no final
                toReturn.sortBy { conta ->
                    conta.timestamp
                }
                setupAdapter(toReturn)

            }
            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        mDatabaseReference?.child("Conta")?.child(pathStr)?.addValueEventListener(postListener)
    }

    inner class GetOrcamento: AsyncTask<String, Unit, Orcamento>() {
        override fun doInBackground(vararg params: String?): Orcamento? {
            val valorMesdb = appDatabase.orcamentoDAO().buscarMes(params[0]!!)
            return valorMesdb
        }
    }

    inner class GuardarValorMesa: AsyncTask<MesaOrc, Unit, Unit>() {
        override fun doInBackground(vararg params: MesaOrc?) {
            appDatabase.mesaDAO().guardar(params[0]!!)
        }
    }

    inner class GetIdOrcamento: AsyncTask<String, Unit, Int>() {
        override fun doInBackground(vararg params: String?): Int? {
            val idMesCorrente = appDatabase.orcamentoDAO().buscarIdMes(params[0]!!)
            return idMesCorrente
        }
    }
    private fun currentMonth2():String {
        val month: Calendar = Calendar.getInstance()
        val mesAtual = month.get(Calendar.MONTH)
        var mes = when (mesAtual) {
            0 -> "Janeiro"
            1 -> "Fevereiro"
            2 ->  "Março"
            3 ->  "Abril"
            4 -> "Maio"
            5 -> "Junho"
            6 -> "Julho"
            7 ->  "Agosto"
            8 ->  "Setembro"
            9 ->  "Outubro"
            10 -> "Novembro"
            11 ->  "Dezembro"
            else -> "Mês"
        }
        return mes
    }
}
