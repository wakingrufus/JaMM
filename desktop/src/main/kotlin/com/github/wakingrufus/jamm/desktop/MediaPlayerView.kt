package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.javafx.*
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import java.io.ByteArrayInputStream

class MediaPlayerView(
    val library: ObservableLibrary,
    mediaPlayerController: MediaPlayerController
) : BorderPane(), Logging {
    val nowPlayingArea = StackPane()

    init {
        this.center = nowPlayingArea
        mediaPlayerController.getNowPlayingProperty().onChange { track ->
            nowPlayingArea.children.clear()
            nowPlayingArea.children.add(VBox().apply {
                track?.also {
                    it.image?.also { imageBytes ->
                        HBox().apply {
                            this.alignment = Pos.CENTER
                            imageView(Image(ByteArrayInputStream(imageBytes))) {
                                this.fitHeight = 256.0
                                this.fitWidth = 256.0
                            }
                        }.attachTo(this)

                    }
                    alignment = Pos.CENTER
                    label(it.title) {
                        font = Font.font(14.0)
                        alignment = Pos.CENTER
                    }
                    label(it.albumArtist.name) {
                        style = "-fx-font-weight: bold;"
                        alignment = Pos.CENTER
                    }
                    label(it.album) {
                        style = "-fx-font-style: italic;"
                        alignment = Pos.CENTER
                    }
                }

            })
        }
        bottom<HBox> {
            button("⏹️ Stop") {
                action {
                    mediaPlayerController.stop()
                }
            }
            button("▶ Play️") {
                action {
                    if (mediaPlayerController.play()) {
                        this.text = "⏸ Pause️️"
                    } else {
                        this.text = "▶ Play️"
                    }
                }
            }
            button("⏭️ Next") {
                action {
                    mediaPlayerController.next()
                }
            }
        }
    }
}