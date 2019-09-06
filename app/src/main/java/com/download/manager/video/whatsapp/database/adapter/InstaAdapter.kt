package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.widgets.blurview.FastBlurUtil
import kotlinx.android.synthetic.main.item_insta.view.*
import java.util.ArrayList
import java.io.*
import java.text.DecimalFormat
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.download.manager.video.whatsapp.R
import java.net.HttpURLConnection
import java.net.URL


class InstaAdapter (private val context: Context, private var instaEntity: List<InstaEntity>) :
    RecyclerView.Adapter<InstaAdapter.ModuleHolder>(), Filterable {

    private lateinit var downloader: Downloader
    private var originalModel: List<InstaEntity> = instaEntity

    private val handler: Handler = Handler()

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
                val results = ArrayList<InstaEntity>()
                if (constraint != null) {
                    if (originalModel.isNotEmpty()) {
                        for (cd in originalModel) {
                            when {
                                cd.name.toLowerCase().contains(constraint.toString()) -> results.add(cd)
                            }
                        }
                    }
                    oReturn.values = results
                }

                return oReturn
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                instaEntity = results.values as ArrayList<InstaEntity>
                notifyDataSetChanged()
            }
        }
    }

    inner class ModuleHolder (view: View) : RecyclerView.ViewHolder(view) {
        val imageClear = view.insta_image_clear
        val instaType = view.insta_type
        val instaID = view.insta_id

        /**
         * Actions loaded below
         */
        val instaDownload = view.insta_download
        val instaProgress = view.insta_progress
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_insta, parent, false)

        return ModuleHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleHolder, position: Int) {
        val item = instaEntity[position]

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
        }

        val outputfile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager/insta")
        if (!outputfile.exists()) {
            outputfile.mkdirs()
        }
    }

    override fun getItemCount(): Int {
        return instaEntity.size
    }

    fun setInsta(instaEntitys: List<InstaEntity>) {
        originalModel = instaEntitys
        instaEntity = instaEntitys
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