import java.io.File
import java.io.BufferedReader

fun main() {

    val bufRead : BufferedReader = File("src/main/kotlin/db.lorem").bufferedReader()
    //val startTime = System.currentTimeMillis()

    val parser = Parser(tokenCreator(bufRead))
   // println(parser.createAST())



    //println(System.currentTimeMillis() - startTime)
}


fun tokenCreator (file: BufferedReader) : List<Pair<String, String>>  {



    var lexicon : String = file.readText().replace("\t+".toRegex(), "")

    val numRegex  = Regex("^[1-9]\\d*(\\.\\d+)?\$")
    val dataRegex = Regex("[:(){}]")
    val token = mutableListOf<Pair<String, String>>()
    var index = 0
    var concatChars = ""

    fun tokenSurroundedChars(startingChar : String, endingChar: String = startingChar, specifiedTokenType: String) {
        val endIndex = lexicon.indexOf(endingChar, index + 1)
        if (endIndex == -1) {
            throw Error("unclosed character.")
        }
        val verb = lexicon.substring(index, endIndex + 1)

        index = endIndex
        token.add(Pair(verb, specifiedTokenType))
    }

    while( index < lexicon.length) {

        val char = lexicon[index]

        concatChars += char

        when {

            char.toString().matches(dataRegex) -> token.add( Pair(char.toString(), "" ) )

            char == '<' ->  tokenSurroundedChars("<", endingChar = ">", specifiedTokenType = "grouper")

            char == '"' ->  tokenSurroundedChars(startingChar = "\"",specifiedTokenType = "string")

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

                index = numIndex

                token.add(Pair(numberOf, "number"))

            }
            char == '\'' ->  tokenSurroundedChars(startingChar = "'", specifiedTokenType = "char")

            char == ';' -> token.add(Pair(char.toString(), "break"))

            concatChars.contains("\\s*str (?<=\\s|^)[_a-zA-Z]*(?=[\\s=,:;]+)".toRegex()) -> {

                val indexType = concatChars.indexOf("str ")

                val declaration = concatChars.substring(indexType)
                token.add(Pair(declaration, "declaration"))

                concatChars = ""
            }
            char == '=' -> token.add(Pair(char.toString(), "initialization"))



            else -> {

             //   throw Error("Unknown token $char")
            }

        }

        index++
    }


    return token

}






