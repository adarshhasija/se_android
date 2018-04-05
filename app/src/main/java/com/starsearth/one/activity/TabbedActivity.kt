package com.starsearth.one.activity

import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics

import com.starsearth.one.R
import com.starsearth.one.activity.profile.PhoneNumberActivity
import com.starsearth.one.domain.Task
import com.starsearth.one.domain.MainMenuItem
import com.starsearth.one.domain.MoreOptionsMenuItem
import com.starsearth.one.domain.SEBaseObject
import com.starsearth.one.fragments.MainMenuItemFragment
import com.starsearth.one.fragments.MoreOptionsMenuItemFragment
import kotlinx.android.synthetic.main.activity_tabbed.*
import kotlinx.android.synthetic.main.fragment_tabbed.view.*

class TabbedActivity : AppCompatActivity(), MainMenuItemFragment.OnListFragmentInteractionListener, MoreOptionsMenuItemFragment.OnListFragmentInteractionListener {

    override fun setListFragmentProgressBarVisibility(visibility: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = visibility
        if (visibility == View.VISIBLE) {
            progressBar.announceForAccessibility(getString(R.string.loading) + " " + getString(R.string.please_wait))
        }
        else {
            progressBar.announceForAccessibility(getString(R.string.loading_complete))
        }
    }

    fun sendAnalytics(item: SEBaseObject) {
        val bundle = Bundle()
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, item.id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.title)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "list_item")
        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun sendAnalytics(selected: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected)
        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    override fun onListFragmentInteraction(item: MoreOptionsMenuItem) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        sendAnalytics(item.text1)
        val intent: Intent
        val title = item.text1
        if (title != null && title.contains("Keyboard")) {
            intent = Intent(this, KeyboardActivity::class.java)
            startActivity(intent)
        } else if (title != null && title.contains("Phone")) {
            intent = Intent(this, PhoneNumberActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onListFragmentInteraction(item: MainMenuItem) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val teachingContent = item.teachingContent
        sendAnalytics((teachingContent as SEBaseObject))
        val intent = Intent(this, ResultActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("teachingContent", teachingContent)
        intent.putExtras(bundle)
        startActivity(intent)
    }



    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
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
            var fragment : Fragment = MoreOptionsMenuItemFragment.newInstance(1)
            when (position) {
                0 -> {
                    fragment = MainMenuItemFragment.newInstance(1)
                }
                else -> {

                }
            }
            return fragment
        }

        override fun getPageTitle(position: Int): CharSequence {
            var title : CharSequence = "More"
            when (position) {
                0 -> {
                    title = "Main"
                }
                else -> {
                }
            }
            return title; //super.getPageTitle(position)
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_tabbed, container, false)
            rootView.section_label.text = getString(R.string.section_format, arguments.getInt(ARG_SECTION_NUMBER))
            return rootView
        }

        companion object {
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
