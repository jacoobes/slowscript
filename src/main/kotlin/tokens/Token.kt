package tokens

data class Token(val type : TOKEN_TYPES, val lexeme: String, val line: Int) {

    constructor(type : TOKEN_TYPES, lexeme: String, literalValue: Any?, line: Int): this(type,lexeme, line ) {

    }

}



