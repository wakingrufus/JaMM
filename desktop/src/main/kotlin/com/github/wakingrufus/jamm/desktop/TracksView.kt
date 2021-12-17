package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TracksView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane() {
    val tracks = FXCollections.observableArrayList<Track>()
    lateinit var queryField: TextField
    lateinit var tagSelection: ComboBox<String>
    lateinit var yearSelection: ComboBox<String>
    fun applyFilter() {
        GlobalScope.launch(Dispatchers.Default) {
            val filtered = library.tracks.filtered { track ->
                (queryField.text.isNotBlank()
                        || track.title.toLowerCase().contains(queryField.text.toLowerCase())
                        || track.album.toLowerCase().contains(queryField.text.toLowerCase())
                        || track.albumArtist.name.toLowerCase().contains(queryField.text.toLowerCase()))
                        && (tagSelection.selectionModel.selectedItem == null || track.tags.contains(tagSelection.selectionModel.selectedItem))
                        && (yearSelection.selectionModel.selectedItem == null || track.releaseDate?.year.toString() == yearSelection.selectionModel.selectedItem)
            }
            withContext(Dispatchers.JavaFx) {
                tracks.clear()
                tracks.setAll(filtered)
            }
        }
    }

    init {
        library.addListener { applyFilter() }
        top<HBox> {
            label("Text")
            queryField = add {

                this.onAction = EventHandler {
                    applyFilter()
                }
            }
            label("Tag")
            tagSelection = add {
                items = library.tracks.flatMappedUnique { it.tags }.sorted()
                this.selectionModel.selectedItemProperty().onChange {
                    applyFilter()
                }
            }
            label("Year")
            yearSelection = add {
                items = library.tracks.grouped { it.releaseDate?.year?.toString() }.sorted()
                this.selectionModel.selectedItemProperty().onChange {
                    applyFilter()
                }
            }
            button("Missing Data") {
                action {
                    GlobalScope.launch(Dispatchers.JavaFx) {
                        tracks.clear()
                        tracks.setAll(library.tracks.filtered { track ->
                            track.releaseDate == null || track.albumArtist.name == "*UNKNOWN*"
                        })
                    }
                }
            }
        }

        center<StackPane> {
            trackTable(tracks, library, mediaPlayer)
        }
    }
}