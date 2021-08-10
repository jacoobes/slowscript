package compiler.inputTypes

import compiler.interpreter.Init
import compiler.tokens.TOKEN_TYPES
import compiler.tokens.Token


abstract class Statement {

    interface StateVisitor<R> {

        fun visit(visitor: Print): Any
        fun visit(visitor: Expression): Any?
        fun visit(declaration: Declaration): Any?
        fun visit(block: Block)
        fun visit(tree: If): Any?
        fun visit(loop: While): Any?
        fun visit(function: Function): Any?
        fun visit(arg: Return): Any?
        fun visit(classDec: ClassDec): Any?


    }

    class Function(val fnName: Token, val parameters: List<Token>, val body: List<Statement>) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {
            return visitor.visit(this)
        }

    }

    class Print(val expr: compiler.inputTypes.Expression) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any {

            return visitor.visit(this)

        }

    }

    class Expression(val expr: compiler.inputTypes.Expression) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class Block(val statementList: List<Statement>) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any {

            return visitor.visit(this)

        }

    }


    class Declaration(val name: Token, val expr: compiler.inputTypes.Expression?) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class If(val ifBranch: compiler.inputTypes.Expression, val acted: Statement?, val elseBranch: Statement?) :
        Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class While(val expr: compiler.inputTypes.Expression, val body: Statement) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {

            return visitor.visit(this)

        }

    }

    class Return(val name: Token, val value: compiler.inputTypes.Expression?) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {
            return visitor.visit(this)
        }

    }

    class ClassDec(
        val name: Token,
        val methods: List<Function>,
        val superClass: compiler.inputTypes.Expression.Variable?,
        val init: Init?
    ) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {
            return visitor.visit(this)
        }

    }


    abstract fun <R> accept(visitor: StateVisitor<R>): Any?
}

