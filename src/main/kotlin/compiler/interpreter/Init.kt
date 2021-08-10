package compiler.interpreter

import compiler.env.Env
import compiler.inputTypes.Statement
import compiler.tokens.Token


class Init(val name: Token, val executeBlock: Statement.Block) : Callee {
    private val closure = Env()

    override fun call(interpreter: InterVisitor, arguments: List<Any?>) =
        interpreter.executeBlock(executeBlock.statementList, closure)

    override fun arity(): Int = 0

}