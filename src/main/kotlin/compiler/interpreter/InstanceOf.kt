package compiler.interpreter

import compiler.env.Entity
import compiler.tokens.Token

class InstanceOf(private val clase: Entity) {
    private val properties = HashMap<String, Any>()
    override fun toString(): String {
        return "${clase.name} instance"
    }

    fun get(name: Token): Any? {
        if (properties.containsKey(name.lexeme)) {
            return properties[name.lexeme]
        }
        clase.findMethod(name.lexeme)?.let { return it.bind(this) }
            ?: throw RuntimeError("Undefined property ${name.lexeme} of $clase", name)

    }

    fun set(name: Token, value: Any) {
        properties[name.lexeme] = value
    }

}