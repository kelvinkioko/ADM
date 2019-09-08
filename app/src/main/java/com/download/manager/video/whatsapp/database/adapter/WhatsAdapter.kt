package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.entity.WhatsEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridAdapter
import kotlinx.android.synthetic.main.item_whats.view.*
import com.bumptech.glide.Glide
import com.download.manager.video.whatsapp.engine.Constants
import kotlinx.android.synthetic.main.item_header.view.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

class WhatsAdapter (private val context: Context, private var whatsEntity: List<WhatsEntity>) : StickyHeaderGridAdapter() {

    private val sections = ArrayList<Section>()
    private var originalModel: List<WhatsEntity> = whatsEntity

    lateinit var clickListener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    interface OnItemClickListener {
        fun parentClick(view: View, position: Int, userCode: String)
    }

    private inner class Section {
        var alpha: String = ""
        var whatsEntity: ArrayList<WhatsEntity> = ArrayList()
    }

    inner class ItemViewHolder (view: View) : StickyHeaderGridAdapter.ItemViewHolder(view) {
        val imageClear = view.whats_image_clear
        val whatsType = view.whats_type
        val whatsID = view.whats_id
        val whatsPending = view.whats_pending

        /**
         * Actions loaded below
         */
        val whatsDownload = view.whats_download
        val whatsProgress = view.whats_progress
    }

    inner class HeaderViewHolder (headerView: View) : StickyHeaderGridAdapter.HeaderViewHolder(headerView) {
        val whatsHeader = headerView.insta_header
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_whats, parent, false))
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): HeaderViewHolder {
        return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false))
    }

    override fun onBindHeaderViewHolder(holder: StickyHeaderGridAdapter.HeaderViewHolder, section: Int) {
        val whats = sections[section]
        val header = holder as HeaderViewHolder

        header.whatsHeader.text = whats.alpha
    }

    override fun onBindItemViewHolder(viewHolder: StickyHeaderGridAdapter.ItemViewHolder?, section: Int, offset: Int) {
        val whats = sections[section]
        val holder = viewHolder as ItemViewHolder
        val item = whats.whatsEntity[offset]

        holder.whatsID.text = item.id.toString()
        holder.imageClear.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(context).load(item.liveUrl).into(holder.imageClear)
        if (item.type.equals("Video", true)){
            holder.whatsType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_video, null)!!)
        }else {
            holder.whatsType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_image, null)!!)
        }

        val whatsFile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "whats")
        if (!whatsFile.exists()) { whatsFile.mkdirs() }
        if (item.localUrl.equals("downloaded", false)){ holder.whatsPending.visibility = View.GONE }

        holder.whatsDownload.setOnClickListener {
            val sourceFile = File(item.liveUrl)
            val destinationFile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "whats" + File.separator + item.name)

            if (!destinationFile.parentFile.exists()) { destinationFile.parentFile.mkdirs() }
            if (!destinationFile.exists()) { destinationFile.createNewFile() }

            var source: FileChannel? = null
            var destination: FileChannel? = null

            try {
                source = FileInputStream(sourceFile).channel
                destination = FileOutputStream(destinationFile).channel
                destination!!.transferFrom(source, 0, source!!.size())
            } finally {
                source?.close()
                destination?.close()
            }

            holder.whatsPending.visibility = View.GONE
        }
    }

    fun setWhats(whatsEntities: List<WhatsEntity>) {
        this.originalModel = whatsEntities
        this.whatsEntity = whatsEntities
        sections.clear()

        var alpha: String
        var secAlpha = ""
        var currentSection: Section? = null
        for (whats in whatsEntities) {
            alpha = whats.datecreated
            when {
                secAlpha.isEmpty() -> {
                    secAlpha = whats.datecreated

                    currentSection = Section()
                    currentSection.alpha = whats.datecreated
                    currentSection.whatsEntity.add(whats)
                    if (currentSection != null) {
                        sections.add(currentSection)
                    }
                }
                secAlpha.equals(alpha, true) -> currentSection!!.whatsEntity.add(whats)
                else -> {
                    if (currentSection != null) {
                        sections.add(currentSection)
                    }
                    secAlpha = whats.datecreated

                    currentSection = Section()
                    currentSection.alpha = whats.datecreated
                    currentSection.whatsEntity.add(whats)
                }
            }
        }
        notifyAllSectionsDataSetChanged()
    }

    override fun getSectionCount(): Int {
        return sections.size
    }

    override fun getSectionItemCount(sectionIndex: Int): Int {
        return sections[sectionIndex].whatsEntity.size
    }

}