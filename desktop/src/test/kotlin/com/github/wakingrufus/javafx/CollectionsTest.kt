package com.github.wakingrufus.javafx

import javafx.collections.FXCollections
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
}