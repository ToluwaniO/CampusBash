package toluog.campusbash.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProviders
import toluog.campusbash.R
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_view_event.*
import toluog.campusbash.utils.AppContract
import toluog.campusbash.view.viewmodel.ViewEventViewModel

class ViewEventActivity : AppCompatActivity() {
    private val TAG = ViewEventActivity::class.java.simpleName
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportPostponeEnterTransition()
        Log.d(TAG, intent?.getStringExtra("eventId"))
        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host) as NavHostFragment? ?: return
        navController = host.navController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host).navigateUp()
}
