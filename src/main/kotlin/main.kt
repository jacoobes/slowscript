import java.io.File
import java.io.BufferedReader
fun main() {

    val bufRead : BufferedReader = File("src/main/kotlin/db.lorem").bufferedReader()
    //val startTime = System.currentTimeMillis()

    val parser = Parser(tokenCreator(bufRead))
    println(parser.createAST())
    //println(System.currentTimeMillis() - startTime)
}


fun tokenCreator (file: BufferedReader) : List<Pair<Any, String>>  {

    var lexicon : String = file.readText()

    val numRegex  = Regex("^[1-9]\\d*(\\.\\d+)?\$")
    val dataRegex = Regex("[:(){}]")
    val token = mutableListOf<Pair<Any, String>>()
    var index = 0
    lexicon = lexicon.replace("\\s+".toRegex(), "")

    while( index < lexicon.length) {

        val char = lexicon[index]

        when {

            char.toString().matches(dataRegex) -> token.add( Pair(char, "" ) )

            char == '<' ->
            {
                val secondTriangle = lexicon.indexOf('>', index + 1)
                if(secondTriangle == -1) {
                    break
                }
                val verb = lexicon.substring(index, secondTriangle + 1)

                if(!isValidVerb(verb)) throw Error("Unknown verb $verb")

                lexicon = lexicon.drop(secondTriangle + 1)
                index = -1
                token.add(Pair(verb, "verb"))
            }
            char == '"' ->
            {

                val secondQuote = lexicon.indexOf(char, index + 1)

                if(secondQuote == -1) {
                    break
                }

                val stringOf = lexicon.substring(index, secondQuote + 1)

                lexicon = lexicon.drop(secondQuote + 1)
                index = -1
                token.add(Pair(stringOf, "string"))
            }

            char.toString().matches(numRegex) -> {

                var numIndex = index
                while(true) {

                    if(lexicon[numIndex+1].toString().matches(numRegex)) {
                        numIndex++
                    }
                    else {
                        break
                    }
                }
                val numberOf = lexicon.substring(index, numIndex + 1)
                lexicon = lexicon.drop(numIndex  + 1)
                index = -1
                token.add(Pair(numberOf, "number"))

            }
            char == '\'' -> {
                val secondQuote = lexicon.indexOf(char, index + 1)

                if(secondQuote == -1) {
                    break
                }

                val stringOf = lexicon.substring(index, secondQuote + 1)

                lexicon = lexicon.drop(secondQuote + 1)
                index = -1
                token.add(Pair(stringOf, "databaseName"))
            }
            char == ';' -> token.add(Pair(char, "break"))

            char == '!' -> {
                val secondChar = lexicon.indexOf('=', index + 1)

                if(secondChar == -1) {
                    break
                }

                val stringOf = lexicon.substring(index, secondChar + 1)

                lexicon = lexicon.drop(secondChar + 1)
                index = -1
                token.add(Pair(stringOf, "declaration"))
            }

            else -> {
                throw Error("Unknown token $char")
            }

        }

        index++
    }


    return token

}

fun isValidVerb(verb : String): Boolean {
    val mutatedVerb = verb.removePrefix("<").removeSuffix(">").trim()
    return mutatedVerb.matches(Regex("STORE|TO|FIND|GET|FROM|SEND|AS"))
}





