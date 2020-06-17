package project.inflabnet.mytest.login

import android.content.Intent
import project.inflabnet.mytest.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val background = object: Thread() {
            override fun run() {
                try {
                    Thread.sleep(4000)
                    val intent = Intent(baseContext, LoginActivity::class.java )
                    startActivity(intent)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        background.start()
    }
}
