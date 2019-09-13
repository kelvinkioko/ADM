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
import com.download.manager.video.whatsapp.database.entity.FaceEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridAdapter
import kotlinx.android.synthetic.main.item_insta.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import com.bumptech.glide.Glide
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.util.ArrayList

class FaceAdapter (private val context: Context, private var faceEntity: List<FaceEntity>) : StickyHeaderGridAdapter() {

    private lateinit var downloader: Downloader
    private val sections = ArrayList<Section>()
    private var originalModel: List<FaceEntity> = faceEntity

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
        var faceEntity: ArrayList<FaceEntity> = ArrayList()
    }

    inner class ItemViewHolder (view: View) : StickyHeaderGridAdapter.ItemViewHolder(view) {
        val imageClear = view.insta_image_clear
        val faceType = view.insta_type
        val faceID = view.insta_id
        val facePending = view.insta_pending

        /**
         * Actions loaded below
         */
        val faceDownload = view.insta_download
        val faceProgress = view.insta_progress
        val facePlay = view.insta_play
        val facePause = view.insta_pause
        val faceCancel = view.insta_cancel
        val faceError = view.insta_error
        val faceSuccess = view.insta_success
    }

    inner class HeaderViewHolder (headerView: View) : StickyHeaderGridAdapter.HeaderViewHolder(headerView) {
        val faceHeader = headerView.insta_header
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_insta, parent, false))
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): HeaderViewHolder {
        return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false))
    }

    override fun onBindHeaderViewHolder(holder: StickyHeaderGridAdapter.HeaderViewHolder, section: Int) {
        val face = sections[section]
        val header = holder as HeaderViewHolder

        header.faceHeader.text = face.alpha
    }

    override fun onBindItemViewHolder(viewHolder: StickyHeaderGridAdapter.ItemViewHolder?, section: Int, offset: Int) {
        val face = sections[section]
        val holder = viewHolder as ItemViewHolder
        val item = face.faceEntity[offset]

        holder.faceID.text = item.id.toString()
        holder.imageClear.scaleType = ImageView.ScaleType.CENTER_CROP
        holder.faceProgress.max = 100.toFloat()
        holder.faceProgress.progress = 36.toFloat()
        if (item.localUrl.isEmpty()){
            if (item.type.equals("Video", true)){
                Glide.with(context).load(item.videoUrl).into(holder.imageClear)
                holder.faceType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_video, null)!!)
            }else {
                Glide.with(context).load(item.imageUrl).into(holder.imageClear)
                holder.faceType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_image, null)!!)
            }
        }else{
            Glide.with(context).load(item.localUrl).into(holder.imageClear)
            holder.facePending.visibility = View.GONE
        }

        val videofile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "face" + File.separator + "videos")
        val imagefile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "face" + File.separator + "images")
        if (!videofile.exists()) { videofile.mkdirs() }
        if (!imagefile.exists()) { imagefile.mkdirs() }

        val url = if (item.type.equals("video", true)){ item.videoUrl }else{ item.imageUrl }

        Log.e("Image url", item.imageUrl)
        Log.e("Video url", item.videoUrl)

        if (item.localUrl.isEmpty()){
            downloadFile(holder, url, item)
            downloader.download()
        }

        holder.faceProgress.max = 100.toFloat()
        if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
            holder.faceProgress.visibility = View.VISIBLE
            holder.faceProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()

            holder.facePause.visibility = View.GONE
            holder.facePlay.visibility = View.VISIBLE
            holder.faceCancel.visibility = View.VISIBLE
            holder.faceDownload.visibility = View.GONE
            holder.faceError.visibility = View.GONE
            holder.faceSuccess.visibility = View.GONE
        }
        else{
            holder.faceProgress.visibility = View.GONE

            holder.facePause.visibility = View.GONE
            holder.facePlay.visibility = View.GONE
            holder.faceCancel.visibility = View.VISIBLE
            holder.faceDownload.visibility = View.VISIBLE
            holder.faceError.visibility = View.GONE
            holder.faceSuccess.visibility = View.GONE
        }

        holder.faceDownload.setOnClickListener {
            downloadFile(holder, url, item)
            downloader.download()
        }

        holder.facePause.setOnClickListener {
            downloader.pauseDownload()
        }

        holder.facePlay.setOnClickListener {
            downloadFile(holder, url, item)
            downloader.resumeDownload()
        }

        holder.faceCancel.setOnClickListener {
            downloader.cancelDownload()
        }
    }

    fun downloadFile(viewHolder: StickyHeaderGridAdapter.ItemViewHolder?, url: String, item: FaceEntity){
        val holder = viewHolder as ItemViewHolder
        downloader = Downloader.Builder(context, url).downloadListener(object : OnDownloadListener {
            override fun onStart() {
                Log.e("Download status", "Started")
                handler.post {
                    holder.facePause.visibility = View.VISIBLE
                    holder.facePlay.visibility = View.GONE
                    holder.faceCancel.visibility = View.VISIBLE
                    holder.faceDownload.visibility = View.GONE
                    holder.faceError.visibility = View.GONE
                    holder.faceSuccess.visibility = View.GONE
                    holder.faceProgress.visibility = View.VISIBLE
                }
            }

            override fun onPause() {
                Log.e("Download status", "Paused")
                handler.post {
                    holder.facePause.visibility = View.GONE
                    holder.facePlay.visibility = View.VISIBLE
                    holder.faceCancel.visibility = View.VISIBLE
                    holder.faceDownload.visibility = View.GONE
                    holder.faceError.visibility = View.GONE
                    holder.faceSuccess.visibility = View.GONE
                }
            }

            override fun onResume() {
                Log.e("Download status", "Resumed")
                handler.post {
                    holder.facePause.visibility = View.VISIBLE
                    holder.facePlay.visibility = View.GONE
                    holder.faceCancel.visibility = View.VISIBLE
                    holder.faceDownload.visibility = View.GONE
                    holder.faceError.visibility = View.GONE
                    holder.faceSuccess.visibility = View.GONE
                }
            }

            override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {
                handler.post {
                    holder.faceProgress.max = totalSize.toFloat()
                    holder.faceProgress.progress = downloadedSize.toFloat()
                    DatabaseApp().getFaceDao(context).updateFace(downloadedSize.toString(), totalSize.toString(), item.id)
                }
            }

            override fun onCompleted(file: File?) {
                handler.post {
                    holder.facePause.visibility = View.GONE
                    holder.facePlay.visibility = View.GONE
                    holder.faceCancel.visibility = View.GONE
                    holder.faceDownload.visibility = View.GONE
                    holder.faceError.visibility = View.GONE
                    holder.faceSuccess.visibility = View.VISIBLE
                }

                val final = if (item.type.equals("video", true)){
                    File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "face" + File.separator + "videos" + File.separator + file?.name)
                }else{
                    File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager" + File.separator + "face" + File.separator + "images" + File.separator + file?.name)
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

                DatabaseApp().getFaceDao(context).updateFace(final.length().toString(), final.length().toString(), item.id)
                DatabaseApp().getFaceDao(context).updateLocalURL(final.toString(), item.id)
                DatabaseApp().getFaceDao(context).updateName(final.name, item.id)

                if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
                    holder.faceProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()
                }
                faceEntity = DatabaseApp().getFaceDao(context).getFaceList()
                notifyDataSetChanged()
            }

            override fun onFailure(reason: String?) {
                Log.e("Download status", reason)
                handler.post {
                    holder.facePause.visibility = View.GONE
                    holder.facePlay.visibility = View.GONE
                    holder.faceCancel.visibility = View.GONE
                    holder.faceDownload.visibility = View.VISIBLE
                    holder.faceError.visibility = View.VISIBLE
                    holder.faceSuccess.visibility = View.GONE
                }
            }

            override fun onCancel() {
                Log.e("Download status", "Cancelled")
                handler.post {
                    holder.facePause.visibility = View.GONE
                    holder.facePlay.visibility = View.GONE
                    holder.faceCancel.visibility = View.GONE
                    holder.faceDownload.visibility = View.VISIBLE
                    holder.faceError.visibility = View.GONE
                    holder.faceSuccess.visibility = View.GONE
                }
            }
        }).build()
    }

    fun setFace(faceEntities: List<FaceEntity>) {
        this.originalModel = faceEntities
        this.faceEntity = faceEntities
        sections.clear()

        var alpha: String
        var secAlpha = ""
        var currentSection: Section? = null
        for (face in faceEntities) {
            alpha = face.datecreated
            when {
                secAlpha.isEmpty() -> {
                    secAlpha = face.datecreated

                    currentSection = Section()
                    currentSection.alpha = face.datecreated
                    currentSection!!.faceEntity.add(face)
                    if (currentSection != null) { sections.add(currentSection) }
                }
                secAlpha.equals(alpha, true) -> { currentSection!!.faceEntity.add(face) }
                else -> {
                    secAlpha = face.datecreated

                    currentSection = Section()
                    currentSection.alpha = face.datecreated
                    currentSection!!.faceEntity.add(face)
                    if (currentSection != null) { sections.add(currentSection) }
                }
            }
        }
        notifyAllSectionsDataSetChanged()
    }

    override fun getSectionCount(): Int {
        return sections.size
    }

    override fun getSectionItemCount(sectionIndex: Int): Int {
        return sections[sectionIndex].faceEntity.size
    }

}