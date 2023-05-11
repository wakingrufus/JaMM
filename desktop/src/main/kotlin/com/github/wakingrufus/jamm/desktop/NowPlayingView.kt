package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.AlbumArtist
import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.paint.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.controlsfx.control.SearchableComboBox
import java.io.ByteArrayInputStream
import kotlin.time.Duration
import kotlin.time.DurationUnit

private val logger = KotlinLogging.logger {}

class NowPlayingView(
    val library: ObservableLibrary,
    val mediaPlayerController: MediaPlayerController,
    val queue: ObservableList<Track>,
    viewAlbum: (AlbumKey) -> Unit,
    viewAlbumArtist: (AlbumArtist) -> Unit,
    viewTag: (String) -> Unit
) : BorderPane() {
    init {
        center<BorderPane> {
            center<BorderPane> {
                bind(mediaPlayerController.getNowPlayingProperty()) { t ->
                    t?.also { track ->
                        val tagList = FXCollections.observableList(track.tags.toMutableList())
                        left<VBox> {
                            label("Tags")
                            val allTags = library.tracks
                                .mapped { it.tags }
                                .flattened()
                                .grouped { it }
                                .sorted()
                            children.add(SearchableComboBox(allTags).apply {
                                selectionModel.selectedItemProperty().addListener { _, o, selectedItem ->
                                    if (!tagList.contains(selectedItem)) {
                                        library.setTags(track, track.tags.plus(selectedItem))
                                        tagList.add(selectedItem)
                                    }
                                }
                            })

                            add<VBox> {
                                children.bind(tagList) { tag ->
                                    BorderPane().apply {
                                        this.border = Border(
                                            BorderStroke(
                                                Color.BLACK, BorderStrokeStyle.SOLID,
                                                CornerRadii(2.0), BorderWidths.DEFAULT
                                            )
                                        )
                                        center<Label> {
                                            this.text = tag
                                            clickableHoverEffect()
                                            setOnMouseClicked {
                                                viewTag.invoke(tag)
                                            }
                                        }
                                        right<StackPane> {
                                            button("x") {
                                                setOnAction {
                                                    tagList.remove(tag)
                                                    library.setTags(track, track.tags.minus(tag))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            add<HBox> {
                                val newTagBox = add<TextField> {
                                    setOnAction {
                                        library.setTags(track, track.tags.plus(text))
                                        tagList.add(text)
                                    }
                                }
                                button("Create Tag") {
                                    setOnAction {
                                        library.setTags(track, track.tags.plus(newTagBox.text))
                                        tagList.add(newTagBox.text)
                                    }
                                }
                            }
                        }
                        center<VBox> {
                            library.getTrackArt(track)?.also { imageBytes ->
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
                                clickableHoverEffect()
                                this.textFill
                                setOnMouseClicked {
                                    viewAlbumArtist.invoke(track.albumArtist)
                                }
                            }
                            label(track.album) {
                                style =
                                    "-fx-font-style: italic; -fx-font-size: 24; -fx-font-family: 'Noto Sans CJK JP';"
                                alignment = Pos.CENTER
                                clickableHoverEffect()
                                setOnMouseClicked {
                                    viewAlbum.invoke(track.albumKey)
                                }
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
                val loadingLabel = top<Label> {
                    text = "Loading Library..."
                }
                library.addListener {
                    GlobalScope.launch(Dispatchers.JavaFx) {
                        bar.progress = 0.0
                        loadingLabel.text = ""
                    }
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
                            button("x") {
                                setOnAction {
                                    queue.remove(track)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun formatTime(duration: Duration): String {
        val seconds: Long = duration.toLong(DurationUnit.SECONDS)
        val HH = seconds / 3600
        val MM = seconds % 3600 / 60
        val SS = seconds % 60
        return String.format("%02d:%02d:%02d", HH, MM, SS)
    }
}
