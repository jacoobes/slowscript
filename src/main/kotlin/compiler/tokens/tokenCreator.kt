package compiler.tokens

import compiler.Sscript
import compiler.tokens.TOKEN_TYPES.*
import java.io.BufferedReader

class Tokenizer(file: BufferedReader) {
    private val src: String = file.use { it.readText() }

    val tokens = mutableListOf<Token>()

    private var index = 0
    private var line = 1
    private var start = 0


    private val isAtEnd
        get() = index >= src.length

    private val currentChar
        get() = src.elementAt(index)

    private fun advance() = src.elementAt(index++)

    private fun Char.matches(expected: Char): Boolean {
        if (isAtEnd) return false
        if (this != expected) return false

        index++
        return true
    }

    private fun MutableList<Token>.addComplex(type: TOKEN_TYPES) {
        val text = src.substring(start, index)
        add(Token(type, text, line))
    }

    private fun MutableList<Token>.addComplex(type: TOKEN_TYPES, value: String, literalValue: Any?) {
        add(Token(type, value, literalValue, line))
    }

    private val peek
        get() = if (isAtEnd) 0.toChar() else currentChar
    private val peekNext
        get() = if (index + 1 >= src.length) 0.toChar() else src.elementAt(index + 1)

    fun string() {
        while (peek != '"' && !isAtEnd) advance()
        if (isAtEnd) Sscript.error(line, "Incomplete string")

        advance()

        val cut = src.substring(start + 1, index - 1)
        tokens.addComplex(STRING, cut, cut)
    }


    fun tokenize(): MutableList<Token> {
        while (!isAtEnd) {
            start = index
            val char = advance()

            when {

                char == '{' -> tokens.add(Token(LEFT_BRACE, char.toString(), line))
                char == '}' -> tokens.add(Token(RIGHT_BRACE, char.toString(), line))
                char == '(' -> tokens.add(Token(LEFT_PAREN, char.toString(), line))
                char == ')' -> tokens.add(Token(RIGHT_PAREN, char.toString(), line))
                char == ';' -> tokens.add(Token(SEMICOLON, char.toString(), line))
                char == '.' -> tokens.add(Token(DOT, char.toString(), line))
                char == ',' -> tokens.add(Token(COMMA, char.toString(), line))
                char == ':' -> tokens.add(Token(COLON, char.toString(), line))
                char == '?' -> tokens.add(Token(QUESTION, char.toString(), line))

                char == '#' -> {
                    while (currentChar.matches('#') && !isAtEnd) {
                        advance()
                    }
                }

                char == '!' -> {
                    if (currentChar.matches('=')) tokens.addComplex(NOT_EQUAL)
                    else tokens.add(Token(NOT, char.toString(), line))
                }

                char == '<' -> {
                    if (currentChar.matches('=')) tokens.addComplex(LESS_THAN_OR_EQUAL)
                    else tokens.add(Token(LEFT_TRIANGLE, char.toString(), line))
                }

                char == '>' -> {
                    if (currentChar.matches('=')) tokens.addComplex(GREAT_THAN_OR_EQUAL)
                    else tokens.add(Token(RIGHT_TRIANGLE, char.toString(), line))
                }
                char == '=' -> {
                    if (currentChar.matches('=')) tokens.addComplex(EQUAL_EQUAL)
                    else tokens.add(Token(ASSIGNMENT, char.toString(), line))
                }

                char == '+' -> {
                    when {
                        currentChar.matches('=') -> tokens.addComplex(PLUS_EQUALS)
                        currentChar.matches('+') -> tokens.addComplex(INCREMENT)
                        else -> tokens.add(Token(PLUS, char.toString(), line))
                    }
                }

                char == '-' -> {
                    when {
                        currentChar.matches('=') -> tokens.addComplex(MINUS_EQUALS)
                        currentChar.matches('>') -> tokens.addComplex(ARROW)
                        currentChar.matches('-') -> tokens.addComplex(DECREMENT)
                        else -> tokens.add(Token(MINUS, char.toString(), line))
                    }
                }
                char == '%' -> {
                    if (currentChar.matches('=')) tokens.addComplex(MOD_EQUALS)
                    else tokens.add(Token(MODULUS, char.toString(), line))
                }

                char == '*' -> {
                    if (currentChar.matches('=')) tokens.addComplex(MULT_EQUAL)
                    else tokens.add(Token(MULT, char.toString(), line))
                }

                char == '/' -> {
                    when {
                        currentChar.matches('/') -> while (peek != '\n' && !isAtEnd) advance()
                        currentChar.matches('=') -> tokens.addComplex(DIV_EQUALS)
                        else -> tokens.add(Token(DIVIDE, char.toString(), line))
                    }
                }

                char == '&' -> if (currentChar.matches('&')) tokens.addComplex(AND)
                char == '|' -> if (currentChar.matches('|')) tokens.addComplex(OR)

                char == '"' -> string()


                char.isDigit() -> {

                    while (peek.isDigit()) {
                        advance()
                        if (isAtEnd) break
                    }

                    if (peek == '.' && peekNext.isDigit()) {
                        advance()
                        while (currentChar.isDigit()) {
                            advance()
                            if (isAtEnd) break
                        }
                    }

                    tokens.addComplex(NUMBER, src.substring(start, index), src.substring(start, index).toDouble())

                }


                char.isWhitespace() -> if (char == '\n') line++
                else -> {
                    if (char.isLetter()) {
                        while (peek.isLetterOrDigit()) {
                            advance()
                            if (isAtEnd) break
                        }

                        val type = Sscript.reservedKeywords.let {
                            val word = src.substring(start, index)
                            it.getOrElse(word) { IDENTIFIER }
                        }

                        tokens.addComplex(type)

                    } else {
                        Sscript.error(line, "Unexpected character", char.toString())

                    }


                }


            }

        }
        tokens.add(Token(END, "", line))
        return tokens

    }

}



