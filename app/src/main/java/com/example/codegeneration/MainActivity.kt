package com.example.codegeneration


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.Image
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.codegeneration.databinding.ActivityMainBinding
import kotlin.random.Random


const val EXCALIBUR_SOUND = "excalibur"
const val DRAWABLE_DEFTYPE = "drawable"
const val BACKGROUND_ARTORIA = "artoria"
const val BACKGROUND_MORDRED = "mordred"
const val ROUND_ARTORIA = "artoria_round"
const val ROUND_MORDRED = "mordred_round"
const val DELETED_BUBBLE = "magic_circle"
private const val BURST_SOUND = "bubble_burst"
private const val RAW_DEFTYPE = "raw"
const val MAX_BUBBLE_ON_LINE = 6
const val BUBBLES_TO_CHANGE_SCREEN = 10

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bubbleList: MutableList<Bubble>
    private lateinit var deleteBubbleList: MutableList<ImageView>
    private lateinit var projectLogic: ProjectLogic
    private var screenHeight = 0
    private var backgroundName = BACKGROUND_ARTORIA
    private var roundName = ROUND_ARTORIA
    private lateinit var lParams: ConstraintLayout.LayoutParams

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageWidth: Int
        bubbleList = mutableListOf()
        deleteBubbleList= mutableListOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            imageWidth = windowManager.currentWindowMetrics.bounds.width() / MAX_BUBBLE_ON_LINE
            screenHeight = windowManager.currentWindowMetrics.bounds.height()
        } else {
            imageWidth = windowManager.defaultDisplay.width / MAX_BUBBLE_ON_LINE
            screenHeight = windowManager.defaultDisplay.height
        }
        projectLogic = ProjectLogic(bubbleList, screenHeight, imageWidth / 2)

        lParams = ConstraintLayout.LayoutParams(imageWidth, imageWidth)
        binding.backgroundImage.setOnTouchListener { _, motionEvent ->
            createBubble(
                motionEvent,
                lParams
            )
        }
        binding.backgroundImage.z=0F


        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.warning_title))
        alertDialog.setMessage(getString(R.string.warning_text))
        alertDialog.setNeutralButton(
            getString(R.string.OK)
        ) { _, _ -> }
        alertDialog.show()
        move()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBubble(event: MotionEvent, lParams: ConstraintLayout.LayoutParams): Boolean {

        if (canAddBubble(event,  lParams)) {
            val x = event.x
            val y = event.y
            val newBubble = ImageView(this)
            newBubble.setImage(roundName)
            newBubble.x = x - lParams.width / 2
            newBubble.y = y - lParams.height / 2
            newBubble.id= (0..100000000).random()
            binding.root.addView(newBubble, lParams)
            bubbleList.add(Bubble(newBubble, 1))
            newBubble.z=2F


            val listener = View.OnTouchListener(function = { view, event ->
                if(event.action==MotionEvent.ACTION_DOWN){
                    for(i in 0 until bubbleList.size){
                        if(bubbleList[i].image.id==view.id){
                            bubbleList.removeAt(i)
                            break
                        }
                    }
                }
                if (event.action == MotionEvent.ACTION_MOVE) {
                    view.x = event.rawX - lParams.width
                    view.y = event.rawY - lParams.height
                }
                if(event.action==MotionEvent.ACTION_UP){
                    if(!canAddBubble(event, lParams)){
                        deleteBubbleList.add(view as ImageView)
                        view.z= 1F
                        (view as ImageView).setImage(DELETED_BUBBLE)
                        playSound(BURST_SOUND)
                        view.setOnTouchListener(null)
                    }else{
                        bubbleList.add(Bubble(view as ImageView,1))
                    }
                }
                true
            })
            newBubble.setOnTouchListener(listener)

            if (bubbleList.size % BUBBLES_TO_CHANGE_SCREEN == 0) {
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

    @SuppressLint("ClickableViewAccessibility")
    fun playSound(fileName: String) {
        val mp = MediaPlayer.create(
            this,
            resources.getIdentifier(fileName, RAW_DEFTYPE, packageName)
        )

        binding.backgroundImage.setOnTouchListener(null)

        mp.setOnCompletionListener {
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
        playSound(EXCALIBUR_SOUND)

        val time = (7000 / bubbleList.size).toLong()
        for (bubble in bubbleList) {
            binding.root.removeView(bubble.image)
            binding.root.invalidate()
            Thread.sleep(time)
        }
        for(bubble in deleteBubbleList){
            binding.root.removeView(bubble)
        }
        bubbleList.clear()
    }

    private fun canAddBubble(
        event: MotionEvent,
        lParams: ConstraintLayout.LayoutParams
    ): Boolean {
        val radius=lParams.width/2

        if (event.rawY - radius <= 0 || event.rawY + 2 * lParams.height >= screenHeight) {
            return false
        }
        for (bubble in bubbleList) {
            if (projectLogic.distance(
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
                projectLogic.move(bubbleList)
                deleteBurst()
                move()
            }
        }.start()
    }
    private fun deleteBurst(){
        for (i in bubbleList.size - 1 downTo 0) {
            if (bubbleList[i].isDelete) {
                bubbleList[i].image.setImage(DELETED_BUBBLE)
                playSound(BURST_SOUND)
                bubbleList[i].image.setOnTouchListener(null)
                bubbleList[i].image.z= -1F
                deleteBubbleList.add(bubbleList[i].image)
                bubbleList.removeAt(i)
            }
        }
    }
}
