package compiler

import tokens.TOKEN_TYPES
import tokens.Token
import java.io.BufferedReader

fun tokenCreator (file: BufferedReader)  : List<Token> {

    val src: String = file.readText()

    val token = mutableListOf<Token>()


    var index = 0;
    var line = 1;
    var start = 0;


    val isAtEnd = { index >= src.length; }
    val advance = { src.elementAt(index++) }

    val match = fun (expected: Char): Boolean {
        if (isAtEnd()) return false;
        if (src.elementAt(index) != expected) return false;

        index++;
        return true
    }

    fun addComplexToken(type: TOKEN_TYPES) {
        val text: String = src.substring(start, index)
        token.add(Token(type, text, line))
    }

    fun addComplexToken(type: TOKEN_TYPES, value: String, literalValue: Any?) {
        token.add(Token(type, value, literalValue, line))
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
        }
        advance()
        addComplexToken(TOKEN_TYPES.STRING, src.substring(start + 1, index - 1), src.substring(start + 1, index - 1) )
    }


    while (!isAtEnd()) {
        start = index
        var char = advance()

        when {

            char == '{' -> token.add(Token(TOKEN_TYPES.LEFT_BRACE, char.toString(), line))
            char == '}' -> token.add(Token(TOKEN_TYPES.RIGHT_BRACE, char.toString(), line))
            char == '(' -> token.add(Token(TOKEN_TYPES.LEFT_PAREN, char.toString(), line))
            char == ')' -> token.add(Token(TOKEN_TYPES.RIGHT_PAREN, char.toString(), line))
            char == ';' -> token.add(Token(TOKEN_TYPES.SEMICOLON, char.toString(), line))
            char == '.' -> token.add(Token(TOKEN_TYPES.DOT, char.toString(), line))
            char == ',' -> token.add(Token(TOKEN_TYPES.COMMA, char.toString(), line))
            char == ':' -> token.add(Token(TOKEN_TYPES.COLON, char.toString(), line))
            char == '&' -> token.add(Token(TOKEN_TYPES.AND, char.toString(), line))
            char == '?' -> token.add(Token(TOKEN_TYPES.QUESTION, char.toString(), line))
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
            char == 'o' -> {
                if (match('r')) addComplexToken(TOKEN_TYPES.OR)
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
                if (match('=')) addComplexToken(TOKEN_TYPES.MOD_EQUALS)
                else token.add(Token(TOKEN_TYPES.MODULUS, char.toString(), line))
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
                    if(isAtEnd()) break;
                }

                if (peek() == '.' && peekNext().isDigit()) {
                    advance()
                    while (src.elementAt(index).isDigit()) {
                        advance()
                        if(isAtEnd()) break;
                    };
                }

                addComplexToken(TOKEN_TYPES.NUMBER, src.substring(start, index), src.substring(start, index).toDouble())

            }


            char.isWhitespace() -> if (char == '\n') line++

            else -> {
                if (char.isLetter()) {
                    while (src.elementAt(index).isLetterOrDigit()) {
                        advance()
                    }

                    val type = piekLite.reservedKeywords().let {
                        val word = src.substring(start, index)
                        it.getOrElse(word) { TOKEN_TYPES.IDENTIFIER }
                    }

                    addComplexToken(type)

                } else {
                    piekLite.error(line, "Unexpected character", char.toString())

                }


            }


        }

    }
    token.add(Token(TOKEN_TYPES.END, "", line))
    return token
}