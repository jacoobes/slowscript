package compiler


import compiler.Interpreter.InterVisitor
import compiler.Interpreter.RuntimeError
import compiler.Parser.Parser
import compiler.tokenCreator
import tokens.TOKEN_TYPES
import kotlin.system.exitProcess
import java.io.File
import java.io.BufferedReader
import compiler.utils.Stopwatch
import tokens.Token

class piekLite {

    companion object {
        var hadError = false;
        var hadRuntimeError = false;

        val timer = Stopwatch()
        fun run( args : Array<String>) {

            if(args.size != 1) {
                println("Usage: piekL [script]")
                exitProcess(64);

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
                        InterVisitor().interpret(it)
                    }
                }

                }

            }



        fun error (line: Int, message: String, where: String ) {
            println(Error("[line $line] Error $where: $message"));
            hadError = true
        }

        fun error(line: Int, message: String,) {
            println(Error("[line $line] Error : $message"));
            hadError = true
        }
        fun error(token:Token, message: String)  {
            if(token.type == TOKEN_TYPES.END) {
                error("line ${token.line} unexpected end of program with ${token.lexeme}")
            } else {
                error(token.line, "$message ${token.lexeme}")
            }

        }

        fun reservedKeywords() : HashMap<String, TOKEN_TYPES> {
            val reservedKeywords = hashMapOf<String, TOKEN_TYPES>()
            reservedKeywords["super"] = TOKEN_TYPES.SUPER
            reservedKeywords["class"] = TOKEN_TYPES.CLASS
            reservedKeywords["return"] = TOKEN_TYPES.RETURN
            reservedKeywords["var"] = TOKEN_TYPES.MUTABLE_VARIABLE
            reservedKeywords["val"] = TOKEN_TYPES.IMMUTABLE_VARIABLE
            reservedKeywords["task"] = TOKEN_TYPES.TASK
            reservedKeywords["false"] = TOKEN_TYPES.FALSE
            reservedKeywords["true"] = TOKEN_TYPES.TRUE
            reservedKeywords["null"] = TOKEN_TYPES.NULL
            reservedKeywords["if"] = TOKEN_TYPES.IF
            reservedKeywords["else if"] = TOKEN_TYPES.ELSE_IF
            reservedKeywords["else"] = TOKEN_TYPES.ELSE
            reservedKeywords["loop"] = TOKEN_TYPES.LOOP
            reservedKeywords["while"] = TOKEN_TYPES.WHILE
            reservedKeywords["of"] = TOKEN_TYPES.OF
            reservedKeywords["from"] = TOKEN_TYPES.FROM
            reservedKeywords["super"] = TOKEN_TYPES.SUPER
            reservedKeywords["this"] = TOKEN_TYPES.THIS
            reservedKeywords["public"] = TOKEN_TYPES.PUBLIC
            reservedKeywords["private"] = TOKEN_TYPES.PRIVATE
            reservedKeywords["protected"] = TOKEN_TYPES.PROTECTED
            reservedKeywords["all"] = TOKEN_TYPES.ALL
            reservedKeywords["NaN"] = TOKEN_TYPES.NaN
            reservedKeywords["log"] = TOKEN_TYPES.DISPLAY
            return reservedKeywords
        }

        fun error(error: RuntimeError) {
            println("${error.message} line ${error.token.line}" )
            hadRuntimeError = true
        }


    }


}