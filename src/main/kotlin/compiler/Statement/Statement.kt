package compiler.Statement

import compiler.Expression
import tokens.Token


abstract class Statement {

    interface StateVisitor<R> {

        fun <R> visit(visitor: Print): Any
        fun <R> visit(visitor: Expression): Any
        fun <R> visit(declaration: Declaration): Any?

    }

    class Print(val expr: compiler.Expression) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any {

            return visitor.visit<R>(this)

        }

    }

    class Expression(val expr: compiler.Expression) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any {

            return visitor.visit<R>(this)

        }

    }
    class Declaration(val name: Token, val expr: compiler.Expression?) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }


    abstract fun <R> accept(visitor: StateVisitor<R>): Any?
}

