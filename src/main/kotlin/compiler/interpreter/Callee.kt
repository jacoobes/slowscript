package compiler.interpreter

import compiler.Statement.Statement

interface Callee {

    fun call(interpreter : InterVisitor, arguments: List<Any?>) : Any?
    fun arity() : Int
}