package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.util.Log
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
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.util.ArrayList

class InstaAdapter (private val context: Context, private var instaEntity: List<InstaEntity>) : StickyHeaderGridAdapter() {

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
        val instaPending = view.insta_pending

        /**
         * Actions loaded below
         */
        val instaDownload = view.insta_download
        val instaProgress = view.insta_progress
        val instaPlay = view.insta_play
        val instaPause = view.insta_pause
        val instaCancel = view.insta_cancel
        val instaError = view.insta_error
        val instaSuccess = view.insta_success
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
            holder.instaPending.visibility = View.GONE
        }

        val videofile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "insta" + File.separator + "videos")
        val imagefile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "insta" + File.separator + "images")
        if (!videofile.exists()) { videofile.mkdirs() }
        if (!imagefile.exists()) { imagefile.mkdirs() }

        val url = if (item.type.equals("video", true)){ item.videoUrl }else{ item.imageUrl }

        holder.instaProgress.max = 100.toFloat()
        if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
            holder.instaProgress.visibility = View.VISIBLE
            holder.instaProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()

            holder.instaPause.visibility = View.GONE
            holder.instaPlay.visibility = View.VISIBLE
            holder.instaCancel.visibility = View.VISIBLE
            holder.instaDownload.visibility = View.GONE
            holder.instaError.visibility = View.GONE
            holder.instaSuccess.visibility = View.GONE
        }else{
            holder.instaProgress.visibility = View.GONE

            holder.instaPause.visibility = View.GONE
            holder.instaPlay.visibility = View.GONE
            holder.instaCancel.visibility = View.VISIBLE
            holder.instaDownload.visibility = View.VISIBLE
            holder.instaError.visibility = View.GONE
            holder.instaSuccess.visibility = View.GONE
        }

        holder.instaDownload.setOnClickListener {
            downloader = Downloader.Builder(context, url).downloadListener(object : OnDownloadListener {
                override fun onStart() {
                    Log.e("Download status", "Started")
                    handler.post {
                        holder.instaPause.visibility = View.VISIBLE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.VISIBLE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                        holder.instaProgress.visibility = View.VISIBLE
                    }
                }

                override fun onPause() {
                    Log.e("Download status", "Paused")
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.VISIBLE
                        holder.instaCancel.visibility = View.VISIBLE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }

                override fun onResume() {
                    Log.e("Download status", "Resumed")
                    handler.post {
                        holder.instaPause.visibility = View.VISIBLE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.VISIBLE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }

                override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {
                    handler.post {
                        holder.instaProgress.max = totalSize.toFloat()
                        holder.instaProgress.progress = downloadedSize.toFloat()
                        DatabaseApp().getInstaDao(context).updateInsta(downloadedSize.toString(), totalSize.toString(), item.id)
                    }
                }

                override fun onCompleted(file: File?) {
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.GONE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.VISIBLE
                    }

                    val final = if (item.type.equals("video", true)){
                        File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "insta" + File.separator + "videos" + File.separator + file?.name)
                    }else{
                        File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "insta" + File.separator + "images" + File.separator + file?.name)
                    }

                    if (!final.parentFile.exists()) { final.parentFile.mkdirs() }
                    if (!final.exists()) { final.createNewFile() }

                    var source: FileChannel? = null
                    var destination: FileChannel? = null

                    try {
                        source = FileInputStream(file).channel
                        destination = FileOutputStream(final).channel
                        destination!!.transferFrom(source, 0, source!!.size())
                    } finally {
                        file?.delete()
                        source?.close()
                        destination?.close()
                    }

                    DatabaseApp().getInstaDao(context).updateInsta(final.length().toString(), final.length().toString(), item.id)
                    DatabaseApp().getInstaDao(context).updateLocalURL(final.toString(), item.id)
                    DatabaseApp().getInstaDao(context).updateName(final.name, item.id)

                    if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
                        holder.instaProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()
                    }
                    instaEntity = DatabaseApp().getInstaDao(context).getInstaList()
                    notifyDataSetChanged()
                }

                override fun onFailure(reason: String?) {
                    Log.e("Download status", "Failed")
                    Log.e("Download status", reason)
                    // Log.d(TAG, "onFailure: reason --> $reason")
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.GONE
                        holder.instaDownload.visibility = View.VISIBLE
                        holder.instaError.visibility = View.VISIBLE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }

                override fun onCancel() {
                    Log.e("Download status", "Cancelled")
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.GONE
                        holder.instaDownload.visibility = View.VISIBLE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }
            }).build()
            downloader.download()
        }

        holder.instaPause.setOnClickListener {
            downloader.pauseDownload()
        }

        holder.instaPlay.setOnClickListener {
            downloader = Downloader.Builder(context, url).downloadListener(object : OnDownloadListener {
                override fun onStart() {
                    Log.e("Download status", "Started")
                    handler.post {
                        holder.instaPause.visibility = View.VISIBLE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.VISIBLE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                        holder.instaProgress.visibility = View.VISIBLE
                    }
                }

                override fun onPause() {
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.VISIBLE
                        holder.instaCancel.visibility = View.VISIBLE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }

                override fun onResume() {
                    handler.post {
                        holder.instaPause.visibility = View.VISIBLE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.VISIBLE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }

                override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {
                    handler.post {
                        holder.instaProgress.max = totalSize.toFloat()
                        holder.instaProgress.progress = downloadedSize.toFloat()
                        DatabaseApp().getInstaDao(context).updateInsta(downloadedSize.toString(), totalSize.toString(), item.id)
                    }
                }

                override fun onCompleted(file: File?) {
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.GONE
                        holder.instaDownload.visibility = View.GONE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.VISIBLE
                    }

                    val final = if (item.type.equals("video", true)){
                        File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "insta" + File.separator + "videos" + File.separator + file?.name)
                    }else{
                        File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "insta" + File.separator + "images" + File.separator + file?.name)
                    }

                    if (!final.parentFile.exists()) { final.parentFile.mkdirs() }
                    if (!final.exists()) { final.createNewFile() }

                    var source: FileChannel? = null
                    var destination: FileChannel? = null

                    try {
                        source = FileInputStream(file).channel
                        destination = FileOutputStream(final).channel
                        destination!!.transferFrom(source, 0, source!!.size())
                    } finally {
                        file?.delete()
                        source?.close()
                        destination?.close()
                    }

                    DatabaseApp().getInstaDao(context).updateInsta(final.length().toString(), final.length().toString(), item.id)
                    DatabaseApp().getInstaDao(context).updateLocalURL(final.toString(), item.id)
                    DatabaseApp().getInstaDao(context).updateName(final.name, item.id)

                    if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
                        holder.instaProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()
                    }

                    instaEntity = DatabaseApp().getInstaDao(context).getInstaList()
                    notifyDataSetChanged()
                }

                override fun onFailure(reason: String?) {
                    Log.e("Download status", reason)
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.GONE
                        holder.instaDownload.visibility = View.VISIBLE
                        holder.instaError.visibility = View.VISIBLE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }

                override fun onCancel() {
                    handler.post {
                        holder.instaPause.visibility = View.GONE
                        holder.instaPlay.visibility = View.GONE
                        holder.instaCancel.visibility = View.GONE
                        holder.instaDownload.visibility = View.VISIBLE
                        holder.instaError.visibility = View.GONE
                        holder.instaSuccess.visibility = View.GONE
                    }
                }
            }).build()
            downloader.resumeDownload()
        }

        holder.instaCancel.setOnClickListener {
            downloader.cancelDownload()
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
            when {
                secAlpha.isEmpty() -> {
                    secAlpha = insta.datecreated

                    currentSection = Section()
                    currentSection.alpha = insta.datecreated
                    currentSection.instaEntity.add(insta)
                    if (currentSection != null) {
                        sections.add(currentSection)
                    }
                }
                secAlpha.equals(alpha, true) -> currentSection!!.instaEntity.add(insta)
                else -> {
                    if (currentSection != null) {
                        sections.add(currentSection)
                    }
                    secAlpha = insta.datecreated

                    currentSection = Section()
                    currentSection.alpha = insta.datecreated
                    currentSection.instaEntity.add(insta)
                }
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