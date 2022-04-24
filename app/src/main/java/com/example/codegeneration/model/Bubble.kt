package com.example.codegeneration.model

import android.widget.ImageView

data class Bubble(val image: ImageView, var vector: Vector, var isDelete: Boolean = false)

data class Vector(var x: Double, var y: Double)
