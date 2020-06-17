package project.inflabnet.mytest.login


import android.content.Intent
import project.inflabnet.mytest.HomeActivity
import project.inflabnet.mytest.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    //global variables
    private var email: String? = null
    private var password: String? = null
    //Firebase references
    private var mAuth: FirebaseAuth? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initialise()
        getToken()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        txtVersion.append(version)
    }

    private fun initialise() {
        mAuth = FirebaseAuth.getInstance()
        tv_forgot_password!!
            .setOnClickListener {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        ForgotPasswordActivity::class.java
                    )
                )
            }
        btn_register_account!!
            .setOnClickListener {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        CreateAccountActivity::class.java
                    )
                )
            }
        btn_login!!.setOnClickListener { loginUser() }
    }

    private fun loginUser() {
        email = et_email?.text.toString()
        password = et_password?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressbar.visibility = View.VISIBLE
            Log.d(TAG, "Entrando.....")
            mAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    progressbar.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Sucesso na autenticação
                        Log.d(TAG, "Sucesso na autenticação")
                        updateUI()
                    } else {
                        //Se a autenticação falhou, envia mensagem para usuário
                        Log.e(TAG, "Acesso com email falhou", task.exception)
                        Toast.makeText(this@LoginActivity, task.exception?.message,
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Entre todos os detalhes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getToken() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    token = task.result?.token

                    //Toast.makeText(this, "Token ${token}", Toast.LENGTH_SHORT).show()
                })
    }

    private fun updateUI() {
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        //Toast.makeText(baseContext, "Token ${token}", Toast.LENGTH_SHORT).show()
        intent.putExtra("token",token)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}