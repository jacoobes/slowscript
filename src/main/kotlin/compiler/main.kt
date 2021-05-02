

import compiler.Stopwatch
import compiler.piekLite
import tokens.TOKEN_TYPES
import tokens.Token
import java.io.BufferedReader
import java.io.File

import kotlin.system.exitProcess

fun main(args : Array<String>) {
    val timer = Stopwatch()


  //  Compiler.piekLite.run(args)

timer.start()

    val bufRead : BufferedReader = File("src/main/kotlin/compiler/first.pkl").bufferedReader()

    println(tokenCreator(bufRead))
    println()
    println("${timer.elapsedTime} ms elapsed")

timer.stop()


}

fun tokenCreator (file: BufferedReader)  : List<Token> {

    val src: String = file.readText()

    val token = mutableListOf<Token>()


    var index = 0;
    var line = 1;
    var start = 0;


    fun isAtEnd(): Boolean {
        return index >= src.length; }

    fun advance(): Char {

        return src.elementAt(index++)

    }

    fun match(expected: Char): Boolean {
        if (isAtEnd()) return false;
        if (src.elementAt(index) != expected) return false;

        index++;
        return true
    }

    fun addComplexToken(type: TOKEN_TYPES) {
        val text: String = src.substring(start, index)
        token.add(Token(type, text, line))
    }

    fun addComplexToken(type: TOKEN_TYPES, value: Any) {
        token.add(Token(type, value, line))
    }

    fun peek(): Char {
        if (isAtEnd()) return 0.toChar()
        return src.elementAt(index)
    }

    fun peekNext(): Char {
        if (index + 1 >= src.length) return 0.toChar()
        return src.elementAt(index + 1)
    }

    fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                piekLite.error(line, "Strings cannot enter into a new line", src[index].toString())
            }
            advance()
        }
        if (isAtEnd()) {
            piekLite.error(line, "Incomplete string")
            return
        }
        advance()
        addComplexToken(TOKEN_TYPES.STRING, src.substring(start + 1, index - 1))
    }


    while (!isAtEnd()) {
        start = index
        var char = advance()

        if(piekLite.hadError) {
           exitProcess(65)
        }

        when {


            char == '{' -> token.add(Token(TOKEN_TYPES.LEFT_BRACE, char.toString(), line))
            char == '}' -> token.add(Token(TOKEN_TYPES.RIGHT_BRACE, char.toString(), line))
            char == '(' -> token.add(Token(TOKEN_TYPES.LEFT_PAREN, char.toString(), line))
            char == ')' -> token.add(Token(TOKEN_TYPES.RIGHT_PAREN, char.toString(), line))
            char == ';' -> token.add(Token(TOKEN_TYPES.SEMICOLON, char.toString(), line))
            char == '.' -> token.add(Token(TOKEN_TYPES.DOT, char.toString(), line))
            char == ',' -> token.add(Token(TOKEN_TYPES.COMMA, char.toString(), line))
            char == ':' -> token.add(Token(TOKEN_TYPES.COLON, char.toString(), line))

            char == '!' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.NOT_EQUAL)
                else token.add(Token(TOKEN_TYPES.NOT, char.toString(), line))

            }
            char == '<' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.LESS_THAN_OR_EQUAL)
                else token.add(Token(TOKEN_TYPES.LEFT_TRIANGLE, char.toString(), line))
            }

            char == '>' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.GREAT_THAN_OR_EQUAL)
                else token.add(Token(TOKEN_TYPES.RIGHT_TRIANGLE, char.toString(), line))
            }
            char == '=' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.EQUAL_EQUAL)
                else token.add(Token(TOKEN_TYPES.ASSIGNMENT, char.toString(), line))
            }
            char == '?' -> {
                if (match('+')) addComplexToken(TOKEN_TYPES.AND)
                else token.add(Token(TOKEN_TYPES.OR, char.toString(), line))
            }
            char == '+' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.PLUS_EQUALS)
                else if (match('+')) addComplexToken(TOKEN_TYPES.INCREMENT)
                else token.add(Token(TOKEN_TYPES.PLUS, char.toString(), line))
            }

            char == '-' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.MINUS_EQUALS)
                else if (match('-')) addComplexToken(TOKEN_TYPES.DECREMENT)
                else token.add(Token(TOKEN_TYPES.MINUS, char.toString(), line))
            }
            char == '%' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.MODULUS)
                else token.add(Token(TOKEN_TYPES.MOD_EQUALS, char.toString(), line))
            }
            char == '*' -> {
                if (match('=')) addComplexToken(TOKEN_TYPES.MULT_EQUAL)
                else token.add(Token(TOKEN_TYPES.MULT, char.toString(), line))
            }

            char == '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else if (match('=')) {
                    addComplexToken(TOKEN_TYPES.DIV_EQUALS)
                } else {
                    token.add(Token(TOKEN_TYPES.DIVIDE, char.toString(), line))
                }
            }

            char == '"' -> {
                string()
            }

            char.isDigit() -> {

                while (src.elementAt(index).isDigit()) {

                    advance()
                }

                if (peek() == '.' && peekNext().isDigit()) {
                    advance()
                    while (src.elementAt(index).isDigit()) advance()
                }

                addComplexToken(TOKEN_TYPES.NUMBER, src.substring(start, index).toDouble())

            }


            char.isWhitespace() -> if (char == '\n') {
                line++
            }

            else -> {
                if (char.isLetter()) {
                    while (src.elementAt(index).isLetterOrDigit()) {
                        advance()
                    }

                    val hashReserved = piekLite.reservedKeywords()
                    val word = src.substring(start, index)
                    val type = hashReserved.getOrElse(word, { TOKEN_TYPES.IDENTIFIER})
                    addComplexToken(type)

                } else {
                    piekLite.error(line, "Unexpected character", char.toString())

                }


            }


        }

    }
    return token
}

