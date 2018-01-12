package toluog.campusbash.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.profile_fragment_layout.*
import org.jetbrains.anko.support.v4.act
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util

/**
 * Created by oguns on 12/26/2017.
 */
class ProfileFragment(): Fragment(), AdapterView.OnItemSelectedListener {

    private val TAG = ProfileFragment::class.java.simpleName
    var rootView: View? = null
    lateinit var university:String
    lateinit var country: String
    val interests = ArrayList<String>()
    lateinit var adapter: ArrayAdapter<String>
    lateinit var spinnerAdadpter: ArrayAdapter<CharSequence>
    val util = Util()
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater?.inflate(R.layout.profile_fragment_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseManager.getUser()
        if(user != null) {
            Glide.with(rootView?.context).load(user.photoUrl).into(profile_image)
            profile_name.text = user.displayName
            profile_email.text = user.email
        }

        university = Util.getPrefString(act, AppContract.PREF_UNIVERSITY_KEY)
        country = Util.getPrefString(act, AppContract.PREF_COUNTRY_KEY)

        profile_university.text = university
        profile_country.text = country

        val ints = Util.getPrefStringSet(act, AppContract.PREF_EVENT_TYPES_KEY)
        if(ints != null) interests.addAll(ints)

        Log.d(TAG, "Universities $interests")

        adapter = ArrayAdapter(rootView?.context, R.layout.text_view_layout, interests)
        profile_interest_list.adapter = adapter

        profile_interest_list.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
                interests.remove(interests[position])
                adapter.notifyDataSetChanged()
        }

        spinnerAdadpter = ArrayAdapter.createFromResource(rootView?.context, R.array.party_types,
                R.layout.text_view_layout)
        profile_spinner.adapter = spinnerAdadpter
        profile_spinner.onItemSelectedListener = this

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intrs = spinnerAdadpter.getItem(position).toString()
        if(!interests.contains(intrs)) {
            interests.add(intrs)
            adapter.notifyDataSetChanged()
        }
    }
}