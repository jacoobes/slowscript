
import com.sun.org.apache.xpath.internal.operations.Bool
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException
import java.util.*


class Parser(private var tokenList: List<Pair<Any, String>>) {

    private var expressionList = mutableListOf<Any>()

    init {
    println(tokenList)
    }

    fun createAST() {
    var index = 0
     expressionList.add(0, Tree.Parent(newProgram = "Program"))
        expressionList.drop(0)
        while(index < tokenList.size) {

            if(tokenList[index].second == "") {
                index++
            }

            if(tokenList[index].second == "declaration") {

                expressionList.add(Tree.Declaration(type = Tree.Function(typeVerb = tokenList[index].first.toString(),
                    doTo = Tree.Value(type = tokenList[index + 1].second,
                        data = tokenList[index + 1].first, hasAlias = true))))

                if(tokenList.elementAt(index + 1).second != "databaseName" ) {
                    throw IllegalArgumentException("Expected : databaseName after new declaration")
                }
                expressionList.add(Tree.Value(type = tokenList[index + 3].second, data = tokenList[index + 3].first, hasAlias = false ))

            }


            if(tokenList[index].second == "break") {
                expressionList.add("STATEMENT END")
                tokenList = tokenList.drop(index + 1)
                index = -1
            }
            index++
        }

    println(expressionList)
    println(tokenList)
    }



    }

