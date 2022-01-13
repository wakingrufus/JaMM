package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.AlbumArtist
import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.javafx.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import java.io.ByteArrayInputStream

class PlayerBarView(
    val library: ObservableLibrary,
    mediaPlayerController: MediaPlayerController,
    viewAlbum: (AlbumKey) -> Unit,
    viewAlbumArtist : (AlbumArtist) -> Unit
) : HBox(), Logging {
    init {
        val playButton = ImageView(Image(PlayerBarView::class.java.getResourceAsStream("/play-white.png"))).apply {
            this.fitHeight = 64.0
            this.fitWidth = 64.0
        }
        val pauseButton = ImageView(Image(PlayerBarView::class.java.getResourceAsStream("/pause-white.png"))).apply {
            this.fitHeight = 64.0
            this.fitWidth = 64.0
        }
        val nextButton = ImageView(Image(PlayerBarView::class.java.getResourceAsStream("/next-white.png"))).apply {
            this.fitHeight = 32.0
            this.fitWidth = 32.0
        }
        val stopButton = ImageView(Image(PlayerBarView::class.java.getResourceAsStream("/stop-white.png"))).apply {
            this.fitHeight = 32.0
            this.fitWidth = 32.0
        }

        bind(mediaPlayerController.getNowPlayingProperty()) { track ->
            this.children.clear()
            add<HBox> {
                this.padding = Insets(1.0, 16.0, 1.0, 1.0)
                track?.also {
                    library.getTrackArt(track)?.also { imageBytes ->
                        HBox().apply {
                            this.padding = Insets(1.0, 16.0, 1.0, 1.0)
                            this.alignment = Pos.CENTER
                            imageView(Image(ByteArrayInputStream(imageBytes))) {
                                this.fitHeight = 96.0
                                this.fitWidth = 96.0
                            }
                        }.attachTo(this)

                    }
                    alignment = Pos.CENTER_LEFT
                    add<VBox> {
                        alignment = Pos.CENTER_LEFT
                        padding = Insets(1.0, 10.0, 1.0, 1.0)
                        label(it.title) {
                            //  font = Font.font("Noto Sans CJK JP", 24.0)
                            style = "-fx-font-size: 24; -fx-font-family: 'Noto Sans CJK JP';"
                            alignment = Pos.CENTER_LEFT
                        }
                        label(it.albumArtist.name) {
                            font = Font.font(18.0)
                            style = "-fx-font-weight: bold; -fx-font-family: 'Noto Sans CJK JP';"
                            alignment = Pos.CENTER_LEFT
                            clickableHoverEffect()
                            setOnMouseClicked {
                                viewAlbumArtist.invoke(track.albumArtist)
                            }
                        }
                        label(it.album) {
                            font = Font.font(18.0)
                            style = "-fx-font-style: italic; -fx-font-family: 'Noto Sans CJK JP';"
                            alignment = Pos.CENTER_LEFT
                            clickableHoverEffect()
                            setOnMouseClicked {
                                viewAlbum.invoke(track.albumKey)
                            }
                        }
                    }

                    button(stopButton) {
                        tooltip = Tooltip("Stop")
                        padding = Insets(10.0, 10.0, 10.0, 10.0)
                        action {
                            mediaPlayerController.stop()
                        }
                    }
                    button(pauseButton) {
                        tooltip = Tooltip("Pause")
                        padding = Insets(10.0, 10.0, 10.0, 10.0)
                        action {
                            if (mediaPlayerController.play()) {
                                this.tooltip = Tooltip("Pause")
                                this.graphic = pauseButton
                            } else {
                                this.tooltip = Tooltip("Play")
                                this.graphic = playButton
                            }
                        }
                    }
                    button(nextButton) {
                        tooltip = Tooltip("Next")
                        padding = Insets(10.0, 10.0, 10.0, 10.0)
                        action {
                            mediaPlayerController.next()
                        }
                    }
                }
            }
        }
    }
}