package ru.hse.spb

fun runParser(text: String) = Parser(Lexer(text).parseTokens()).parseTree()

class ParserException(message: String): Exception(message)

class Parser(private var tokens: MutableList<Token>) {

    private fun takeFirst(): Token {
        val token = tokens.first()
        drop()
        return token
    }

    private fun check(type: TokenType) = !tokens.isEmpty() && tokens.first().type == type

    private fun check(token: Token) = !tokens.isEmpty() && tokens.first() == token

    private fun checkAndDrop(token: Token): Token {
        val first = takeFirst()
        if (first != token) {
            throw ParserException("Wanted token $token but actually was $first")
        }
        return first
    }

    private fun checkAndDrop(type: TokenType): Token {
        val first = takeFirst()
        if (first.type != type) {
            throw ParserException("Wanted token type $type but actually was $first")
        }
        return first
    }

    private fun drop() {
        tokens = tokens.drop(1).toMutableList()
    }

    fun parseTree(): File {
        return File(parseBlock())
    }

    private fun parseBlock(): Block {
        val statements = mutableListOf<Statement>()
        while (!tokens.isEmpty() && !check(Token("}"))) {
            statements.add(parseStatement())
        }
        return Block(statements)
    }

    private fun parseBlockWithBranches(): Block {
        checkAndDrop(Token("{"))
        val block = parseBlock()
        checkAndDrop(Token("}"))
        return block
    }

    private fun parseStatement(): Statement {
        return when {
            Function.check(tokens) -> parseFunction()
            Variable.check(tokens) -> parseVariable()
            While.check(tokens) -> parseWhile()
            If.check(tokens) -> parseIf()
            Assignment.check(tokens) -> parseAssignment()
            Return.check(tokens) -> parseReturn()
            else -> parseExpression()
        }
    }

    private fun parseFunction(): Function {
        checkAndDrop(Token.FUN)
        val name = parseIdentifier()
        checkAndDrop(Token("("))
        val args = mutableListOf<Identifier>()
        if (check(TokenType.IDENTIFIER)) {
            args.add(parseIdentifier())
            while (check(Token(","))) {
                checkAndDrop(Token(","))
                args.add(parseIdentifier())
            }
        }
        checkAndDrop(Token(")"))
        val body = parseBlockWithBranches()
        return Function(name, args, body)
    }

    private fun parseVariable(): Variable {
        checkAndDrop(Token.VAR)
        val name = parseIdentifier()
        val expr = if (check(Token("="))) {
            checkAndDrop(Token("="))
            parseExpression()
        } else null
        return Variable(name, expr)
    }

    private fun parseWhile(): While {
        checkAndDrop(Token.WHILE)
        checkAndDrop(Token("("))
        val condition = parseExpression()
        checkAndDrop(Token(")"))
        val body = parseBlockWithBranches()
        return While(condition, body)
    }

    private fun parseIf(): If {
        checkAndDrop(Token.IF)
        checkAndDrop(Token("("))
        val condition = parseExpression()
        checkAndDrop(Token(")"))
        val ifBody = parseBlockWithBranches()
        val elseBody = if (check(Token.ELSE)) {
            checkAndDrop(Token.ELSE)
            parseBlockWithBranches()
        } else null
        return If(condition, ifBody, elseBody)
    }

    private fun parseAssignment(): Assignment {
        val name = parseIdentifier()
        checkAndDrop(Token("="))
        val expr = parseExpression()
        return Assignment(name, expr)
    }

    private fun parseReturn(): Return {
        checkAndDrop(Token.RETURN)
        val expr = parseExpression()
        return Return(expr)
    }

    private fun parseExpression(priority: Int = 6): Expression {
        if (priority == 0) {
            return parseValue()
        }
        val left = parseExpression(priority - 1)
        if (check(TokenType.OPERATION) && operationTokens[tokens.first().str] == priority) {
            val op = checkAndDrop(TokenType.OPERATION).str
            val right = parseExpression(priority)
            return BinaryExpression(left, op, right)
        }
        return left
    }

    private fun parseValue(): Expression {
        return when {
            FunctionCall.check(tokens) -> parseFunctionCall()
            Identifier.check(tokens) -> parseIdentifier()
            Number.check(tokens) -> parseNumber()
            check(Token("(")) -> {
                checkAndDrop(Token("("))
                val expr = parseExpression()
                checkAndDrop(Token(")"))
                expr
            }
            else -> throw ParserException("Unexpected token ${tokens.first().str}")
        }
    }

    private fun parseFunctionCall(): FunctionCall {
        val name = parseIdentifier()
        checkAndDrop(Token("("))
        val args = mutableListOf<Expression>()
        if (!tokens.isEmpty() && tokens.first() != Token(")")) {
            args.add(parseExpression())
            while (check(Token(","))) {
                checkAndDrop(Token(","))
                args.add(parseExpression())
            }
        }
        checkAndDrop(Token(")"))
        return FunctionCall(name, args)
    }

    private fun parseIdentifier() = checkAndDrop(TokenType.IDENTIFIER).toIdentifier()

    private fun parseNumber() = checkAndDrop(TokenType.NUMBER).toNumber()
}