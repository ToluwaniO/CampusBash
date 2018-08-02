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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.pick_university_fragment_layout.*
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
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
    private lateinit var countries: Array<String>
    private var universities = ArrayList<String>()
    private var viewModel: FirstOpenViewModel? = null
    private var country: String? = null
    private var university: String? = null
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var universityAdapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.pick_university_fragment_layout, container, false)
        viewModel = ViewModelProviders.of(this).get(FirstOpenViewModel::class.java)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countries = resources.getStringArray(R.array.countries)
        countryAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, countries)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        universityAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, universities)
        universityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        country_picker.adapter = countryAdapter
        university_picker.adapter = universityAdapter

        country_picker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                country = countries[pos]
                uni_title.visibility = View.VISIBLE
                university_picker.visibility = View.VISIBLE
                viewModel?.getUniversities(countries[pos])?.observe(this@PickUniversityFragment, Observer {
                    universities.clear()
                    it?.forEach { uni ->
                        
                        universities.add(uni.name)
                    }
                    universityAdapter.notifyDataSetChanged()
                })
            }

        }


        university_picker.onItemSelectedListener {
            this.onItemSelected { adapterView, view, i, l ->
                Log.d(TAG, "UNIVERSITIES -> ${universities.size}")
                university = universities[i]
            }
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
}