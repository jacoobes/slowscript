package compiler.Statement

import compiler.Expression
import tokens.Token



abstract class Statement {

    interface StateVisitor<R> {

        fun <R> visit(visitor: Print): Any
        fun <R> visit(visitor: Expression): Any?
        fun <R> visit(declaration: Declaration): Any?
        fun <R> visit(block: Block)
        fun <R> visit(tree: If): Any?
        fun <R> visit(loop: While): Any?

    }

    class Print(val expr: compiler.Expression) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any {

            return visitor.visit<R>(this)

        }

    }

    class Expression(val expr: compiler.Expression) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }
    class Block(val statementList : List<Statement>) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any {

            return visitor.visit<R>(this)

        }

    }


    class Declaration(val name: Token, val expr: compiler.Expression?) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }

    class If(val ifBranch: compiler.Expression, val acted: Statement?, val elseBranch : Statement?) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }

    class While(val expr: compiler.Expression, val body: Statement) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit<R>(this)

        }

    }



    abstract fun <R> accept(visitor: StateVisitor<R>): Any?
}

