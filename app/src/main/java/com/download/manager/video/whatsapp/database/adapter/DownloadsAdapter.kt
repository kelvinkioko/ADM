package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import kotlinx.android.synthetic.main.item_download.view.*
import java.util.ArrayList
import java.io.*
import java.nio.channels.FileChannel
import java.text.DecimalFormat


class DownloadsAdapter (private val context: Context, private var downloadsEntity: List<DownloadsEntity>) :
    RecyclerView.Adapter<DownloadsAdapter.ModuleHolder>(), Filterable {

    private lateinit var downloader: Downloader
    private var originalModel: List<DownloadsEntity> = downloadsEntity

    private val handler: Handler = Handler()
    private var chosenVehicle: String = ""

    lateinit var clickListener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    interface OnItemClickListener {
        fun parentClick(view: View, position: Int, userCode: String)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val oReturn = FilterResults()
                val results = ArrayList<DownloadsEntity>()
                if (constraint != null) {
                    if (originalModel.isNotEmpty()) {
                        for (cd in originalModel) {
                            when {
                                cd.name.toLowerCase().contains(constraint.toString()) -> results.add(cd)
                                cd.url.contains(constraint.toString()) -> results.add(cd)
                            }
                        }
                    }
                    oReturn.values = results
                }

                return oReturn
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                downloadsEntity = results.values as ArrayList<DownloadsEntity>
                notifyDataSetChanged()
            }
        }
    }

    inner class ModuleHolder (view: View) : RecyclerView.ViewHolder(view) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)

        return ModuleHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleHolder, position: Int) {
        val item = downloadsEntity[position]

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

    override fun getItemCount(): Int {
        return downloadsEntity.size
    }

    fun setDownloads(downloadsEntitys: List<DownloadsEntity>) {
        originalModel = downloadsEntitys
        downloadsEntity = downloadsEntitys
        notifyDataSetChanged()
    }

    private fun getFileSize(size: Long): String {
        if (size <= 0)
            return "0"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()

        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}