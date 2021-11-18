package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.collections.ObservableList
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaException
import javafx.scene.media.MediaPlayer
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

class MediaPlayerView(val queue: ObservableList<Track>) : BorderPane(), Logging {
    val nowPlayingArea = StackPane()
    var javaFxMediaPlayer: MediaPlayer? = null
    var clip: Clip? = null

    fun play() {
        if (javaFxMediaPlayer == null) {
            playNext()
        }
    }

    fun stop() {
        javaFxMediaPlayer?.stop()
        javaFxMediaPlayer = null
        this@MediaPlayerView.clip?.stop()
        this@MediaPlayerView.clip = null
        nowPlayingArea.children.clear()
    }

    fun playNext() {
        queue.firstOrNull()?.also { track ->
            try {
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
            } catch (ex: MediaException) {
                if (ex.type == MediaException.Type.MEDIA_UNSUPPORTED) {
                    logger().info("unsupported file: ${track.file.name} falling back to Java SPI")
                    val stream = AudioSystem.getAudioInputStream(track.file)
                    val baseFormat: AudioFormat = stream.format

                    val targetFormat = AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED, baseFormat.sampleRate,
                        16, baseFormat.channels, baseFormat.channels * 2, baseFormat.sampleRate, false
                    )

                    val convertedStream = AudioSystem.getAudioInputStream(targetFormat, stream)
                    clip = AudioSystem.getClip()
                    clip?.open(convertedStream)
                    clip?.start()
                } else {
                    logger().error(ex.message, ex)
                }
            }
            nowPlayingArea.children.clear()
            nowPlayingArea.children.add(VBox().apply {
                track.image?.also {
                    imageView(Image(ByteArrayInputStream(it))) {
                        this.fitHeight = 128.0
                        this.fitWidth = 128.0
                    }
                }
                label(track.title) { style = "-fx-text-alignment: center;" }
                label(track.albumArtist.name) { style = "-fx-font-weight: bold; -fx-text-alignment: center;" }
                label(track.album) { style = "-fx-font-style: italic; -fx-text-alignment: center;" }
            })
            queue.remove(track)
        }
    }

    init {
        this.center = nowPlayingArea
        bottom<HBox> {
            button("Stop") {
                action {
                    stop()
                }
            }
            button("Play") {
                action {
                    play()
                }
            }
        }
    }
}