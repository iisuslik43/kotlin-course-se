package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LexerTest {

    private fun tokens(vararg str: String): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        for (s in str) {
            tokens.add(Token(s, 0))
        }
        return tokens
    }

    @Test
    fun keyWord() {
        assertEquals(TokenType.KEY_WORD, Token("if", 0).type)
        assertEquals(TokenType.KEY_WORD, Token("else", 0).type)
        assertEquals(TokenType.KEY_WORD, Token("while", 0).type)
        assertEquals(TokenType.KEY_WORD, Token("fun", 0).type)
        assertEquals(TokenType.KEY_WORD, Token("var", 0).type)
        assertEquals(TokenType.KEY_WORD, Token("return", 0).type)
    }

    @Test
    fun numbers() {
        assertEquals(TokenType.NUMBER, Token("43", 0).type)
        assertEquals(TokenType.NUMBER, Token("23", 0).type)
    }

    @Test(expected = InLineException::class)
    fun badToken() {
        assertEquals(TokenType.NUMBER, Token("23asdasdg2", 0).type)
    }

    @Test
    fun identifiers() {
        assertEquals(TokenType.IDENTIFIER, Token("abvs2f", 0).type)
        assertEquals(TokenType.IDENTIFIER, Token("good_var", 0).type)
    }

    @Test
    fun simpleTest() {
        assertEquals(tokens("2","+","2","*","3"), runLexer("2 + 2*3"))
    }

    @Test
    fun comment() {
        assertEquals(tokens("2","+","2"), runLexer("2 + 2//*3"))
    }
}