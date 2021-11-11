package com.github.wakingrufus.javafx

import javafx.beans.value.*
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList


fun <T> ObservableValue<T>.onChange(op: (T?) -> Unit) = apply { addListener { o, oldValue, newValue -> op(newValue) } }
fun ObservableBooleanValue.onChange(op: (Boolean) -> Unit) = apply { addListener { o, old, new -> op(new ?: false) } }
fun ObservableIntegerValue.onChange(op: (Int) -> Unit) = apply { addListener { o, old, new -> op((new ?: 0).toInt()) } }
fun ObservableLongValue.onChange(op: (Long) -> Unit) = apply { addListener { o, old, new -> op((new ?: 0L).toLong()) } }
fun ObservableFloatValue.onChange(op: (Float) -> Unit) = apply {
    addListener { o, old, new ->
        op((new ?: 0f).toFloat())
    }
}

fun ObservableDoubleValue.onChange(op: (Double) -> Unit) = apply {
    addListener { o, old, new ->
        op((new ?: 0.0).toDouble())
    }
}

fun <T> ObservableList<T>.onChange(op: (ListChangeListener.Change<out T>) -> Unit) = apply {
    addListener(ListChangeListener { op(it) })
}
