package ru.hse.spb

import org.junit.Test

class InterpretatorTest {

    @Test
    fun simpleTest() {
        val code =
                """
var a = 20
var b = 10
if (a > b) {
    println(1)
} else {
    println(0)
}
                """.trimIndent()
        runFile(code)
    }
}