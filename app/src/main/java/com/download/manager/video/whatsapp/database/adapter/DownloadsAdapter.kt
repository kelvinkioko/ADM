package com.download.manager.video.whatsapp.database.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import com.download.manager.video.whatsapp.widgets.SectioningAdapter
import kotlinx.android.synthetic.main.item_download.view.*
import kotlinx.android.synthetic.main.item_insta_header.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.*

class DownloadsAdapter (private val context: Context, private var downloadsEntity: List<DownloadsEntity>) : SectioningAdapter() {

    private lateinit var downloader: Downloader
    private val sections = ArrayList<Section>()
    private var originalModel: List<DownloadsEntity> = downloadsEntity

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
        var downloadsEntity: ArrayList<DownloadsEntity> = ArrayList()
    }

    inner class ItemViewHolder (view: View) : SectioningAdapter.ItemViewHolder(view) {
        val idName = view.id_name
        val idDetails = view.id_details
        val idID = view.id_id
        val idProgress = view.id_downloadprogress

        val idDownload = view.id_download
        val idPlay = view.id_play
        val idPause = view.id_pause
        val idCancel = view.id_cancel
        val idError = view.id_error
        val idSuccess = view.id_success
    }

    inner class HeaderViewHolder (headerView: View) : SectioningAdapter.HeaderViewHolder(headerView) {
        val instaHeader = headerView.insta_header
    }

    override fun getNumberOfSections(): Int { return sections.size }

    override fun getNumberOfItemsInSection(sectionIndex: Int): Int { return sections[sectionIndex].downloadsEntity.size }

    override fun doesSectionHaveHeader(sectionIndex: Int): Boolean { return true }

    override fun doesSectionHaveFooter(sectionIndex: Int): Boolean { return false }

    override fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false))
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): HeaderViewHolder {
        return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_insta_header, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindItemViewHolder(viewHolder: SectioningAdapter.ItemViewHolder, sectionIndex: Int, itemIndex: Int, itemType: Int) {
        val insta = sections[sectionIndex]
        val holder = viewHolder as ItemViewHolder
        val item = insta.downloadsEntity[itemIndex]

        holder.idID.text = item.id.toString()
        holder.idName.text = item.name

        holder.idProgress.max = 100.toFloat()
        if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
            holder.idDetails.text = getFileSize(item.downloaded.toLong()) + "/" + getFileSize(item.size.toLong())
            holder.idProgress.visibility = View.VISIBLE
            holder.idProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()

            holder.idPause.visibility = View.GONE
            holder.idPlay.visibility = View.VISIBLE
            holder.idCancel.visibility = View.VISIBLE
            holder.idDownload.visibility = View.GONE
            holder.idError.visibility = View.GONE
            holder.idSuccess.visibility = View.GONE
        }else{
            holder.idProgress.visibility = View.GONE
            holder.idDetails.text = "Pending. Tap the download button to start downloading"

            holder.idPause.visibility = View.GONE
            holder.idPlay.visibility = View.GONE
            holder.idCancel.visibility = View.VISIBLE
            holder.idDownload.visibility = View.VISIBLE
            holder.idError.visibility = View.GONE
            holder.idSuccess.visibility = View.GONE
        }

        val outputfile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager")
        if (!outputfile.exists()) {
            outputfile.mkdirs()
        }

        if (item.localurl.isNotEmpty()){
            val fileChecker = File(item.localurl)
            if (fileChecker.exists()) {
                holder.idPause.visibility = View.GONE
                holder.idPlay.visibility = View.GONE
                holder.idCancel.visibility = View.GONE
                holder.idDownload.visibility = View.GONE
                holder.idError.visibility = View.GONE
                holder.idSuccess.visibility = View.VISIBLE
                holder.idProgress.progress = 100.toFloat()
            }
        }

        holder.idDownload.setOnClickListener {
            downloader = Downloader.Builder(context, item.url).downloadListener(object : OnDownloadListener {
                override fun onStart() {
                    Log.e("Download status", "Started")
                    handler.post {
                        holder.idPause.visibility = View.VISIBLE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.VISIBLE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                        holder.idProgress.visibility = View.VISIBLE
                    }
                }

                override fun onPause() {
                    Log.e("Download status", "Paused")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.VISIBLE
                        holder.idCancel.visibility = View.VISIBLE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                        holder.idDetails.text = holder.idDetails.text.toString().trim().replace("Downloading ...", "Paused ...")
                    }
                }

                override fun onResume() {
                    Log.e("Download status", "Resumed")
                    handler.post {
                        holder.idPause.visibility = View.VISIBLE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.VISIBLE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                    }
                }

                override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {
                    handler.post {
                        holder.idProgress.max = totalSize.toFloat()
                        holder.idProgress.progress = downloadedSize.toFloat()
                        holder.idDetails.text = "Downloading ..." + percent.toString().plus("%")
                        DatabaseApp().getDownloadsDao(context).updateDownloads(downloadedSize.toString(), totalSize.toString(), item.id)
                    }
                }

                override fun onCompleted(file: File?) {
                    Log.e("Download status", "Complete")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.GONE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.VISIBLE
                        Log.e("TAG COMPLETE", "onCompleted: file --> $file")
                    }
                    val final = File((Environment.getExternalStorageDirectory()).toString() + File.separator + "Download Manager" + File.separator + file?.name)

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

                    DatabaseApp().getDownloadsDao(context).updateDownloads(final.length().toString(), final.length().toString(), item.id)
                    DatabaseApp().getDownloadsDao(context).updateLocalURL(final.toString(), item.id)
                    DatabaseApp().getDownloadsDao(context).updateName(final.name, item.id)

                    if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
                        holder.idDetails.text = "Download Complete " + getFileSize(item.downloaded.toLong()) + "/" + getFileSize(item.size.toLong())
                        holder.idProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()
                    }
                    downloadsEntity = DatabaseApp().getDownloadsDao(context).getDownloadsList()
                    notifyDataSetChanged()
                }

                override fun onFailure(reason: String?) {
                    Log.e("Download status", "Failed")
                    Log.e("Download status", reason)
                    // Log.d(TAG, "onFailure: reason --> $reason")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.GONE
                        holder.idDownload.visibility = View.VISIBLE
                        holder.idError.visibility = View.VISIBLE
                        holder.idSuccess.visibility = View.GONE
                    }
                }

                override fun onCancel() {
                    Log.e("Download status", "Cancelled")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.GONE
                        holder.idDownload.visibility = View.VISIBLE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                    }
                }
            }).build()
            downloader.download()
        }

        holder.idPause.setOnClickListener {
            downloader.pauseDownload()
        }

        holder.idPlay.setOnClickListener {
            downloader = Downloader.Builder(context, item.url).downloadListener(object : OnDownloadListener {
                override fun onStart() {
                    Log.e("Download status", "Started")
                    handler.post {
                        holder.idPause.visibility = View.VISIBLE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.VISIBLE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                        holder.idProgress.visibility = View.VISIBLE
                    }
                }

                override fun onPause() {
                    Log.e("Download status", "Paused")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.VISIBLE
                        holder.idCancel.visibility = View.VISIBLE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                        holder.idDetails.text = holder.idDetails.text.toString().trim().replace("Downloading ...", "Paused ...")
                    }
                }

                override fun onResume() {
                    Log.e("Download status", "Resumed")
                    handler.post {
                        holder.idPause.visibility = View.VISIBLE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.VISIBLE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                    }
                }

                override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {
                    handler.post {
                        holder.idProgress.max = totalSize.toFloat()
                        holder.idProgress.progress = downloadedSize.toFloat()
                        holder.idDetails.text = "Downloading ..." + percent.toString().plus("%")
                        DatabaseApp().getDownloadsDao(context).updateDownloads(downloadedSize.toString(), totalSize.toString(), item.id)
                    }
                }

                override fun onCompleted(file: File?) {
                    Log.e("Download status", "Complete")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.GONE
                        holder.idDownload.visibility = View.GONE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.VISIBLE
                        Log.e("TAG COMPLETE", "onCompleted: file --> $file")
                    }
                    val final = File((Environment.getExternalStorageDirectory()).toString() + File.separator + "Download Manager" + File.separator + file?.name)

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

                    DatabaseApp().getDownloadsDao(context).updateDownloads(final.length().toString(), final.length().toString(), item.id)
                    DatabaseApp().getDownloadsDao(context).updateLocalURL(final.toString(), item.id)
                    DatabaseApp().getDownloadsDao(context).updateName(final.name, item.id)

                    if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
                        holder.idDetails.text = "Download Complete " + getFileSize(item.downloaded.toLong()) + "/" + getFileSize(item.size.toLong())
                        holder.idProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()
                    }
                    downloadsEntity = DatabaseApp().getDownloadsDao(context).getDownloadsList()
                    notifyDataSetChanged()
                }

                override fun onFailure(reason: String?) {
                    Log.e("Download status", "Failed")
                    Log.e("Download status", reason)
                    // Log.d(TAG, "onFailure: reason --> $reason")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.GONE
                        holder.idDownload.visibility = View.VISIBLE
                        holder.idError.visibility = View.VISIBLE
                        holder.idSuccess.visibility = View.GONE
                    }
                }

                override fun onCancel() {
                    Log.e("Download status", "Cancelled")
                    handler.post {
                        holder.idPause.visibility = View.GONE
                        holder.idPlay.visibility = View.GONE
                        holder.idCancel.visibility = View.GONE
                        holder.idDownload.visibility = View.VISIBLE
                        holder.idError.visibility = View.GONE
                        holder.idSuccess.visibility = View.GONE
                    }
                }
            }).build()
            downloader.resumeDownload()
        }

        holder.idCancel.setOnClickListener {
            downloader.cancelDownload()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindHeaderViewHolder(holder: SectioningAdapter.HeaderViewHolder, sectionIndex: Int, headerType: Int) {
        val insta = sections[sectionIndex]
        val header = holder as HeaderViewHolder

        header.instaHeader.text = insta.alpha
    }

    fun setDownloads(instaEntities: List<DownloadsEntity>) {
        this.originalModel = instaEntities
        this.downloadsEntity = instaEntities
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
                currentSection.downloadsEntity.add(insta)
            }else if (secAlpha.equals(alpha, true)){
                currentSection!!.downloadsEntity.add(insta)
            }else{
                if (currentSection != null) {
                    sections.add(currentSection)
                }
                secAlpha = insta.datecreated

                currentSection = Section()
                currentSection.alpha = insta.datecreated
                currentSection.downloadsEntity.add(insta)
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