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

        return ternary()
    }

    private fun ternary() : Expression {
        var ternary = equality()
        while(matchAndAdvance(TOKEN_TYPES.QUESTION)) {
            val question = previous().apply {
                if(lexeme != "?") ParseError.error(line, "Expected ? character for expected ternary, not $lexeme")
            }

            val firstOption = ternary()

            if(matchAndAdvance(TOKEN_TYPES.COLON)) {
                val colon = previous().apply {
                    if (lexeme != ":") ParseError.error(line, "Expected : character for expected ternary, not $lexeme")
                }
            val secondOption = ternary()
           ternary = Expression.Ternary(ternary, question, firstOption, colon, secondOption)
            }
        }
        return ternary
    }

    private fun equality(): Expression {
        var equality = comparison()
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
    //primary Expressions are Literals
    private fun primary() : Expression {
        if(matchAndAdvance(TOKEN_TYPES.TRUE)) return Expression.Literal(true)
        if(matchAndAdvance(TOKEN_TYPES.NULL)) return Expression.Literal(null)
        if(matchAndAdvance(TOKEN_TYPES.FALSE)) return Expression.Literal(false)
        if(matchAndAdvance(TOKEN_TYPES.NaN)) return Expression.Literal(Double.NaN)
        if(matchAndAdvance(TOKEN_TYPES.NUMBER, TOKEN_TYPES.STRING)) {
            return Expression.Literal(previous().literalValue)
        }
        if(matchAndAdvance(TOKEN_TYPES.LEFT_PAREN)) {
            val expression : Expression = expression()
            consume(TOKEN_TYPES.RIGHT_PAREN, "expected ) after expression")
            return Expression.Grouping(expression)
        }
        throw error("${peek().lexeme}, unexpected expression, line : ${peek().line}")
    }
    // if check() is true, advances and returns true, else returns false
    private fun matchAndAdvance(vararg types: TOKEN_TYPES): Boolean {

        if (types.any { check(it) }) {
            advance()
            return true
        }
        return false
    }
    //check TOKEN_TYPES is equal to given TOKEN_TYPEs
    private fun check(type: TOKEN_TYPES): Boolean {
        if (isAtEnd()) return false
        if (type == peek().type) {

            return true
        }
        return false
    }

    //returns the current token of token list
    private fun peek(): Token {
        return tokens.elementAt(current)
    }
    //checks if is at end
    private fun isAtEnd(): Boolean {
        return tokens.lastIndex  == current
    }
    //returns token of index - 1
    private fun previous(): Token {
        return tokens[current - 1]
    }
    //advances and increments token
    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }
    //advances and throws error if current token is not desired
    private fun consume(token: TOKEN_TYPES, message: String) : Token {
        if(check(token)) return advance()
        throw ParseError.error(peek().line, message)
    }
    //parser
   fun parse() : Expression? {
       return try {
           expression()
       } catch (error: ParseError) {
           null
       }

    }


}