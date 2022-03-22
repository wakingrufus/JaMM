package com.github.wakingrufus.jammdroid

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun App(tags: Set<String>) {
    val tags = remember { mutableStateOf(tags.toList()) }
    MaterialTheme {
        Column {
            Row {

            }
            Row {
                Column {
                    Text("Tags")

                    LazyColumn {
                        items(count = tags.value.size, key = { tags.value.get(it) }) { tag ->
                            Row {
                                Text(tags.value[tag])
                                Button(
                                    onClick = {

                                    }) {
                                    Text("Play")
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

@Preview(device = "id:pixel_5")
@Composable
fun PreviewApp() {
    App(setOf("A", "B"))
}

