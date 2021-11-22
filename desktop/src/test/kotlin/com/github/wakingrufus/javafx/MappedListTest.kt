package com.github.wakingrufus.javafx

import javafx.collections.FXCollections
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


internal class MappedListTest {
    @Test
    fun test_add() {
        val source = FXCollections.observableArrayList<String>()
        val mappedList = source.mapped { it.length }
        assertThat(mappedList).isEmpty()
        source.add("test")
        assertThat(mappedList).hasSize(1)
        assertThat(mappedList).containsExactly(4)

    }

    @Test
    fun test_remove() {
        val source = FXCollections.observableArrayList("test")
        val mappedList = source.mapped { it.length }
        assertThat(mappedList).hasSize(1)
        assertThat(mappedList).containsExactly(4)
        source.remove(0,1)
        assertThat(source).isEmpty()
        assertThat(mappedList).isEmpty()
    }

    @Test
    fun test_removeAll() {
        val source = FXCollections.observableArrayList("test")
        val mappedList = source.mapped { it.length }
        assertThat(mappedList).hasSize(1)
        assertThat(mappedList).containsExactly(4)
        source.removeAll("test")
        assertThat(mappedList).isEmpty()
    }
}