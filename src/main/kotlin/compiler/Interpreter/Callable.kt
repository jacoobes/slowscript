package compiler.Interpreter

import compiler.Statement.*

class Callable(private val declaration: Statement.Function, private val closure : Env) : Callee {
    override fun call(interpreter: InterVisitor, arguments: List<Any?>): Any? {
        val functionEnv = Env(closure)
        for ((index, args) in declaration.parameters.withIndex()) {
            arguments[index]?.let { functionEnv.define(args.lexeme, it) }
        }
        try {
            interpreter.executeBlock(declaration.body, functionEnv)
        } catch (returnStmt : Return) {
            return returnStmt.value
        }
        return null
    }

    override fun arity(): Int {
        return declaration.parameters.size
    }

    override fun toString(): String {
        return declaration.fnName.lexeme
    }
}