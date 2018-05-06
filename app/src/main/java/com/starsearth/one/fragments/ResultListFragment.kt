package com.starsearth.one.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.starsearth.one.R
import com.starsearth.one.adapter.MyResultRecyclerViewAdapter
import com.starsearth.one.domain.*
import java.util.*
import kotlin.collections.HashSet
import android.widget.TextView
import android.view.Gravity





/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class ResultListFragment : Fragment() {
    // TODO: Customize parameters
    private var mColumnCount = 1
    private var isActivityPaused = true
    private var mJustCompletedResultsSet: MutableSet<Result> = HashSet<Result>()
    private var mTeachingContent: Any? = null
    private var mDatabase: DatabaseReference? = null
    private var mListener: OnListFragmentInteractionListener? = null

    private val mChildEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val result = if ((mTeachingContent as Task)?.type == Task.Type.TYPING_TIMED || (mTeachingContent as Task)?.type == Task.Type.TYPING_UNTIMED) {
                dataSnapshot.getValue(ResultTyping::class.java)
            } else {
                dataSnapshot.getValue(ResultGestures::class.java)
            }
            if ((mTeachingContent as SEBaseObject)?.id != result!!.task_id) {
                return;
            }

            var isHighScore = false
            val adapter = (view as RecyclerView).adapter
            val high_score = (adapter as MyResultRecyclerViewAdapter).getItem("high_score")
            val last_tried = adapter.getItem("last_tried")

            adapter.putItem("last_tried", result)

            if (high_score == null) {
                isHighScore = true
                adapter.putItem("high_score", result)
            }
            else if (adapter.isHigScore(result)) {
                isHighScore = true
                adapter.putItem("high_score", result)
                mDatabase?.child((high_score as Result).uid)?.removeValue() //delete from the database
            }
            
            if (result!!.isJustCompleted) {
                mJustCompletedResultsSet.add(result)
                justCompletedTask(result, isHighScore)
                setReturnResult(result)
            }

            //make sure last_tried is not the current(new) high_score
            last_tried?.let { if ((it as Result)?.uid != (adapter.getItem("high_score") as Result).uid) {
                mDatabase?.child(it.uid)?.removeValue() //delete from the database
                }
            }


         /*   if (adapter.itemCount > 1) {
                (adapter as MyResultRecyclerViewAdapter).removeOldestItem()
                //adapter.removeItem(lastItem) //not needed for Queue
                //mDatabase?.child((lastItem as Result).uid)?.removeValue() //delete from the database
            }   */

            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onStop() {
        super.onStop()
        mJustCompletedResultsSet.clear() //clear the queue before proceeding
    }

    override fun onPause() {
        super.onPause()
        isActivityPaused = true
    }

    override fun onResume() {
        super.onResume()

        isActivityPaused = false

        //If we are returning from an ad,
        //Take the result out of the set and display
        if (mJustCompletedResultsSet.size > 0) {
            mJustCompletedResultsSet.forEach {

                justCompletedTask(it, isHighScore())
            }
            mJustCompletedResultsSet.clear()
        }
    }

    private fun isHighScore() : Boolean {
        val adapter = (view as RecyclerView).adapter as MyResultRecyclerViewAdapter
        return (adapter.getItem("high_score") as Result).uid == (adapter.getItem("last_tried") as Result).uid
    }

    private fun justCompletedTask(result: Any?, isHighScore: Boolean) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_new_result,null)
        val tvResult = layout.findViewById<TextView>(R.id.tv_result)
        if (!isActivityPaused) {
            if (isHighScore) {
                tvResult.setText(R.string.high_score)
            }
            else if (result is ResultTyping){
                tvResult.setText(result.getResultToast(context, (mTeachingContent as Task)?.type))
            }
            else if (result is ResultGestures) {
                //Toast.makeText(context, result.getResultToast(context, isHighScore), Toast.LENGTH_SHORT).show()
                tvResult.setText(result.resultToast)
            }

            val toast = Toast(context)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layout
            toast.show()
        }


        //if (result is ResultTyping && !isActivityPaused) {
        //    Toast.makeText(context, result.getResultToast(context, (mTeachingContent as Task)?.type, isHighScore), Toast.LENGTH_SHORT).show()
       //}


        if (isHighScore) {
            mListener?.onNewHighScore(result)
        }
    }

    private fun setReturnResult(result: Any?) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putString("uid", (result as Result)?.uid)
        intent.putExtras(bundle)
        activity.setResult(Activity.RESULT_OK, intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mTeachingContent = arguments.getParcelable(ARG_TEACHING_CONTENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_resulttyping_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager = LinearLayoutManager(context)
            } else {
                view.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            val tasks = if (mTeachingContent is Course) {
                (mTeachingContent as Course)!!.getTasks()
            } else {
                ArrayList(Arrays.asList(mTeachingContent))
            }
            val results = LinkedHashMap<String, Any>()
            view.adapter = MyResultRecyclerViewAdapter(tasks as List<Task>, results, mListener)

            val currentUser = FirebaseAuth.getInstance().currentUser
            mDatabase = FirebaseDatabase.getInstance().getReference("results")
            mDatabase?.keepSynced(true)
            val query = mDatabase?.orderByChild("userId")?.equalTo(currentUser!!.uid)
            query?.addChildEventListener(mChildEventListener);
        }
        return view
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        mDatabase?.removeEventListener(mChildEventListener)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onNewHighScore(item: Any?)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_TEACHING_CONTENT = "teaching_content"

        // TODO: Customize parameter initialization
        fun newInstance(teachingContent: Parcelable?): ResultListFragment {
            val fragment = ResultListFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEACHING_CONTENT, teachingContent)
            fragment.arguments = args
            return fragment
        }
    }
}
