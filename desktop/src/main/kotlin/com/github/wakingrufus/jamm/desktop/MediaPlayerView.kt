package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaException
import javafx.scene.media.MediaPlayer
import javafx.scene.text.Font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

class MediaPlayerView(val queue: ObservableList<Track>, val library: ObservableLibrary) : BorderPane(), Logging {
    val nowPlayingArea = StackPane()
    var javaFxMediaPlayer: MediaPlayer? = null
    var clip: Clip? = null

    fun play(): Boolean {
        return if (javaFxMediaPlayer == null && clip == null) {
            playNext()
            true
        } else {
            return if (javaFxMediaPlayer?.status == MediaPlayer.Status.PLAYING) {
                javaFxMediaPlayer?.pause()
                false
            } else if (javaFxMediaPlayer?.status == MediaPlayer.Status.PAUSED) {
                javaFxMediaPlayer?.play()
                true
            } else if (clip?.isRunning == true) {
                clip?.stop()
                false
            } else {
                clip?.start()
                true
            }
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
                clip = null
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
                    clip?.addLineListener {
                        if (it.type == LineEvent.Type.STOP) {
                            playNext()
                        }
                    }
                    javaFxMediaPlayer = null
                } else {
                    logger().error(ex.message, ex)
                }
            }
            nowPlayingArea.children.clear()
            nowPlayingArea.children.add(VBox().apply {
                track.image?.also {
                    HBox().apply {
                        this.alignment = Pos.CENTER
                        imageView(Image(ByteArrayInputStream(it))) {
                            this.fitHeight = 256.0
                            this.fitWidth = 256.0
                        }
                    }.attachTo(this)

                }
                alignment = Pos.CENTER
                label(track.title) {
                    font = Font.font(14.0)
                    alignment = Pos.CENTER
                }
                label(track.albumArtist.name) {
                    style = "-fx-font-weight: bold;"
                    alignment = Pos.CENTER
                }
                label(track.album) {
                    style = "-fx-font-style: italic;"
                    alignment = Pos.CENTER
                }
            })
            GlobalScope.launch(Dispatchers.JavaFx) {
                queue.remove(track)
                if (queue.isEmpty() && getPreference(Preference.CONTINUOUS_PLAY, "false").toBoolean()) {
                    queue.add(library.tracks.random())
                }
            }
        }
    }

    init {
        this.center = nowPlayingArea
        bottom<HBox> {
            button("⏹️ Stop") {
                action {
                    stop()
                }
            }
            button("▶ Play️") {
                action {
                    if (play()) {
                        this.text = "⏸ Pause️️"
                    } else {
                        this.text = "▶ Play️"
                    }
                }
            }
            button("⏭️ Next") {
                action {
                    stop()
                    playNext()
                }
            }
        }
    }
}