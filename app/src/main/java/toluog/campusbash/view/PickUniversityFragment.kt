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
import android.widget.Spinner
import android.widget.Switch
import kotlinx.android.synthetic.main.list_with_x_layout.view.*
import kotlinx.android.synthetic.main.pick_university_fragment_layout.*
import org.jetbrains.anko.support.v4.act
import toluog.campusbash.R
import toluog.campusbash.model.University
import java.lang.ClassCastException

/**
 * Created by oguns on 12/28/2017.
 */
class PickUniversityFragment(): Fragment(), AdapterView.OnItemSelectedListener{

    interface PickUniversityListener {
        fun universitySelectionDone(country: String, name: String)
    }

    private val TAG = PickUniversityFragment::class.java.simpleName
    private var rootView: View? = null
    private lateinit var callback: PickUniversityListener
    private lateinit var counAdapter: ArrayAdapter<CharSequence>
    private lateinit var uniAdapter: ArrayAdapter<CharSequence>
    private var viewModel: FirstOpenViewModel? = null
    private var country: String? = null
    private var university: String? = null
    private val universities = ArrayList<CharSequence>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.pick_university_fragment_layout, container, false)
        viewModel = ViewModelProviders.of(this).get(FirstOpenViewModel::class.java)

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        next_button.setOnClickListener {
            val uni = university
            if (uni!= null) {
                val con = country as String
                callback.universitySelectionDone(con, uni)
            }
        }
    }

    private fun setUpUniSpinner(){
        viewModel?.getUniversities(country!!)?.observe(this, Observer { unis ->
            if(unis != null){
                universities.clear()
                unis.mapTo(universities) { it.name }
                uniAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        //TODO: Fix spinner before launch!!!!!!!!!!!!!!!!!!!!
        counAdapter = ArrayAdapter.createFromResource(rootView?.context, R.array.countries,
                R.layout.text_view_layout)
        //counAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        uniAdapter = ArrayAdapter(rootView?.context, R.layout.text_view_layout, universities)
        //uniAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        country_spinner.adapter = counAdapter
        university_spinner.adapter = uniAdapter

        university_spinner.onItemSelectedListener = this
        country_spinner.onItemSelectedListener = this
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            callback = context as PickUniversityListener
        } catch (e: ClassCastException){
            e.printStackTrace()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id){
            R.id.university_spinner -> {
                university_spinner.setSelection(position)
                university = universities[position].toString()
            }
            R.id.country_spinner -> {
                country = resources.getStringArray(R.array.countries)[position]
                setUpUniSpinner()
            }
        }
    }
}