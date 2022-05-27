package com.example.codegeneration.ui


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.window.layout.WindowMetricsCalculator
import com.example.codegeneration.R
import com.example.codegeneration.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var uiHelper: UIHelper

    private val maxBubblesOnLine = 6

    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lParams = ConstraintLayout.LayoutParams(getImageWidth(), getImageWidth())
        binding.backgroundImage.setOnTouchListener { _, motionEvent ->
            uiHelper.createBubble(
                motionEvent,
                lParams
            )
        }

        uiHelper = UIHelper(this, binding, lParams)
        binding.buttonClear.setOnClickListener { uiHelper.changeBackground(true) }
        binding.buttonClear.background.setTint(R.color.blue)

        soundAlert()

        uiHelper.setBackground()
        uiHelper.startMoving()
    }

    private fun getImageWidth(): Int {
        val bounds: Rect = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this).bounds
        return Integer.min(bounds.right, bounds.bottom) / maxBubblesOnLine
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

}
