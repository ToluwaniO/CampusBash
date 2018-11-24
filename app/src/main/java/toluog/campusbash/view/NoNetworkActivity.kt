package toluog.campusbash.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_no_network.*
import org.jetbrains.anko.design.snackbar
import toluog.campusbash.R

class NoNetworkActivity : AppCompatActivity(), NoNetworkFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_network)
    }

    override fun onTryAgainClicked(connected: Boolean) {
        if(connected) {
            finish()
        } else {
            snackbar(container, R.string.no_internet)
        }
    }
}
