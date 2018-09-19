package ru.hse.spb

class Context(private val olderContext: Context? = null,
              private val functions: MutableMap<String, Function> = mutableMapOf(),
              private val vars: MutableMap<String, Int> = mutableMapOf()) {

    fun addFunction(function: Function) {
        functions[function.name.name] = function
    }

    override fun toString(): String {
        return "\nFunctions: $functions\nVariables: $vars\n Old context:\n---------------\n$olderContext\n---------------\n"
    }

    fun getFunction(functionName: Identifier, argsLength: Int): Function {
        val function = functions[functionName.name]
        if (function != null) {
            if (!function.checkArgumentsSize(argsLength)) {
                throw InLineException(functionName.line,
                        "Function ${functionName.name} hasn't $argsLength arguments")
            } else {
                return function
            }
        } else if (olderContext != null) {
            return olderContext.getFunction(functionName, argsLength)
        } else {
            throw InLineException(functionName.line, "There is no such function ${functionName.name}")
        }
    }

    fun addVar(identifier: Identifier, value: Int) { // TODO if
        vars[identifier.name] = value
    }

    fun reassignVar(identifier: Identifier, value: Int) {
        if (identifier.name in vars) {
            vars[identifier.name] = value
        } else {
            throw InLineException(identifier.line, "There is no such var ${identifier.name}")
        }
    }

    fun getVar(varName: Identifier): Int {
        val variable = vars[varName.name]
        if (variable != null) {
            return variable
        } else if (olderContext != null) {
            return getVar(varName)
        } else {
            throw InLineException(varName.line, "There is no such var ${varName.name}")
        }
    }

    companion object {

        private val printlnFunction = object : Function(Identifier(-1, "println"),
                listOf(), Block(-1, listOf())) {

            override fun checkArgumentsSize(argsSize: Int) = true

            override fun compute(oldContext: Context, arguments: List<Int>): Int {
                arguments.forEach { print("$it ") }
                println()
                return 0
            }
        }

        fun default(): Context {
            return Context(functions = mutableMapOf("println" to printlnFunction))
        }
    }
}