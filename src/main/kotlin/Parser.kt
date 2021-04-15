class Parser(private var tokenList: List<Pair<String, String>>) {

    private var expressionList = mutableListOf<Any>()

    init {
        println(tokenList)
    }

//    fun createAST() {
//        var index = 0
//        expressionList.add(0, Tree.Parent(newProgram = "Program"))
//        expressionList.drop(0)
//        while (index < tokenList.size) {
//
//            if (tokenList[index].second == "") {
//                index++
//            }
//
//            if (tokenList[index].second == "declaration") {
//                statement(true)
//                expressionList.add(
//                    Tree.Declaration(
//                        type = Tree.Function(
//                            typeVerb = tokenList[index].first.toString(),
//                            doTo = Tree.Value(
//                                type = tokenList[index + 1].second,
//                                data = tokenList[index + 1].first, hasAlias = true
//                            )
//                        )
//                    )
//                )
//
//                if (tokenList.elementAt(index + 1).second != "databaseName") {
//                    throw IllegalArgumentException("Expected : databaseName after new declaration")
//                }
//                expressionList.add(
//                    Tree.Value(
//                        type = tokenList[index + 3].second,
//                        data = tokenList[index + 3].first,
//                        hasAlias = false
//                    )
//                )
//
//            }
//
//
//            if(tokenList[index].second == "verb") {
//                statement(true)
//
//                expressionList.add(
//                    Tree.Function(
//                        typeVerb = tokenList[index].first.toString(),
//                        doTo = Tree.Value(
//                            type = "a",
//                            data = "a",
//                            hasAlias = true
//                        )
//                    )
//                )
//
//
//
//
//            }
//
//
//
//            if (tokenList[index].second == "break") {
//                statement()
//                tokenList = tokenList.drop(index + 1)
//                index = -1
//            }
//            index++
//        }
//
//        println(expressionList)
//        println(tokenList)
//    }
//    private fun statement(began: Boolean = false) {
//        if(began) {
//            expressionList.add("NEW STATEMENT")
//        } else {
//            expressionList.add("END STATEMENT")
//        }
//    }
//
//

}


