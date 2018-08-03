package com.perqin.letmego.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes

/**
 * Created by perqin on 2018/08/03.
 */

fun createBitmapFromShapeResource(context: Context, @DrawableRes resId: Int): Bitmap {
    val drawable = context.getDrawable(resId)
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.draw(canvas)
    return bitmap
}
