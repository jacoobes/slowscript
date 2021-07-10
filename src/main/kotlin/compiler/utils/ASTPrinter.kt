package compiler.utils

import compiler.inputTypes.Expression

//First Type of "Interpreter" in that it prints out all expressions, (does not truly evaluate it)

class ASTPrinter : Expression.Visitor<String> {

    fun print(expression: Expression): Any? {
        return expression.accept(this)
    }


    private fun <R> parenthesize(name: String, vararg expression: Expression): String {
        val builder = StringBuilder().apply { append("(").append(name) }
        for (expr in expression) {

            builder.append(" ")
            builder.append(expr.accept(this))

        }
        builder.append(')')

        return builder.toString()
    }

    override fun visit(expression: Expression.Binary): String {
        return parenthesize<String>(expression.operator.lexeme, expression.left, expression.right)
    }

    override fun visit(expression: Expression.Ternary): String {
        return parenthesize<String>(
            "?:",
            expression.left,
            expression.middle,
            expression.right
        )
    }

    override fun visit(expression: Expression.Unary): String {
        return parenthesize<String>(expression.prefix.lexeme, expression.value)
    }

    override fun visit(expression: Expression.Literal): String {
        if (expression.value.toString() == "null") return "null"
        return expression.value.toString()
    }

    override fun visit(expression: Expression.Grouping): String {
        return parenthesize<String>("group", expression.expression)
    }

    override fun visit(variable: Expression.Variable): Any? {
        return parenthesize<String>(variable.name.lexeme, variable)
    }

    override fun visit(assignment: Expression.Assignment): Any? {
        return parenthesize<String>(assignment.name.lexeme, assignment)
    }

    override fun visit(logical: Expression.Logical): Any? {
        TODO("Not yet implemented")
    }

    override fun visit(call: Expression.Call): Any? {
        TODO("Not yet implemented")
    }

    override fun visit(get: Expression.Get): Any? {
        TODO("Not yet implemented")
    }

    override fun visit(set: Expression.Set): Any? {
        TODO("Not yet implemented")
    }

    override fun visit(instance: Expression.Instance): Any? {
        TODO("Not yet implemented")
    }

    override fun visit(expr: Expression.Supe): Any? {
        TODO("Not yet implemented")
    }


}