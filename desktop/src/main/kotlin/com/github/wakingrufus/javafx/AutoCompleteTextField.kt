package com.github.wakingrufus.javafx

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.TextField
import java.util.*

class AutoCompleteTextField(val entries: ObservableList<String>) : TextField() {

    /** The popup used to select an entry.  */
    private var entriesPopup: ContextMenu = ContextMenu()

    init {
        textProperty().addListener(ChangeListener { observableValue: ObservableValue<out String?>?, s: String?, s2: String? ->
            if (text.length == 0) {
                entriesPopup.hide()
            } else {
                val searchResult = LinkedList<String>()
                searchResult.addAll(TreeSet(entries).subSet(text, text + Character.MAX_VALUE))
                if (entries.size > 0) {
                    populatePopup(searchResult)
                    if (!entriesPopup.isShowing) {
                        entriesPopup.show(this@AutoCompleteTextField, Side.BOTTOM, 0.0, 0.0)
                    }
                } else {
                    entriesPopup.hide()
                }
            }
        })

        focusedProperty().addListener { observableValue: ObservableValue<out Boolean?>?, aBoolean: Boolean?, aBoolean2: Boolean? ->
            entriesPopup.hide()
        }
    }


    /**
     * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
     * @param searchResult The set of matching strings.
     */
    private fun populatePopup(searchResult: List<String>) {
        val menuItems: MutableList<CustomMenuItem> = LinkedList()
        // If you'd like more entries, modify this line.
        val maxEntries = 10
        val count = Math.min(searchResult.size, maxEntries)
        for (i in 0 until count) {
            val result = searchResult[i]
            val entryLabel = Label(result)
            val item = CustomMenuItem(entryLabel, true)
            item.setOnAction {
                text = result
                entriesPopup.hide()
            }
            menuItems.add(item)
        }
        entriesPopup.items.clear()
        entriesPopup.items.addAll(menuItems)
    }
}