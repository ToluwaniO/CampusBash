package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.pick_university_fragment_layout.*
import org.jetbrains.anko.support.v4.selector
import toluog.campusbash.R
import java.lang.ClassCastException
import kotlin.collections.ArrayList

/**
 * Created by oguns on 12/28/2017.
 */
class PickUniversityFragment(): Fragment(){

    interface PickUniversityListener {
        fun universitySelectionDone(country: String, name: String)
    }

    private val TAG = PickUniversityFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var callback: PickUniversityListener
    private lateinit var countries: List<String>
    private var viewModel: FirstOpenViewModel? = null
    private var country: String? = null
    private var university: String? = null
    private var universities = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.pick_university_fragment_layout, container, false)
        viewModel = ViewModelProviders.of(this).get(FirstOpenViewModel::class.java)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countries = resources.getStringArray(R.array.countries).asList()


        country_text.setOnClickListener {
            selector(getString(R.string.select_country), countries, { dialogInterface, i ->
                country_text.text = countries[i]
                country = countries[i]
                viewModel?.getUniversities(countries[i])?.observe(this, Observer {
                    universities.clear()
                    it?.forEach { uni ->
                        universities.add(uni.name)
                    }
                })
            })
        }

        university_text.setOnClickListener {
            Log.d(TAG, "UNIVERSITIES -> ${universities.size}")
            selector(getString(R.string.select_university), universities, { dialogInterface, i ->
                university_text.text = universities[i]
                university = universities[i]
            })
        }

        next_button.setOnClickListener {
            val uni = university
            val con = country
            if (uni!= null && con != null) {
                callback.universitySelectionDone(con, uni)
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            callback = context as PickUniversityListener
        } catch (e: ClassCastException){
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val refWatcher = MainApplication.getRefWatcher(activity?.applicationContext)
        refWatcher?.watch(this)
    }
}