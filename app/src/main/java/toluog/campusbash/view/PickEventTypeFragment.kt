package toluog.campusbash.view

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.igalata.bubblepicker.BubblePickerListener
import com.igalata.bubblepicker.adapter.BubblePickerAdapter
import com.igalata.bubblepicker.model.BubbleGradient
import com.igalata.bubblepicker.model.PickerItem
import com.igalata.bubblepicker.rendering.BubblePicker
import kotlinx.android.synthetic.main.pick_event_type_fragment_layout.*
import kotlinx.android.synthetic.main.pick_event_type_fragment_layout.view.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.design.snackbar
import toluog.campusbash.R
import toluog.campusbash.R.array.colors
import java.lang.ClassCastException

/**
 * Created by oguns on 12/28/2017.
 */
class PickEventTypeFragment(): Fragment() {

    interface PickEventTypeListener {
        fun eventsPickDone(selected: Set<String>)
    }

    private var callback: PickEventTypeListener? = null
    private val TAG = PickEventTypeFragment::class.java.simpleName
   private lateinit var rootView: View
    lateinit var evetTypes: Array<String>
    val selectedTypes = HashSet<String>()

    private lateinit var boldTypeface: Typeface
    private lateinit var mediumTypeface: Typeface
    private lateinit var regularTypeface: Typeface
    private var assets: AssetManager? = null

    companion object {
        private const val ROBOTO_BOLD = "roboto_bold.ttf"
        private const val ROBOTO_MEDIUM = "roboto_medium.ttf"
        private const val ROBOTO_REGULAR = "roboto_regular.ttf"
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.pick_event_type_fragment_layout, container, false)

        assets = rootView.context?.assets
        boldTypeface = Typeface.createFromAsset(assets, ROBOTO_BOLD)
        mediumTypeface = Typeface.createFromAsset(assets, ROBOTO_MEDIUM)
        regularTypeface = Typeface.createFromAsset(assets, ROBOTO_REGULAR)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        evetTypes = resources.getStringArray(R.array.party_types)
        next_button.setOnClickListener {
            if(selectedTypes.size < 2){
                view?.context?.getString(R.string.items_at_least_two)?.let { x -> snackbar(container, x) }
            }
            else callback?.eventsPickDone(selectedTypes)
        }
        updateUi()
    }

    override fun onResume() {
        super.onResume()
        picker.onResume()
    }

    override fun onPause() {
        super.onPause()
        picker.onPause()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            callback = context as PickEventTypeListener
        } catch (e: ClassCastException){
            e.printStackTrace()
        }
    }

    private fun updateUi(){
        picker.centerImmediately = true
        picker.bubbleSize = 20
        picker.adapter = object : BubblePickerAdapter {
            override val totalCount = evetTypes.size
            override fun getItem(position: Int): PickerItem {
                return PickerItem().apply {
                    val colors = rootView.context.resources.obtainTypedArray(R.array.colors)
                    title = evetTypes[position]
                    gradient = BubbleGradient(colors.getColor((position * 2) % 8, 0),
                            colors.getColor((position * 2) % 8 + 1, 0), BubbleGradient.VERTICAL)
                    typeface = mediumTypeface
                    textColor = ContextCompat.getColor(rootView.context, android.R.color.white)
                    backgroundImage = ContextCompat.getDrawable(rootView.context, R.drawable.sit_down_event)
                }
            }

        }

        picker.listener = object : BubblePickerListener {
            override fun onBubbleDeselected(item: PickerItem) {
                selectedTypes.remove(item.title)
                Log.d(TAG, "Just removed ${item.title}")
                number_selected_view.text = getString(R.string.x_items_selected, selectedTypes.size)
            }

            override fun onBubbleSelected(item: PickerItem) {
                val type = item.title
                if(type != null){
                    selectedTypes.add(type)
                    Log.d(TAG, "Just added $type")
                    number_selected_view.text = getString(R.string.x_items_selected, selectedTypes.size)
                }
            }
        }
    }
}