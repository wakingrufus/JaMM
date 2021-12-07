package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class NowPlayingView(
    val library: ObservableLibrary,
    val mediaPlayerController: MediaPlayerController,
    val queue: ObservableList<Track>,
) : BorderPane(), Logging {
    init {
        center<BorderPane> {
            center<BorderPane> {
                bind(mediaPlayerController.getNowPlayingProperty()) { t ->
                    t?.also { track ->
                        val tagList = FXCollections.observableList(track.tags.toMutableList())
                        left<VBox> {
                            label("Tags")
                            add<HBox> {
                                autoComplete(library.tracks.mapped { it.tags }.flattened().grouped { it }) {
                                    onAction = EventHandler {
                                        library.setTags(track, track.tags.plus(text))
                                        tagList.add(text)
                                    }
                                }
                            }
                            add<VBox> {
                                children.bind(tagList) { tag ->
                                    HBox().apply {
                                        label(tag)
                                        button("x") {
                                            action {
                                                tagList.remove(tag)
                                                library.setTags(track, track.tags.minus(tag))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        center<VBox> {

                            track.image?.also { imageBytes ->
                                add<HBox> {
                                    alignment = Pos.CENTER
                                    imageView(Image(ByteArrayInputStream(imageBytes))) {
                                        fitHeight = 720.0
                                        fitWidth = 720.0
                                    }
                                }
                            }
                            alignment = Pos.CENTER
                            label(track.title) {
                                style = "-fx-font-size: 32; -fx-font-family: 'Noto Sans CJK JP';"
                                alignment = Pos.CENTER
                            }
                            label(track.albumArtist.name) {
                                style = "-fx-font-weight: bold; -fx-font-size: 24; -fx-font-family: 'Noto Sans CJK JP';"
                                alignment = Pos.CENTER
                            }
                            label(track.album) {
                                style =
                                    "-fx-font-style: italic; -fx-font-size: 24; -fx-font-family: 'Noto Sans CJK JP';"
                                alignment = Pos.CENTER
                            }

                        }
                    }

                }
            }
            bottom<BorderPane> {
                val cur = left<Label> {
                    this.maxWidth(120.0)
                }
                val bar = center<ProgressBar> {
                    this.maxWidth = Double.MAX_VALUE
                }
                val total = right<Label> {
                    this.maxWidth(120.0)
                }
                mediaPlayerController.getProgressProperty().onChange {
                    GlobalScope.launch(Dispatchers.JavaFx) {
                        bar.progress = it
                        cur.text = formatTime(mediaPlayerController.getCurrentPosition())
                        total.text = formatTime(mediaPlayerController.getTotalDuration())
                    }
                }
            }
        }

        right<VBox> {
            alignment = Pos.TOP_CENTER
            label("Play Queue") {
                alignment = Pos.CENTER
                style = "-fx-font-size: 32;"
            }
            add<ScrollPane> {
                content = VBox().apply {
                    spacing = 10.0
                    children.bind(queue) { track ->
                        HBox().apply {
                            padding = Insets(2.0, 2.0, 2.0, 2.0)
                            label(track.title) {
                                style = "-fx-font-family: 'Noto Sans CJK JP';"
                            }
                            label(" - ")
                            label(track.albumArtist.name) {
                                style = "-fx-font-family: 'Noto Sans CJK JP'; -fx-font-weight: bold;"
                            }
                        }
                    }
                }
            }
        }
    }

    fun formatTime(duration: Duration): String {
        val seconds: Long = duration.toLong(TimeUnit.SECONDS)
        val HH = seconds / 3600
        val MM = seconds % 3600 / 60
        val SS = seconds % 60
        return String.format("%02d:%02d:%02d", HH, MM, SS)
    }
}
