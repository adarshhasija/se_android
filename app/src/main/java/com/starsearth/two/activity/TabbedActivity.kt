package com.starsearth.two.activity

import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

import com.starsearth.two.R
import com.starsearth.two.domain.SEOneListItem
import com.starsearth.two.fragments.lists.SeOneListFragment
import kotlinx.android.synthetic.main.activity_tabbed.*
import kotlinx.android.synthetic.main.fragment_tabbed.view.*

class TabbedActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        if (PlaceholderFragment.NUMBER_OF_TABS > 1) {
            tabLayout?.visibility = View.VISIBLE
        }
        else {
            tabLayout?.visibility = View.GONE
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val availability = GoogleApiAvailability.getInstance()
        val available = availability.isGooglePlayServicesAvailable(applicationContext)
        if (available != ConnectionResult.SUCCESS) {
            availability.showErrorDialogFragment(this@TabbedActivity, available, 1)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_tabbed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1)
            var fragment : Fragment?
            when (position) {
                0 -> {
                    fragment = SeOneListFragment.newInstance(SEOneListItem.Type.TAG)
                }
                else -> {
                    fragment = SeOneListFragment.newInstance(SEOneListItem.Type.TAG)
                }
            }
            return fragment
        }

        override fun getPageTitle(position: Int): CharSequence {
            var title : CharSequence = applicationContext?.resources?.getString(R.string.actions) as CharSequence
            when (position) {
                0 -> {
                    title = applicationContext?.resources?.getString(R.string.actions) as CharSequence
                }
                else -> {
                    title = applicationContext?.resources?.getString(R.string.records) as CharSequence
                }
            }
            return title; //super.getPageTitle(position)
        }

        override fun getCount(): Int {
            // Change this depending on how many pages you want
            return PlaceholderFragment.NUMBER_OF_TABS
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_tabbed, container, false)
            rootView.section_label.text = getString(R.string.section_format, arguments?.getInt(ARG_SECTION_NUMBER))

            return rootView
        }

        companion object {
            val NUMBER_OF_TABS = 1 //Change this if you want to change the number of tabs shown

            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
