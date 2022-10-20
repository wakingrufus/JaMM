package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.jamm.lastfm.LastFmClient
import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.scene.media.Media
import javafx.scene.media.MediaException
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.time.Instant
import javax.sound.sampled.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@OptIn(ExperimentalTime::class)
class JfxMediaPlayerController(
    val queue: ObservableList<Track>,
    val library: ObservableLibrary,
    val lastFm: Property<LastFmClient>
) :
    MediaPlayerController, Logging {
    val nowPlayingProperty = SimpleObjectProperty<Track>()
    val progressProperty = SimpleDoubleProperty()
    val scrobbledProperty = SimpleBooleanProperty()
    override fun getProgress(): Double {
        return javaFxMediaPlayer?.let { it.currentTime.toSeconds().div(it.totalDuration.toSeconds()) }
            ?: clip?.let { it.microsecondPosition.toDouble().div(it.microsecondLength.toDouble()) } ?: 0.0
    }

    override fun getCurrentPosition(): Duration {
        return javaFxMediaPlayer?.let { it.currentTime.toSeconds().toDuration(DurationUnit.SECONDS) }
            ?: clip?.let { it.microsecondPosition.toDuration(DurationUnit.MICROSECONDS) } ?: Duration.ZERO
    }

    override fun getTotalDuration(): Duration {
        val mp3Duration = javaFxMediaPlayer?.let {
            it.totalDuration.toSeconds().let { if (it.isNaN()) null else it.toDuration(DurationUnit.SECONDS) }
        }
        val oggDuration = clip?.microsecondLength?.toDuration(DurationUnit.MICROSECONDS)
        return mp3Duration ?: oggDuration ?: Duration.ZERO
    }

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

    override fun getProgressProperty(): DoubleProperty {
        return progressProperty
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

    private val stopListener: LineListener = LineListener {
        if (it.type == LineEvent.Type.STOP) {
            playNext()
        }
    }

    private fun playNext() {
        queue.firstOrNull()?.also { track ->
            if (!track.file.exists()) {
                library.removeTrack(track)
                playNext()
            }
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
                    library.removeTrack(track)
                    playNext()
                }
            }
            scrobbledProperty.set(false)
            GlobalScope.launch(Dispatchers.Default) {
                lastFm.value?.nowPlaying(track)
                val lastFmCount = lastFm.value.getPlaycount(track) ?: 0
                if (lastFmCount > track.playCount) {
                    library.updatePlayCount(track, lastFmCount)
                }
            }
            GlobalScope.launch(Dispatchers.JavaFx) {
                nowPlayingProperty.set(track)
                updateProgress(track)
                queue.remove(track)
                if (queue.isEmpty()) {
                    val continuousMode = getPreference(Preference.CONTINUOUS_MODE, ContinuousMode.OFF)
                    if (continuousMode == ContinuousMode.RANDOM) {
                        queue.add(library.tracks.random())
                    } else if (continuousMode == ContinuousMode.TAG) {
                        queue.add(library.tracks
                            .filter { it.tags.any { track.tags.contains(it) } }
                            .randomOrNull()
                            ?: library.tracks.random())
                    }
                }
            }
        }
    }

    fun updateProgress(track: Track) {
        GlobalScope.launch(Dispatchers.Default) {
            if ((clip != null || javaFxMediaPlayer != null) && nowPlayingProperty.get() == track) {
                if (!scrobbledProperty.get() && (getCurrentPosition().toInt(DurationUnit.SECONDS) > 30
                            || (getTotalDuration().toInt(DurationUnit.SECONDS) < 30 && getProgress() > 0.5))
                ) {
                    scrobbledProperty.set(true)
                    lastFm.value?.scrobble(
                        Instant.now().minusSeconds(getCurrentPosition().toLong(DurationUnit.SECONDS)), track
                    )
                    library.countPlay(track)
                }
                progressProperty.set(getProgress())
                delay(100.toDuration(DurationUnit.MILLISECONDS))
                updateProgress(track)
            }
        }
    }

    override fun stop() {
        javaFxMediaPlayer?.stop()
        javaFxMediaPlayer = null
        clip?.removeLineListener(stopListener)
        clip?.stop()
        clip?.close()
        clip = null
        GlobalScope.launch(Dispatchers.JavaFx) {
            nowPlayingProperty.set(null)
        }
    }
}