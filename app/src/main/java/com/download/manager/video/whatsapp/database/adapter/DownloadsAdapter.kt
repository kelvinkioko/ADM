package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import kotlinx.android.synthetic.main.item_download.view.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList

class DownloadsAdapter (private val context: Context, private var downloadsEntity: List<DownloadsEntity>) :
    RecyclerView.Adapter<DownloadsAdapter.ModuleHolder>(), Filterable {

    private lateinit var downloader: Downloader
    private var originalModel: List<DownloadsEntity> = downloadsEntity
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
        holder.idDetails.text = item.downloaded + "/" + item.size

        holder.idProgress.max = 100
        if (item.downloaded.toInt() != 0 || item.size.toInt() != 0)
        holder.idProgress.progress = (item.downloaded.toInt()/item.size.toInt()) * 100

        val outputfile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager")
        if (!outputfile.exists()) {
            outputfile.mkdirs()
        }

        holder.idDownload.setOnClickListener {
            downloader = Downloader.Builder(context, item.url).downloadListener(object : OnDownloadListener {
                override fun onStart() {
                    Log.e("Download status", "Started")
                    holder.idPause.visibility = View.VISIBLE
                    holder.idCancel.visibility = View.VISIBLE
                    holder.idDownload.visibility = View.GONE
                    holder.idError.visibility = View.GONE
                    holder.idSuccess.visibility = View.GONE
                }

                override fun onPause() {
                    Log.e("Download status", "Paused")
                    holder.idPause.visibility = View.GONE
                    holder.idCancel.visibility = View.VISIBLE
                    holder.idDownload.visibility = View.VISIBLE
                    holder.idError.visibility = View.GONE
                    holder.idSuccess.visibility = View.GONE
                }

                override fun onResume() {
                    Log.e("Download status", "Resumed")
                    holder.idPause.visibility = View.VISIBLE
                    holder.idCancel.visibility = View.VISIBLE
                    holder.idDownload.visibility = View.GONE
                    holder.idError.visibility = View.GONE
                    holder.idSuccess.visibility = View.GONE
                }

                override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {
                    holder.idProgress.max = totalSize
                    holder.idProgress.progress = downloadedSize
                    holder.idDetails.text = percent.toString().plus("%")
                }

                override fun onCompleted(file: File?) {
                    Log.e("Download status", "Complete")
                    holder.idPause.visibility = View.GONE
                    holder.idCancel.visibility = View.GONE
                    holder.idDownload.visibility = View.GONE
                    holder.idError.visibility = View.VISIBLE
                    holder.idSuccess.visibility = View.GONE
                    Log.d("TAG COMPLETE", "onCompleted: file --> $file")
                    val final = File((Environment.getExternalStorageDirectory()).toString() + File.separator + "Download Manager")

                    file?.copyTo(final)
                    file?.delete()
                }

                override fun onFailure(reason: String?) {
                    Log.e("Download status", "Failed")
                    Log.e("Download status", reason)
                    // Log.d(TAG, "onFailure: reason --> $reason")
                    holder.idPause.visibility = View.GONE
                    holder.idCancel.visibility = View.GONE
                    holder.idDownload.visibility = View.VISIBLE
                    holder.idError.visibility = View.VISIBLE
                    holder.idSuccess.visibility = View.GONE
                }

                override fun onCancel() {
                    Log.e("Download status", "Cancelled")
                    holder.idPause.visibility = View.GONE
                    holder.idCancel.visibility = View.GONE
                    holder.idDownload.visibility = View.VISIBLE
                    holder.idError.visibility = View.GONE
                    holder.idSuccess.visibility = View.GONE
                }
            }).build()
            downloader.download()
        }

        holder.idPause.setOnClickListener {
            downloader.pauseDownload()
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
}