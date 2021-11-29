package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.*
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
            trackTable(tracks,library,mediaPlayer)
        }
    }
}