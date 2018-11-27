package toluog.campusbash.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_no_network.*
import toluog.campusbash.R
import toluog.campusbash.utils.extension.snackbar

class NoNetworkActivity : AppCompatActivity(), NoNetworkFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_network)
    }

    override fun onTryAgainClicked(connected: Boolean) {
        if(connected) {
            finish()
        } else {
            container.snackbar(R.string.no_internet)
        }
    }
}
