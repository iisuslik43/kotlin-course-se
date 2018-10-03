package ru.hse.spb

class Context(private val olderContext: Context? = null,
              private val functions: MutableMap<String, Function> = mutableMapOf(),
              private val vars: MutableMap<String, Int> = mutableMapOf()) {

    fun addFunction(function: Function) {
        functions[function.name.name] = function
    }

    fun getFunction(functionName: Identifier, argsLength: Int): Function {
        val function = functions[functionName.name]
        if (function != null) {
            if (!function.checkArgumentsSize(argsLength)) {
                throw InterpreterException(functionName.line,
                        "Function ${functionName.name} hasn't $argsLength arguments")
            } else {
                return function
            }
        } else if (olderContext != null) {
            return olderContext.getFunction(functionName, argsLength)
        } else {
            throw InterpreterException(functionName.line, "There is no such function ${functionName.name}")
        }
    }

    fun addVar(identifier: Identifier, value: Int) { // TODO if
        vars[identifier.name] = value
    }

    fun reassignVar(identifier: Identifier, value: Int) {
        if (identifier.name in vars) {
            vars[identifier.name] = value
        } else if (olderContext != null) {
            olderContext.reassignVar(identifier, value)
        } else {
            throw InterpreterException(identifier.line, "There is no such var ${identifier.name}")
        }
    }

    fun getVar(varName: Identifier): Int {
        val variable = vars[varName.name]
        if (variable != null) {
            return variable
        } else if (olderContext != null) {
            return olderContext.getVar(varName)
        } else {
            throw InterpreterException(varName.line, "There is no such var ${varName.name}")
        }
    }

    companion object {

        private val printlnFunction = object : Function(Identifier(-1, "println"),
                emptyList(), Block(-1, emptyList())) {

            override fun checkArgumentsSize(argsSize: Int) = true

            override fun compute(oldContext: Context, arguments: List<Int>): Int {
                arguments.forEach { print("$it ") }
                println()
                return 0
            }
        }

        fun default() = Context(functions = mutableMapOf("println" to printlnFunction))


        fun test(fakePrintln: Function) = Context(functions = mutableMapOf("println" to fakePrintln))
    }
}