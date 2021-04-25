import jdk.nashorn.internal.objects.NativeRegExp.source
import java.io.BufferedReader
import java.io.File
import java.lang.Error
import kotlin.system.exitProcess


fun main(args : Array<String>) {
    val timer = Stopwatch()


  //  piekLite.run(args)

timer.start()
    //val parser = Parser(tokenCreator(bufRead))
   // println(parser.createAST())
    val bufRead : BufferedReader = File("src/main/kotlin/first.pkL").bufferedReader()
    println(tokenCreator(bufRead))
timer.stop()
println()
println("${timer.elapsedTime} ms elapsed")

}

fun tokenCreator (file: BufferedReader)  : List<Token> {

    var src: String = file.readText()

    val token = mutableListOf<Token>()


    var index = 0;
    var line = 1;
    var start = 0;


    fun isAtEnd(): Boolean { return index >= src.length; }

    fun advance() : Char {

        return src.elementAt(index++)

    }
    fun match(expected: Char) : Boolean {
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

    fun peek() : Char {
        if(isAtEnd()) return 0.toChar()
        return src.elementAt(index)
    }

    fun peekNext() : Char {
        if(index + 1 >= src.length) return 0.toChar()
        return src.elementAt(index + 1)
    }

    fun isAlpha(char: Char) : Boolean {
        return char.isLetter() || char == '_'
    }

    fun isAlphaNumeric(char: Char) : Boolean {
        return isAlpha(char) && char.isDigit()
    }

    fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') {
                piekLite.error(line, "Strings cannot enter into a new line", src[index].toString() )
            }
            advance()
        }
        if(isAtEnd()) {
            piekLite.error(line, "Incomplete string")
            return
        }
        advance()
        addComplexToken(TOKEN_TYPES.STRING, src.substring(start + 1, index - 1))
    }

     while (!isAtEnd()) {
         start = index
         var char = advance()

        when {

            piekLite.hadError -> exitProcess(65)
            char == '{' -> token.add(Token(TOKEN_TYPES.LEFT_BRACE, char.toString(), line))
            char == '}' -> token.add(Token(TOKEN_TYPES.RIGHT_BRACE, char.toString(), line))
            char == '(' -> token.add(Token(TOKEN_TYPES.LEFT_PAREN, char.toString(), line))
            char == ')' -> token.add(Token(TOKEN_TYPES.RIGHT_PAREN, char.toString(), line))
            char == ';' -> token.add(Token(TOKEN_TYPES.SEMICOLON, char.toString(), line))
            char == '.' -> token.add(Token(TOKEN_TYPES.DOT, char.toString(), line))
            char == '%' -> token.add(Token(TOKEN_TYPES.MODULUS, char.toString(), line))
            char == ',' -> token.add(Token(TOKEN_TYPES.COMMA, char.toString(), line))
            char == ':' -> token.add(Token(TOKEN_TYPES.COLON, char.toString(), line))

            char == '!' -> {
                if(match('=')) addComplexToken(TOKEN_TYPES.NOT_EQUAL)
                else token.add(Token(TOKEN_TYPES.NOT, char.toString(), line))

            }
            char == '<' -> {
                if(match('=')) addComplexToken(TOKEN_TYPES.LESS_THAN_OR_EQUAL)
                else token.add(Token(TOKEN_TYPES.LESS, char.toString(), line))
            }

            char == '>' -> {
                if(match('=')) addComplexToken(TOKEN_TYPES.GREAT_THAN_OR_EQUAL)
                else token.add(Token(TOKEN_TYPES.GREATER, char.toString(), line))
            }
            char == '=' -> {
                if(match('=')) addComplexToken(TOKEN_TYPES.EQUAL_EQUAL)
                else token.add(Token(TOKEN_TYPES.ASSIGNMENT, char.toString(), line))
            }
            char == '?' -> {
                if(match('+')) addComplexToken(TOKEN_TYPES.AND)
                else token.add(Token(TOKEN_TYPES.OR, char.toString(), line))
            }

            char == '/' -> {
                if(match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    token.add(Token(TOKEN_TYPES.DIVIDE, char.toString(), line))
                }
            }

            char == '"' -> {
                string()
            }

            char.isDigit() -> {

                while(src.elementAt(index).isDigit()) {

                    advance()
                }

                if(peek() == '.' && peekNext().isDigit()) {
                    advance()
                    while(src.elementAt(index).isDigit()) advance()
                }

                addComplexToken(TOKEN_TYPES.NUMBER, src.substring(start, index).toDouble())

            }


            char.isWhitespace() -> {

                if(char == '\n') {
                    line++
                }
            }

            else -> {

               // piekLite.error(line, "Unexpected character", char.toString())

                }


            }


        }


    return token
}


//        when {
//            piekLite.hadError -> exitProcess(65);
//
//            char == '"' -> {
//                val secondChar = src.findAnyOf(listOf("\""), index + 1)?.first ?: throw Error("Unclosed string")
//
//               val string = src.substring(index, secondChar + 1)
//                concatChars = ""
//                index = secondChar + 1
//                token.add(Pair(string, TOKEN_TYPES.STRING))
//            }
//
//            char.toString().matches(dataRegex) -> {
//                token.add(Pair(char.toString(), ""))
//                index++;
//            }
//
//            char == ',' -> {
//                token.add(Pair(char.toString(), ""))
//                concatChars = ""
//                index++;
//            }
//
//            concatChars == "var" -> {
//                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " ", ")"), index + 1)?.first ?: throw Error("Not initialized")
//                token.add(Pair(src.substring(index + 1, secondIndex), "mutable_variable"))
//                concatChars = ""
//                index = secondIndex;
//            }
//
//            concatChars == "val" -> {
//                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " ", ")"), index + 1)?.first ?: throw Error("Not initialized")
//                token.add(Pair(src.substring(index + 1, secondIndex), "immutable_variable"))
//                concatChars = ""
//                index = secondIndex;
//            }
//
//            concatChars == "true" || concatChars == "false" -> {
//
//                token.add(Pair(concatChars, "boolean_value"))
//
//                concatChars = ""
//            }
//
//            char.toString().matches(numRegex) -> {
//
//                var numIndex = index
//                while(true) {
//
//                    if(src[numIndex+1].toString().matches(numRegex) || src[numIndex + 1] == '.') {
//                        numIndex++
//                    }
//                    else {
//                        break
//                    }
//                }
//                val numberOf = src.substring(index, numIndex + 1)
//
//                index = numIndex + 1
//
//                token.add(Pair(numberOf, "number"))
//
//            }
//
//            char == '=' ->  {
//                if(src.elementAt(index + 1) == '=') {
//                    token.add(Pair("==", "equality_operator"))
//                    index++
//                } else {
//                    token.add(Pair(char.toString(), "initialization"))
//                }
//                index++
//            }
//
//            concatChars == "task" -> {
//                val secondIndex = src.findAnyOf(listOf("("), index)?.first ?: throw Error("Incorrect task syntax found")
//                val taskName = src.substring(index, secondIndex - 1).trim()
//
//
//                if(taskName == "loop") {
//
//                    token.add((Pair(taskName, "task")))
//
//                } else {
//
//                    token.add(Pair(taskName, "task"))
//                }
//                concatChars = ""
//                index = secondIndex;
//
//            }
//            concatChars == "if" -> {
//
//                val secondIndex = src.findAnyOf(listOf(")"), index)?.first ?: throw Error("incomplete if block found")
//                token.add(Pair(src.substring(index - 2, secondIndex + 1), "controlFlow_head"))
//                concatChars = ""
//                index = secondIndex + 1
//            }
//
//            concatChars == "elif" -> {
//
//                val secondIndex = src.findAnyOf(listOf(")"), index)?.first ?: throw Error("incomplete if block found")
//                token.add(Pair(src.substring(index - concatChars.length, secondIndex + 1), "controlFlow_body"))
//                concatChars = ""
//                index = secondIndex + 1
//            }
//
//            concatChars == "else" -> {
//                token.add(Pair(concatChars, "controlFlow_default"))
//                concatChars = ""
//                index++
//            }
//
//
//            char == '<' ->  {
//
//                token.add((Pair(char.toString(), "less_than")))
//                index++
//            }
//
//            char == '>' -> {
//
//                token.add((Pair(char.toString(), "greater_than")))
//                index++
//            }
//
//            concatChars == ">=" -> {
//
//                token.add((Pair(char.toString(), "greater_than_or_equal")))
//                concatChars = ""
//                index++
//            }
//
//            concatChars == "<=" -> {
//
//                token.add((Pair(char.toString(), "less_than_or_equal")))
//                concatChars = ""
//                index++
//            }
//
//            char.toString().matches(operatorRegex) -> {
//                token.add(Pair(char.toString(), "operator"))
//                index++;
//            }
//
//            char == ';' -> {
//                token.add(Pair(char.toString(), "endStatement"))
//
//                concatChars = ""
//                index++
//            }
//
//
//            else -> {
//                if(char.isWhitespace()){
//                index++
//                } else {
//                    concatChars += char
//                    index++;
//                }
//            };