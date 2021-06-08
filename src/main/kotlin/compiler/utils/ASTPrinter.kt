package compiler.utils

import compiler.Expression
import java.lang.StringBuilder

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

    override fun <R> visit(expression: Expression.Binary): String {
     return parenthesize<String>(expression.operator.lexeme, expression.left, expression.right)
    }

    override fun <R> visit(expression: Expression.Ternary): String {
        return parenthesize<String>("${expression.questionMark.lexeme}${expression.colon.lexeme}", expression.left, expression.middle, expression.right)
    }

    override fun <R> visit(expression: Expression.Unary): String {
        return parenthesize<String>(expression.prefix.lexeme, expression.expression)
    }

    override fun <R> visit(expression: Expression.Literal): String {
        if(expression.value.toString() == "null") return "null"
        return expression.value.toString()
    }

    override fun <R> visit(expression: Expression.Grouping): String {
        return parenthesize<String>("group", expression.expression)
    }

    override fun <R> visit(variable: Expression.Variable): Any? {
        return parenthesize<String>(variable.name.lexeme, variable)
    }

    override fun <R> visit(assignment: Expression.Assignment): Any? {
        return parenthesize<String>(assignment.name.lexeme, assignment)
    }

    override fun <R> visit(logical: Expression.Logical): Any? {
        TODO("Not yet implemented")
    }

    override fun <R> visit(call: Expression.Call): Any? {
        TODO("Not yet implemented")
    }

    override fun <R> visit(get: Expression.Get): Any? {
        TODO("Not yet implemented")
    }

    override fun <R> visit(set: Expression.Set): Any? {
        TODO("Not yet implemented")
    }

    override fun <R> visit(instance: Expression.Instance): Any? {
        TODO("Not yet implemented")
    }

    override fun <R> visit(supe: Expression.Supe): Any? {
        TODO("Not yet implemented")
    }
}