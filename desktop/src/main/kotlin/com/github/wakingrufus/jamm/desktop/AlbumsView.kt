package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import java.util.function.Predicate

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

    fun viewAlbum(albumKey: AlbumKey){
        selectedAlbum.set(albumKey)
    }

    init {
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
        }
        val filter: Predicate<Track> = Predicate { track ->
            yearSelection.selectionModel.isEmpty
                    || yearSelection.selectionModel.selectedItem == null
                    || (track.releaseDate == null && yearSelection.selectionModel.selectedItem.isBlank())
                    || (track.releaseDate?.year.toString() == yearSelection.selectionModel.selectedItem)
        }
        left<StackPane> {
            listview(library.tracks
                .filtered(filter)
                .grouped { it.albumKey }
                .sorted(Comparator.comparing { it.albumName })
            ) {
                this.cellFactory = CustomStringCellFactory { it.albumName + " - " + it.albumArtist }
                bindSelected(selectedAlbum)
                yearSelection.selectionModel.selectedItemProperty().onChange {
                    this.items = library.tracks
                        .filtered(filter)
                        .grouped { it.albumKey }
                        .sorted(Comparator.comparing { it.albumName })
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