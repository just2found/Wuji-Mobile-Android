package libs.source.common.adapter

import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.PagerAdapter

/**
 *  
 *
 *
 * Created by admin on 2020/7/29,16:20
 */
abstract class AbsNavFragmentPagerAdapter(private val mFragmentManager: FragmentManager) : PagerAdapter() {

    var data: List<String>? = null
        private set
    private val mSparseArray: SparseArray<String> = SparseArray()

    private var mCurTransaction: FragmentTransaction? = null

    var currentPrimaryItem: Fragment? = null
        private set

    abstract fun getItem(tag: String): Fragment


    override fun getCount(): Int {
        return if (this.data == null) 0 else data!!.size
    }

    fun replaceData(data: List<String>?) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getItemPosition(any: Any): Int {
        return POSITION_NONE
    }


    override fun startUpdate(container: ViewGroup) {
        check(container.id != -1) { "ViewPager with adapter $this requires a view id" }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction()
        }

        val name = makeFragmentName(position)

        val keepValue = mSparseArray.get(position)
        var changed = false
        if (keepValue != null && name == keepValue) {
            changed = true
        }
        var fragment = this.mFragmentManager.findFragmentByTag(name)
        if (changed && fragment != null) {
            mCurTransaction!!.remove(fragment)
            this.mCurTransaction!!.commitNowAllowingStateLoss()
            fragment = null
            this.mCurTransaction = this.mFragmentManager.beginTransaction()
        }
        if (fragment != null) {
            this.mCurTransaction!!.attach(fragment)
        } else {
            fragment = this.getItem(name)
            mSparseArray.put(position, name)
            this.mCurTransaction!!.add(container.id, fragment, name)
        }

        if (fragment !== this.currentPrimaryItem) {
            fragment.setMenuVisibility(false)
            fragment.userVisibleHint = false
        }

        return fragment
    }

    private fun makeFragmentName(position: Int): String {
        return data!![position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction()
        }

        this.mCurTransaction!!.detach(any as Fragment)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, any: Any) {
        val fragment = any as Fragment
        if (fragment !== this.currentPrimaryItem) {
            if (this.currentPrimaryItem != null) {
                this.currentPrimaryItem!!.setMenuVisibility(false)
                this.currentPrimaryItem!!.userVisibleHint = false
            }

            fragment.setMenuVisibility(true)
            fragment.userVisibleHint = true
            this.currentPrimaryItem = fragment
        }

    }

    override fun finishUpdate(container: ViewGroup) {
        if (this.mCurTransaction != null) {
            this.mCurTransaction!!.commitNowAllowingStateLoss()
            this.mCurTransaction = null
        }

    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return (any as Fragment).view === view
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

}