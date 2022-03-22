package com.github.wakingrufus.javafx

import javafx.beans.WeakListener
import javafx.collections.*
import java.lang.ref.WeakReference


/**
 * Bind this list to the given observable list by converting them into the correct type via the given converter.
 * Changes to the observable list are synced.
 */
fun <SourceType, TargetType> MutableList<TargetType>.bind(
    sourceList: ObservableList<SourceType>,
    converter: (SourceType) -> TargetType
): ListConversionListener<SourceType, TargetType> {
    val ignoringParentConverter: (SourceType) -> TargetType = {
        converter(it)
    }
    val listener = ListConversionListener(this, ignoringParentConverter)
    (this as? ObservableList<TargetType>)?.setAll(sourceList.map(ignoringParentConverter)) ?: run {
        clear()
        addAll(sourceList.map(ignoringParentConverter))
    }
    sourceList.removeListener(listener)
    sourceList.addListener(listener)
    return listener
}


/**
 * Listens to changes on a list of SourceType and keeps the target list in sync by converting
 * each object into the TargetType via the supplied converter.
 */
class ListConversionListener<SourceType, TargetType>(
    targetList: MutableList<TargetType>,
    val converter: (SourceType) -> TargetType
) : ListChangeListener<SourceType>,
    WeakListener {
    internal val targetRef: WeakReference<MutableList<TargetType>> = WeakReference(targetList)

    override fun onChanged(change: ListChangeListener.Change<out SourceType>) {
        val list = targetRef.get()
        if (list == null) {
            change.list.removeListener(this)
        } else {
            while (change.next()) {
                if (change.wasPermutated()) {
                    list.subList(change.from, change.to).clear()
                    list.addAll(change.from, change.list.subList(change.from, change.to).map(converter))
                } else {
                    if (change.wasRemoved()) {
                        list.subList(change.from, change.from + change.removedSize).clear()
                    }
                    if (change.wasAdded()) {
                        list.addAll(change.from, change.addedSubList.map(converter))
                    }
                }
            }
        }
    }

    override fun wasGarbageCollected() = targetRef.get() == null

    override fun hashCode() = targetRef.get().hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        val ourList = targetRef.get() ?: return false

        if (other is ListConversionListener<*, *>) {
            val otherList = other.targetRef.get()
            return ourList === otherList
        }
        return false
    }
}

private class ListGroupingBinder<I, O>(val list: ObservableList<O>, val op: (I) -> O) : ListChangeListener<I> {

    override fun onChanged(change: ListChangeListener.Change<out I>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.map(op).forEach { group ->
                    if (change.list.none { op(it) == group }) {
                        list.removeAll(group)
                    }
                }
            }
            if (change.wasAdded()) {
                change.addedSubList.map(op).forEach { group ->
                    if (list.none { it == group }) {
                        list.addAll(group)
                    }
                }
            }
        }
    }
}

private class ListUniqueBinder<T>(val list: ObservableList<T>) : ListChangeListener<T> {

    override fun onChanged(change: ListChangeListener.Change<out T>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.forEach { group ->
                    if (change.list.none { it == group }) {
                        list.removeAll(group)
                    }
                }
            }
            if (change.wasAdded()) {
                change.addedSubList.forEach { group ->
                    if (list.none { it == group }) {
                        list.addAll(group)
                    }
                }
            }
        }
    }
}

private class SetListBinder<T>(val list: ObservableList<T>) : SetChangeListener<T> {

    override fun onChanged(change: SetChangeListener.Change<out T>) {
        if (change.wasRemoved()) {
            list.removeAll(change.elementRemoved)
        }
        if (change.wasAdded()) {
            list.addAll(change.elementAdded)
        }
    }
}

private class ListFlattenBinder<I : Collection<O>, O>(val list: ObservableList<O>) : ListChangeListener<I> {

    override fun onChanged(change: ListChangeListener.Change<out I>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.flatten().forEach { group ->
                    list.remove(list.indexOf(group), list.indexOf(group) + 1)
                }
            }
            if (change.wasAdded()) {
                change.addedSubList.flatten().forEach { group ->
                    list.add(group)
                }
            }
        }
    }
}

private class ListFlatMappedBinder<I, O>(val list: ObservableList<O>, val op: (I) -> Collection<O>) :
    ListChangeListener<I> {

    override fun onChanged(change: ListChangeListener.Change<out I>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.flatMap(op).forEach { group ->
                    list.remove(list.indexOf(group), list.indexOf(group) + 1)
                }
            }
            if (change.wasAdded()) {
                change.addedSubList.flatMap(op).forEach { group ->
                    list.add(group)
                }
            }
        }
    }
}

private class ListFlatMappedUniqueBinder<I, O>(val list: ObservableList<O>, val op: (I) -> Collection<O>) :
    ListChangeListener<I> {

    override fun onChanged(change: ListChangeListener.Change<out I>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.flatMap(op).forEach { group ->
                    if (change.list.none { it == group }) {
                        list.remove(group)
                    }
                }
            }
            if (change.wasAdded()) {
                change.addedSubList.flatMap(op).forEach { group ->
                    if (list.none { it == group }) {
                        list.add(group)
                    }
                }
            }
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

fun <E, F> ObservableList<E>.grouped(op: (E) -> F): ObservableList<F> {
    val list = FXCollections.observableList(this.map(op).toSet().toMutableList())
    this.addListener(ListGroupingBinder(list, op))
    return FXCollections.unmodifiableObservableList(list)
}

fun <E> ObservableList<E>.unique(): ObservableList<E> {
    val list = FXCollections.observableList(this.toSet().toMutableList())
    this.addListener(ListUniqueBinder(list))
    return FXCollections.unmodifiableObservableList(list)
}

fun <I, O> ObservableList<I>.flatMapped(op: (I) -> Collection<O>): ObservableList<O> {
    val list: ObservableList<O> = FXCollections.observableList(this.flatMap(op).toMutableList())
    this.addListener(ListFlatMappedBinder(list, op))
    return FXCollections.unmodifiableObservableList(list)
}

fun <I, O> ObservableList<I>.flatMappedUnique(op: (I) -> Collection<O>): ObservableList<O> {
    val list: ObservableList<O> = FXCollections.observableList(this.flatMap(op).toSet().toMutableList())
    this.addListener(ListFlatMappedUniqueBinder(list, op))
    return FXCollections.unmodifiableObservableList(list)
}

fun <I, O> List<I>.flatMapUnique(op: (I) -> Collection<O>): List<O> {
    return this.flatMap(op).toSet().toMutableList()
}

fun <E : Collection<F>, F> ObservableList<E>.flattened(): ObservableList<F> {
    val list = FXCollections.observableList(this.flatten().toMutableList())
    this.addListener(ListFlattenBinder(list))
    return FXCollections.unmodifiableObservableList(list)
}

fun <E> ObservableSet<E>.toObservableList(): ObservableList<E> {
    val list = FXCollections.observableList(this.toMutableList())
    this.addListener(SetListBinder(list))
    return FXCollections.unmodifiableObservableList(list)
}

fun <E> ObservableSet<E>.sorted(): ObservableList<E> {
    return this.toObservableList().sorted()
}
