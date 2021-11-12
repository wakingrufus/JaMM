package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.*
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import java.nio.file.Paths

class PlaylistView(val library: Library) : BorderPane() {
    val playlists = FXCollections.observableArrayList(library.playlists.sortedBy { it.name })
    val tracks = FXCollections.observableArrayList<Pair<PlaylistTrack , Track?>>()
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
            listview(playlists) {
                this.cellFactory = CustomStringCellFactory { it.name }
                bindSelected(selectedPlayList)
            }
        }
        center<StackPane> {
            tableView(ReadOnlyListWrapper(tracks)) {
                column<Pair<PlaylistTrack , Track?>, String>("#") { it.value.first.playlistTrackNumber.toString().toProperty() }
                column<Pair<PlaylistTrack , Track?>, String>("Title") { it.value.second?.title?.toProperty() ?: it.value.first.pathRelativeToLibrary.toProperty() }
                column<Pair<PlaylistTrack , Track?>, String>("Album") { it.value.second?.album.toProperty() }
                column<Pair<PlaylistTrack , Track?>, String>("Album Artist") { it.value.second?.albumArtist?.name.toProperty() }
            }
        }
    }
}