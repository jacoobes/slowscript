package compiler.Interpreter

import java.lang.RuntimeException

class Return(val value: Any?): RuntimeException(null, null ,false, false)