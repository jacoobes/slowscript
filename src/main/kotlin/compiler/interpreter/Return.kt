package compiler.interpreter

import java.lang.RuntimeException

class Return(val value: Any?): RuntimeException(null, null ,false, false)