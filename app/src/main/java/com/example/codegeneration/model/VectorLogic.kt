package com.example.codegeneration.model


import android.graphics.Rect
import android.util.Log
import com.example.codegeneration.model.Bubble.Companion.changeVectors
import com.example.codegeneration.model.Vector.Companion.distance

class VectorLogic(
    private val bubbleList: MutableList<Bubble>,
    private val bounds: Rect,
    private val radius: Int
) {

    fun move() {
        if (bubbleList.isNotEmpty()) {

            for (bubble in bubbleList) {
                bubble.vector.normalize()
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
}