package com.example.codegeneration


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.codegeneration.databinding.ActivityMainBinding

const val EXCALIBUR_SOUND = "excalibur"
const val DRAWABLE_DEFTYPE = "drawable"
const val BACKGROUND_ARTORIA = "artoria"
const val BACKGROUND_MORDRED = "mordred"
const val ROUND_ARTORIA = "artoria_round"
const val ROUND_MORDRED = "mordred_round"
const val MAX_BUBBLE_ON_LINE = 6
const val BUBBLES_TO_CHANGE_SCREEN = 15

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bubbleList: MutableList<Bubble>
    private lateinit var projectLogic: ProjectLogic
    private var screenHeight = 0
    private var backgroundName = BACKGROUND_ARTORIA
    private var roundName = ROUND_ARTORIA

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageWidth: Int
        bubbleList = mutableListOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            imageWidth = windowManager.currentWindowMetrics.bounds.width() / MAX_BUBBLE_ON_LINE
            screenHeight = windowManager.currentWindowMetrics.bounds.height()
        } else {
            imageWidth = windowManager.defaultDisplay.width / MAX_BUBBLE_ON_LINE
            screenHeight = windowManager.defaultDisplay.height
        }
        projectLogic = ProjectLogic(bubbleList, screenHeight, imageWidth / 2, this, binding)

        val lParams = ConstraintLayout.LayoutParams(imageWidth, imageWidth)
        binding.backgroundImage.setOnTouchListener { _, motionEvent ->
            createBubble(
                motionEvent,
                lParams
            )
        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.warning_title))
        alertDialog.setMessage(getString(R.string.warning_text))
        alertDialog.setNeutralButton(
            getString(R.string.OK)
        ) { _, _ -> }
        alertDialog.show()
        move()
    }

    private fun createBubble(event: MotionEvent, lParams: ConstraintLayout.LayoutParams): Boolean {

        if (canAddBubble(event, lParams.width / 2, lParams)) {
            val x = event.x
            val y = event.y
            val newBubble = ImageView(this)
            newBubble.setImageResource(
                resources.getIdentifier(
                    roundName,
                    DRAWABLE_DEFTYPE,
                    packageName
                )
            )
            newBubble.x = x - lParams.width / 2
            newBubble.y = y - lParams.height / 2
            binding.root.addView(newBubble, lParams)
            bubbleList.add(Bubble(newBubble, 1))

            if (bubbleList.size%BUBBLES_TO_CHANGE_SCREEN == 0) {
                changeBackground()
                return false
            }
        }

        return false
    }

    private fun changeBackground() {
        if (backgroundName == BACKGROUND_ARTORIA) {
            backgroundName = BACKGROUND_MORDRED
            binding.backgroundImage.setImage(backgroundName)
            roundName = ROUND_MORDRED
        } else {
            backgroundName = BACKGROUND_ARTORIA
            binding.backgroundImage.setImage(backgroundName)
            roundName = ROUND_ARTORIA
            clearScreen()
        }
    }

    private fun ImageView.setImage(name: String) {
        return setImageResource(
            resources.getIdentifier(
                name,
                DRAWABLE_DEFTYPE,
                packageName
            )
        )
    }

    private fun clearScreen() {
        projectLogic.playSound(EXCALIBUR_SOUND)
        Thread.sleep(7000L)
        for (bubble in bubbleList) {
            binding.root.removeView(bubble.image)
        }
        bubbleList.clear()
    }


    private fun canAddBubble(
        event: MotionEvent,
        radius: Int,
        lParams: ConstraintLayout.LayoutParams
    ): Boolean {
        if (event.y - lParams.height / 2 <= 0 || event.y + 2 * lParams.height >= screenHeight) {
            return false
        }
        for (bubble in bubbleList) {
            if (projectLogic.distance(
                    bubble.image.x + radius,
                    bubble.image.y + radius,
                    event.x,
                    event.y
                ) < 2 * radius
            ) {
                return false
            }
        }
        return true
    }

    private fun move() {
        object : CountDownTimer(1, 1) {
            override fun onTick(p0: Long) {}

            override fun onFinish() {
                projectLogic.move()
                move()
            }
        }.start()
    }
}