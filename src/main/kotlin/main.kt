import java.io.File
import java.io.BufferedReader


fun main() {
    val timer = Stopwatch()
    val bufRead : BufferedReader = File("src/main/kotlin/db.lorem").bufferedReader()

timer.start()
    //val parser = Parser(tokenCreator(bufRead))
   // println(parser.createAST())

print(tokenCreator(bufRead))
timer.stop()
println()
println("${timer.elapsedTime} ms elapsed")

}

fun tokenCreator (file: BufferedReader)  : List<Pair<String, String>> {
    var src: String = file.readText()
    val token = mutableListOf<Pair<String, String>>()
    var concatChars = ""
    val dataRegex = Regex("[:(){}]")
    var index = 0;
    val numRegex  = Regex("^[1-9]\\d*(\\.\\d+)?\$")
    val operatorRegex = Regex("[+\\-%/*]")

     while (index < src.length) {
        var char = src.elementAt(index)

        when {

            char == '"' -> {
                val secondChar = src.findAnyOf(listOf("\""), index + 1)?.first ?: throw Error("Unclosed string")

               val string = src.substring(index, secondChar + 1)
                concatChars = ""
                index = secondChar + 1
                token.add(Pair(string, "string"))
            }

            char.toString().matches(dataRegex) -> {
                token.add(Pair(char.toString(), ""))
                index++;
            }

            char == ',' -> {
                token.add(Pair(char.toString(), ""))
                concatChars = ""
                index++;
            }

            concatChars == "->" -> {
                val secondIndex = src.findAnyOf(listOf(" ", "{"), index + 1)?.first ?: throw Error("No return type detected.")

                token.add(Pair(src.substring(index - 2, secondIndex), "return_type"))
                concatChars = ""
                index = secondIndex;
            }

            concatChars == "int" -> {
                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " ", ")"), index + 1)?.first ?: throw Error("Not initialized")
                token.add(Pair(src.substring(index + 1, secondIndex), "int_Init"))
                concatChars = ""
                index = secondIndex;
            }

            concatChars == "str" -> {
                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " ", ")"), index + 1)?.first ?: throw Error("Not initialized")
                token.add(Pair(src.substring(index + 1, secondIndex), "str_Init"))
                concatChars = ""
                index = secondIndex;
            }

            concatChars == "boolean" -> {
                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " ", ")"), index + 1)?.first ?: throw Error("Not initialized")
                token.add(Pair(src.substring(index + 1, secondIndex), "bool_Init"))
                concatChars = ""
                index = secondIndex;
            }

            concatChars == "double" -> {

                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " ", ")"), index + 1)?.first ?: throw Error("Not initialized")
                token.add(Pair(src.substring(index + 1, secondIndex), "double_Init"))
                concatChars = ""
                index = secondIndex;
            }

            concatChars == "char" -> {

                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " ", ")"), index + 1)?.first ?: throw Error("Not initialized")
                token.add(Pair(src.substring(index + 1, secondIndex), "char_Init"))
                concatChars = ""
                index = secondIndex;
            }

            concatChars == "true" || concatChars == "false" -> {

                token.add(Pair(concatChars, "boolean_value"))

                concatChars = ""
            }

            char.toString().matches(numRegex) -> {

                var numIndex = index
                while(true) {

                    if(src[numIndex+1].toString().matches(numRegex) || src[numIndex + 1] == '.') {
                        numIndex++
                    }
                    else {
                        break
                    }
                }
                val numberOf = src.substring(index, numIndex + 1)

                index = numIndex + 1

                token.add(Pair(numberOf, "number"))

            }

            char == '=' ->  {
                if(src.elementAt(index + 1) == '=') {
                    token.add(Pair("==", "equality_operator"))
                    index++
                } else {
                    token.add(Pair(char.toString(), "initialization"))
                }
                index++
            }

            concatChars == "task" -> {

                val secondIndex = src.findAnyOf(listOf("("), index)?.first ?: throw Error("Incorrect task syntax found")
                token.add(Pair(src.substring(index, secondIndex - 1), "task"))
                concatChars = ""
                index = secondIndex;

            }

            char.toString().matches(operatorRegex) -> {
                token.add(Pair(char.toString(), "operator"))
                index++;
            }
            char == ';' -> {
                token.add(Pair(char.toString(), "endStatement"))

                concatChars = ""
                index++
            }



            else -> {
                if(char.isWhitespace()){
                index++
                } else {
                    concatChars += char
                    index++;
                }
            };

        }


    }
    return token
}

