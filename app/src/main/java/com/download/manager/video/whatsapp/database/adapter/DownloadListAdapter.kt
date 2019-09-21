package com.download.manager.video.whatsapp.database.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import com.download.manager.video.whatsapp.widgets.SectioningAdapter
import kotlinx.android.synthetic.main.item_download.view.*
import kotlinx.android.synthetic.main.item_download_list.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.*
class DownloadListAdapter  (private val context: Context, private var downloadsEntity: List<DownloadsEntity>) :
    RecyclerView.Adapter<DownloadListAdapter.ModuleHolder>() {

    private var originalModel: List<DownloadsEntity> = downloadsEntity
    lateinit var clickListener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    interface OnItemClickListener {
        fun parentClick(view: View, position: Int, userCode: String)
    }

    inner class ModuleHolder (view: View) : RecyclerView.ViewHolder(view) {
        val listParent = view.list_parent
        val listName = view.list_name
        val listDetails = view.list_details
        val listUrl = view.list_url

        val listDownload = view.list_download
        val listSuccess = view.list_success
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_download_list, parent, false)
        return ModuleHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleHolder, position: Int) {
        val item = downloadsEntity[position]

        holder.listUrl.text = item.url
        holder.listDetails.text = getFileSize(item.size.toLong())
        holder.listName.text = item.name

        if (DatabaseApp().getDownloadsDao(context).countDownload(item.url) > 0){
            holder.listDownload.visibility = View.GONE
            holder.listSuccess.visibility = View.VISIBLE
        }else{
            holder.listDownload.visibility = View.VISIBLE
            holder.listSuccess.visibility = View.GONE
        }

        holder.listParent.setOnClickListener {
            item.downloaded = item.size
            holder.listDownload.visibility = View.GONE
            holder.listSuccess.visibility = View.VISIBLE

            val download = DownloadsEntity(0, item.name, item.url, "", "0", item.size, Legion().getCurrentDate())
            DatabaseApp().getDownloadsDao(context).insertDownloads(download)

            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int {
        return downloadsEntity.size
    }

    fun setList(occupantsEntitys: List<DownloadsEntity>) {
        originalModel = occupantsEntitys
        downloadsEntity = occupantsEntitys
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