package com.github.wakingrufus.javafx

import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CollectionsTest {
    @Test
    fun `test grouped init`() {
        val source = FXCollections.observableArrayList("a", "b", "cd", "ef")
        val lengths = source.grouped { it.length }
        assertThat(lengths).containsExactly(1, 2)
    }

    @Test
    fun `test grouped add`() {
        val source = FXCollections.observableArrayList<String>()
        val lengths = source.grouped { it.length }
        source.add("a")
        assertThat(lengths).containsExactly(1)
        source.add("b")
        assertThat(lengths).containsExactly(1)
        source.add("ac")
        assertThat(lengths).containsExactly(1, 2)
        source.add("as")
        assertThat(lengths).containsExactly(1, 2)
    }

    @Test
    fun `test grouped remove`() {
        val source = FXCollections.observableArrayList("a", "b", "cd", "ef")
        val lengths = source.grouped { it.length }
        assertThat(lengths).containsExactly(1, 2)
        source.remove("cd")
        assertThat(lengths).containsExactly(1, 2)
        source.remove("ef")
        assertThat(lengths).containsExactly(1)
    }

    @Test
    fun `test flattened init`() {
        val source = FXCollections.observableArrayList(listOf("a", "b"), listOf("cd", "ef"))
        val lengths = source.flattened()
        assertThat(lengths).containsExactly("a", "b", "cd", "ef")
    }

    @Test
    fun `test flattened add`() {
        val source = FXCollections.observableArrayList<List<String>>()
        val lengths = source.flattened()
        source.add(listOf("a", "b"))
        assertThat(lengths).containsExactly("a", "b")
        source.add(listOf("b", "c"))
        assertThat(lengths).containsExactly("a", "b", "b", "c")
    }

    @Test
    fun `test flattened remove`() {
        val source = FXCollections.observableArrayList(listOf("a", "b"), listOf("b", "c"))
        val lengths = source.flattened()
        source.remove(1, 2)
        assertThat(lengths).containsExactly("a", "b")
    }

    @Test
    fun `test toObservableList init`() {
        val source = FXCollections.observableSet("a", "b", "cd", "ef")
        val lengths = source.toObservableList()
        assertThat(lengths).containsExactlyInAnyOrder("a", "b", "cd", "ef")
    }

    @Test
    fun `test toObservableList add`() {
        val source = FXCollections.observableSet<String>()
        val lengths = source.toObservableList()
        source.add("a")
        assertThat(lengths).containsExactly("a")
        source.add("b")
        assertThat(lengths).containsExactly("a", "b")
        source.add("a")
        assertThat(lengths).containsExactly("a", "b")
    }

    @Test
    fun `test toObservableList remove`() {
        val source: ObservableSet<String> = FXCollections.observableSet("a", "b", "cd", "ef")
        val lengths = source.toObservableList()
        source.remove("a")
        assertThat(lengths).containsExactlyInAnyOrder("b", "cd", "ef")
    }

    @Test
    fun `test unique init`() {
        val source = FXCollections.observableArrayList("a", "b", "a", "c")
        val lengths = source.unique()
        assertThat(lengths).containsExactly("a", "b", "c")
    }

    @Test
    fun `test unique add`() {
        val source = FXCollections.observableArrayList<String>()
        val lengths = source.unique()
        source.add("a")
        assertThat(lengths).containsExactly("a")
        source.add("b")
        assertThat(lengths).containsExactly("a", "b")
        source.add("a")
        assertThat(lengths).containsExactly("a", "b")
    }

    @Test
    fun `test unique remove`() {
        val source = FXCollections.observableArrayList("a", "b", "cd", "ef")
        val lengths = source.unique()
        source.remove("cd")
        assertThat(lengths).containsExactly("a", "b", "ef")
    }

    data class Container(val s: List<String>)

    @Test
    fun `test chain flatmap and unique`() {
        val source = FXCollections.observableArrayList<Container>()
        val lengths = source.flatMapped { it.s }.unique()
        source.add(Container(listOf("a")))
        assertThat(lengths).containsExactly("a")
        source.add(Container(listOf("a", "b")))
        assertThat(lengths).containsExactly("a", "b")
    }
}
