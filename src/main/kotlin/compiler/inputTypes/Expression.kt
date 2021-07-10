package compiler.inputTypes

import compiler.tokens.Token


abstract class Expression {

    interface Visitor<R> {
        fun visit(expression: Binary): Any?
        fun visit(expr: Unary): Any?
        fun visit(expression: Ternary): Any?
        fun visit(expression: Grouping): Any?
        fun visit(expression: Literal): Any?
        fun visit(variable: Variable): Any?
        fun visit(assignment: Assignment): Any?
        fun visit(logical: Logical): Any?
        fun visit(call: Call): Any?
        fun visit(get: Get): Any?
        fun visit(set: Set): Any?
        fun visit(instance: Instance): Any?
        fun visit(expr: Supe): Any?

    }


    class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class Assignment(val name: Token, val right: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {
            return visitor.visit(this)
        }

    }

    class Unary(val prefix: Token, val value: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {
            return visitor.visit(this)
        }

    }

    class Instance(val inst: Token) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit(this)

        }

    }


    class Grouping(val expression: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class Literal(val value: Any?) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class Variable(val name: Token) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {
            return visitor.visit(this)
        }
    }


    class Ternary(
        val left: Expression,
        val middle: Expression,
        val right: Expression

    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class Logical(val left: Expression, val operator: Token, val right: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class Call(val callee: Expression, val paren: Token, val args: List<Expression>) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {

            return visitor.visit(this)
        }
    }

    class Get(val obj: Expression, val name: Token) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {
            return visitor.visit(this)
        }
    }

    class Set(val expr: Expression, val name: Token, val value: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {
            return visitor.visit(this)
        }
    }

    class Supe(val supe: Token, val method: Token) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): Any? {
            return visitor.visit(this)
        }

    }


    abstract fun <R> accept(visitor: Visitor<R>): Any?


}

