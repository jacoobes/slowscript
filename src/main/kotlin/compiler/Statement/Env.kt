package compiler.Statement
import compiler.Interpreter.RuntimeError
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
        fun get(name: Token): Any? {

            if (values.containsKey(name.lexeme)) {
                return values[name.lexeme]
            }
            if(enclosed != null) {
                return enclosed.get(name)
            }


             throw RuntimeError("undefined variable ${name.lexeme}", name)

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

}