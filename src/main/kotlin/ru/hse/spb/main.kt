package ru.hse.spb

import java.util.*


private class DfsRunner(private val graph: Array<MutableList<Int>>) {
    private enum class Color { WHITE, GREY, BLACK }

    private val vertexColor = Array(graph.size) { Color.WHITE }
    val cycle = mutableListOf<Int>()

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


private fun getCycle(graph: Array<MutableList<Int>>): List<Int> {
    val runner = DfsRunner(graph)
    runner.dfs(0)
    return runner.cycle
}

fun getDistances(graph: Array<MutableList<Int>>): IntArray {
    val cycle = getCycle(graph)
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

fun getGraph(edges: List<Pair<Int, Int>>): Array<MutableList<Int>> {
    val graph = Array<MutableList<Int>>(edges.size) { mutableListOf() }
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