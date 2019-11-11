package com.download.manager.video.whatsapp.database.adapter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.NotificationCompat
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
import kotlinx.android.synthetic.main.item_header.view.*
import com.bumptech.glide.Glide
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.ui.MainActivity
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
        fun parentClick(localUrl: String, type: String)
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
        return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false))
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
        if (item.type.equals("Video", true)){
            holder.instaType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_video, null)!!)
        }else {
            holder.instaType.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_image, null)!!)
        }

        if (item.localUrl.isEmpty()){
            Glide.with(context).load(item.liveUrl).into(holder.imageClear)
        }else{
            Glide.with(context).load(item.localUrl).into(holder.imageClear)
            holder.instaPending.visibility = View.GONE
        }

        val fileLocation = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Android Download Manager" + File.separator + "Instagram ADM")
        if (!fileLocation.exists()) { fileLocation.mkdirs() }

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
            downloadFile(holder, item.liveUrl, item)
            downloader.download()
        }

        holder.instaPause.setOnClickListener {
            downloadFile(holder, item.liveUrl, item)
            if (downloader != null) {
                downloader.pauseDownload()
            }
        }

        holder.instaPlay.setOnClickListener {
            downloadFile(holder, item.liveUrl, item)
            downloader.resumeDownload()
        }

        holder.instaCancel.setOnClickListener {
            downloadFile(holder, item.liveUrl, item)
            if (downloader != null) {
                downloader.cancelDownload()
            }
        }

        holder.imageClear.setOnClickListener {
            if (item.localUrl.isNotEmpty()) {
                clickListener.parentClick(item.localUrl, item.type)
            }
        }
    }

    fun downloadFile(viewHolder: StickyHeaderGridAdapter.ItemViewHolder?, url: String, item: InstaEntity){
        val holder = viewHolder as ItemViewHolder
        downloader = Downloader.Builder(context, url, "Insta:" + item.type).downloadListener(object : OnDownloadListener {
            override fun onStart() {
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

                val final = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Android Download Manager" + File.separator + "Instagram ADM" + File.separator + file?.name)


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
                notifyDownloadComplete()
                notifyDataSetChanged()
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(final)
                context.sendBroadcast(mediaScanIntent)
            }

            override fun onFailure(reason: String?) {
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
                    secAlpha = insta.datecreated

                    currentSection = Section()
                    currentSection.alpha = insta.datecreated
                    currentSection.instaEntity.add(insta)

                    if (currentSection != null) {
                        sections.add(currentSection)
                    }
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

    private fun notifyDownloadComplete(){
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("status", "1")
        val title = "Download complete!"
        val text = "Your IG download is complete. Tap to view"

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

    private fun notify(notification : Notification){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("DownloadManager1292", "Download complete!", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify("Download Manager", 0, notification)
    }

}