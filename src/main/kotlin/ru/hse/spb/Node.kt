package ru.hse.spb

abstract class Node(val line: Int)

fun Int.toBoolean() = this != 0

fun Boolean.toInt() = if (this) 1 else 0

class File(private val block: Block) : Node(block.line) {
    fun run() {
        block.run(Context.default())
    }

    override fun equals(other: Any?) = other is File && other.block == block
}

class Block(line: Int, private val statements: List<Statement>) : Node(line) {
    fun run(context: Context): Int? {
        for (statement in statements) {
            statement.run(context)?.let { return it }
        }
        return null
    }

    override fun equals(other: Any?) = other is Block && other.statements == statements
}

sealed class Statement(line: Int) : Node(line) {
    abstract fun run(context: Context = Context.default()): Int?
}

open class Function(val name: Identifier, private val args: List<Identifier>,
                    private val body: Block) : Statement(name.line) {
    override fun run(context: Context): Int? {
        context.addFunction(this)
        return null
    }

    override fun toString() = "fun $name($args)"

    open fun checkArgumentsSize(argsSize: Int) = argsSize == args.size

    open fun compute(oldContext: Context, arguments: List<Int>): Int {
        val funcContext = Context(oldContext)
        for (i in 0 until args.size) {
            funcContext.addVar(args[i], arguments[i])
        }
        return body.run(funcContext) ?: 0
    }

    override fun equals(other: Any?) = other is Function && other.name == name &&
            other.args == args && other.body == body

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.FUN)
    }
}

class Variable(val name: Identifier, private val expr: Expression?) : Statement(name.line) {
    override fun run(context: Context): Int? {
        val value = expr?.compute(context) ?: 0
        context.addVar(name, value)
        return null
    }

    override fun equals(other: Any?) = other is Variable && other.name == name && other.expr == expr

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.VAR)
    }
}

class While(private val condition: Expression, private val body: Block) : Statement(condition.line) {
    override fun run(context: Context): Int? {
        val whileContext = Context(context)
        while (condition.compute(context).toBoolean()) {
            body.run(whileContext)?.let { return it }
        }
        return null
    }

    override fun equals(other: Any?) = other is While && other.condition == condition && other.body == body


    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.WHILE)
    }
}

class If(private val condition: Expression, private val ifBody: Block,
         private val elseBody: Block?) : Statement(condition.line) {
    override fun run(context: Context): Int? {
        val ifContext = Context(context)
        if (condition.compute(context).toBoolean()) {
            ifBody.run(ifContext)?.let { return it }
        } else if (elseBody != null) {
            ifBody.run(ifContext)?.let { return it }
        }
        return null
    }

    override fun equals(other: Any?) = other is If && other.condition == condition && other.ifBody == ifBody &&
            other.elseBody == elseBody


    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.IF)
    }
}

class Assignment(private val name: Identifier, private val expr: Expression) : Statement(name.line) {
    override fun run(context: Context): Int? {
        context.reassignVar(name, expr.compute(context))
        return null
    }

    override fun equals(other: Any?) = other is Assignment && other.name == name && other.expr == expr
    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER) && tokens.secondIs(Token("="))
    }
}

class Return(private val expr: Expression) : Statement(expr.line) {
    override fun run(context: Context): Int? {
        return expr.compute(context)
    }

    override fun equals(other: Any?) = other is Return && other.expr == expr

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.RETURN)
    }
}

sealed class Expression(line: Int) : Statement(line) {
    override fun run(context: Context): Int? {
        return compute(context)
    }

    abstract fun compute(context: Context): Int
}

class FunctionCall(private val name: Identifier, private val args: List<Expression>) : Expression(name.line) {
    override fun compute(context: Context): Int {
        val function = context.getFunction(name, args.size)
        val arguments = args.map { it.compute(context) }
        return function.compute(context, arguments)
    }

    override fun equals(other: Any?) = other is FunctionCall && other.name == name && other.args == args

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER) && tokens.secondIs(Token("("))
    }
}

class BinaryExpression(private val left: Expression, private val op: String,
                       private val right: Expression) : Expression(left.line) {
    override fun compute(context: Context): Int {
        val l = left.compute(context)
        if (op == "||") {
            return if (l.toBoolean()) 1 else right.compute(context)
        } else if (op == "&&") {
            return if (!l.toBoolean()) 0 else right.compute(context)
        } else {
            val r = right.compute(context)
            println("$r$op$l")
            println((l > r).toInt())
            return when (op) {
                "+" -> l + r
                "-" -> l + r
                "*" -> l * r
                "/" -> l / r
                "%" -> l % r
                "==" -> (l == r).toInt()
                "!=" -> (l != r).toInt()
                "<" -> (l < r).toInt()
                ">" -> (l > r).toInt()
                "<=" -> (l <= r).toInt()
                ">=" -> (l >= r).toInt()
                else -> throw InLineException(line, "Bad operation $op")
            }
        }
    }

    override fun equals(other: Any?) = other is BinaryExpression && other.left == left &&
            other.op == op && other.right == right

    companion object {
        fun check(tokens: MutableList<Token>) = true
    }
}

class Identifier(line: Int, val name: String) : Expression(line) {
    override fun compute(context: Context): Int {
        return context.getVar(this)
    }

    override fun equals(other: Any?) = other is Identifier && name == other.name

    override fun toString() = name

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER)
    }
}

class Number(line: Int, private val value: Int) : Expression(line) {
    override fun compute(context: Context): Int {
        return value
    }


    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.NUMBER)
    }
}
