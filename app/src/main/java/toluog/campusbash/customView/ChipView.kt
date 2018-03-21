package toluog.campusbash.customView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import toluog.campusbash.R
import android.content.res.TypedArray
import android.graphics.drawable.ShapeDrawable


/**
 * Created by oguns on 3/18/2018.
 */
class ChipView : TextView {
    private var clicked: Boolean = false

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    @SuppressLint("ResourceAsColor")
    fun init(set: AttributeSet?) {
        if(set == null) return
        val ta = context.obtainStyledAttributes(set, R.styleable.ChipView, 0, 0)
        val background = ta.getColor(R.styleable.ChipView_backgroundColor, R.color.colorPrimaryDark)
        val clicked = ta.getBoolean(R.styleable.ChipView_clicked, false)
        val clickedBackground = ta.getColor(R.styleable.ChipView_clickedBackgroundColor, R.color.colorPrimary)
        ta.recycle()
        val backDrawable = context.getDrawable(R.drawable.chip_shape) as ShapeDrawable
        if(clicked){
            backDrawable.paint.color = background
        } else {
            backDrawable.paint.color = clickedBackground
        }
        this.clicked = clicked
        setBackground(backDrawable)
    }

    fun setClicked(clicked: Boolean) {
        this.clicked = clicked
        invalidate()
        requestLayout()
    }

}