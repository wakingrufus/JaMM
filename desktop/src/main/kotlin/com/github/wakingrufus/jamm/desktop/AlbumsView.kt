package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumsView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane(), Logging {
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedAlbum = SimpleObjectProperty<AlbumKey>().also {
        it.onChange { selectedAlbumKey ->
            tracks.clear()
            tracks.setAll(library.tracks.filtered { it.albumKey == selectedAlbumKey }
                ?.sortedBy { it.trackNumber }
                ?.sortedBy { it.discNumber })
        }
    }
    lateinit var yearSelection: ComboBox<String>
    lateinit var albumListView: ListView<AlbumKey>

    fun viewAlbum(albumKey: AlbumKey) {
        selectedAlbum.set(albumKey)
    }

    fun applyFilter() {
        GlobalScope.launch(Dispatchers.Default) {
            val newItems = library.tracks
                .filtered { track ->
                    yearSelection.selectionModel.isEmpty
                            || yearSelection.selectionModel.selectedItem == null
                            || (track.releaseDate == null && yearSelection.selectionModel.selectedItem.isBlank())
                            || (track.releaseDate?.year.toString() == yearSelection.selectionModel.selectedItem)
                }
                .grouped { it.albumKey }
                .sorted(Comparator.comparing { it.albumName })
            withContext(Dispatchers.JavaFx) {
                albumListView.items = newItems
            }
        }
    }

    init {
        library.addListener { applyFilter() }
        top<HBox> {
            button("Play Random Album") {
                this.action {
                    library.tracks.grouped { it.albumKey }.random().also { selectedAlbumKey ->
                        mediaPlayer.play(library.tracks.filtered { it.albumKey == selectedAlbumKey }
                            ?.sortedBy { it.trackNumber }
                            ?.sortedBy { it.discNumber }
                            .orEmpty())
                    }
                }
            }
            label("Year:")
            yearSelection = add {
                items = library.tracks.grouped { it.releaseDate?.year?.toString() }
                    .sorted(Comparator.comparing<String, String> { it ?: "" }.reversed())
            }
            button("Clear Filter") {
                action {
                    yearSelection.selectionModel.clearSelection()
                }
            }
        }

        left<StackPane> {
            albumListView = listview(library.tracks
                .grouped { it.albumKey }
                .sorted(Comparator.comparing { it.albumName })
            ) {
                this.cellFactory = CustomStringCellFactory { it.albumName + " - " + it.albumArtist }
                bindSelected(selectedAlbum)
                yearSelection.selectionModel.selectedItemProperty().onChange {
                    applyFilter()
                }
            }
        }
        center<BorderPane> {
            center<StackPane> {
                trackTable(tracks, library, mediaPlayer)
            }
        }
    }
}