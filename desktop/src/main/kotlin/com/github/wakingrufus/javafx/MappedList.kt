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

private class ListMappingBinder<I, O>(val list: ObservableList<O>, val op: (I) -> O) : ListChangeListener<I> {

    override fun onChanged(change: ListChangeListener.Change<out I>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                list.removeAll(change.removed.map(op))
            }
            if (change.wasAdded()) {
                list.addAll(change.addedSubList.map(op))
            }
        }
    }
}

fun <E, F> ObservableList<E>.mapped(op: (E) -> F): ObservableList<F> {
    val list = FXCollections.observableArrayList(this.map(op))
    this.addListener(ListMappingBinder(list, op))
    return FXCollections.unmodifiableObservableList(list)
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