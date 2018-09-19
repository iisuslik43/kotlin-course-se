package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test

class ParserTest {

    @Test
    fun simpleTest() {
        val code = "2 + 2 == 4"
        val plus = BinaryExpression(Number(-1, 2), "+", Number(-1, 2))
        assertEquals(File(Block(0, listOf(BinaryExpression(plus, "==", Number(-1, 4))))), runParser(code))
    }
}