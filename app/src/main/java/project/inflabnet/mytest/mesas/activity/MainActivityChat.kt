package project.inflabnet.mytest.mesas.activity

import android.content.Intent
import project.inflabnet.mytest.R
import project.inflabnet.mytest.mesas.adapter.MessageAdapter
import project.inflabnet.mytest.mesas.model.Message
import project.inflabnet.mytest.login.LoginActivity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main_chat.*

class MainActivityChat : AppCompatActivity() {

    //Firebase references
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("messages")
        createFirebaseListener()
        btnEnviarMsg.setOnClickListener { setupSendButton() }
    }

    //ao clicar para enviar mensagem
    private fun setupSendButton() {
            if (!mainActivityEditText.text.toString().isEmpty()){
                sendData()
            }else{
                Toast.makeText(this, "Por favor, escreva uma mensagem!", Toast.LENGTH_SHORT).show()
            }
    }

    //enviar dados para firebase
    private fun sendData(): String? {
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
//            mDatabaseReference?.
//                child("messages")?.
//                child(java.lang.String.valueOf(System.currentTimeMillis()))?.
//                setValue(Message("$user - ${mainActivityEditText.text.toString()}"))

        }else {
            var intt = Intent(this, LoginActivity::class.java)
            startActivity(intt)
        }

        //limpar a entrada de dados
        mainActivityEditText.setText("")
        return user
    }

    private fun createFirebaseListener(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val toReturn: ArrayList<Message> = ArrayList();

                for(data in dataSnapshot.children){
                    val messageData = data.getValue<Message>(Message::class.java)

                    //unwrap
                    val message = messageData?.let { it } ?: continue

                    toReturn.add(message)
                }

                //sort so newest at bottom
                toReturn.sortBy { message ->
                    message.timestamp
                }

                setupAdapter(toReturn)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        mDatabaseReference?.child("messages")?.addValueEventListener(postListener)
    }

    //mostrar os dados
    private fun setupAdapter(data: ArrayList<Message>){
        val linearLayoutManager = LinearLayoutManager(this)
        mainActivityRecyclerView.layoutManager = linearLayoutManager
        mainActivityRecyclerView.adapter = MessageAdapter(data) {
            Toast.makeText(this, "${it.text} clicked", Toast.LENGTH_SHORT).show()
        }

        //scroll to bottom
        mainActivityRecyclerView.scrollToPosition(data.size - 1)
    }

}
