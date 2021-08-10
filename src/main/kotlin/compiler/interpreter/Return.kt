package compiler.interpreter

data class Return(val value: Any?) : RuntimeException(null, null, false, false)