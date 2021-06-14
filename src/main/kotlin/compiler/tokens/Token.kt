package compiler.tokens

data class Token(val type : TOKEN_TYPES, val lexeme: String, val line: Int) {
    var literalValue : Any? = null
    constructor(type : TOKEN_TYPES, lexeme: String,  literalValue: Any?, line: Int): this(type,lexeme, line ) {
     this.literalValue = literalValue
    }

}



