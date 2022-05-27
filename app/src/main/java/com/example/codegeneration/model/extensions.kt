package com.example.codegeneration.model

import android.widget.ImageView

val drawableDeftype = "drawable"
fun ImageView.setImage(name: String) {
    return setImageResource(
        resources.getIdentifier(
            name,
            drawableDeftype,
            context.packageName
        )
    )
}