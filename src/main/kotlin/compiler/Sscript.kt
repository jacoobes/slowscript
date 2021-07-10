package compiler


import compiler.interpreter.InterVisitor
import compiler.interpreter.RuntimeError
import compiler.parser.Parser
import compiler.resolver.Resolver
import compiler.tokens.TOKEN_TYPES
import compiler.tokens.TOKEN_TYPES.*
import compiler.tokens.Token
import compiler.utils.Stopwatch
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

class Sscript {

    companion object {
        private var hadError = false
        private var hadRuntimeError = false
        private val interpreter = InterVisitor()
        private val resolver = Resolver(interpreter)
        private val timer = Stopwatch()

        fun run(args: Array<String>) {

            if (args.size != 1) {
                println("Usage: spt [script]")
                exitProcess(64)
            }

            timer.start()
            runFile(args[0])
            println()
            timer.stop()
            println("${timer.elapsedTime} ms elapsed")

        }

        private fun runFile(path: String) {
            val mainFile = File(path)
            if(mainFile.isDirectory || mainFile.name != "main.spt") throw IOException("Main file cannot be found or the path is a directory. Name of file must be main.spt")
            if (!hadError) {

               mainFile.bufferedReader().run {
                    val statements = Parser(tokenCreator(this)).parse()

                    statements.let {

                        if (hadError) exitProcess(65)
                        if (hadRuntimeError) exitProcess(70)
                            resolver.resolve(it)
                        if (hadError) return
                            interpreter.interpret(it)
                    }
                }

            }

        }


        fun error(line: Int, message: String, where: String) {
            println(Error("[line $line] Error $where: $message"))
            hadError = true
        }

        fun error(line: Int, message: String) {
            println(Error("[line $line] Error : $message"))
            hadError = true
        }

        fun error(token: Token, message: String) {
            if (token.type == END) {
                error("line ${token.line} unexpected end of program with ${token.lexeme}")
            } else {
                error(token.line, "$message ${token.lexeme}")
            }

        }

        fun reservedKeywords(): HashMap<String, TOKEN_TYPES> {

            return hashMapOf(
                "super" to SUPER,
                "class" to CLASS,
                "return" to RETURN,
                "var" to MUTABLE_VARIABLE,
                "val" to IMMUTABLE_VARIABLE,
                "task" to TASK,
                "false" to FALSE,
                "true" to TRUE,
                "null" to NULL,
                "if" to IF,
                "else" to ELSE,
                "loop" to LOOP,
                "while" to WHILE,
                "of" to OF,
                "from" to FROM,
                "super" to SUPER,
                "this" to THIS,
                "public" to PUBLIC,
                "private" to PRIVATE,
                "protected" to PROTECTED,
                "all" to ALL,
                "NaN" to NaN,
                "log" to DISPLAY,
                "as" to AS,
                "STOP" to STOP,
                "this" to INSTANCE,
                "init" to INIT_BLOCK,
                "module" to MODULE
            )
        }

        fun error(error: RuntimeError) {
            println("${error.message} line ${error.token.line}")
            hadRuntimeError = true
        }


    }


}