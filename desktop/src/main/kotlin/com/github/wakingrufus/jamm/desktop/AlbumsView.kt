package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane

class AlbumsView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane(), Logging {
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedAlbum = SimpleObjectProperty<AlbumKey>().also {
        it.onChange { selectedAlbumKey ->
            tracks.clear()
            tracks.setAll(library.tracks.filtered { it.albumKey == selectedAlbumKey }?.sortedBy { it.trackNumber })
        }
    }

    init {
        top<HBox> {
            button("Play Random Album") {
                this.action {
                    library.tracks.grouped { it.albumKey }.random().also { selectedAlbumKey ->
                        mediaPlayer.play(library.tracks.filtered { it.albumKey ==  selectedAlbumKey}
                            ?.sortedBy { it.trackNumber }
                            ?.sortedBy { it.discNumber }
                            .orEmpty())
                    }
                }
            }
        }
        left<StackPane> {
            listview(library.tracks.grouped { it.albumKey }.sorted(Comparator.comparing { it.albumName })) {
                this.cellFactory = CustomStringCellFactory { it.albumName + " - " + it.albumArtist }
                bindSelected(selectedAlbum)
            }
        }
        center<BorderPane> {
            center<StackPane> {
                trackTable(tracks, library, mediaPlayer)
            }
        }
    }
}