package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Playlist
import com.github.wakingrufus.jamm.common.PlaylistTrack
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane

class PlaylistView(val library: ObservableLibrary, val mediaController: MediaPlayerController) : BorderPane() {
    val tracks = FXCollections.observableArrayList<Pair<PlaylistTrack, Track?>>()
    val selectedPlayList = SimpleObjectProperty<Playlist>().also {
        it.onChange { selected ->
            tracks.clear()
            selected?.run {
                this@PlaylistView.tracks.setAll(this.tracks
                    .map { it to library.trackPaths.get(it.pathRelativeToLibrary) })
            }
        }
    }

    init {
        left<StackPane> {
            listview(library.playlists) {
                this.cellFactory = CustomStringCellFactory { it.name }
                bindSelected(selectedPlayList)
            }
        }
        var tv: TableView<Pair<PlaylistTrack, Track?>>? = null
        center<StackPane> {
            tableView(ReadOnlyListWrapper(tracks)) {
                column<Pair<PlaylistTrack, Track?>, String>("#") {
                    it.value.first.playlistTrackNumber.plus(1).toString().toProperty()
                }
                column<Pair<PlaylistTrack, Track?>, String>("Title") {
                    it.value.second?.title?.toProperty() ?: it.value.first.pathRelativeToLibrary.toProperty()
                }
                column<Pair<PlaylistTrack, Track?>, String>("Album") { it.value.second?.album.toProperty() }
                column<Pair<PlaylistTrack, Track?>, String>("Album Artist") { it.value.second?.albumArtist?.name.toProperty() }
                autoResize()
                tv = this
            }
        }
        bottom<HBox> {
            button("Play All") {
                action {
                    mediaController.play(tv?.items?.map { it.second }?.filterNotNull() ?: listOf())
                }
            }
            button("Shuffle Play All") {
                action {
                    mediaController.play(tv?.items?.shuffled()?.map { it.second }?.filterNotNull() ?: listOf())
                }
            }
            button("Enqueue All") {
                action {
                    mediaController.queue(tv?.items?.map { it.second }?.filterNotNull() ?: listOf())
                }
            }
            button("Play Selected") {
                action {
                    tv?.selectionModel?.selectedItems?.run {
                        mediaController.play(this.mapNotNull { it.second })
                    }
                }
            }
            button("Enqueue Selected") {
                action {
                    tv?.selectionModel?.selectedItems?.run {
                        mediaController.queue(this.mapNotNull { it.second })
                    }
                }
            }
        }
    }
}