package compiler.Interpreter
interface Callee {

    fun call(interpreter : InterVisitor, arguments: List<Any?>) : Any?
    fun arity() : Int
}