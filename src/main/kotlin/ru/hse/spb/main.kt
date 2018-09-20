package ru.hse.spb

import java.util.*
import kotlin.collections.ArrayList


private class DfsRunner(private val graph: List<List<Int>>) {
    private enum class Color { WHITE, GREY, BLACK }

    private val vertexColor = Array(graph.size) { Color.WHITE }
    private val cycle = mutableListOf<Int>()

    fun computeCycle(): List<Int> {
        cycle.clear()
        dfs(0)
        return cycle
    }

    fun dfs(vertex: Int, parent: Int = -1): Int {
        if (vertexColor[vertex] == Color.BLACK) {
            return -1
        }
        if (vertexColor[vertex] == Color.GREY) {
            return vertex
        }
        vertexColor[vertex] = Color.GREY
        for (neighbor in graph[vertex]) {
            if (neighbor == parent) {
                continue
            }
            val result = dfs(neighbor, vertex)
            if (result != -1) {
                cycle.add(vertex)
                return if (vertex == result) -1 else result
            }
        }
        vertexColor[vertex] = Color.BLACK
        return -1
    }
}


fun getDistances(graph: List<List<Int>>): IntArray {
    val cycle = DfsRunner(graph).computeCycle()
    val distance = IntArray(graph.size) { -1 }
    val queue: Queue<Int> = ArrayDeque<Int>()
    for (vertex in cycle) {
        queue.add(vertex)
        distance[vertex] = 0
    }
    while (!queue.isEmpty()) {
        val vertex = queue.remove()
        for (neighbor in graph[vertex]) {
            if (distance[neighbor] == -1) {
                queue.add(neighbor)
                distance[neighbor] = distance[vertex] + 1
            }
        }
    }
    return distance
}

fun getGraph(edges: List<Pair<Int, Int>>): List<List<Int>> {
    val graph = ArrayList<MutableList<Int>>(edges.size)
    (0 until edges.size).forEach { graph.add(mutableListOf()) }
    for ((from, to) in edges) {
        graph[from].add(to)
        graph[to].add(from)
    }
    return graph
}

fun main(args: Array<String>) {
    val vertexCount = readLine()!!.split(' ')[0].toInt()
    val edges = mutableListOf<Pair<Int, Int>>()
    for (i in 1..vertexCount) {
        val (from, to) = readLine()!!.split(' ').map { it.toInt() - 1 }
        edges.add(Pair(from, to))
    }
    val graph = getGraph(edges)
    val distance = getDistances(graph)
    distance.forEach { print("$it ") }
}