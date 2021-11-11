package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.Playlist
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback

class PlaylistCellFactory : Callback<ListView<Playlist>, ListCell<Playlist>> {
    override fun call(param: ListView<Playlist>?): ListCell<Playlist> {
        return object : ListCell<Playlist>() {
            override fun updateItem(person: Playlist?, empty: Boolean) {
                super.updateItem(person, empty)
                text = if (empty || person == null) {
                    null
                } else {
                    person.name
                }
            }
        }
    }
}

class CustomStringCellFactory<T>(val value: (T) -> String) :  Callback<ListView<T>, ListCell<T>>{
    override fun call(param: ListView<T>?): ListCell<T> {
        return object : ListCell<T>() {
            override fun updateItem(person: T?, empty: Boolean) {
                super.updateItem(person, empty)
                text = if (empty || person == null) {
                    null
                } else {
                    value(person)
                }
            }
        }
    }
}