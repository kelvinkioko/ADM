package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridAdapter
import kotlinx.android.synthetic.main.item_insta.view.*
import kotlinx.android.synthetic.main.item_insta_header.view.*
import com.bumptech.glide.Glide
import java.io.File
import java.util.ArrayList

class GridAdapter (private val context: Context, private var instaEntity: List<InstaEntity>) : StickyHeaderGridAdapter() {

    private lateinit var downloader: Downloader
    private val sections = ArrayList<Section>()
    private var originalModel: List<InstaEntity> = instaEntity

    private val handler: Handler = Handler()

    lateinit var clickListener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    interface OnItemClickListener {
        fun parentClick(view: View, position: Int, userCode: String)
    }

    private inner class Section {
        var alpha: String = ""
        var instaEntity: ArrayList<InstaEntity> = ArrayList()
    }

    inner class ItemViewHolder (view: View) : StickyHeaderGridAdapter.ItemViewHolder(view) {
        val imageClear = view.insta_image_clear
        val instaType = view.insta_type
        val instaID = view.insta_id

        /**
         * Actions loaded below
         */
        val instaDownload = view.insta_download
        val instaProgress = view.insta_progress
    }

    inner class HeaderViewHolder (headerView: View) : StickyHeaderGridAdapter.HeaderViewHolder(headerView) {
        val instaHeader = headerView.insta_header
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_insta, parent, false))
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): HeaderViewHolder {
        return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_insta_header, parent, false))
    }

    override fun onBindHeaderViewHolder(holder: StickyHeaderGridAdapter.HeaderViewHolder, section: Int) {
        val insta = sections[section]
        val header = holder as HeaderViewHolder

        header.instaHeader.text = insta.alpha
    }

    override fun onBindItemViewHolder(viewHolder: StickyHeaderGridAdapter.ItemViewHolder?, section: Int, offset: Int) {
        val insta = sections[section]
        val holder = viewHolder as ItemViewHolder
        val item = insta.instaEntity[offset]

        holder.instaID.text = item.id.toString()
        holder.imageClear.scaleType = ImageView.ScaleType.CENTER_CROP
        holder.instaProgress.max = 100.toFloat()
        holder.instaProgress.progress = 36.toFloat()
        if (item.localUrl.isEmpty()){
            if (item.type.equals("Video", true)){
                Glide.with(context).load(item.videoUrl).into(holder.imageClear)
                holder.instaType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_video, null)!!)
            }else {
                Glide.with(context).load(item.imageUrl).into(holder.imageClear)
                holder.instaType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_image, null)!!)
            }
        }else{
            Glide.with(context).load(item.localUrl).into(holder.imageClear)
        }

        val outputfile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager/insta")
        if (!outputfile.exists()) {
            outputfile.mkdirs()
        }
    }

    fun setInsta(instaEntities: List<InstaEntity>) {
        this.originalModel = instaEntities
        this.instaEntity = instaEntities
        sections.clear()

        var alpha: String
        var secAlpha = ""
        var currentSection: Section? = null
        for (insta in instaEntities) {
            alpha = insta.datecreated
            if (secAlpha.isEmpty()){
                secAlpha = insta.datecreated

                currentSection = Section()
                currentSection.alpha = insta.datecreated
                currentSection.instaEntity.add(insta)
            }else if (secAlpha.equals(alpha, true)){
                currentSection!!.instaEntity.add(insta)
            }else{
                if (currentSection != null) {
                    sections.add(currentSection)
                }
                secAlpha = insta.datecreated

                currentSection = Section()
                currentSection.alpha = insta.datecreated
                currentSection.instaEntity.add(insta)
            }
        }

        notifyAllSectionsDataSetChanged()
    }

    override fun getSectionCount(): Int {
        return sections.size
    }

    override fun getSectionItemCount(sectionIndex: Int): Int {
        return sections[sectionIndex].instaEntity.size
    }

}