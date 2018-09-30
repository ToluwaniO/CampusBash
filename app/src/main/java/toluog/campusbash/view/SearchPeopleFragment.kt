package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.search_people_layout.*
import toluog.campusbash.R
import toluog.campusbash.ViewHolder.SearchPeopleViewHolder
import toluog.campusbash.model.PublicProfile

class SearchPeopleFragment: Fragment() {

    private val TAG = SearchPeopleFragment::class.java.simpleName

    private lateinit var rootView: View
    private lateinit var viewModel: PeopleViewModel

    private val people = ArrayList<PublicProfile>()
    private val adapter = SearchPeopleAdapter()

    private val searchWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(s != null) {
                viewModel.search(s.toString())
                Log.d(TAG, "Query text changed")
            } else {
                viewModel.search("")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.search_people_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(PeopleViewModel::class.java)
        search_bar.addTextChangedListener(searchWatcher)
        val lManager = LinearLayoutManager(view.context)
        people_recycler.apply {
            adapter = this@SearchPeopleFragment.adapter
            layoutManager = lManager
            addItemDecoration(DividerItemDecoration(this.context, lManager.orientation))
        }
        viewModel.getPeople().observe(this, Observer {
            people.clear()
            if (it != null) {
                people.addAll(it)
            }
            adapter.searchUpdated()
        })
    }

    inner class SearchPeopleAdapter: RecyclerView.Adapter<SearchPeopleViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPeopleViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_list_item, parent, false)
            return  SearchPeopleViewHolder(view)
        }

        override fun getItemCount() = people.size

        override fun onBindViewHolder(holder: SearchPeopleViewHolder, position: Int) {
            holder.bind(people[position])
        }

        fun searchUpdated() {
            notifyDataSetChanged()
        }
    }
}