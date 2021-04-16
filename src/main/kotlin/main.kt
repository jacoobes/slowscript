import java.io.File
import java.io.BufferedReader


fun main() {

    val bufRead : BufferedReader = File("src/main/kotlin/db.lorem").bufferedReader()
    //val startTime = System.currentTimeMillis()

    //val parser = Parser(tokenCreator(bufRead))
   // println(parser.createAST())

print(tokenCreator(bufRead))

    //println(System.currentTimeMillis() - startTime)
}

fun tokenCreator (file: BufferedReader)  : List<Pair<String, String>> {
    var src: String = file.readText()
    val token = mutableListOf<Pair<String, String>>()
    var concatChars = ""
    val dataRegex = Regex("[:(){}]")
    var index = 0;
    val numRegex  = Regex("^[1-9]\\d*(\\.\\d+)?\$")
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

            concatChars == "int" -> {
                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " "), index + 1)?.first ?: throw Error("Not initialized")
                token.add(Pair(src.substring(index + 1, secondIndex), "int_Init"))
                concatChars = ""
                index = secondIndex;
            }


            concatChars == "str" -> {
                val secondIndex = src.findAnyOf(listOf("=", ";", ",", " "), index + 1)?.first ?: throw Error("Not initialized")
                token.add(Pair(src.substring(index, secondIndex), "str_Init"))
                concatChars = ""
                index = secondIndex;
            }
            char.toString().matches(numRegex) -> {

                var numIndex = index
                while(true) {

                    if(src[numIndex+1].toString().matches(numRegex)) {
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
                token.add(Pair(char.toString(), "initialization"))
                index++
            }

            concatChars == "task" -> {

                val secondIndex = src.findAnyOf(listOf("("), index)?.first ?: throw Error("Incorrect task syntax found")
                token.add(Pair(src.substring(index, secondIndex - 1), "task"))
                concatChars = ""
                index = secondIndex;

            }

            char == ';' -> {
                token.add(Pair(char.toString(), "endStatement"))

//                if(concatChars.isNotEmpty()) {
//                    throw IllegalArgumentException("Unexpected characters $concatChars")
//                }
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

//fun tokenCreator (file: BufferedReader) : List<Pair<String, String>>  {
//
//
//
//    var lexicon : String = file.readText().replace("\t+".toRegex(), "")
//
//
//
//    val token = mutableListOf<Pair<String, String>>()
//    var index = 0
//    var concatChars = ""
//
//    fun tokenSurroundedChars(startingChar : String, endingChar: String = startingChar, specifiedTokenType: String) {
//        val endIndex = lexicon.indexOf(endingChar, index + 1)
//        if (endIndex == -1) {
//            throw Error("unclosed character.")
//        }
//        val verb = lexicon.substring(index, endIndex + 1)
//
//        index = endIndex
//        token.add(Pair(verb, specifiedTokenType))
//    }
//
//    while( index < lexicon.length) {
//
//        val char = lexicon[index]
//
//        concatChars += char
//
//        when {
//
//            char.toString().matches(dataRegex) -> token.add( Pair(char.toString(), "" ) )
//
//            char == '<' ->  tokenSurroundedChars("<", endingChar = ">", specifiedTokenType = "grouper")
//
//            char == '"' ->  tokenSurroundedChars(startingChar = "\"",specifiedTokenType = "string")
//
//            char.toString().matches(numRegex) -> {
//
//                var numIndex = index
//                while(true) {
//
//                    if(lexicon[numIndex+1].toString().matches(numRegex)) {
//                        numIndex++
//                    }
//                    else {
//                        break
//                    }
//                }
//                val numberOf = lexicon.substring(index, numIndex + 1)
//
//                index = numIndex
//
//                token.add(Pair(numberOf, "number"))
//
//            }
//            char == '\'' ->  tokenSurroundedChars(startingChar = "'", specifiedTokenType = "char")
//
//            char == ';' -> {
//
//
//                //"\\s?str[ ]+[_a-zA-Z]+(?=[\\s=;])+").containsMatchIn(concatChars)) {
//
//                token.add(Pair(char.toString(), "break"))
//
//            }
//
////           Regex("\\s?str[ ]+[_a-zA-Z]+(?=[\\s=;])*").containsMatchIn(concatChars) -> {
////
////
////                val declaration = concatChars.substring(concatChars.indexOf("str"))
////
////                token.add(Pair(declaration, "declaration"))
////
////                concatChars = ""
////            }
//            char == '=' -> token.add(Pair(char.toString(), "initialization"))
//
//
//
//            else -> {
//
//             //   throw Error("Unknown token $char")
//            }
//
//        }
//
//        index++
//    }
//
//
//    return token
//
//}
//





