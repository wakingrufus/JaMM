package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane

class TagView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane() {
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedTag = SimpleObjectProperty<String>().also {
        it.onChange { selected ->
            tracks.clear()
            selected?.run {
                this@TagView.tracks.setAll(library.tags.get(this))
            }
        }
    }

    init {
        left<StackPane> {
            listview(library.tags.observableKeys()) {
                bindSelected(selectedTag)
            }
        }
        center<StackPane> {
            trackTable(tracks, library, mediaPlayer)
        }
    }
}