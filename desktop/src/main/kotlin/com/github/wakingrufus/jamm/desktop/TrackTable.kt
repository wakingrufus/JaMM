package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.SelectionMode
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane

fun Pane.trackTable(
    items: ObservableList<Track>,
    library: ObservableLibrary,
    mediaPlayer: MediaPlayerController,
    actions: List<Pair<String, (Track) -> Unit>> = listOf()
): BorderPane {
    return BorderPane().attachTo(this) {
        var tv: TableView<Track>? = null
        center<StackPane> {
            tv = tableView(ReadOnlyListWrapper(items)) {
                column<Track, String>("Title") { it.value.title.toProperty() }
                column<Track, String>("Album Artist") { it.value.albumArtist.name.toProperty() }
                column<Track, String>("Album") { it.value?.album.toProperty() }
                column<Track, Int>("Disc") { it.value.discNumber.toProperty() }
                column<Track, Int>("Track #") { it.value.trackNumber.toProperty() }
                column<Track, String>("Artist"){it.value.artist.name.toProperty()}
                column<Track, Int>("Year") { it.value.releaseDate?.year.toProperty() }
                column<Track, String>("Tags") { it.value.tags.joinToString(",").toProperty() }
                contextMenu {

                    if (actions.isNotEmpty()) {
                        actions.forEach { pair ->
                            actionItem(pair.first) {
                                this@tableView.selectionModel?.selectedItems?.run {
                                    this.forEach {
                                        pair.second.invoke(it)
                                    }
                                }
                            }
                        }
                        this.items.add(SeparatorMenuItem())
                    }
                    actionItem("Play") {
                        this@tableView.selectionModel?.selectedItems?.run {
                            mediaPlayer.play(this)
                        }
                    }
                    actionItem("Enqueue") {
                        this@tableView.selectionModel?.selectedItems?.run {
                            mediaPlayer.queue(this)
                        }
                    }
                    val subMenu = subMenu("Add Tag") {

                    }
                    this.onShown = EventHandler {
                        subMenu.items.clear()
                        library.tracks.flatMappedUnique { it.tags }.sorted().forEach { tag ->
                            subMenu.actionItem(tag) {
                                this@tableView.selectionModel?.selectedItems?.run {
                                    this.forEach {
                                        library.setTags(it, it.tags.plus(tag))
                                    }
                                }
                            }
                        }
                    }
                }
                autoResize()
                this.selectionModel.selectionModeProperty().set(SelectionMode.MULTIPLE)
            }
        }
        bottom<HBox> {
            button("Play All") {
                action {
                    mediaPlayer.play(tv?.items ?: listOf())
                }
            }
            button("Shuffle Play All") {
                action {
                    mediaPlayer.play(tv?.items?.shuffled() ?: listOf())
                }
            }
            button("Enqueue All") {
                action {
                    mediaPlayer.queue(tv?.items ?: listOf())
                }
            }
            button("Play Selected") {
                action {
                    tv?.selectionModel?.selectedItems?.run {
                        mediaPlayer.play(this)
                    }
                }
            }
            button("Enqueue Selected") {
                action {
                    tv?.selectionModel?.selectedItems?.run {
                        mediaPlayer.queue(this)
                    }
                }
            }
        }
    }
}