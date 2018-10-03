package ru.hse.spb

import org.junit.Assert.assertEquals
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
        assertEquals("1 \n", runFileFakePrint(code))
    }

    @Test
    fun fibonacciTest() {
        val code =
                """fun fib(n) {
    if (n <= 1) {
        return 1
    }
    return fib(n - 1) + fib(n - 2)
}

var i = 3
while (i <= 5) {
    println(i, fib(i))
    i = i + 1
}
                """.trimIndent()
        assertEquals("3 3 \n4 5 \n5 8 \n", runFileFakePrint(code))
    }

    @Test
    fun twoFunctions() {
        val code = """fun foo(n) {
    fun bar(m) {
        return m + n
    }

    return bar(10)
}

println(foo(41))""".trimIndent()
        assertEquals("51 \n", runFileFakePrint(code))
    }

    @Test
    fun comments() {
        val code = """
            var a = 2 // * 3
            println(a)
        """.trimIndent()
        assertEquals("2 \n", runFileFakePrint(code))
    }

    @Test
    fun bigIf() {
        val code = """
            var x = 1
            var y = 0
            var z = 1
            if (x && (y || z)) {
                println (1)
            } else {
                println (wrong)
            }
        """.trimIndent()
        assertEquals("1 \n", runFileFakePrint(code))
    }

    @Test
    fun secondInOrNeverComputes() {
        val code = """
            println (1 || error)
        """.trimIndent()
        assertEquals("1 \n", runFileFakePrint(code))
    }

    @Test
    fun secondInAndNeverComputes() {
        val code = """
            println (0 && error)
        """.trimIndent()
        assertEquals("0 \n", runFileFakePrint(code))
    }

}