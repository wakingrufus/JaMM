package com.github.wakingrufus.javafx

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import java.io.File

fun Clipboard.setContent(op: ClipboardContent.() -> Unit) {
    val content = ClipboardContent()
    op(content)
    setContent(content)
}

fun Clipboard.putString(value: String) = setContent { putString(value) }
fun Clipboard.putFiles(files: MutableList<File>) = setContent { putFiles(files) }
fun Clipboard.put(dataFormat: DataFormat, value: Any) = setContent { put(dataFormat, value) }

