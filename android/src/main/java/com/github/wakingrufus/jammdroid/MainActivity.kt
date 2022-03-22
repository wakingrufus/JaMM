package com.github.wakingrufus.jammdroid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(getAllAudioFromDevice(this).flatMap {
                val audioFile = AudioFileIO.read(it)
                val tag = audioFile.tag
                tag.getFirst(FieldKey.TAGS).split(",").filter { it.isNotBlank() }
            }.toSet())
        }
    }
}
