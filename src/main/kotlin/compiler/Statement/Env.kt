package compiler.Statement
import compiler.interpreter.RuntimeError
import tokens.Token

class Env(private val enclosed : Env? = null) {
    private var values: HashMap<String, Any?> = HashMap()

        fun define(name: Token, value: Any?) {
            if (values.containsKey(name.lexeme)) {
                println("WARNING: variable has already been declared as ${name.lexeme} in the same scope. The current value of ${name.lexeme} remains the same.")
                return
            }

            values[name.lexeme] = value
        }
        fun define(name: String, value: Any) {
            if (values.containsKey(name)) {
                println("WARNING: variable has already been declared as $name in the same scope. The current value of $name remains the same.")
                return
            }

            values[name] = value
        }
        fun get(name: Token): Any? {

            if (values.containsKey(name.lexeme)) {
                return values[name.lexeme]
            }
            if(enclosed != null) {
                return enclosed.get(name)
            }


             throw RuntimeError("Undefined variable ${name.lexeme}", name)

        }

        fun assign(identifier: Token, reassignVal: Any?) {
            if(values.containsKey(identifier.lexeme)) {
                values[identifier.lexeme] = reassignVal
                return
            }

            if(enclosed != null) {
                enclosed.assign(identifier,reassignVal)
                return
            }
            throw RuntimeError("Variable ${identifier.lexeme} has not been declared!", identifier)
        }

        fun getAt(distance: Int, lexeme: String): Any? {
            return ancestor(distance).values[lexeme]
        }
        fun assignAt(distance: Int, name: Token, value: Any?) {
            ancestor(distance).values[name.lexeme] = value
        }

        private fun ancestor(distance: Int): Env {
            var environment : Env = this
            var i = 0
            while( i < distance) {
                environment = environment.enclosed!!
                    i++
            }
            return environment
        }



}