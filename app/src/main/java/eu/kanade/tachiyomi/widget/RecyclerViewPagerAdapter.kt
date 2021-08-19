package eu.kanade.tachiyomi.widget

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter
import java.util.Stack

abstract class RecyclerViewPagerAdapter : ViewStatePagerAdapter() {

    private val pool = Stack<View>()

    var recycle = true
        set(value) {
            if (!value) pool.clear()
            field = value
        }

    protected abstract fun createView(container: ViewGroup): View

    protected abstract fun bindView(view: View, position: Int)

    protected open fun recycleView(view: View, position: Int) {}

    override fun createView(container: ViewGroup, position: Int): View {
        val view = if (pool.isNotEmpty()) {
            pool.pop().setViewPagerPositionParam(position)
        } else {
            createView(container)
        }
        bindView(view, position)
        return view
    }

    override fun destroyView(container: ViewGroup, position: Int, view: View) {
        recycleView(view, position)
        if (recycle) pool.push(view)
    }

    /**
     * Making sure that this ViewPager child view has the correct "position" layout param
     * after being recycled.
     */
    private fun View.setViewPagerPositionParam(position: Int): View {
        val params = layoutParams
        if (params is ViewPager.LayoutParams) {
            if (!params.isDecor) {
                try {
                    val positionField = ViewPager.LayoutParams::class.java.getDeclaredField("position")
                    positionField.isAccessible = true
                    positionField.setInt(params, position)
                    layoutParams = params
                } catch (e: NoSuchFieldException) {
                } catch (e: IllegalAccessException) {
                }
            }
        }
        return this
    }
}
