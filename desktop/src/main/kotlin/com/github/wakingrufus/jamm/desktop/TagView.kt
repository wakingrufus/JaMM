package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

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
            val tagList = FXCollections.observableArrayList<String>()
            val listView = listview(tagList) {
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
            library.addTagListener {
                val oldSelection = selectedTag.get()
                GlobalScope.launch(Dispatchers.JavaFx) {
                    tagList.clear()
                    tagList.addAll(library.tracks.flatMapUnique { it.tags }.sorted())
                    if (oldSelection != null) {
                        listView.selectionModel.select(oldSelection)
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