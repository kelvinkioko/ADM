package com.download.manager.video.whatsapp.database.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import com.download.manager.video.whatsapp.widgets.SectioningAdapter
import kotlinx.android.synthetic.main.item_download.view.*
import kotlinx.android.synthetic.main.item_insta.view.*
import kotlinx.android.synthetic.main.item_insta_header.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.*

class SectionableAdapter (private val context: Context, private var instaEntity: List<InstaEntity>) : SectioningAdapter() {

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

    inner class ItemViewHolder (view: View) : SectioningAdapter.ItemViewHolder(view) {
        val imageClear = view.insta_image_clear
        val instaType = view.insta_type
        val instaID = view.insta_id

        /**
         * Actions loaded below
         */
        val instaDownload = view.insta_download
        val instaProgress = view.insta_progress
    }

    inner class HeaderViewHolder (headerView: View) : SectioningAdapter.HeaderViewHolder(headerView) {
        val instaHeader = headerView.insta_header
    }

    override fun getNumberOfSections(): Int { return sections.size }

    override fun getNumberOfItemsInSection(sectionIndex: Int): Int { return sections[sectionIndex].instaEntity.size }

    override fun doesSectionHaveHeader(sectionIndex: Int): Boolean { return true }

    override fun doesSectionHaveFooter(sectionIndex: Int): Boolean { return false }

    override fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_insta, parent, false))
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): HeaderViewHolder {
        return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_insta_header, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindItemViewHolder(viewHolder: SectioningAdapter.ItemViewHolder, sectionIndex: Int, itemIndex: Int, itemType: Int) {
        val insta = sections[sectionIndex]
        val holder = viewHolder as ItemViewHolder
        val item = insta.instaEntity[itemIndex]

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

    @SuppressLint("SetTextI18n")
    override fun onBindHeaderViewHolder(holder: SectioningAdapter.HeaderViewHolder, sectionIndex: Int, headerType: Int) {
        val insta = sections[sectionIndex]
        val header = holder as HeaderViewHolder

        header.instaHeader.text = insta.alpha
    }

    fun setDownloads(instaEntities: List<InstaEntity>) {
        this.originalModel = instaEntities
        this.instaEntity = instaEntities
        sections.clear()

        var alpha: String
        var currentSection: Section? = null
        for (insta in instaEntities) {
            if (currentSection != null) {
                sections.add(currentSection)
            }
            currentSection = Section()
            alpha = insta.datecreated
            currentSection.alpha = alpha

            if (currentSection != null) {
                currentSection.instaEntity.add(insta)
            }
        }

        notifyAllSectionsDataSetChanged()
    }

    private fun getFileSize(size: Long): String {
        if (size <= 0)
            return "0"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()

        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

}