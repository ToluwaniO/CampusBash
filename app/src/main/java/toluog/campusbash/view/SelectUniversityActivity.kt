package toluog.campusbash.view

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_select_university.*
import kotlinx.android.synthetic.main.university_search_layout.*
import toluog.campusbash.R
import toluog.campusbash.model.University
import toluog.campusbash.utils.AppContract
import toluog.campusbash.view.viewmodel.SelectUniversityViewModel

class SelectUniversityActivity : AppCompatActivity() {

    private val TAG = SelectUniversityActivity::class.java.simpleName
    private val universities = arrayListOf<UniversitySelector>()
    private val selectedUniversities = hashSetOf<String>()
    private var liveUniversities: LiveData<List<University>>? = null
    private lateinit var viewModel: SelectUniversityViewModel
    private lateinit var adapter: UniversityAdapter

    private val watcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
        override fun onTextChanged(seq: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (seq == null) {
                search("")
            } else {
                search(seq.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_university)
        val unis = intent?.extras?.get(AppContract.UNIVERSITIES) as List<String>?
        if (unis != null) {
            selectedUniversities.addAll(unis)
        }
        viewModel = ViewModelProviders.of(this).get(SelectUniversityViewModel::class.java)
        adapter = UniversityAdapter()
        search_bar.addTextChangedListener(watcher)
        university_recycler.apply {
            adapter = this@SelectUniversityActivity.adapter
            layoutManager = LinearLayoutManager(this@SelectUniversityActivity)
        }
        search("")
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_university_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.action_save -> {
                setResult(Activity.RESULT_OK, Intent().apply {
                    val unis = Array(selectedUniversities.size) {""}
                    for ((i, uni) in selectedUniversities.withIndex()) {
                        unis[i] = uni
                    }
                    putExtra(AppContract.UNIVERSITIES, unis)
                })
                finish()
            }
        }
        return true
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(AppContract.UNIVERSITIES, selectedUniversities.toArray())
        })
        super.onBackPressed()
    }

    fun search(query: String) {
        liveUniversities?.removeObservers(this)
        liveUniversities = if (query.isBlank()) {
            viewModel.getUniversities()
        } else {
            viewModel.getUniversities("%$query%")
        }
        liveUniversities?.observe(this, Observer {
            Log.d(TAG, "$it")
            universities.clear()
            it?.let { it1 -> updateUniversityList(it1) }
            adapter.notifyDataSetChanged()
        })
    }

    private fun updateUniversityList(unis: List<University>) {
        unis.forEach {
            universities.add(UniversitySelector(it.name, false))
        }
    }

    inner class UniversityAdapter: RecyclerView.Adapter<SelectUniversityViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectUniversityViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.university_search_layout,
                    parent, false)
            return SelectUniversityViewHolder(view)
        }
        override fun getItemCount() = universities.size
        override fun onBindViewHolder(holder: SelectUniversityViewHolder, position: Int) {
            holder.bind(universities[position])
        }
    }

    inner class SelectUniversityViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(selector: UniversitySelector) {
            selector.selected = selectedUniversities.contains(selector.university)
            university_checkbox.isChecked = selector.selected
            university_text.text = selector.university
            university_checkbox.setOnCheckedChangeListener { compoundButton, checked ->
                if (checked) {
                    selectedUniversities.add(selector.university)
                } else {
                    selectedUniversities.remove(selector.university)
                }
                Log.d(TAG, "$selectedUniversities")
            }
//            containerView.setOnClickListener {
//                val checked = !university_checkbox.isChecked
//                university_checkbox.isChecked = checked
//                if (checked) {
//                    selectedUniversities.add(selector.university)
//                } else {
//                    selectedUniversities.remove(selector.university)
//                }
//                Log.d(TAG, "$selectedUniversities")
//            }
        }
    }
}

data class UniversitySelector(var university: String = "", var selected: Boolean = false)


