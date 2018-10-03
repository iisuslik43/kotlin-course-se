package ru.hse.spb

import java.lang.Exception

val splitTokens = setOf("(", ")", ";", ",", "{", "}")
val operationTokens = hashMapOf("+" to 2, "-" to 2, "*" to 1, "/" to 1, "%" to 1, "==" to 4, "!=" to 4, ">" to 3, ">=" to 3,
        "<" to 3, "<=" to 3, "&&" to 5, "||" to 6, "=" to 9)
val keyWords = setOf("fun", "var", "while", "if", "else", "return")
val blankTokens = setOf(" ", "\n", "\t")

class LexerException(message: String) : Exception(message)

data class Token(val str: String) {
    val type = when {
        str in keyWords -> TokenType.KEY_WORD
        str in operationTokens -> TokenType.OPERATION
        str in splitTokens -> TokenType.SPLIT
        isNumber() -> TokenType.NUMBER
        else -> {
            val regex = "[a-z_]\\w*".toRegex()
            if (regex.matchEntire(str) == null) {
                throw LexerException("Bad token $str")
            }
            TokenType.IDENTIFIER
        }
    }

    private fun isNumber(): Boolean {
        try {
            str.toInt()
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }

    fun toIdentifier(): Identifier {
        if (type != TokenType.IDENTIFIER) {
            throw LexerException("This is not an identifier")
        }
        return Identifier(str)
    }

    fun toNumber(): Number {
        if (type != TokenType.NUMBER) {
            throw LexerException("This is not a number")
        }
        return Number(str.toInt())
    }

    override fun toString() = type.toString()

    companion object {
        val FUN = Token("fun")
        val IF = Token("if")
        val VAR = Token("var")
        val WHILE = Token("while")
        val ELSE = Token("else")
        val RETURN = Token("return")
    }
}

fun MutableList<Token>.firstIs(token: Token) = !isEmpty() && first() == token

fun MutableList<Token>.firstIs(type: TokenType) = !isEmpty() && first().type == type

fun MutableList<Token>.secondIs(token: Token) = size > 1 && get(1) == token


enum class TokenType {
    KEY_WORD,
    NUMBER,
    SPLIT,
    IDENTIFIER,
    OPERATION
}