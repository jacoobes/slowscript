package compiler.interpreter

interface Callee {

    fun call(interpreter: InterVisitor, arguments: List<Any?> = emptyList()): Any?

    fun arity(): Int
}