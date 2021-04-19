

interface Tree {

    data class Function(val typeVerb : String, val doTo: Value,)

    data class Value(val type: String, val data: Any, val hasAlias: Boolean)

    data class Declaration(val name: String )
}

