package toluog.campusbash.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_interests.*
import org.jetbrains.anko.act
import toluog.campusbash.R
import toluog.campusbash.adapters.InterestAdapter
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.Util
import kotlin.collections.ArrayList

class InterestsActivity : AppCompatActivity(), InterestAdapter.OnCheckedChangeListener {

    lateinit var interests: MutableSet<String>
    private val intsList = ArrayList<String>()
    private val TAG = InterestsActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interests)

        interests = Util.getPrefStringSet(act, AppContract.PREF_EVENT_TYPES_KEY)
        Log.d(TAG, "$interests")

        intsList.addAll(interests)

        val list = ArrayList<String>()
        list.addAll(resources.getStringArray(R.array.party_types))
        val adapter = InterestAdapter(list, interests, this)
        recycler.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(recycler.context, layoutManager.orientation)
        recycler.addItemDecoration(dividerItemDecoration)

    }

    override fun onPause() {
        super.onPause()
        Util.setPrefStringSet(act, AppContract.PREF_EVENT_TYPES_KEY, interests)
    }

    override fun onDestroy() {
        super.onDestroy()
        Util.setPrefStringSet(act, AppContract.PREF_EVENT_TYPES_KEY, interests)
    }

    override fun checkBoxClicked(interest: String, isChecked: Boolean) {
        Log.d(TAG, "checkBoxClicked(interest=$interest, isChecked=$isChecked)")
        if(isChecked) interests.add(interest)
        else interests.remove(interest)
        Log.d(TAG, "$interests")
    }
}
