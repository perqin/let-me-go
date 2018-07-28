package com.perqin.letmego.utils

import com.tencent.tencentmap.mapsdk.maps.model.TencentMapGestureListener

/**
 * @author perqin
 */
open class TencentMapGestureAdapter : TencentMapGestureListener {
    override fun onDown(p0: Float, p1: Float) = false

    override fun onDoubleTap(p0: Float, p1: Float) = false

    override fun onFling(p0: Float, p1: Float) = false

    override fun onSingleTap(p0: Float, p1: Float) = false

    override fun onScroll(p0: Float, p1: Float) = false

    override fun onMapStable() {
    }

    override fun onUp(p0: Float, p1: Float) = false

    override fun onLongPress(p0: Float, p1: Float) = false
}
