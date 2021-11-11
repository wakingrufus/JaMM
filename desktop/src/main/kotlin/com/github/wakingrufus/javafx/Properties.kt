package com.github.wakingrufus.javafx

import javafx.beans.property.*

fun Double.toProperty(): DoubleProperty = SimpleDoubleProperty(this)
fun Float.toProperty(): FloatProperty = SimpleFloatProperty(this)
fun Long.toProperty(): LongProperty = SimpleLongProperty(this)
fun Int.toProperty(): IntegerProperty = SimpleIntegerProperty(this)
fun Boolean.toProperty(): BooleanProperty = SimpleBooleanProperty(this)
fun String.toProperty(): StringProperty = SimpleStringProperty(this)

fun String?.toProperty() = SimpleStringProperty(this ?: "")
fun Double?.toProperty() = SimpleDoubleProperty(this ?: 0.0)
fun Float?.toProperty() = SimpleFloatProperty(this ?: 0.0F)
fun Long?.toProperty() = SimpleLongProperty(this ?: 0L)
fun Boolean?.toProperty() = SimpleBooleanProperty(this ?: false)
fun <T : Any> T?.toProperty() = SimpleObjectProperty<T>(this)