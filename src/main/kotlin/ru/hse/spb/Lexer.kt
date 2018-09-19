package ru.hse.spb

fun runLexer(text: String) = Lexer(text).parseTokens()

class Lexer(private val text: String) {
    private val tokens = mutableListOf<Token>()

    private fun String.nextIs(pos: Int, text: String): Boolean {
        return pos + text.length <= this.length
                && this.substring(pos, pos + text.length - 1) == text
    }

    private fun StringBuilder.toToken(lineNumber: Int) {
        if (!isEmpty()) {
            tokens.add(Token(toString(), lineNumber))
            setLength(0)
        }
    }

    fun parseTokens(): MutableList<Token> {


        val lines = text.split('\n')
        val builder = StringBuilder()
        for ((lineNumber, line) in lines.withIndex()) {
            var i = 0
            while (i in line.indices) {
                val current = line.substring(i, i + 1)
                val twoCurrent = if (i + 1 in line.indices) line.substring(i, i + 2) else ""
                if (line.nextIs(i, "//")) {
                    builder.toToken(lineNumber)
                    break
                } else if (current in blankTokens) {
                    builder.toToken(lineNumber)
                } else if (current in splitTokens || current in operationTokens) {
                    builder.toToken(lineNumber)
                    tokens.add(Token(current, lineNumber))
                } else if (twoCurrent in operationTokens) {
                    builder.toToken(lineNumber)
                    tokens.add(Token(twoCurrent, lineNumber))
                    i++
                } else {
                    builder.append(current)
                }
                i++
            }
            builder.toToken(lineNumber)
        }
        return tokens
    }
}