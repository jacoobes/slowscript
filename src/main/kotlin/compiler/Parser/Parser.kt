package compiler.Parser


import compiler.Expression
import compiler.Interpreter.RuntimeError


import compiler.Statement.Statement
import compiler.piekLite

import tokens.TOKEN_TYPES
import tokens.Token
import java.lang.RuntimeException


/**
 * Recursive Descent Parser
 **/

class Parser(private val tokens: List<Token>) {
    private var current = 0

    private open class ParseError() : RuntimeException() {
        companion object {
            fun error(line: Int, message: String): ParseError {
                piekLite.error(line, message)
                return ParseError()
            }
        }

    }

    private fun expression(): Expression {

        return ternary()
    }

    private fun ternary(): Expression {
        var ternary = equality()
        while (matchAndAdvance(TOKEN_TYPES.QUESTION)) {
            val question = previous().apply {
                if (lexeme != "?") ParseError.error(line, "Expected ? character for expected ternary, not $lexeme")
            }

            val firstOption = ternary()

            if (matchAndAdvance(TOKEN_TYPES.COLON)) {
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
        while (matchAndAdvance(
                TOKEN_TYPES.LESS_THAN_OR_EQUAL,
                TOKEN_TYPES.GREAT_THAN_OR_EQUAL,
                TOKEN_TYPES.LEFT_TRIANGLE,
                TOKEN_TYPES.RIGHT_TRIANGLE
            )
        ) {
            val token = previous()
            val rightSide = term()
            left = Expression.Binary(left, token, rightSide)
        }
        return left
    }

    private fun term(): Expression {
        var left = factor()
        while (matchAndAdvance(TOKEN_TYPES.PLUS, TOKEN_TYPES.MINUS)) {
            val token = previous()
            val right = factor()
            left = Expression.Binary(left, token, right)
        }
        return left

    }

    private fun factor(): Expression {
        var left = unary()
        while (matchAndAdvance(TOKEN_TYPES.MULT, TOKEN_TYPES.DIVIDE, TOKEN_TYPES.MODULUS)) {
            val token = previous()
            val right = unary()
            left = Expression.Binary(left, token, right)
        }
        return left

    }

    private fun unary(): Expression {

        if (matchAndAdvance(TOKEN_TYPES.MINUS, TOKEN_TYPES.NOT)) {
            return Expression.Unary(previous(), unary())
        }
        return primary()
    }

    //primary Expressions are Literals
    private fun primary(): Expression {

        //returns Expression given a token type
        return when {
            matchAndAdvance(TOKEN_TYPES.TRUE) -> Expression.Literal(true)
            matchAndAdvance(TOKEN_TYPES.NULL) -> Expression.Literal(null)
            matchAndAdvance(TOKEN_TYPES.FALSE) -> Expression.Literal(false)
            matchAndAdvance(TOKEN_TYPES.NaN) -> Expression.Literal(Double.NaN)
            matchAndAdvance(TOKEN_TYPES.NUMBER, TOKEN_TYPES.STRING) -> Expression.Literal(previous().literalValue)
            matchAndAdvance(TOKEN_TYPES.LEFT_PAREN) -> {
                val expression: Expression = expression()
                consume(TOKEN_TYPES.RIGHT_PAREN, "expected ) after expression")
                Expression.Grouping(expression)
            }
            matchAndAdvance(TOKEN_TYPES.IDENTIFIER) -> {
                Expression.Variable(previous())
            }
            else -> throw error("${peek().lexeme}, unexpected expression, [line : ${peek().line}]")
        }

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
        return tokens.lastIndex == current
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
    private fun consume(token: TOKEN_TYPES, message: String): Token {
        if (check(token)) return advance()
        throw ParseError.error(peek().line, message)
    }

    private fun statements(): Statement {
        if (matchAndAdvance(TOKEN_TYPES.DISPLAY)) return printStatement()
        return expressionStatement()
    }

    private fun expressionStatement(): Statement {
        val value = expression()
        consume(TOKEN_TYPES.SEMICOLON, "Statements end with new line only")
        return Statement.Expression(value)
    }

    private fun printStatement(): Statement {

        if (matchAndAdvance(TOKEN_TYPES.LEFT_PAREN)) {
            val value = expression()
            consume(TOKEN_TYPES.RIGHT_PAREN, "expected ) after value to print")
            return Statement.Print(value)
        }

        throw ParseError.error(peek().line, "expected ( before value to print")

    }

    private fun declaration(): Statement? {
        return try {
            if (matchAndAdvance(TOKEN_TYPES.IMMUTABLE_VARIABLE, TOKEN_TYPES.MUTABLE_VARIABLE)) {
               return variableDecl()
            }
             statements()
        } catch (runtimeErr: RuntimeError) {
           synchronize()
           null
        }

    }

    private fun variableDecl(): Statement {
        consume(TOKEN_TYPES.IDENTIFIER, "No identifier for variable has been found")
        val tokenName = previous()
        val value: Expression?


            if (matchAndAdvance(TOKEN_TYPES.ASSIGNMENT)) {
                value = expression()
                return Statement.Declaration(tokenName, value)
            }

                return  Statement.Declaration(tokenName, null)
    }

    private fun synchronize() {
        println("hi")
    }

    //parser
    fun parse(): List<Statement> {
        val declaration = mutableListOf<Statement>()

            while (!isAtEnd()) {
                declaration()?.let { declaration.add(it) }
            }
        return  declaration

    }
}