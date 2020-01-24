package com.download.manager.video.whatsapp.widgets

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.download.manager.video.whatsapp.widgets.ReadableBottomBar.ItemType

data class BottomBarItem(
    val index: Int,
    val text: String,
    val textSize: Float,
    @ColorInt val textColor: Int,
    val drawable: Drawable,
    val type: ItemType
)