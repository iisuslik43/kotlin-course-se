package ru.hse.spb

val splitTokens = setOf("(", ")", ";", ",", "{", "}")
val operationTokens = hashMapOf("+" to 2, "-" to 2, "*" to 1, "/" to 1, "%" to 1, "==" to 4, "!=" to 4, ">" to 3, ">=" to 3,
        "<" to 3, "<=" to 3, "&&" to 5, "||" to 6, "=" to 9)
val keyWords = setOf("fun", "var", "while", "if", "else", "return")
val blankTokens = setOf(" ", "\n", "\t")

class Token(val str: String, val line: Int = 0) {
    val type = when {
        str in keyWords -> TokenType.KEY_WORD
        str in operationTokens -> TokenType.OPERATION
        str in splitTokens -> TokenType.SPLIT
        isNumber() -> TokenType.NUMBER
        else -> {
            val regex = "[a-z_]\\w*".toRegex()
            if (regex.matchEntire(str) == null) {
                throw InLineException(line, "Bad token $str")
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
            throw InLineException(line, "This is not an identifier")
        }
        return Identifier(line, str)
    }

    fun toNumber(): Number {
        if (type != TokenType.NUMBER) {
            throw InLineException(line, "This is not a number")
        }
        return Number(line, str.toInt())
    }

    override fun toString(): String {
        return "$type: $str in line $line"
    }

    override fun equals(other: Any?): Boolean {
        return other is Token && type == other.type && str == other.str
    }

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

fun MutableList<Token>.secondIs(type: TokenType) = size > 1 && get(1).type == type


open class InLineException(val line: Int, message: String): Exception(message)

enum class TokenType {
    KEY_WORD,
    NUMBER,
    SPLIT,
    IDENTIFIER,
    OPERATION
}