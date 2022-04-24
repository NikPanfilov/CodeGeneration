package com.example.codegeneration.model


import android.graphics.Rect
import android.util.Log
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val VECTOR_LENGTH = 2.0

class VectorLogic(
    private val bubbleList: MutableList<Bubble>,
    private val bounds: Rect,
    private val radius: Int
) {

    fun move() {
        if (bubbleList.isNotEmpty()) {

            for (bubble in bubbleList) {
                normalize(bubble.vector)
                bubble.image.y += bubble.vector.y.toFloat()
                bubble.image.x += bubble.vector.x.toFloat()
            }

            fixCollision()
        }
    }

    private fun fixEdgeCollision(collisions: MutableList<Int>) {
        for (i in 0 until bubbleList.size) {
            if (bubbleList[i].image.y + 3 * radius >= bounds.bottom || bubbleList[i].image.y <= bounds.top) {
                bubbleList[i].vector.y *= -1
                collisions[i]++
            }
            if (bubbleList[i].image.x + 2 * radius >= bounds.right || bubbleList[i].image.x <= bounds.left) {
                bubbleList[i].vector.x *= -1
                collisions[i]++
            }
        }
    }

    private fun fixCollision() {
        val collisions = mutableListOf<Int>()
        for (i in 0 until bubbleList.size) {
            collisions.add(0)
        }
        for (i in 0..bubbleList.size - 2) {
            for (j in i + 1 until bubbleList.size) {
                if (distance(
                        bubbleList[i].image.x,
                        bubbleList[i].image.y,
                        bubbleList[j].image.x,
                        bubbleList[j].image.y
                    ) < 2 * radius
                ) {
                    if (bubbleList[i].image.y > bubbleList[j].image.y) {
                        changeVectors(bubbleList[j], bubbleList[i])
                    } else {
                        changeVectors(bubbleList[i], bubbleList[j])
                    }
                    collisions[i]++
                    collisions[j]++
                }
            }
        }
        fixEdgeCollision(collisions)

        for (i in collisions.size - 1 downTo 0) {
            if (collisions[i] > 1) {
                Log.i("col", i.toString() + " " + collisions[i].toString())
                bubbleList[i].isDelete = true
            }
        }
    }

    private fun changeVectors(bubble1: Bubble, bubble2: Bubble) {
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

        direction1.x = component1 * cos(180 / (2 * kotlin.math.PI))
        direction1.y += component2 * sin(180 / (2 * kotlin.math.PI))
        bubble1.vector = direction1

        direction2.x = component2 * cos(angle)
        direction2.y += component2 * sin(angle)
        bubble2.vector = direction2

    }

    private fun normalize(vector: Vector) {
        val lengthInversion = VECTOR_LENGTH / sqrt(vector.x.pow(2) + vector.y.pow(2))
        vector.x *= lengthInversion
        vector.y *= lengthInversion
    }

    companion object {
        fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double =
            sqrt((x1 - x2).toDouble().pow(2.0) + (y1 - y2).toDouble().pow(2.0))

        fun distance(vector1: Vector, vector2: Vector) = sqrt(
            (vector1.x - vector2.x).pow(2.0) +
                    (vector1.y - vector2.y).pow(2.0)
        )
    }
}

