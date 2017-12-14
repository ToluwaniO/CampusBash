package toluog.campusbash.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import toluog.campusbash.R
import toluog.campusbash.Utils.AppContract

/**
 * Created by oguns on 12/13/2017.
 */
class EventsFragment() : Fragment() {

    private var rootView: View? = null
    private var myEvents = false

    init {

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = arguments
        if(bundle != null){
            myEvents = bundle.getBoolean(AppContract.MY_EVENT_BUNDLE)
        }

        rootView = inflater?.inflate(R.layout.events_layout, container, false)
        return rootView
    }
}