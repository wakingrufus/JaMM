package com.github.wakingrufus.javafx

import com.github.wakingrufus.javafx.onChange
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.util.Callback

@DslMarker
annotation class JavaFxDsl

/**
 * Add the given node to the pane, invoke the node operation and return the node. The `opcr` name
 * is an acronym for "op connect & return".
 */
inline fun <T : Node> opcr(parent: Pane, node: T, op: T.() -> Unit = {}) = node.apply {
    parent.children.add(this)
    op(this)
}

/**
 * Attaches the node to the pane and invokes the node operation.
 */
inline fun <T : Node> T.attachTo(parent: Pane, op: T.() -> Unit = {}): T = opcr(parent, this, op)

/**
 * Attaches the node to the pane and invokes the node operation.
 * Because the framework sometimes needs to setup the node, another lambda can be provided
 */
inline fun <T : Node> T.attachTo(
    parent: Pane,
    after: T.() -> Unit,
    before: (T) -> Unit
) = this.also(before).attachTo(parent, after)

fun <T> Node.bind(list: ObservableList<T>, block: (change: ListChangeListener.Change<out T>) -> Unit) {
    list.addListener(ListChangeListener { block(it) })
}

fun <T> TableView<out T>.bindSelected(model: ObjectProperty<in T>) {
    selectionModel.selectedItemProperty().onChange {
        model.set(it)
    }
}

fun <T> Pane.listview(values: ObservableList<T>? = null, op: ListView<T>.() -> Unit = {}) =
    ListView<T>().attachTo(this, op) {
        it.items = values
    }

fun Pane.buttonBar(block: ButtonBar.() -> Unit): ButtonBar {
    return ButtonBar().apply(block).also {
        this.children.add(it)
    }
}

fun <T> ListView<T>.bindSelected(property: Property<T>) {
    selectionModel.selectedItemProperty().onChange {
        property.value = it
    }
}

fun Pane.button(text: String, block: Button.() -> Unit): Button {
    return Button(text).apply(block).also {
        this.children.add(it)
    }
}

fun Group.buttonBar(block: ButtonBar.() -> Unit): ButtonBar {
    return ButtonBar().apply(block).also {
        this.children.add(it)
    }
}

fun Group.button(text: String, block: Button.() -> Unit): Button {
    return Button(text).apply(block).also {
        this.children.add(it)
    }
}

@JavaFxDsl
inline fun <reified T : Node> Group.add(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.children.add(it)
    }
}

@JavaFxDsl
inline fun <reified T : Node> Pane.add(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.children.add(it)
    }
}

@JavaFxDsl
inline fun Pane.imageView(image: Image, block: ImageView.() -> Unit = {}): ImageView {
    return ImageView(image).apply(block).also {
        this.children.add(it)
    }
}


/**
 * Create a column with a value factory that extracts the value from the given callback.
 */
fun <S, T> TableView<S>.column(
    title: String,
    valueProvider: (TableColumn.CellDataFeatures<S, T>) -> ObservableValue<T>
): TableColumn<S, T> {
    val column = TableColumn<S, T>(title)
    column.cellValueFactory = Callback { valueProvider(it) }
    addColumnInternal(column)
    return column
}

@Suppress("UNCHECKED_CAST")
fun <S> TableView<S>.addColumnInternal(column: TableColumn<S, *>, index: Int? = null) {
    val columnTarget = properties["tornadofx.columnTarget"] as? ObservableList<TableColumn<S, *>> ?: columns
    if (index == null) columnTarget.add(column) else columnTarget.add(index, column)
}

fun Pane.label(text: String): Label {
    return this.add {
        this.text = text
    }
}

inline fun <reified T> Pane.tableView(
    items: ObservableList<T>,
    config: TableView<T>.() -> Unit = {}
): TableView<T> {
    val tv = TableView(items)
    this.children.add(tv)
    return tv
}

inline fun <reified T : Node> BorderPane.right(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.right = it
    }
}

inline fun <reified T : Node> BorderPane.left(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.left = it
    }
}

inline fun <reified T : Node> BorderPane.top(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.top = it
    }
}

inline fun <reified T : Node> BorderPane.bottom(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.bottom = it
    }
}

inline fun <reified T : Node> BorderPane.center(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.center = it
    }
}

inline fun <reified T : Parent> scene(
    width: Double = -1.0, height: Double = -1.0,
    depthBuffer: Boolean = false,
    antiAliasing: SceneAntialiasing = SceneAntialiasing.DISABLED, block: T.() -> Unit
): Scene {
    return Scene(
        T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block),
        width,
        height,
        depthBuffer,
        antiAliasing
    )
}

fun Button.action(action: (ActionEvent) -> Unit) {
    this.onAction = EventHandler(action)
}