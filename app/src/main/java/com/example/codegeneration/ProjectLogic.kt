package com.example.codegeneration


import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.example.codegeneration.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import java.lang.Exception
import kotlin.math.pow
import kotlin.math.sqrt

private const val BURST_SOUND = "bubble_burst"
private const val RAW_DEFTYPE = "raw"

class ProjectLogic(
    private val bubbleList: MutableList<Bubble>,
    private val height: Int,
    private val radius: Int,
    private val context: Context,
    private val binding: ActivityMainBinding
) {

    fun move(): Boolean {
        if (bubbleList.isNotEmpty()) {

            for (bubble in bubbleList) {
                bubble.image.y += bubble.direction
            }

            fixCollision()
        }
        return true
    }

    private fun changeDirection(bubble: Bubble) {
        bubble.direction *= -1
    }

    private fun checkDirection(collisions:MutableList<Int>) {
        for (i in 0 until bubbleList.size) {
            if (bubbleList[i].image.y + 3 * radius >= height || bubbleList[i].image.y <= 0) {
                changeDirection(bubbleList[i])
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
                    changeDirection(bubbleList[i])
                    changeDirection(bubbleList[j])
                    collisions[i]++
                    collisions[j]++
                }
            }
        }
        checkDirection(collisions)

        for (i in collisions.size - 1 downTo 0) {
            if (collisions[i] > 1) {
                playSound(BURST_SOUND)
                binding.root.removeView(bubbleList[i].image)
                bubbleList.removeAt(i)
            }
        }
    }

    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double =
        sqrt((x1 - x2).toDouble().pow(2.0) + (y1 - y2).toDouble().pow(2.0))

    fun playSound(fileName: String) {
        val mp = MediaPlayer.create(
            context,
            context.resources.getIdentifier(fileName, RAW_DEFTYPE, context.packageName)
        )
        mp.start()
    }
}

