package compiler.Statement

import compiler.tokens.Token



abstract class Statement {

    interface StateVisitor<R> {

        fun <R> visit(visitor: Print): Any
        fun <R> visit(visitor: Expression): Any?
        fun <R> visit(declaration: Declaration): Any?
        fun <R> visit(block: Block)
        fun <R> visit(tree: If): Any?
        fun <R> visit(loop: While): Any?
        fun <R> visit(function: Function): Any?
        fun <R> visit(arg: Return): Any?
        fun <R> visit(classDec: ClassDec): Any?



    }
    class Function(val fnName : Token, val parameters : List<Token>, val body : List<Statement>) : Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {
            return visitor.visit<R>(this)
        }

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
    class Return(val name: Token, val value: compiler.Expression?): Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {
            return visitor.visit<R>(this)
        }

    }

    class ClassDec(val name: Token, val methods: List<Function>, val superClass: compiler.Expression.Variable?): Statement() {
        override fun <R> accept(visitor: StateVisitor<R>): Any? {
            return visitor.visit<R>(this)
        }

    }





    abstract fun <R> accept(visitor: StateVisitor<R>): Any?
}

