package ru.hse.spb

fun Int.toBoolean() = this != 0

fun Boolean.toInt() = if (this) 1 else 0

fun runFileFakePrint(file: String): String {
    val fakePrintln = object : Function(Identifier("println"),
            emptyList(), Block(emptyList())) {

        override fun checkArgumentsSize(argsSize: Int) = true

        val builder = StringBuilder()

        override fun compute(oldContext: Context, arguments: List<Int>): Int {
            arguments.forEach { builder.append("$it ") }
            builder.append("\n")
            return 0
        }
    }
    runParser(file).run(Context.test(fakePrintln))
    return fakePrintln.builder.toString()
}

class InterpreterException(message: String): Exception(message)

sealed class Node

data class File(private val block: Block) : Node() {
    fun run(context: Context = Context.default()) {
        block.run(context)
    }

    override fun toString() = block.toString()
}

data class Block(private val statements: List<Statement>) : Node() {
    fun run(context: Context): Int? {
        for (statement in statements) {
            statement.run(context)?.let { if (statement !is FunctionCall) return it }
        }
        return null
    }


    override fun toString(): String {
        val builder = StringBuilder()
        statements.forEach { builder.append(it.toString() + "\n") }
        return builder.toString()
    }
}

sealed class Statement : Node() {
    abstract fun run(context: Context = Context.default()): Int?
}

open class Function(val name: Identifier, private val args: List<Identifier>,
                    private val body: Block) : Statement() {
    override fun run(context: Context): Int? {
        context.addFunction(this)
        return null
    }

    override fun toString() = "fun $name($args){\n$body\n}"

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

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.FUN)
    }
}

data class Variable(val name: Identifier, private val expr: Expression?) : Statement() {
    override fun run(context: Context): Int? {
        val value = expr?.compute(context) ?: 0
        context.addVar(name, value)
        return null
    }

    override fun toString() = "var $name = $expr"

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.VAR)
    }
}

data class While(private val condition: Expression, private val body: Block) : Statement() {
    override fun run(context: Context): Int? {
        val whileContext = Context(context)
        while (condition.compute(context).toBoolean()) {
            body.run(whileContext)?.let { return it }
        }
        return null
    }

    override fun toString(): String = "while ($condition) {\n$body\n}"

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.WHILE)
    }
}

data class If(private val condition: Expression, private val ifBody: Block,
         private val elseBody: Block?) : Statement() {
    override fun run(context: Context): Int? {
        val ifContext = Context(context)
        if (condition.compute(context).toBoolean()) {
            ifBody.run(ifContext)?.let { return it }
        } else if (elseBody != null) {
            ifBody.run(ifContext)?.let { return it }
        }
        return null
    }

    override fun toString(): String = "if ($condition) {\n$ifBody\n}" +
            if (elseBody != null) "else {\n$elseBody\n}" else ""

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.IF)
    }
}

data class Assignment(private val name: Identifier, private val expr: Expression) : Statement() {
    override fun run(context: Context): Int? {
        context.reassignVar(name, expr.compute(context))
        return null
    }

    override fun toString() = "$name = $expr"

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER) && tokens.secondIs(Token("="))
    }
}

data class Return(private val expr: Expression) : Statement() {
    override fun run(context: Context): Int? {
        return expr.compute(context)
    }

    override fun toString() = "return $expr"

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(Token.RETURN)
    }
}

sealed class Expression : Statement() {
    override fun run(context: Context): Int? {
        return compute(context)
    }

    abstract fun compute(context: Context): Int
}

data class FunctionCall(private val name: Identifier, private val args: List<Expression>) : Expression() {
    override fun compute(context: Context): Int {
        val function = context.getFunction(name, args.size)
        val arguments = args.map { it.compute(context) }
        return function.compute(context, arguments)
    }

    override fun toString() = "$name($args)"

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER) && tokens.secondIs(Token("("))
    }
}

data class BinaryExpression(private val left: Expression, private val op: String,
                       private val right: Expression) : Expression() {
    override fun compute(context: Context): Int {
        val l = left.compute(context)
        when (op) {
            "||" -> return if (l.toBoolean()) 1 else right.compute(context)
            "&&" -> return if (!l.toBoolean()) 0 else right.compute(context)
            else -> {
                val r = right.compute(context)
                return when (op) {
                    "+" -> l + r
                    "-" -> l - r
                    "*" -> l * r
                    "/" -> l / r
                    "%" -> l % r
                    "==" -> (l == r).toInt()
                    "!=" -> (l != r).toInt()
                    "<" -> (l < r).toInt()
                    ">" -> (l > r).toInt()
                    "<=" -> (l <= r).toInt()
                    ">=" -> (l >= r).toInt()
                    else -> throw InterpreterException("Unexpected operation $op")
                }
            }
        }
    }

    override fun toString() = "($left $op $right)"
}

data class Identifier(val name: String) : Expression() {
    override fun compute(context: Context): Int {
        return context.getVar(this)
    }

    override fun toString() = name

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER)
    }
}

data class Number(private val value: Int) : Expression() {
    override fun compute(context: Context): Int {
        return value
    }

    override fun toString() = value.toString()

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.NUMBER)
    }
}
