package com.github.wakingrufus.jamm.desktop

import javafx.scene.control.Labeled
import javafx.scene.paint.Color
import javafx.scene.paint.Paint

fun Labeled.clickableHoverEffect() {
    var defaultColor: Paint? = null
    this.setOnMouseEntered {
        defaultColor = this.textFill
        this.textFill = Color.ORANGE
    }
    this.setOnMouseExited {
        this.textFill = defaultColor
    }
}