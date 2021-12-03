package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.io.ByteArrayInputStream

class NowPlayingView(
    val library: ObservableLibrary,
    mediaPlayerController: MediaPlayerController,
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
}
