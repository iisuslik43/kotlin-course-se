package ru.hse.spb

fun runLexer(text: String) = Lexer(text).parseTokens()

class Lexer(private val text: String) {
    private val tokens = mutableListOf<Token>()

    private fun StringBuilder.toToken() {
        if (!isEmpty()) {
            tokens.add(Token(toString()))
            setLength(0)
        }
    }

    fun parseTokens(): MutableList<Token> {
        val lines = text.split('\n')
        val builder = StringBuilder()
        for (line in lines) {
            var i = 0
            while (i in line.indices) {
                val current = line.substring(i, i + 1)
                val twoCurrent = if (i + 1 in line.indices) line.substring(i, i + 2) else ""
                if (twoCurrent == "//") {
                    builder.toToken()
                    break
                } else if (current in blankTokens) {
                    builder.toToken()
                } else if (twoCurrent in operationTokens) {
                    builder.toToken()
                    tokens.add(Token(twoCurrent))
                    i++
                } else if (current in splitTokens || current in operationTokens) {
                    builder.toToken()
                    tokens.add(Token(current))
                } else {
                    builder.append(current)
                }
                i++
            }
            builder.toToken()
        }
        return tokens
    }
}
