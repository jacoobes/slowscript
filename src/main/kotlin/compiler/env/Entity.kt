package compiler.env

import compiler.interpreter.*

/**
 * Base class, kinda like Object in Java
 * Single and only base class for slowScript
 */
open class Entity(
    val name: String,
    private val superclass: Entity?,
    private val allMethods: HashMap<String, Callable>,
    private val init: Init?
) :
    Callee {
    /**
     * Call and instantiate a new class instance
     */
    override fun call(interpreter: InterVisitor, arguments: List<Any?>): InstanceOf {
        init?.call(interpreter, arguments)
        val newObj = findMethod("object")
        val instance = InstanceOf(this)
        newObj?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun arity(): Int {
        val newObj = findMethod("object")
        return newObj?.arity() ?: 0
    }

    override fun toString(): String {
        return name
    }

    fun findMethod(name: String): Callable? {

        allMethods.let {
            if (it.containsKey(name)) return it[name] ?: throw RuntimeException("No method found on ${it[name]}")
            if (superclass != null) return superclass.findMethod(name)
        }
        return null
    }
}