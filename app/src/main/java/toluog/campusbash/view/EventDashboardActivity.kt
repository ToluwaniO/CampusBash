package toluog.campusbash.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem

import toluog.campusbash.R
import kotlinx.android.synthetic.main.activity_event_dashboard.*
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.extension.intentFor
import toluog.campusbash.view.viewmodel.EventDashboardViewModel

class EventDashboardActivity : AppCompatActivity() {

    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var viewmodel: EventDashboardViewModel
    private val fragments = arrayListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_dashboard)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val eventId = intent.extras.getString(AppContract.EVENT_ID)
        fragments.apply {
            add(EventDashboardFragment.newInstance(eventId))
            add(TicketDashboardFragment.newInstance(eventId))
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        tabs.setupWithViewPager(container)
        scan_button.setOnClickListener {
            startActivity(intentFor<ScannerActivity>().apply {
                putExtra(AppContract.EVENT_ID, eventId)
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments[position]
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.sales)
                1 -> return getString(R.string.tickets)
            }
            return null
        }
    }
}
