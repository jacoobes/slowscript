package compiler.Statement

import compiler.Interpreter.RuntimeError
import tokens.Token

class Env {
    companion object {
        private var values: HashMap<String, Any?> = hashMapOf()


        fun define(name: Token, value: Any?) {
            if (values.containsKey(name.lexeme)) {
                println("WARNING: variable has already been declared as ${name.lexeme}. The current value of ${name.lexeme} has been overridden to the most recent declaration.")
            }
            values[name.lexeme] = value
        }

        fun get(name: Token): Any? {
            if (values.containsKey(name.lexeme)) {
                return values[name.lexeme]
            }
            throw RuntimeError("undefined variable ${name.lexeme}", name)
        }
    }
}