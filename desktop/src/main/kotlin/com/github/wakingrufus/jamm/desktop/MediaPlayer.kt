package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import javafx.beans.property.Property

interface MediaPlayerController {
    fun play(tracks: List<Track>)

    fun stop()

    fun next()

    fun play(): Boolean

    fun queue(tracks: List<Track>)

    fun getNowPlayingProperty(): Property<Track>
}