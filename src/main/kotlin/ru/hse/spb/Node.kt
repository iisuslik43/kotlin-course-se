package ru.hse.spb

abstract class Node(val line: Int)

fun Int.toBoolean() = this != 0

fun Boolean.toInt() = if (this) 1 else 0

fun runFile(file: String) = runParser(file).run()

fun runFileFakePrint(file: String): String {
    val fakePrintln = object : Function(Identifier(-1, "println"),
            emptyList(), Block(-1, emptyList())) {

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

class InterpreterException(line: Int, message: String): InLineException(line, message)

class File(private val block: Block) : Node(block.line) {
    fun run(context: Context = Context.default()) {
        block.run(context)
    }

    override fun equals(other: Any?) = other is File && other.block == block

    override fun toString() = block.toString()
}

class Block(line: Int, private val statements: List<Statement>) : Node(line) {
    fun run(context: Context): Int? {
        for (statement in statements) {
            statement.run(context)?.let { if (statement !is FunctionCall) return it }
        }
        return null
    }

    override fun equals(other: Any?) = other is Block && other.statements == statements

    override fun toString(): String {
        val builder = StringBuilder()
        statements.forEach { builder.append(it.toString() + "\n") }
        return builder.toString()
    }
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

    override fun toString() = "var $name = $expr"

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

    override fun toString(): String = "while ($condition) {\n$body\n}"

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

    override fun toString(): String = "if ($condition) {\n$ifBody\n}" +
            if (elseBody != null) "else {\n$elseBody\n}" else ""

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

    override fun toString() = "$name = $expr"

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER) && tokens.secondIs(Token("="))
    }
}

class Return(private val expr: Expression) : Statement(expr.line) {
    override fun run(context: Context): Int? {
        return expr.compute(context)
    }

    override fun equals(other: Any?) = other is Return && other.expr == expr

    override fun toString() = "return $expr"

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

    override fun toString() = "$name($args)"

    override fun equals(other: Any?) = other is FunctionCall && other.name == name && other.args == args

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.IDENTIFIER) && tokens.secondIs(Token("("))
    }
}

class BinaryExpression(private val left: Expression, private val op: String,
                       private val right: Expression) : Expression(left.line) {
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
                    else -> throw InterpreterException(line, "Unexpected operation $op")
                }
            }
        }
    }

    override fun toString() = "($left $op $right)"

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

    override fun toString() = value.toString()


    override fun equals(other: Any?) = other is Number && value == other.value

    companion object {
        fun check(tokens: MutableList<Token>) = tokens.firstIs(TokenType.NUMBER)
    }
}
