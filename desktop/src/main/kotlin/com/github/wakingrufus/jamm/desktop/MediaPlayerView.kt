package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.Track
import com.github.wakingrufus.javafx.*
import javafx.collections.ObservableList
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.ByteArrayInputStream

class MediaPlayerView(val queue: ObservableList<Track>) : BorderPane() {
    val nowPlayingArea = StackPane()
    var javaFxMediaPlayer: MediaPlayer? = null
    fun play() {
        if (javaFxMediaPlayer == null) {
            playNext()
        }
    }

    fun playNext() {
        queue.firstOrNull()?.also { track ->
            val uri = track.file.toURI().toASCIIString()
            val media = Media(uri)
            if (javaFxMediaPlayer?.status == MediaPlayer.Status.PLAYING) {
                javaFxMediaPlayer?.stop()
            }
            javaFxMediaPlayer = MediaPlayer(media)
            javaFxMediaPlayer?.play()
            javaFxMediaPlayer?.onEndOfMedia = Runnable {
                playNext()
            }
            nowPlayingArea.children.clear()
            nowPlayingArea.children.add(VBox().apply {
                track.image?.also {
                    imageView(Image(ByteArrayInputStream(it))) {
                        this.fitHeight = 128.0
                        this.fitWidth = 128.0
                    }
                }
                label(track.title) { style = "-fx-text-alignment: center;"}
                label(track.albumArtist.name) { style = "-fx-font-weight: bold; -fx-text-alignment: center;" }
                label(track.album) { style = "-fx-font-style: italic; -fx-text-alignment: center;" }
            })
            queue.remove(track)
        }
    }

    init {
        this.center = nowPlayingArea
        bottom<HBox> {
            button("Play") {
                action {
                    play()
                }
            }
        }
    }
}