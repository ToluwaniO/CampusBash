package toluog.campusbash.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.interest_item_layout.*
import toluog.campusbash.R

/**
 * Created by oguns on 1/20/2018.
 */
class InterestAdapter(private val interests: ArrayList<String>, private val intrstSet: Set<String>?,
                      context: Context): RecyclerView.Adapter<InterestAdapter.ViewHolder>() {
    private val checkboxListener: OnCheckedChangeListener

    interface OnCheckedChangeListener {
        fun checkBoxClicked(interest: String,isChecked: Boolean)
    }

    init {
        this.checkboxListener = context as OnCheckedChangeListener
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(interests[position], intrstSet?.contains(interests[position])?: false,
                checkboxListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.interest_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = interests.size

    class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(title: String, isChecked: Boolean, listener: OnCheckedChangeListener) {
            interest_title.text = title
            interest_check_box.isChecked = isChecked
            interest_check_box.setOnClickListener{listener.checkBoxClicked(title,
                    interest_check_box.isChecked)}
        }
    }
}