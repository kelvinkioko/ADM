package com.download.manager.video.whatsapp.database.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.entity.BookmarkEntity
import kotlinx.android.synthetic.main.item_album.view.*
import kotlinx.android.synthetic.main.item_bookmark.view.*
import java.text.DecimalFormat

class BookmarkAdapter (private val context: Context, private var bookmarkEntity: List<BookmarkEntity>) :
    RecyclerView.Adapter<BookmarkAdapter.ModuleHolder>() {

    private var originalModel: List<BookmarkEntity> = bookmarkEntity
    lateinit var clickListener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    interface OnItemClickListener {
        fun parentClick(view: View, position: Int, userCode: String)
    }

    inner class ModuleHolder (view: View) : RecyclerView.ViewHolder(view) {
        val bookmarkIcon = view.album_cover
        val bookmarkName = view.album_title
        val bookmarkUrl = view.album_url
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return ModuleHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleHolder, position: Int) {
        val item = bookmarkEntity[position]

        holder.bookmarkName.text = item.name
        holder.bookmarkUrl.text = item.url

        when {
            item.name.contains("Google") -> holder.bookmarkIcon.setImageDrawable(context.getDrawable(R.drawable.book_search))
            item.name.contains("Facebook") -> holder.bookmarkIcon.setImageDrawable(context.getDrawable(R.drawable.book_facebook))
            item.name.contains("Twitter") -> holder.bookmarkIcon.setImageDrawable(context.getDrawable(R.drawable.book_twitter))
            item.name.contains("Daily Motion") -> holder.bookmarkIcon.setImageDrawable(context.getDrawable(R.drawable.book_dailymotion))
            item.name.contains("Vimeo") -> holder.bookmarkIcon.setImageDrawable(context.getDrawable(R.drawable.book_vimeo))
            item.name.contains("Tubidy") -> holder.bookmarkIcon.setImageDrawable(context.getDrawable(R.drawable.book_tubidy))
        }

    }

    override fun getItemCount(): Int {
        return bookmarkEntity.size
    }

    fun setList(bookmarkEntitys: List<BookmarkEntity>) {
        originalModel = bookmarkEntitys
        bookmarkEntity = bookmarkEntitys
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