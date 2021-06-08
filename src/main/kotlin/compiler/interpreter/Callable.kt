package compiler.interpreter

import compiler.Statement.*


open class Callable(private val declaration: Statement.Function, private val closure : Env, private val isInitializer: Boolean) : Callee {

    override fun call(interpreter: InterVisitor, arguments: List<Any?>): Any? {
        val functionEnv = Env(closure)
        for ((index, args) in declaration.parameters.withIndex()) {
            arguments[index]?.let { functionEnv.define(args.lexeme, it) }
        }
        try {
            interpreter.executeBlock(declaration.body, functionEnv)
        } catch (returnStmt : Return) {

            if(isInitializer) return closure.getAt(0, "instance")
            return returnStmt.value
        }
        if(isInitializer) return closure.getAt(0, "instance")
        return null
    }

    override fun arity(): Int {
        return declaration.parameters.size
    }

    override fun toString(): String {
        return declaration.fnName.lexeme
    }

    fun bind(instance : InstanceOf) : Callable {
        val instanceEnv = Env(closure)
        instanceEnv.define("instance", instance )
        return Callable(declaration, instanceEnv, isInitializer)
    }

}