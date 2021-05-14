package compiler.Parser


import compiler.Expression
import compiler.piekLite
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException
import tokens.TOKEN_TYPES
import tokens.Token
import java.lang.RuntimeException
import kotlin.math.exp


class Parser(private val tokens: List<Token>) {
    private var current = 0

    private open class ParseError() : RuntimeException() {
        companion object {
        fun error(line: Int, message:String) : ParseError {
            piekLite.error(line, message)
            return ParseError()
            }
        }

    }


    private fun expression(): Expression {

        return equality()
    }

    private fun equality(): Expression {
        var equality: Expression = comparison()
        while (matchAndAdvance(TOKEN_TYPES.NOT_EQUAL, TOKEN_TYPES.EQUAL_EQUAL)) {
            val token = previous()
            val secondComparison = comparison()
            equality = Expression.Binary(equality, token, secondComparison)
        }
        return equality

    }

    private fun comparison(): Expression {
        var left = term()
        while(matchAndAdvance(TOKEN_TYPES.LESS_THAN_OR_EQUAL, TOKEN_TYPES.GREAT_THAN_OR_EQUAL, TOKEN_TYPES.LEFT_TRIANGLE, TOKEN_TYPES.RIGHT_TRIANGLE)) {
            val token = previous()
            val rightSide = term()
            left = Expression.Binary(left, token, rightSide)
        }
        return left
    }

    private fun term(): Expression {
        var left = factor()
        while(matchAndAdvance(TOKEN_TYPES.PLUS, TOKEN_TYPES.MINUS)) {
            val token = previous()
            val right = factor()
            left = Expression.Binary(left, token, right)
        }
        return left

    }

    private fun factor() : Expression {
        var left = unary()
        while(matchAndAdvance(TOKEN_TYPES.MULT, TOKEN_TYPES.DIVIDE, TOKEN_TYPES.MODULUS)) {
            val token = previous()
            val right = unary()
            left = Expression.Binary(left, token, right)
        }
        return left

    }

    private fun unary() : Expression{

        if(matchAndAdvance(TOKEN_TYPES.MINUS, TOKEN_TYPES.NOT)) {
            return Expression.Unary(previous(), unary())
        }
        return primary()
    }

    private fun primary() : Expression {
        if(matchAndAdvance(TOKEN_TYPES.TRUE)) return Expression.Literal(true)
        if(matchAndAdvance(TOKEN_TYPES.NULL)) return Expression.Literal(null)
        if(matchAndAdvance(TOKEN_TYPES.FALSE)) return Expression.Literal(false)

        if(matchAndAdvance(TOKEN_TYPES.NUMBER, TOKEN_TYPES.STRING)) {
            return Expression.Literal(previous().literalValue)
        }
        if(matchAndAdvance(TOKEN_TYPES.LEFT_PAREN)) {
            val expression : Expression = expression()
            consume(TOKEN_TYPES.RIGHT_PAREN, "expected ) after expression")
            return Expression.Grouping(expression)
        }
        throw error("${peek().lexeme}, unexpected expression")
    }

    private fun matchAndAdvance(vararg types: TOKEN_TYPES): Boolean {

        if (types.any { check(it) }) {
            advance()
            return true
        }
        return false
    }

    private fun check(type: TOKEN_TYPES): Boolean {
        if (isAtEnd()) return false
        if (type == peek().type) {

            return true
        }
        return false
    }

    private fun peek(): Token {
        return tokens.elementAt(current)
    }

    private fun isAtEnd(): Boolean {
        return tokens.lastIndex  == current
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }
    private fun consume(token: TOKEN_TYPES, message: String) : Token {
        if(check(token)) return advance()
        throw ParseError.error(peek().line, message)
    }

   fun parse() : Expression? {
       return try {
           expression()
       } catch (error: ParseError) {
           null
       }

    }


}