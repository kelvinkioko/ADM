package com.download.manager.video.whatsapp.database.adapter

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Environment
import android.os.Handler
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
import kotlinx.android.synthetic.main.item_header.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.*
import android.app.PendingIntent
import com.download.manager.video.whatsapp.ui.MainActivity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.core.app.NotificationCompat

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
        fun parentClick(localUrl: String)
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
        val idImageClear = view.id_image_clear
        val idParent = view.id_parent

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
        return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindItemViewHolder(viewHolder: SectioningAdapter.ItemViewHolder, sectionIndex: Int, itemIndex: Int, itemType: Int) {
        val insta = sections[sectionIndex]
        val holder = viewHolder as ItemViewHolder
        val item = insta.downloadsEntity[itemIndex]

        holder.idID.text = item.id.toString()
        holder.idName.text = item.name
        if (item.localurl.isNotEmpty()){
            val fileLoc = File(item.localurl)
            if (fileLoc.exists()) {
                when {
                    item.localurl.contains(".mp4") -> {
                        val density = context.resources.displayMetrics.density
                        val paddingPixel = (8 * density).toInt()
                        holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                        holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_mp4,null ))
                    }
                    item.localurl.contains(".jpeg") -> {
                        val density = context.resources.displayMetrics.density
                        val paddingPixel = (8 * density).toInt()
                        holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                        holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_jpeg,null ))
                    }
                    item.localurl.contains(".jpg") -> {
                        val density = context.resources.displayMetrics.density
                        val paddingPixel = (8 * density).toInt()
                        holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                        holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_jpeg,null ))
                    }
                    item.localurl.contains(".mp3") -> {
                        val density = context.resources.displayMetrics.density
                        val paddingPixel = (8 * density).toInt()
                        holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                        holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_mp3,null ))
                    }
                }

                holder.idPause.visibility = View.GONE
                holder.idPlay.visibility = View.GONE
                holder.idCancel.visibility = View.VISIBLE
                holder.idDownload.visibility = View.GONE
                holder.idError.visibility = View.GONE
                holder.idSuccess.visibility = View.GONE
                holder.idProgress.progress = 100.toFloat()

                holder.idDetails.text = item.datecreated + ", " + getFileSize(fileLoc.length())
            }else {
                holder.idDetails.text = item.datecreated
            }
        }else {
            when {
                item.url.contains(".mp4") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_mp4,null ))
                }
                item.url.contains(".jpeg") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_jpeg,null ))
                }
                item.url.contains(".jpg") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_jpeg,null ))
                }
                item.url.contains(".mp3") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.idImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.idImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_mp3,null ))
                }
            }

            holder.idProgress.max = 100.toFloat()
            if (item.downloaded.toInt() != 0 && item.size.toInt() != 0) {
                holder.idDetails.text = getFileSize(item.downloaded.toLong()) + "/" + getFileSize(item.size.toLong())
                holder.idProgress.visibility = View.VISIBLE
                holder.idProgress.progress = ((item.downloaded.toInt() / item.size.toInt()) * 100).toFloat()

                holder.idPause.visibility = View.GONE
                holder.idPlay.visibility = View.VISIBLE
                holder.idCancel.visibility = View.VISIBLE
                holder.idDownload.visibility = View.GONE
                holder.idError.visibility = View.GONE
                holder.idSuccess.visibility = View.GONE
            } else {
                holder.idProgress.visibility = View.GONE
                holder.idDetails.text = "Tap download to start downloading"

                holder.idPause.visibility = View.GONE
                holder.idPlay.visibility = View.GONE
                holder.idCancel.visibility = View.VISIBLE
                holder.idDownload.visibility = View.VISIBLE
                holder.idError.visibility = View.GONE
                holder.idSuccess.visibility = View.GONE
            }
        }

        val outputfile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Android Download Manager")
        if (!outputfile.exists()) { outputfile.mkdirs() }

        holder.idDownload.setOnClickListener {
            downloadFile(holder, item)
            downloader.download()
        }

        holder.idPause.setOnClickListener {
            downloader.pauseDownload()
        }

        holder.idPlay.setOnClickListener {
            downloadFile(holder, item)
            downloader.resumeDownload()
        }

        holder.idCancel.setOnClickListener {
            DatabaseApp().getDownloadsDao(context).deleteDownloadsByID(item.id)
            setDownloads(DatabaseApp().getDownloadsDao(context).getDownloadsList())
            notifyDataSetChanged()
        }

        holder.idParent.setOnClickListener {
            if (item.localurl.isNotEmpty()){
                clickListener.parentClick(item.localurl)
            }
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
            when {
                secAlpha.isEmpty() -> {
                    secAlpha = insta.datecreated

                    currentSection = Section()
                    currentSection.alpha = insta.datecreated
                    currentSection.downloadsEntity.add(insta)

                    if (currentSection != null) {
                        sections.add(currentSection)
                    }
                }
                secAlpha.equals(alpha, true) -> currentSection!!.downloadsEntity.add(insta)
                else -> {
                    secAlpha = insta.datecreated

                    currentSection = Section()
                    currentSection.alpha = insta.datecreated
                    currentSection.downloadsEntity.add(insta)

                    if (currentSection != null) {
                        sections.add(currentSection)
                    }
                }
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

    private fun downloadFile(viewHolder: SectioningAdapter.ItemViewHolder, item: DownloadsEntity){
        val holder = viewHolder as ItemViewHolder
        downloader = Downloader.Builder(context, item.url, item.name).downloadListener(object : OnDownloadListener {
            override fun onStart() {
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
                notifyDownloadComplete()
                handler.post {
                    holder.idPause.visibility = View.GONE
                    holder.idPlay.visibility = View.GONE
                    holder.idCancel.visibility = View.GONE
                    holder.idDownload.visibility = View.GONE
                    holder.idError.visibility = View.GONE
                    holder.idSuccess.visibility = View.VISIBLE
                }
                val final = File((Environment.getExternalStorageDirectory()).toString() + File.separator + "Android Download Manager" + File.separator + file?.name)

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

                item.localurl = final.toString()

                if (item.downloaded.toInt() != 0 && item.size.toInt() != 0){
                    holder.idDetails.text = "Download Complete " + getFileSize(item.downloaded.toLong()) + "/" + getFileSize(item.size.toLong())
                    holder.idProgress.progress = ((item.downloaded.toInt()/item.size.toInt()) * 100).toFloat()
                }
                downloadsEntity = DatabaseApp().getDownloadsDao(context).getDownloadsList()
                notifyDataSetChanged()

                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(final)
                context.sendBroadcast(mediaScanIntent)
            }

            override fun onFailure(reason: String?) {
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
    }

    private fun notifyDownloadComplete(){
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("status", "1")
        val title = "Download complete!"
        val text = "Your download is complete. Tap to view"

        val google_play_url = "https://play.google.com/store/apps/details?id="

        val msg = context.resources.getString(R.string.share_message) + " "
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg + google_play_url + context.packageName)
        shareIntent.type = "text/plain"

        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.packageName))
        val ratePendingIntent = PendingIntent.getActivity(context, 0, rateIntent, 0)

        val builder = NotificationCompat.Builder(context, "DownloadManager1292")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_settings_rate, "Rate", ratePendingIntent)
            .addAction(R.drawable.ic_settings_rate, "Share", PendingIntent.getActivity(context, 0, Intent.createChooser(shareIntent, "Share..."), PendingIntent.FLAG_UPDATE_CURRENT))
        notify(builder.build())
    }

    private fun notify(notification :Notification){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("DownloadManager1292", "Download complete!", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify("Download Manager", 0, notification)
    }

}