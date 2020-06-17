package project.inflabnet.mytest.fragments


import android.content.Intent
import project.inflabnet.mytest.R
import project.inflabnet.mytest.maps.HomeMapsActivity
import project.inflabnet.mytest.mesas.activity.MesaActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.android.synthetic.main.fragment_home_menu.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class HomeMenuFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_home_menu, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

		MobileAds.initialize(context) {}
		MobileAds.setRequestConfiguration(
			RequestConfiguration.Builder()
				//.setTestDeviceIds(Arrays.asList("275AE6C1B126313586B3368295FE5B80"))
				.build()
		)

		// Create an ad request.
		val adRequest = AdRequest.Builder().build()

		// Start loading the ad in the background.
		ad_view_menu.loadAd(adRequest)

        btnMapsMenu.setOnClickListener {
            val intt = Intent(this.context!!.applicationContext, HomeMapsActivity::class.java)
            startActivity(intt)
        }

        btnMesas.setOnClickListener {
            val intt = Intent(this.context!!.applicationContext, MesaActivity::class.java)
            startActivity(intt)
        }

        btnTelaOrcamento.setOnClickListener {
            findNavController().navigate(R.id.action_homeMenuFragment_to_orcamentoFragment)

        }

    }
}
