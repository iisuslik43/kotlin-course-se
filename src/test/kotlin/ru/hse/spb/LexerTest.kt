package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LexerTest {

    private fun tokens(vararg str: String): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        for (s in str) {
            tokens.add(Token(s))
        }
        return tokens
    }

    @Test
    fun keyWord() {
        assertEquals(TokenType.KEY_WORD, Token("if").type)
        assertEquals(TokenType.KEY_WORD, Token("else").type)
        assertEquals(TokenType.KEY_WORD, Token("while").type)
        assertEquals(TokenType.KEY_WORD, Token("fun").type)
        assertEquals(TokenType.KEY_WORD, Token("var").type)
        assertEquals(TokenType.KEY_WORD, Token("return").type)
    }

    @Test
    fun numbers() {
        assertEquals(TokenType.NUMBER, Token("43").type)
        assertEquals(TokenType.NUMBER, Token("23").type)
    }

    @Test(expected = LexerException::class)
    fun badToken() {
        assertEquals(TokenType.NUMBER, Token("23asdasdg2").type)
    }

    @Test
    fun identifiers() {
        assertEquals(TokenType.IDENTIFIER, Token("abvs2f").type)
        assertEquals(TokenType.IDENTIFIER, Token("good_var").type)
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