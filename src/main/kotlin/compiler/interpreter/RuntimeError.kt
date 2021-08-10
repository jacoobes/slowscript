package compiler.interpreter

import compiler.tokens.Token

class RuntimeError(override val message: String?, val token: Token) : RuntimeException(message)