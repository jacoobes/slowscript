package compiler

import tokens.*
import java.lang.StringBuilder


abstract class Expression {

    interface Visitor<R> {
        fun <R> visit(expression: Expression.Binary): Any?
        fun <R> visit(expression: Expression.Unary): Any?
        fun <R> visit(expression: Expression.Ternary): Any?
        fun <R> visit(expression: Expression.Grouping): Any?
        fun <R> visit(expression: Expression.Literal): Any?
        fun <R> visit(variable: Expression.Variable): Any?
        fun <R> visit(assignment: Expression.Assignment): Any?
        fun <R> visit(logical: Expression.Logical): Any?
        fun <R> visit(call: Expression.Call): Any?
    }


    class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }

    class Assignment(val name: Token, val right: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)


        }


    }


    class Unary(val prefix: Token, val expression: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }


    class Grouping(val expression: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }

    class Literal(val value: Any?) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }

    class Variable(val name: Token) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {
            return visitor.visit<R>(this)
        }
    }


    class Ternary(
        val left: Expression,
        val questionMark: Token,
        val middle: Expression,
        val colon: Token,
        val right: Expression

    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }

    class Logical(val left: Expression, val operator: Token, val right: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }

    class Call(val callee: Expression, val paren: Token, val args: List<Expression>) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }


    abstract fun <R> accept(visitor: Visitor<R>): Any?


}

