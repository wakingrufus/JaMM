package com.github.wakingrufus.javafx

import javafx.collections.*

private class MapKeyBinder<K, V>(val list: ObservableList<K>) : MapChangeListener<K, V> {

    override fun onChanged(change: MapChangeListener.Change<out K, out V>) {
        if (change.wasRemoved()) {
            list.remove(change.key)
        }
        if (change.wasAdded()) {
            list.add(change.key)
        }
    }
}

private class MapValueBinder<K, V>(val list: ObservableList<V>) : MapChangeListener<K, V> {

    override fun onChanged(change: MapChangeListener.Change<out K, out V>) {
        if (change.wasRemoved()) {
            list.remove(change.valueRemoved)
        }
        if (change.wasAdded()) {
            list.add(change.valueAdded)
        }
    }
}

fun <K, V> ObservableMap<K, V>.observableKeys(): ObservableList<K> {
    val list = FXCollections.observableArrayList(this.keys)
    this.addListener(MapKeyBinder(list))
    return FXCollections.unmodifiableObservableList(list)
}

fun <K, V> ObservableMap<K, V>.observableValues(): ObservableList<V> {
    val list = FXCollections.observableArrayList(this.values)
    this.addListener(MapValueBinder(list))
    return FXCollections.unmodifiableObservableList(list)
}