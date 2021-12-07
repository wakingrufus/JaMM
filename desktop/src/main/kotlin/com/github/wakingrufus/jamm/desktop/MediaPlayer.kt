package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import javafx.beans.property.DoubleProperty
import javafx.beans.property.Property
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
interface MediaPlayerController {
    fun play(tracks: List<Track>)

    fun stop()

    fun next()

    fun play(): Boolean

    fun queue(tracks: List<Track>)

    fun getNowPlayingProperty(): Property<Track>
    fun getProgressProperty(): DoubleProperty
    fun getProgress(): Double
    fun getCurrentPosition(): Duration
    fun getTotalDuration(): Duration
}