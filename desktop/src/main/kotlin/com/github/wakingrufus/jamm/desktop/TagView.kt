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
                this@TagView.tracks.setAll(library.tracks.filtered { it.tags.contains(this) })
            }
        }
    }

    fun viewTag(tag: String) {
        selectedTag.set(tag)
    }

    init {
        left<StackPane> {
            listview(library.tracks.flatMappedUnique { it.tags }.sorted()) {
                bindSelected(selectedTag)
                contextMenu {
                    actionItem("Export") {
                        selectionModel?.selectedItems?.run {
                            this.forEach {
                                library.exportTagPlaylist(it)
                            }
                        }
                    }
                }
            }
        }
        center<StackPane> {
            trackTable(tracks, library, mediaPlayer, listOf("Remove From Tag" to {
                library.setTags(it, it.tags.minus(selectedTag.get()))
            }))
        }
    }
}