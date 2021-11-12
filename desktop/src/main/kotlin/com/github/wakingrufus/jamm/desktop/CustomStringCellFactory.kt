package com.github.wakingrufus.jamm.desktop

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback

class CustomStringCellFactory<T>(val value: (T) -> String) : Callback<ListView<T>, ListCell<T>> {
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