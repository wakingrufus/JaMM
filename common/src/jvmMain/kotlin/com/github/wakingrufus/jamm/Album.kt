package com.github.wakingrufus.jamm

import java.net.URI
import java.time.LocalDate

data class Album( val artist: AlbumArtist, val name: String, val releaseDate: LocalDate?, val coverImage: ByteArray?)