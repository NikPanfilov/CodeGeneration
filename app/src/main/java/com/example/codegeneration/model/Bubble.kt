package com.example.codegeneration.model

import android.widget.ImageView
import com.example.codegeneration.model.Vector.Companion.distance
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class Bubble(val image: ImageView, var vector: Vector, var isDelete: Boolean = false) {
    companion object {
        fun changeVectors(bubble1: Bubble, bubble2: Bubble) {
            val center1 = Vector(bubble1.image.x.toDouble(), bubble1.image.y.toDouble())
            val center2 = Vector(bubble2.image.x.toDouble(), bubble2.image.y.toDouble())
            val direction1 = Vector(center1.x + bubble1.vector.x, center1.y + bubble1.vector.y)
            val direction2 = Vector(center2.x + bubble2.vector.x, center2.y + bubble2.vector.y)

            val triangle = Triangle(mutableListOf(center1, center2, direction1))

            var component1 = cos(triangle.getAngle(0))
            component1 *= distance(center1, direction1)
            direction1.y = distance(center1, direction1) * sin(triangle.getAngle(0))

            triangle.point[2] = direction2

            var component2 = -cos(triangle.getAngle(1))
            component2 *= distance(center2, direction2)
            direction2.y = distance(center2, direction2) * sin(triangle.getAngle(1))

            component1 = component2.also { component2 = component1 }

            triangle.point[2] = Vector(center1.x, center2.y)

            val angle = triangle.getAngle(0)

            direction1.x = component1 * cos(180 / (2 * PI))
            direction1.y += component2 * sin(180 / (2 * PI))
            bubble1.vector = direction1

            direction2.x = component2 * cos(angle)
            direction2.y += component2 * sin(angle)
            bubble2.vector = direction2
        }
    }
}
