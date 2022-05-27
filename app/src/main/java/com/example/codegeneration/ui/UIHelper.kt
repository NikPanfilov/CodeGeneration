package com.example.codegeneration.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.window.layout.WindowMetricsCalculator
import com.example.codegeneration.R
import com.example.codegeneration.databinding.ActivityMainBinding
import com.example.codegeneration.model.Bubble
import com.example.codegeneration.model.Vector
import com.example.codegeneration.model.VectorLogic
import com.example.codegeneration.model.setImage
import kotlin.random.Random

class UIHelper(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val lParams: ConstraintLayout.LayoutParams
) {
    private val maxSpeed = 5.0
    private val bubbleNumToChangeScreen = 20
    private val rawDeftype = "raw"
    private val burstSound = "bubble_burst"
    private val deletedBubbleGray = "magic_circle_gray"
    private val clearScreenSound = "excalibur"
    private val roundGray = "mordred_round"
    private val deletedBubbleBlue = "magic_circle_blue"
    private val roundBlue = "artoria_round"


    private val backLandscape1 = "background_landscape_first"
    private val backLandscape2 = "background_landscape_second"
    private val backPortrait1 = "background_portrait_first"
    private val backPortrait2 = "background_portrait_second"

    private var deletedImage = deletedBubbleBlue
    private var roundName = roundBlue
    private var idCounter = 1
    private var isFirstBackground = true

    private lateinit var backgroundFirst: String
    private lateinit var backgroundSecond: String
    private var bounds: Rect = WindowMetricsCalculator.getOrCreate()
        .computeCurrentWindowMetrics(context as Activity).bounds
    private var bubbleList: MutableList<Bubble> = mutableListOf()
    private var deleteBubbleList: MutableList<ImageView> = mutableListOf()

    init {
        setOrientationOptions()
    }

    fun startMoving() {
        move(VectorLogic(bubbleList, bounds, lParams.width / 2))
    }

    private fun setOrientationOptions() {
        when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                backgroundFirst = backLandscape1
                backgroundSecond = backLandscape2
            }
            else -> {
                backgroundFirst = backPortrait1
                backgroundSecond = backPortrait2
            }
        }
    }

    fun setBackground() {
        binding.backgroundImage.setImage(backgroundFirst)
        binding.backgroundImage.z = 0F
        binding.backgroundImage.setOnDragListener(choiceDragListener())
        binding.buttonClear.setOnDragListener(choiceDragListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    fun createBubble(event: MotionEvent, lParams: ConstraintLayout.LayoutParams): Boolean {
        if (canAddBubble(event, lParams)) {
            val newBubble = ImageView(context)
            newBubble.setImage(roundName)
            newBubble.x = event.rawX - lParams.width / 2
            newBubble.y = event.rawY - lParams.height
            newBubble.id = idCounter
            idCounter++
            binding.root.addView(newBubble, lParams)
            bubbleList.add(Bubble(newBubble, createVector()))
            newBubble.z = 2F


            val listener = View.OnTouchListener(function = { view, _ ->
                for (bubble in bubbleList) {
                    if (view.id == bubble.image.id) {
                        draggingBubble = bubble
                        bubbleList.remove(bubble)
                        break
                    }
                }
                view.visibility = View.INVISIBLE
                view.startDragAndDrop(
                    ClipData.newPlainText("", ""),
                    View.DragShadowBuilder(view),
                    view,
                    0
                )
                true
            })
            newBubble.setOnTouchListener(listener)

            if ((bubbleList.size + deleteBubbleList.size) % bubbleNumToChangeScreen == 0) {
                changeBackground()
            }
        }

        return false
    }

    private fun createVector(): Vector {
        val x = Random.nextDouble(-maxSpeed, maxSpeed)
        val y = Random.nextDouble(-maxSpeed, maxSpeed)
        return Vector(x, y)
    }

    @SuppressLint("ResourceAsColor", "UseCompatLoadingForColorStateLists")
    fun changeBackground(force: Boolean = false) {
        if (isFirstBackground && !force) {
            binding.buttonClear.background.setTint(R.color.gray)
            deletedImage = deletedBubbleGray
            roundName = roundGray
            binding.backgroundImage.setImage(backgroundSecond)
        } else {
            binding.buttonClear.background.setTint(R.color.blue)
            deletedImage = deletedBubbleBlue
            roundName = roundBlue
            changeImageWithSound(clearScreenSound, binding.backgroundImage, backgroundFirst)
        }
        isFirstBackground = !isFirstBackground
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun changeImageWithSound(soundFileName: String, imageView: ImageView, image: String) {
        val mp = MediaPlayer.create(
            context,
            context.resources.getIdentifier(soundFileName, rawDeftype, context.packageName)
        )

        if (soundFileName == clearScreenSound) {
            binding.backgroundImage.setOnTouchListener(null)
            binding.buttonClear.isClickable = false
        } else
            imageView.setImage(image)

        mp.setOnCompletionListener {
            if (soundFileName == clearScreenSound) {
                clearScreen()
                binding.buttonClear.isClickable = true
            }
            imageView.setImage(image)
            binding.backgroundImage.setOnTouchListener { _, motionEvent ->
                createBubble(
                    motionEvent,
                    lParams
                )
            }
        }


        mp.start()
    }

    private fun canAddBubble(
        event: MotionEvent,
        lParams: ConstraintLayout.LayoutParams
    ): Boolean {
        val radius = lParams.width / 2

        if (event.rawY - lParams.height <= bounds.top || event.rawY + radius >= bounds.bottom
            || event.rawX - radius <= bounds.left || event.rawX + radius >= bounds.right
        ) {
            return false
        }
        for (bubble in bubbleList) {
            if (Vector.distance(
                    bubble.image.x + radius,
                    bubble.image.y + radius,
                    event.rawX,
                    event.rawY
                ) < 2 * radius
            ) {
                return false
            }
        }
        return true
    }

    private lateinit var draggingBubble: Bubble
    private fun choiceDragListener() = View.OnDragListener { view, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                draggingBubble.image.x = event.x - lParams.width / 2
                draggingBubble.image.y = event.y - lParams.height / 2
            }
            DragEvent.ACTION_DROP -> {
                if (view.id == binding.backgroundImage.id || view.id == binding.buttonClear.id) {
                    bubbleList.add(draggingBubble)
                } else {
                    view.z = 1F
                    changeImageWithSound(burstSound, draggingBubble.image, deletedImage)
                }
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                draggingBubble.image.visibility = View.VISIBLE
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.z = 1F
                changeImageWithSound(burstSound, draggingBubble.image, deletedImage)
            }
        }
        true
    }


    @SuppressLint("ClickableViewAccessibility")
    fun deleteBurst() {
        for (i in bubbleList.size - 1 downTo 0) {
            if (bubbleList[i].isDelete) {
                changeImageWithSound(burstSound, bubbleList[i].image, deletedImage)
                bubbleList[i].image.setOnTouchListener(null)
                bubbleList[i].image.z = 1F
                deleteBubbleList.add(bubbleList[i].image)
                bubbleList.removeAt(i)
            }
        }
    }

    private fun clearScreen() {
        for (bubble in bubbleList) {
            binding.root.removeView(bubble.image)
        }
        for (bubble in deleteBubbleList) {
            binding.root.removeView(bubble)
        }
        bubbleList.clear()
    }

    private fun move(vectorLogic: VectorLogic) {
        object : CountDownTimer(1, 1) {
            override fun onTick(p0: Long) {}

            override fun onFinish() {
                vectorLogic.move()
                deleteBurst()
                move(vectorLogic)
            }
        }.start()
    }
}