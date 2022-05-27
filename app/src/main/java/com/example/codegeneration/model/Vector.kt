package com.example.codegeneration.model

import kotlin.math.pow
import kotlin.math.sqrt

class Vector(var x: Double, var y: Double) {

    companion object {
        fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double =
            sqrt((x1 - x2).toDouble().pow(2.0) + (y1 - y2).toDouble().pow(2.0))

        fun distance(vector1: Vector, vector2: Vector) = sqrt(
            (vector1.x - vector2.x).pow(2.0) +
                    (vector1.y - vector2.y).pow(2.0)
        )
    }

    private val vectorLength = 2.0
    fun normalize() {
        val lengthInversion = vectorLength / sqrt(this.x.pow(2) + this.y.pow(2))
        this.x *= lengthInversion
        this.y *= lengthInversion
    }
}