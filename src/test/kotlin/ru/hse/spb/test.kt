package ru.hse.spb

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class TestSource {

    @Test
    fun testFirst() {
        val graph = getGraph(listOf(Pair(0, 2), Pair(3, 2), Pair(3, 1), Pair(0, 1)))
        assertArrayEquals(IntArray(graph.size) { 0 }, getDistances(graph))
    }

    @Test
    fun testSecond() {
        val graph = getGraph(listOf(Pair(0, 1), Pair(2, 3), Pair(5, 3), Pair(1, 2), Pair(0, 2), Pair(2, 4)))
        assertArrayEquals(intArrayOf(0, 0, 0, 1, 1, 2), getDistances(graph))
    }

    @Test
    fun testBigger() {
        val graph = getGraph(listOf(Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 5), Pair(3, 4), Pair(4, 5), Pair(5, 6)))
        assertArrayEquals(intArrayOf(3, 2, 1, 0, 0, 0, 1), getDistances(graph))
    }
}