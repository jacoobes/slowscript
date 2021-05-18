package compiler.Interpreter

import compiler.Expression
import compiler.piekLite
import tokens.TOKEN_TYPES


class InterVisitor : Expression.Visitor<Any> {
    override fun <R> visit(expression: Expression.Unary): Any? {
        val unary : Any? = evaluate(expression.expression)

      return when(expression.prefix.type) {
            TOKEN_TYPES.MINUS -> {
                if(unary is Double) {
                    return -unary
                }
                null
            }
            TOKEN_TYPES.NOT -> {
               if( isTruthy(unary, expression) == true) {
                   return true
               }
                   false
            }
            else ->  null
        }

    }

    override fun <R> visit(expression: Expression.Ternary): Any? {
        val left : Any? = evaluate(expression.left)
        return evalTernary(left, expression)
    }

    override fun <R> visit(expression: Expression.Binary): Any? {
        val left : Any? = evaluate(expression.left)
        val right : Any? = evaluate(expression.right)

        return when(expression.operator.type) {

            TOKEN_TYPES.PLUS -> {
                add(left, right)
            }
            TOKEN_TYPES.MINUS -> {
                subtract(left, right)
            }
            TOKEN_TYPES.MODULUS -> {
                mod(left, right)
            }
            TOKEN_TYPES.DIVIDE -> {
                divide(left, right)
            }
            TOKEN_TYPES.RIGHT_TRIANGLE -> {
                greaterThan(left, right, expression)
            }
            TOKEN_TYPES.LEFT_TRIANGLE ->{
                lessThan(left, right, expression)
            }
            TOKEN_TYPES.GREAT_THAN_OR_EQUAL -> {
                greaterOrEqual(left, right, expression)
            }
            TOKEN_TYPES.LESS_THAN_OR_EQUAL -> {
                lesserOrEqual(left, right, expression)
            }
            TOKEN_TYPES.EQUAL_EQUAL -> {
                isEqual(left, right, expression)
            }
            TOKEN_TYPES.NOT_EQUAL -> {
                !isEqual(left, right, expression)
            }


            else -> null
        }
    }

    override fun <R> visit(expression: Expression.Literal): Any? {
        return expression.value
    }

    override fun <R> visit(expression: Expression.Grouping): Any? {
        return evaluate(expression.expression)
    }

    private fun evaluate(expression: Expression) : Any? {
       return expression.accept(this)
    }

    private fun isTruthy(unary: Any?, expression: Expression.Unary): Any? {

        return unary?.let {
             if (it is Boolean) return !it
            throw RuntimeError("Failed use of NOT operator on value $unary", expression.prefix)
        } ?:return true
    }
    private fun add(left : Any? , right: Any?) : Any {
        if(left is Double && right is Double) {
            return left + right
        }
       if((left is String && right is String) || (left is String && right is Double)) {
           return "$left$right"
       }
        return Double.NaN
    }
    private fun subtract(left: Any?, right: Any?) : Double {
        if(left is Double && right is Double) {
            return left - right
        }
        return Double.NaN

    }

    private fun mod(left: Any?, right: Any?) : Double{
        if(left is Double && right is Double) {
            return left % right
        }
        return Double.NaN
    }

    private fun divide(left: Any?, right: Any?) : Double {
        if(left is Double && right is Double) {
            return left / right
        }
        return Double.NaN
    }

    private fun greaterThan(left: Any?, right: Any?, expression: Expression.Binary) : Boolean {
        if(left is Double && right is Double) {
            return left > right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }
    private fun lessThan(left: Any?, right: Any?, expression: Expression.Binary) : Boolean {
        if(left is Double && right is Double) {
            return left < right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }
    private fun greaterOrEqual(left: Any?, right: Any?, expression: Expression.Binary) : Boolean {
        if(left is Double && right is Double) {
            return left >= right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }
    private fun lesserOrEqual(left: Any?, right: Any?, expression: Expression.Binary) : Boolean {
        if(left is Double && right is Double) {
            return left <= right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }
    private fun isEqual(left: Any?, right: Any?, expression: Expression.Binary) : Boolean {
        if(left == null && right == null) return true
        if(left == null ) return false

            return left == right
    }

    private fun evalTernary(toBeOperated : Any?, expression: Expression.Ternary) : Any? {
        if(toBeOperated == null || toBeOperated == false) return evaluate(expression.right)

            return evaluate(expression.middle)
    }

    fun stringify(value: Any?) : Any? {
            val text = value.toString()
           if(text.endsWith(".0")) {
               return text.substring(0, text.length-2)

        }

      return value
   }



   fun interpret (expression: Expression): Any {
        return try{
            val value : Any? = evaluate(expression)

            println(stringify(value))
        } catch(error: RuntimeError) {
            piekLite.error(error)
        }
    }



}