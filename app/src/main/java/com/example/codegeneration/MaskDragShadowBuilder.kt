package com.example.codegeneration

import android.graphics.Canvas
import android.graphics.Point
import android.view.View
import androidx.core.content.res.ResourcesCompat

class MaskDragShadowBuilder(view:View):View.DragShadowBuilder(view) {
    private val shadow = ResourcesCompat.getDrawable(view.context.resources, R.drawable.magic_circle, view.context.theme)
    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        val width: Int = view.width
        val height: Int = view.height
        shadow?.setBounds(0, 0, width, height)
        size.set(width, height)
        touch.set(width / 2, height / 2)
    }

    override fun onDrawShadow(canvas: Canvas) {
        shadow?.draw(canvas)
    }
}