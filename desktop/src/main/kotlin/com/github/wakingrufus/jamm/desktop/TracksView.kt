package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

class TracksView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane() {
    val tracks = FXCollections.observableArrayList<Track>()

    init {
        var tv: TableView<Track>? = null
        top<StackPane> {
            TextField().apply {
                this.onAction = EventHandler { query ->
                    tracks.clear()
                    GlobalScope.launch(Dispatchers.JavaFx) {
                        if (this@apply.text.isNotBlank()) {
                            tracks.setAll(library.tracks.filter {
                                it.title.toLowerCase().contains(this@apply.text.toLowerCase())
                                        || it.album.toLowerCase().contains(this@apply.text.toLowerCase())
                                        || it.albumArtist.name.toLowerCase().contains(this@apply.text.toLowerCase())
                            })
                        } else {
                            tracks.setAll(library.tracks)
                        }
                    }
                }
            }.attachTo(this)
        }

        center<StackPane> {
            tv = tableView(ReadOnlyListWrapper(tracks)) {
                column<Track, String>("Title") {
                    it.value?.title?.toProperty() ?: it.value.path.toProperty()
                }
                column<Track, String>("Album Artist") { it.value?.albumArtist?.name.toProperty() }
                column<Track, String>("Album") { it.value?.album.toProperty() }

                column<Track, Int>("#") {
                    it.value.trackNumber.toProperty()
                }
                autoResize()
                this.selectionModel.selectionModeProperty().set(SelectionMode.MULTIPLE)
            }
        }
        bottom<HBox> {
            button("Play Selected") {
                action {
                    tv?.selectionModel?.selectedItems?.run {
                        mediaPlayer.play(this)
                    }
                }
            }
        }
    }
}