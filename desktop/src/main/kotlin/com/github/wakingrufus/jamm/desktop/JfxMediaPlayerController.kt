package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.media.Media
import javafx.scene.media.MediaException
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import javax.sound.sampled.*

class JfxMediaPlayerController(
    val queue: ObservableList<Track>,
    val library: ObservableLibrary
) :
    MediaPlayerController, Logging {
    val nowPlayingProperty = SimpleObjectProperty<Track>()
    override fun play(tracks: List<Track>) {
        logger().info("adding ${tracks.size} to queue")
        stop()
        queue.clear()
        queue.addAll(tracks)
        play()
    }

    override fun next() {
        stop()
        playNext()
    }

    override fun queue(tracks: List<Track>) {
        logger().info("adding ${tracks.size} to queue")
        queue.addAll(tracks)
    }

    override fun getNowPlayingProperty(): Property<Track> {
        return nowPlayingProperty
    }

    var javaFxMediaPlayer: MediaPlayer? = null
    var clip: Clip? = null

    override fun play(): Boolean {
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
private val stopListener : LineListener = LineListener{
        if (it.type == LineEvent.Type.STOP) {
            playNext()
        }
}
    private fun playNext() {
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
                    clip?.addLineListener(stopListener)
                    javaFxMediaPlayer = null
                } else {
                    logger().error(ex.message, ex)
                }
            }
            GlobalScope.launch(Dispatchers.JavaFx) {
                nowPlayingProperty.set(track)
                queue.remove(track)
                if (queue.isEmpty() && getPreference(Preference.CONTINUOUS_PLAY, "false").toBoolean()) {
                    queue.add(library.tracks.random())
                }
            }
        }
    }

    override fun stop() {
        javaFxMediaPlayer?.stop()
        javaFxMediaPlayer = null
        clip?.removeLineListener(stopListener)
        clip?.stop()
        clip = null
        GlobalScope.launch(Dispatchers.JavaFx) {
            nowPlayingProperty.set(null)
        }
    }
}