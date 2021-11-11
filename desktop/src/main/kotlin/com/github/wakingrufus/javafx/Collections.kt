package com.github.wakingrufus.javafx

import javafx.beans.WeakListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import java.lang.ref.WeakReference


/**
 * Bind this list to the given observable list by converting them into the correct type via the given converter.
 * Changes to the observable list are synced.
 */
fun <SourceType, TargetType> MutableList<TargetType>.bind(sourceList: ObservableList<SourceType>, converter: (SourceType) -> TargetType): ListConversionListener<SourceType, TargetType> {
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
class ListConversionListener<SourceType, TargetType>(targetList: MutableList<TargetType>,
                                                     val converter: (SourceType) -> TargetType) : ListChangeListener<SourceType>,
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