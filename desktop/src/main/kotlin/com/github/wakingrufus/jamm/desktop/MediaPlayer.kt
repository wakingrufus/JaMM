package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track

interface MediaPlayerController {
    fun play(tracks: List<Track>)

    fun queue(tracks: List<Track>)
}