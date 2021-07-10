package compiler

import compiler.tokens.TOKEN_TYPES
import compiler.tokens.TOKEN_TYPES.*
import compiler.tokens.Token
import java.io.BufferedReader

fun tokenCreator(file: BufferedReader): List<Token> {

    val src: String = file.readText()

    val token = mutableListOf<Token>()


    var index = 0
    var line = 1
    var start = 0


    val isAtEnd = { index >= src.length; }
    val advance = { src.elementAt(index++) }

    val match = fun(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (src.elementAt(index) != expected) return false

        index++
        return true
    }

    val addComplexToken = fun(type: TOKEN_TYPES) {
        val text: String = src.substring(start, index)
        token.add(Token(type, text, line))
    }

    fun addComplexToken(type: TOKEN_TYPES, value: String, literalValue: Any?) {
        token.add(Token(type, value, literalValue, line))
    }

    fun peek() = if (isAtEnd()) 0.toChar() else src.elementAt(index)


    fun peekNext(): Char {
        if (index + 1 >= src.length) return 0.toChar()
        return src.elementAt(index + 1)
    }


    fun string() {

        while (peek() != '"' && !isAtEnd()) advance()
        if (isAtEnd()) {
            Sscript.error(line, "Incomplete string")
        }
        advance()
        val cut = src.substring(start + 1, index - 1)

        addComplexToken(STRING, cut, cut)
    }

    while (!isAtEnd()) {
        start = index
        val char = advance()

        when {

            char == '{' -> token.add(Token(LEFT_BRACE, char.toString(), line))
            char == '}' -> token.add(Token(RIGHT_BRACE, char.toString(), line))
            char == '(' -> token.add(Token(LEFT_PAREN, char.toString(), line))
            char == ')' -> token.add(Token(RIGHT_PAREN, char.toString(), line))
            char == ';' -> token.add(Token(SEMICOLON, char.toString(), line))
            char == '.' -> token.add(Token(DOT, char.toString(), line))
            char == ',' -> token.add(Token(COMMA, char.toString(), line))
            char == ':' -> token.add(Token(COLON, char.toString(), line))
            char == '?' -> token.add(Token(QUESTION, char.toString(), line))

            char == '#' -> {
                while (!match('#') && !isAtEnd()) {
                    advance()

                }
            }

            char == '!' -> {
                if (match('=')) addComplexToken(NOT_EQUAL)
                else token.add(Token(NOT, char.toString(), line))
            }

            char == '<' -> {
                if (match('=')) addComplexToken(LESS_THAN_OR_EQUAL)
                else token.add(Token(LEFT_TRIANGLE, char.toString(), line))
            }

            char == '>' -> {
                if (match('=')) addComplexToken(GREAT_THAN_OR_EQUAL)
                else token.add(Token(RIGHT_TRIANGLE, char.toString(), line))
            }
            char == '=' -> {
                if (match('=')) addComplexToken(EQUAL_EQUAL)
                else token.add(Token(ASSIGNMENT, char.toString(), line))
            }

            char == '+' -> {
                when {
                    match('=') -> addComplexToken(PLUS_EQUALS)
                    match('+') -> addComplexToken(INCREMENT)
                    else -> token.add(Token(PLUS, char.toString(), line))
                }
            }

            char == '-' -> {
                when {
                    match('=') -> addComplexToken(MINUS_EQUALS)
                    match('>') -> addComplexToken(ARROW)
                    match('-') -> addComplexToken(DECREMENT)
                    else -> token.add(Token(MINUS, char.toString(), line))
                }
            }
            char == '%' -> {
                if (match('=')) addComplexToken(MOD_EQUALS)
                else token.add(Token(MODULUS, char.toString(), line))
            }

            char == '*' -> {
                if (match('=')) addComplexToken(MULT_EQUAL)
                else token.add(Token(MULT, char.toString(), line))
            }

            char == '/' -> {
                when {
                    match('/') -> while (peek() != '\n' && !isAtEnd()) advance()
                    match('=') -> addComplexToken(DIV_EQUALS)
                    else -> token.add(Token(DIVIDE, char.toString(), line))
                }
            }

            char == '&' -> {
                if (match('&')) addComplexToken(AND)
                else token.add(Token(AMPER, char.toString(), line))
            }
            char == '|' -> {
                if (match('|')) addComplexToken(OR)
                else token.add(Token(LINE, char.toString(), line))
            }

            char == '"' -> string()


            char.isDigit() -> {

                while (peek().isDigit()) {
                    advance()
                    if (isAtEnd()) break
                }

                if (peek() == '.' && peekNext().isDigit()) {
                    advance()
                    while (src.elementAt(index).isDigit()) {
                        advance()
                        if (isAtEnd()) break
                    }
                }

                addComplexToken(NUMBER, src.substring(start, index), src.substring(start, index).toDouble())

            }


            char.isWhitespace() -> if (char == '\n') line++
            else -> {
                if (char.isLetter()) {
                    while (peek().isLetterOrDigit()) {
                        advance()
                        if (isAtEnd()) break
                    }

                    val type = Sscript.reservedKeywords().let {
                        val word = src.substring(start, index)
                        it.getOrElse(word) { IDENTIFIER }
                    }

                    addComplexToken(type)

                } else {
                    Sscript.error(line, "Unexpected character", char.toString())

                }


            }


        }

    }
    token.add(Token(END, "", line))
    return token
}


