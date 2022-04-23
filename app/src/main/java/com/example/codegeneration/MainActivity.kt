package com.example.codegeneration


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.window.layout.WindowMetricsCalculator
import com.example.codegeneration.databinding.ActivityMainBinding
import kotlin.random.Random


const val EXCALIBUR_SOUND = "excalibur"
const val DRAWABLE_DEFTYPE = "drawable"
const val BACKGROUND_ARTORIA = "artoria"
const val BACKGROUND_MORDRED = "mordred"
const val ROUND_ARTORIA = "artoria_round"
const val ROUND_MORDRED = "mordred_round"
const val DELETED_BUBBLE_ARTORIA = "magic_circle_blue"
const val DELETED_BUBBLE_MORDRED = "magic_circle_gray"
private const val BURST_SOUND = "bubble_burst"
private const val RAW_DEFTYPE = "raw"
const val MAX_BUBBLE_ON_LINE = 6
const val BUBBLES_TO_CHANGE_SCREEN = 20
const val MAX_SPEED = 5.0

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bubbleList: MutableList<Bubble>
    private lateinit var deleteBubbleList: MutableList<ImageView>
    private lateinit var projectLogic: ProjectLogic
    private lateinit var lastVector: Vector
    private lateinit var lParams: ConstraintLayout.LayoutParams
    private lateinit var bounds: Rect
    private var deletedImage= DELETED_BUBBLE_ARTORIA
    private var backgroundName = BACKGROUND_ARTORIA
    private var roundName = ROUND_ARTORIA
    private var idCounter = 1

    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bubbleList = mutableListOf()
        deleteBubbleList = mutableListOf()
        bounds = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this).bounds
        val imageWidth = bounds.right / MAX_BUBBLE_ON_LINE
        projectLogic = ProjectLogic(bubbleList, bounds, imageWidth / 2)

        lParams = ConstraintLayout.LayoutParams(imageWidth, imageWidth)
        binding.backgroundImage.setOnTouchListener { _, motionEvent ->
            createBubble(
                motionEvent,
                lParams
            )
        }
        binding.backgroundImage.z = 0F

        soundAlert()

        binding.buttonClear.setOnClickListener{changeBackground(true)}
        binding.buttonClear.background.setTint(R.color.blue)
        move()
    }

    private fun soundAlert() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.warning_title))
        alertDialog.setMessage(getString(R.string.warning_text))
        alertDialog.setNeutralButton(
            getString(R.string.OK)
        ) { _, _ -> }
        alertDialog.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBubble(event: MotionEvent, lParams: ConstraintLayout.LayoutParams): Boolean {

        if (canAddBubble(event, lParams)) {
            val x = event.rawX
            val y = event.rawY
            val newBubble = ImageView(this)
            newBubble.setImage(roundName)
            newBubble.x = x - lParams.width / 2
            newBubble.y = y - lParams.height / 2
            newBubble.id = idCounter
            idCounter++
            binding.root.addView(newBubble, lParams)
            bubbleList.add(Bubble(newBubble, createVector()))
            newBubble.z = 2F


            val listener = View.OnTouchListener(function = { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    for (i in 0 until bubbleList.size) {
                        if (bubbleList[i].image.id == view.id) {
                            lastVector = bubbleList[i].vector
                            bubbleList.removeAt(i)
                            break
                        }
                    }
                }
                if (event.action == MotionEvent.ACTION_MOVE) {
                    view.x = event.rawX - lParams.width
                    view.y = event.rawY - lParams.height
                }
                if (event.action == MotionEvent.ACTION_UP) {
                    if (!canAddBubble(event, lParams)) {
                        deleteBubbleList.add(view as ImageView)
                        view.z = 1F
                        playSound(BURST_SOUND,view, deletedImage)
                        view.setOnTouchListener(null)
                    } else {
                        bubbleList.add(Bubble(view as ImageView, lastVector))
                    }
                }
                true
            })
            newBubble.setOnTouchListener(listener)

            if ((bubbleList.size+deleteBubbleList.size) % BUBBLES_TO_CHANGE_SCREEN == 0) {
                changeBackground()
            }
        }

        return false
    }

    private fun createVector(): Vector {
        val x = Random.nextDouble(-MAX_SPEED, MAX_SPEED)
        val y = Random.nextDouble(-MAX_SPEED, MAX_SPEED)
        return Vector(x, y)
    }

    @SuppressLint("ResourceAsColor", "UseCompatLoadingForColorStateLists")
    private fun changeBackground(force: Boolean =false) {
        if (backgroundName == BACKGROUND_ARTORIA && !force) {
            binding.buttonClear.background.setTint(R.color.gray)
            deletedImage=DELETED_BUBBLE_MORDRED
            backgroundName = BACKGROUND_MORDRED
            roundName = ROUND_MORDRED
            binding.backgroundImage.setImage(backgroundName)
        } else {
            binding.buttonClear.background.setTint(R.color.blue)
            deletedImage= DELETED_BUBBLE_ARTORIA
            backgroundName = BACKGROUND_ARTORIA
            roundName = ROUND_ARTORIA
            playSound(EXCALIBUR_SOUND,binding.backgroundImage,backgroundName)
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

    @SuppressLint("ClickableViewAccessibility")
    fun playSound(fileName: String, imageView: ImageView,image:String) {
        val mp = MediaPlayer.create(
            this,
            resources.getIdentifier(fileName, RAW_DEFTYPE, packageName)
        )

        if (fileName == EXCALIBUR_SOUND)
            binding.backgroundImage.setOnTouchListener(null)
        else
            imageView.setImage(image)

        mp.setOnCompletionListener {
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

    private fun clearScreen() {
        for (bubble in bubbleList) {
            binding.root.removeView(bubble.image)
        }
        for (bubble in deleteBubbleList) {
            binding.root.removeView(bubble)
        }
        bubbleList.clear()
    }

    private fun canAddBubble(
        event: MotionEvent,
        lParams: ConstraintLayout.LayoutParams
    ): Boolean {
        val radius = lParams.width / 2

        if (event.rawY <= bounds.top || event.rawY + lParams.height >= bounds.bottom
            || event.rawX <= bounds.left || event.rawX >= bounds.right
        ) {
            return false
        }
        for (bubble in bubbleList) {
            if (ProjectLogic.distance(
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

    private fun move() {
        object : CountDownTimer(1, 1) {
            override fun onTick(p0: Long) {}

            override fun onFinish() {
                projectLogic.move()
                deleteBurst()
                move()
            }
        }.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun deleteBurst() {
        for (i in bubbleList.size - 1 downTo 0) {
            if (bubbleList[i].isDelete) {
                playSound(BURST_SOUND,bubbleList[i].image, deletedImage)
                bubbleList[i].image.setOnTouchListener(null)
                bubbleList[i].image.z = 1F
                deleteBubbleList.add(bubbleList[i].image)
                bubbleList.removeAt(i)
            }
        }
    }
}
