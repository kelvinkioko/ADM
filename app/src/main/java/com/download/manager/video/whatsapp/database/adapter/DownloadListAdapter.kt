package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import kotlinx.android.synthetic.main.item_download_list.view.*
import java.text.DecimalFormat

class DownloadListAdapter (private val context: Context, private var downloadsEntity: List<DownloadsEntity>) :
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
        val listImageClear = view.list_image_clear
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

        if (item.url.isNotEmpty()) {
            when {
                item.url.contains(".mp4") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.listImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.listImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_mp4,null ))
                }
                item.url.contains(".jpeg") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.listImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.listImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_jpeg,null ))
                }
                item.url.contains(".jpg") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.listImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.listImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_jpeg,null ))
                }
                item.url.contains(".mp3") -> {
                    val density = context.resources.displayMetrics.density
                    val paddingPixel = (8 * density).toInt()
                    holder.listImageClear.setPadding(0, paddingPixel, 0, paddingPixel)
                    holder.listImageClear.setImageDrawable(VectorDrawableCompat.create(context.resources, R.drawable.icon_file_mp3,null ))
                }
            }
        }

        if (DatabaseApp().getDownloadsDao(context).countDownload(item.url) > 0){
            holder.listDownload.visibility = View.GONE
            holder.listSuccess.visibility = View.VISIBLE
        }else{
            holder.listDownload.visibility = View.VISIBLE
            holder.listSuccess.visibility = View.GONE
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