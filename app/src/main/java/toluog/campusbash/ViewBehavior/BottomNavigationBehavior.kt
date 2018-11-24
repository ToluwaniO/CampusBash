package toluog.campusbash.ViewBehavior

import android.content.Context
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.view.View
import androidx.core.view.ViewCompat
import android.util.AttributeSet


/**
 * Created by oguns on 12/31/2017.
 */
class BottomNavigationBehavior:
        CoordinatorLayout.Behavior<BottomNavigationView> {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {

    }
    constructor():super()

    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigationView,
                                 dependency: View): Boolean {
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView,
                                     directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView,
                                   target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (dy < 0) {
            showBottomNavigationView(child);
        } else if (dy > 0) {
            hideBottomNavigationView(child);
        }
    }

    private fun hideBottomNavigationView(view: BottomNavigationView) {
        view.animate().translationY(view.height.toFloat())
    }

    private fun showBottomNavigationView(view: BottomNavigationView) {
        view.animate().translationY(0f)
    }
}