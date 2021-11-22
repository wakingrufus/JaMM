package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane

class AlbumsView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane(), Logging {
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedAlbum = SimpleObjectProperty<AlbumKey>().also {
        it.onChange {
            tracks.clear()
            tracks.setAll(library.albumTracks[it]?.sortedBy { it.trackNumber })
        }
    }

    init {
        top<HBox> {
            button("Play Random Album") {
                this.action {
                    library.albums.keys.random().also {
                        mediaPlayer.play(library.albumTracks[it]?.sortedBy { it.trackNumber }.orEmpty())
                    }
                }
            }
        }
        left<StackPane> {
            listview(library.albums.observableKeys().sorted(Comparator.comparing { it.albumName })) {
                this.cellFactory = CustomStringCellFactory { it.albumName + " - " + it.albumArtist }
                bindSelected(selectedAlbum)
            }
        }
        center<BorderPane> {
            center<BorderPane> {
                center<StackPane> {
                    tableView(ReadOnlyListWrapper(tracks)) {
                        column<Track, String>("#") { it.value.trackNumber.toString().toProperty() }
                        column<Track, String>("Title") { it.value.title.toProperty() }
                        column<Track, String>("Album") { it.value.album.toProperty() }
                        column<Track, String>("Album Artist") { it.value.albumArtist.name.toProperty() }
                        autoResize()
                    }
                }
                bottom<HBox> {
                    button("Play") {
                        action {
                            mediaPlayer.play(tracks)
                        }
                    }
                }
            }
        }
    }
}