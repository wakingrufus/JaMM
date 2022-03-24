package com.github.wakingrufus.javafx

import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.control.skin.TableColumnHeader
import javafx.scene.control.skin.TableHeaderRow
import javafx.scene.control.skin.TableViewSkinBase
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.util.Callback
import java.lang.reflect.InvocationTargetException


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

inline fun <T : Node> opcr(parent: Group, node: T, op: T.() -> Unit = {}) = node.apply {
    parent.children.add(this)
    op(this)
}

/**
 * Attaches the node to the pane and invokes the node operation.
 */
inline fun <T : Node> T.attachTo(parent: Pane, op: T.() -> Unit = {}): T = opcr(parent, this, op)

/**
 * Attaches the node to the pane and invokes the node operation.
 */
inline fun <T : Node> T.attachTo(parent: Group, op: T.() -> Unit = {}): T = opcr(parent, this, op)

/**
 * Attaches the node to the pane and invokes the node operation.
 * Because the framework sometimes needs to setup the node, another lambda can be provided
 */
inline fun <T : Node> T.attachTo(
    parent: Pane,
    after: T.() -> Unit,
    before: T.() -> Unit
) = this.apply(before).attachTo(parent, after)

fun <T, N : Pane> N.bind(property: Property<T>, block: N.(value: T?) -> Unit) {
    property.onChange {
        this.children.clear()
        this.block(it)
    }
}

fun <T> TableView<out T>.bindSelected(model: ObjectProperty<in T>) {
    selectionModel.selectedItemProperty().onChange {
        model.set(it)
    }
}

fun <T> Pane.listview(values: ObservableList<T>? = null, op: ListView<T>.() -> Unit = {}) =
    ListView<T>().attachTo(this, op) {
        items = values
    }

fun Pane.buttonBar(block: ButtonBar.() -> Unit): ButtonBar {
    return ButtonBar().attachTo(this, block)
}

fun <T> ListView<T>.bindSelected(property: Property<T>) {
    selectionModel.selectedItemProperty().onChange {
        property.value = it
    }
}

fun Pane.button(text: String, block: Button.() -> Unit): Button {
    return Button(text).attachTo(this, block)
}

fun Pane.button(image: ImageView, block: Button.() -> Unit): Button {
    return Button(null, image).attachTo(this, block)
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

inline fun <reified T : Node> Group.add(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().attachTo(this, block)
}

inline fun <reified T : Node> Pane.add(block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().attachTo(this, block) {

    }
}

inline fun <reified T : Node> GridPane.add(column: Int, row: Int, block: T.() -> Unit = {}): T {
    return T::class.constructors.first { it.parameters.isEmpty() }.call().apply(block).also {
        this.add(it, column, row)
    }
}

fun Pane.autoComplete(
    entries: ObservableList<String>,
    block: AutoCompleteTextField.() -> Unit = {}
): AutoCompleteTextField {
    return AutoCompleteTextField(entries).attachTo(this, block)
}

inline fun Pane.imageView(image: Image, block: ImageView.() -> Unit = {}): ImageView {
    return ImageView(image).apply(block).attachTo(this)
}

/**
 * Create a column with a value factory that extracts the value from the given callback.
 */
fun <S, T> TableView<S>.column(
    title: String,
    valueProvider: (TableColumn.CellDataFeatures<S, T>) -> ObservableValue<T>
): TableColumn<S, T> {
    val column = TableColumn<S, T>(title).apply {
        //TODO smart language detection
        this.style = "-fx-font-family: 'Noto Sans CJK JP';"
    }
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

fun Pane.label(text: String, config: Label.() -> Unit): Label {
    return this.add {
        this.text = text
        config(this)
    }
}

inline fun <reified T> Pane.tableView(
    items: ObservableList<T>,
    config: TableView<T>.() -> Unit = {}
): TableView<T> {
    return TableView(items).attachTo(this, config)
}

fun ContextMenu.actionItem(name: String, onAction: EventHandler<ActionEvent>) {
    items.add(MenuItem(name).apply {
        setOnAction(onAction)
    })
}

fun Menu.actionItem(name: String, onAction: EventHandler<ActionEvent>): MenuItem {
    return MenuItem(name).apply {
        setOnAction(onAction)
    }.also { items.add(it) }
}

fun ContextMenu.subMenu(name: String, block: Menu.() -> Unit): Menu {
    return Menu(name).apply(block).also { items.add(it) }
}

fun Control.contextMenu(builder: ContextMenu.() -> Unit) {
    this.contextMenu = ContextMenu().apply(builder)
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

fun Pane.menuBar(menuBar: MenuBar.() -> Unit) {
    MenuBar().apply(menuBar).attachTo(this)
}

fun MenuBar.menu(name: String, menu: Menu.() -> Unit): Menu {
    return Menu(name).apply(menu).also { this.menus.add(it) }
}

fun Menu.subMenu(name: String, menu: Menu.() -> Unit): Menu {
    return Menu(name).apply(menu).also { this.items.add(it) }
}
class TOGGLE(val menu: Menu, val toggleGroup: ToggleGroup){
    fun radio(name: String, radio: RadioMenuItem.() -> Unit){
        RadioMenuItem(name).apply(radio).apply {
            menu.items.add(this)
            this.toggleGroup = toggleGroup
        }
    }
}
fun Menu.toggleGroup(toggleGroup: TOGGLE.() -> Unit){
    TOGGLE(this, ToggleGroup()).apply(toggleGroup)
}
fun Menu.item(name: String, item: MenuItem.() -> Unit): MenuItem {
    return MenuItem(name).apply(item).also { this.items.add(it) }
}

fun Menu.separator(item: SeparatorMenuItem.() -> Unit): MenuItem {
    return SeparatorMenuItem().apply(item).also { this.items.add(it) }
}

fun Menu.checkItem(name: String, item: CheckMenuItem.() -> Unit): CheckMenuItem {
    return CheckMenuItem(name).apply(item).also { this.items.add(it) }
}

fun TabPane.tab(name: String, content: () -> Pane) : Tab {
   return Tab(name, content.invoke()).also {
        this.tabs.add(it)
    }
}

val resizeMethod = TableColumnHeader::class.java.getDeclaredMethod("resizeColumnToFitContent", Int::class.java).apply {
    this.isAccessible = true
}
val headerMethod = TableViewSkinBase::class.java.getDeclaredMethod("getTableHeaderRow").apply {
    this.isAccessible = true
}
val headerCol = TableHeaderRow::class.java.getDeclaredMethod("getColumnHeaderFor", TableColumnBase::class.java).apply {
    this.isAccessible = true
}

fun <T> TableView<T>.autoResize() {
    items.addListener(ListChangeListener {
        for (column in columns) {
            try {
                resizeMethod.invoke(
                    headerCol.invoke(
                        headerMethod.invoke(this.skin as TableViewSkinBase<*, *, *, *, *>) as TableHeaderRow,
                        column
                    ) as TableColumnHeader, -1
                )
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
    })
}