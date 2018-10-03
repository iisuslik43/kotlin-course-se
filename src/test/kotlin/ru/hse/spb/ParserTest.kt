package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test

class ParserTest {

    fun fileFrom(vararg statements: Statement) = File(Block(0, statements.toList()))

    fun ident(str: String) = Identifier(-1, str)

    fun number(value: Int) = Number(-1, value)

    val arithmNode = BinaryExpression(number(2), "+", number(2))

    val predicate = BinaryExpression(arithmNode, "==", number(4))

    @Test
    fun simpleTest() {
        val code = "2 + 2 == 4"
        assertEquals(fileFrom(BinaryExpression(arithmNode, "==", number(4))),
                runParser(code))
    }

    @Test
    fun bracketsTest() {
        val code = "(2 + 2) * 3"
        assertEquals(fileFrom(BinaryExpression(arithmNode, "*", number(3))),
                runParser(code))
    }

    @Test
    fun priorityTest() {
        val code = "2 + 2 * 3"
        assertEquals(fileFrom(BinaryExpression(number(2), "+",
                BinaryExpression(number(2), "*", number(3)))),
                runParser(code))
    }

    @Test
    fun functionWithEmptyBody() {
        val code = "fun foo(a, b) {}"
        assertEquals(fileFrom(Function(ident("foo"), listOf(ident("a"), ident("b")),
                Block(0, emptyList()))),
                runParser(code))
    }

    @Test
    fun functionWithEmptyArgs() {
        val code = "fun foo() {return 0}"
        assertEquals(fileFrom(Function(ident("foo"), emptyList(),
                Block(0, listOf(Return(number(0)))))), runParser(code))
    }

    @Test
    fun functionWithTwoStatements() {
        val code = "fun foo() {var x = 2 kek()}"
        assertEquals(fileFrom(Function(ident("foo"), emptyList(),
                Block(0, listOf(Variable(ident("x"), number(2)),
                        FunctionCall(ident("kek"), emptyList()))))), runParser(code))
    }

    @Test
    fun functionCallEmptyArgs() {
        val code = "foo()"
        assertEquals(fileFrom(FunctionCall(ident("foo"), emptyList())), runParser(code))
    }

    @Test
    fun functionCallTwoArgs() {
        val code = "kek(a,b)"
        assertEquals(fileFrom(FunctionCall(ident("kek"), listOf(ident("a"), ident("b")))), runParser(code))
    }

    @Test
    fun returnNumber() {
        val code = "return 2"
        assertEquals(fileFrom(Return(number(2))), runParser(code))
    }

    @Test
    fun returnExpr() {
        val code = "return 2 + 2"
        assertEquals(fileFrom(Return(arithmNode)), runParser(code))
    }

    @Test
    fun variableTest() {
        val code = "var x = 3"
        assertEquals(fileFrom(Variable(ident("x"), number(3))), runParser(code))
    }

    @Test
    fun variableEmptyExprTest() {
        val code = "var x"
        assertEquals(fileFrom(Variable(ident("x"), null)), runParser(code))
    }

    @Test
    fun assignmentTest() {
        val code = "x = 2"
        assertEquals(fileFrom(Assignment(ident("x"), number(2))), runParser(code))
    }

    @Test
    fun whileTest() {
        val code = "while(2 + 2 == 4) {i = 4}"
        assertEquals(fileFrom(While(predicate, Block(0, listOf(Assignment(ident("i"), number(4)))))),
                runParser(code))
    }

    @Test
    fun ifTest() {
        val code = "if(2 + 2 == 4) {i = 4}"
        assertEquals(fileFrom(If(predicate, Block(0, listOf(Assignment(ident("i"), number(4)))),
                null)),
                runParser(code))
    }

    @Test
    fun ifElseTest() {
        val code = "if(2 + 2 == 4) {i = 4} else {return 2}"
        assertEquals(fileFrom(If(predicate, Block(0, listOf(Assignment(ident("i"), number(4)))),
                Block(0, listOf(Return(number(2)))))),
                runParser(code))
    }

    @Test
    fun hardExpression() {
        val code = "(2 + foo(kek)) * lol"
        assertEquals(fileFrom(BinaryExpression(BinaryExpression(number(2), "+",
                FunctionCall(ident("foo"), listOf(ident("kek")))), "*", ident("lol"))),
                runParser(code))
    }
}