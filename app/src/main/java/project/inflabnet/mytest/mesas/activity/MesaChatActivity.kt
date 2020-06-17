package project.inflabnet.mytest.mesas.activity

import android.content.Context
import android.content.Intent
import project.inflabnet.mytest.R
import project.inflabnet.mytest.mesas.adapter.MessageAdapter
import project.inflabnet.mytest.mesas.model.MesaData
import project.inflabnet.mytest.mesas.model.Message
import project.inflabnet.mytest.login.LoginActivity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_mesa_chat.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MesaChatActivity : AppCompatActivity() {

    //Firebase references
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var user: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mesa_chat)

        //Recebendo os Valores da activity MesaActivity
        val mesaData = intent.getSerializableExtra("mesa") as MesaData
        txtNomedaMesa.text = mesaData.nameMesa
        txtProp.text = mesaData.proprietarioMesa
        val pathStr = mesaData.nameMesa+"chat"
        //Toast.makeText(this,pathStr,Toast.LENGTH_SHORT).show()
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Chat")
        createFirebaseListener(pathStr)
        btnEnviarMsg.setOnClickListener { setupSendButton(pathStr) }
        user = pegaUser()
    }

    //pegar usuário logado
    private fun pegaUser() : String? {
        val userEmail = mAuth?.currentUser?.email
        if (userEmail != null) {
            user = if (userEmail.contains("@")) {
                userEmail.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            } else {
                userEmail.toString()
            }
        }
        return user
    }
    //ao clicar para enviar mensagem
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupSendButton(pathStr: String) {
        if (mainChatEditText2.text.toString().isNotEmpty()){
            sendData(pathStr)
        }else{
            Toast.makeText(this, "Por favor, escreva uma mensagem!", Toast.LENGTH_SHORT).show()
        }
    }

    //enviar dados para firebase
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendData(pathStr: String){
        //pegar o usuário
        if (user != null) {
            //gravar no Fbase
            val tStamp = java.lang.String.valueOf(System.currentTimeMillis())
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val hora = current.format(formatter)
            mDatabaseReference?.
                child(pathStr)?.
                child(tStamp)?.
                setValue(Message(user!!,  "${mainChatEditText2.text}", tStamp, false,hora))

        }else {
            //MANDA LOGAR NOVAMENTE SE O SISTEMA NÃO CONSEGUE IDENTIFICAR O USUÁRIO
            val intt = Intent(this, LoginActivity::class.java)
            startActivity(intt)
        }
        //limpar a entrada de dados
        mainChatEditText2.setText("")
        // Hide the keyboard.
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(btnEnviarMsg.windowToken, 0)
    }

    //listener para colocar as mensagens na tela
    private fun createFirebaseListener(pathStr : String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val toReturn: ArrayList<Message> = ArrayList()
                var newMessage: Message?
                for(data in dataSnapshot.children){
                    val messageData = data.getValue<Message>(Message::class.java)
                    //abrir
                    val message = messageData?.let { it } ?: continue
                    if (message.userChat == user){
                        val selfCheck: Boolean = true
                        newMessage = message.userChat?.let { message.text?.let { it1 -> message.timestamp?.let { it2 -> message.hora?.let { it3 -> Message(it, it1, it2,selfCheck, it3) } } } }
                    } else newMessage = message
                    if (newMessage != null) {
                        toReturn.add(newMessage)
                    }
                }
                //ordenar para o mais novo aparecer no final
                toReturn.sortBy { message ->
                    message.timestamp
                }
                //envia dados para o rectcleview
                setupAdapter(toReturn)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext,"Erro ao contectar ao DB", Toast.LENGTH_SHORT).show()
            }
        }
        mDatabaseReference?.child(pathStr)?.addValueEventListener(postListener)
    }

    //mostrar os dados
    private fun setupAdapter(data: ArrayList<Message>){
        val linearLayoutManager = LinearLayoutManager(this)
        mainActivityRecyclerView.layoutManager = linearLayoutManager
        mainActivityRecyclerView.adapter = user?.let {
            MessageAdapter(data) {
            //Toast.makeText(this, "${it.text} clicked", Toast.LENGTH_SHORT).show()
        }
        }
        //vai para o final
        mainActivityRecyclerView.scrollToPosition(data.size - 1)
    }
}
