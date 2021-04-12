

interface Tree {

    data class Parent(val newProgram: String = "Program")

    data class Expression(val newExpression : String = "Expression")

    data class Function(val typeVerb : String, val doTo: Value,)

    data class Value(val type: String, val data : Any, val hasAlias: Boolean)

    data class Declaration(val type: Function)
}

