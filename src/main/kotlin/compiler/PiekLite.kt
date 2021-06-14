package compiler


import compiler.interpreter.InterVisitor
import compiler.interpreter.RuntimeError
import compiler.parser.Parser
import compiler.resolver.Resolver
import compiler.tokens.TOKEN_TYPES
import kotlin.system.exitProcess
import java.io.File
import compiler.utils.Stopwatch
import compiler.tokens.Token

class piekLite {

    companion object {
        private var hadError = false
        private var hadRuntimeError = false
        private val interpreter = InterVisitor()
        private val resolver = Resolver(interpreter)
        private val timer = Stopwatch()
        fun run( args : Array<String>) {

            if(args.size != 1) {
                println("Usage: piekL [script]")
                exitProcess(64)

            }
                timer.start()
                runFile(args[0])
                println()
                println("${timer.elapsedTime} ms elapsed")
                timer.stop()


        }


        private fun runFile(path: String) {

            if(!hadError) {
                File(path).bufferedReader().run {
                    val statements = Parser(tokenCreator(this)).parse()
                    statements.let {

                        if (hadError)  exitProcess(65)
                        if(hadRuntimeError) exitProcess(70)
                            resolver.resolve(it)
                        if(hadError) return
                            interpreter.interpret(it)
                    }
                }

                }

            }



        fun error (line: Int, message: String, where: String ) {
            println(Error("[line $line] Error $where: $message"))
            hadError = true
        }

        fun error(line: Int, message: String,) {
            println(Error("[line $line] Error : $message"))
            hadError = true
        }
        fun error(token:Token, message: String)  {
            if(token.type == TOKEN_TYPES.END) {
                error("line ${token.line} unexpected end of program with ${token.lexeme}")
            } else {
                error(token.line, "$message ${token.lexeme}")
            }

        }

        fun reservedKeywords(): HashMap<String, TOKEN_TYPES> {

            return hashMapOf(
                "super" to TOKEN_TYPES.SUPER,
                "class" to TOKEN_TYPES.CLASS,
                "return" to TOKEN_TYPES.RETURN,
                "var" to TOKEN_TYPES.MUTABLE_VARIABLE,
                "task" to TOKEN_TYPES.TASK,
                "false" to TOKEN_TYPES.FALSE,
                "true" to TOKEN_TYPES.TRUE,
                "null" to TOKEN_TYPES.NULL,
                "if" to TOKEN_TYPES.IF,
                "else" to TOKEN_TYPES.ELSE,
                "loop" to TOKEN_TYPES.LOOP,
                "while" to TOKEN_TYPES.WHILE,
                "of" to TOKEN_TYPES.OF,
                "from" to TOKEN_TYPES.FROM,
                "super" to TOKEN_TYPES.SUPER,
                "this" to TOKEN_TYPES.THIS,
                "public" to TOKEN_TYPES.PUBLIC,
                "private" to TOKEN_TYPES.PRIVATE,
                "protected" to TOKEN_TYPES.PROTECTED,
                "all" to TOKEN_TYPES.ALL,
                "NaN" to TOKEN_TYPES.NaN,
                "log" to TOKEN_TYPES.DISPLAY,
                "as" to TOKEN_TYPES.AS,
                "STOP" to TOKEN_TYPES.STOP,
                "instance" to TOKEN_TYPES.INSTANCE,
                "init" to TOKEN_TYPES.INIT_BLOCK
            )
        }

        fun error(error: RuntimeError) {
            println("${error.message} line ${error.token.line}" )
            hadRuntimeError = true
        }


    }


}