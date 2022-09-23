package com.starsearth.two.adapter

import android.graphics.BitmapFactory
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.starsearth.two.R
import com.starsearth.two.domain.AutismContent


import com.starsearth.two.fragments.AutismStoryFragment.OnListFragmentInteractionListener
import com.starsearth.two.fragments.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_autismstory.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class MyAutismStoryRecyclerViewAdapter(
        private val mValues: List<Any>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyAutismStoryRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private var mDownloadedImages: HashMap<String, ByteArray?>? = null

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as? AutismContent
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_autismstory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val autismContent : AutismContent? = AutismContent(mValues[position] as? Map<String, Any>)
        var contentDescription = ""
        holder.mTextViewMain.visibility = View.GONE
        holder.mTextViewLine1.visibility = View.GONE
        holder.mTextViewLine2.visibility = View.GONE
        holder.mImageView.visibility = View.GONE
        autismContent?.title?.let {
            holder.mTextViewMain.text = it
            contentDescription += it
            holder.mTextViewMain.visibility = View.VISIBLE
        }
        autismContent?.textLine1?.let {
            holder.mTextViewLine1.text = it
            contentDescription += " " + it
            holder.mTextViewLine1.visibility = View.VISIBLE
        }
        autismContent?.textLine2?.let {
            holder.mTextViewLine2.text = it
            contentDescription += " " + it
            holder.mTextViewLine2.visibility = View.VISIBLE
        }
        if (contentDescription.length > 0) {
            holder.mCardView.contentDescription = contentDescription
        }

        if (autismContent?.hasImage == true) {
            if (mDownloadedImages?.containsKey(autismContent.id.toString()) == true) {
                val byteArray = mDownloadedImages!!.get(autismContent.id.toString())
                if (byteArray != null) {
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    holder.mImageView.setImageBitmap(bitmap)
                    holder.mImageLoadingIcon.visibility = View.GONE
                    holder.mImageLoadingError.visibility = View.GONE
                }
                else {
                    holder.mImageView.setImageBitmap(null)
                    holder.mImageView.visibility = View.GONE
                    holder.mImageLoadingIcon.visibility = View.GONE
                    holder.mImageLoadingError.visibility = View.VISIBLE
                }
            }
            else {
                holder.mImageLoadingIcon.visibility = View.VISIBLE
            }
        }
        else {
            holder.mImageLoadingIcon.visibility = View.GONE
            holder.mImageLoadingError.visibility = View.GONE
            holder.mImageView.setImageBitmap(null)
            holder.mImageView.visibility = View.GONE
        }

    /*    when (autismContent?.id) {
            "741" -> holder.mImageView.setImageResource(R.drawable.autism_1)
            "74100" -> holder.mImageView.setImageResource(R.drawable.autism_1_5)
            "742" -> holder.mImageView.setImageResource(R.drawable.autism_2)
            "743" -> holder.mImageView.setImageResource(R.drawable.autism_3)
            "744" -> holder.mImageView.setImageResource(R.drawable.autism_4)
            "745" -> holder.mImageView.setImageResource(R.drawable.autism_5)
            "746" -> holder.mImageView.setImageResource(R.drawable.autism_6)
            "747" -> holder.mImageView.setImageResource(R.drawable.autism_7)
            "748" -> holder.mImageView.setImageResource(R.drawable.autism_8)
            "749" -> holder.mImageView.setImageResource(R.drawable.autism_9)
            "7410" -> holder.mImageView.setImageResource(R.drawable.autism_10)
            "7411" -> holder.mImageView.setImageResource(R.drawable.autism_11)
            "7412" -> holder.mImageView.setImageResource(R.drawable.autism_12)
            "7413" -> holder.mImageView.setImageResource(R.drawable.autism_13)
            "7414" -> holder.mImageView.setImageResource(R.drawable.autism_14)
            "7415" -> holder.mImageView.setImageResource(R.drawable.autism_15)
            "7416" -> holder.mImageView.setImageResource(R.drawable.autism_16)
            "7417" -> holder.mImageView.setImageResource(R.drawable.autism_17)
            "7418" -> holder.mImageView.setImageResource(R.drawable.autism_18)
            "7419" -> holder.mImageView.setImageResource(R.drawable.autism_19)
            "7420" -> holder.mImageView.setImageResource(R.drawable.autism_20)
            "7421" -> holder.mImageView.setImageResource(R.drawable.autism_21)
            "7422" -> holder.mImageView.setImageResource(R.drawable.autism_22)
            "7423" -> holder.mImageView.setImageResource(R.drawable.autism_23)
            "7424" -> holder.mImageView.setImageResource(R.drawable.autism_24)
            "74_25" -> holder.mImageView.setImageResource(R.drawable.autism_25)
            "75100" -> holder.mImageView.setImageResource(R.drawable.effective_parenting_1)
            "752" -> holder.mImageView.setImageResource(R.drawable.effective_parenting_2)
            "753" -> holder.mImageView.setImageResource(R.drawable.effective_parenting_3)
            "754" -> holder.mImageView.setImageResource(R.drawable.effective_parenting_4)
            "755" -> holder.mImageView.setImageResource(R.drawable.effective_parenting_5)
            else -> {
                holder.mImageView.setImageDrawable(null)
            }

        }   */

        holder.mImageView.visibility = View.VISIBLE

        with(holder.mCardView) {
            tag = autismContent
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    fun addImage(contentId: String, byteArray: ByteArray?) {
        if (mDownloadedImages == null) {
            mDownloadedImages = HashMap()
        }
        mDownloadedImages?.put(contentId, byteArray)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mCardView: CardView = mView.cvMain
        val mImageView: ImageView = mView.ivMain
        val mTextViewMain: TextView = mView.tvMain
        val mTextViewLine1: TextView = mView.tvLine1
        val mTextViewLine2: TextView = mView.tvLine2
        val mImageLoadingIcon: RelativeLayout = mView.rlLoading
        val mImageLoadingError: RelativeLayout = mView.rlLoadingError

        override fun toString(): String {
            return super.toString() + " '" + mTextViewMain.text + "'"
        }
    }
}
