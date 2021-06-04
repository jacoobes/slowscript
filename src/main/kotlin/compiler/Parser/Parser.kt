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

    private open class ParseError : RuntimeException() {
        companion object {
            fun error(line: Int, message: String): ParseError {
                piekLite.error(line, message)
                return ParseError()
            }
        }

    }

    private fun declaration(): Statement {
        try {
            if (matchAndAdvance(TOKEN_TYPES.IMMUTABLE_VARIABLE, TOKEN_TYPES.MUTABLE_VARIABLE)) {
                return variableDecl()
            }
            if(matchAndAdvance(TOKEN_TYPES.TASK)) return task()
            return statements()
        } catch (runtimeErr: RuntimeError) {
            synchronize()
        }
        throw ParseError.error(peek().line, "No top level declaration found")
    }
    private fun task() : Statement {
        val fnName = consume(TOKEN_TYPES.IDENTIFIER, "No function name provided!")
        consume(TOKEN_TYPES.LEFT_PAREN, "( expected after ${fnName.lexeme}")
        val params = mutableListOf<Token>()
        if(!check(TOKEN_TYPES.RIGHT_PAREN)) {
            do {
                params.add(consume(TOKEN_TYPES.IDENTIFIER, "Parameters can only be alphanumeric"),)
            } while (matchAndAdvance(TOKEN_TYPES.COMMA))
        }
       consume(TOKEN_TYPES.RIGHT_PAREN, "Closing \")\" expected after parameters")
       consume(TOKEN_TYPES.LEFT_BRACE, "{ expected after )")

        return Statement.Function(fnName, params, block())

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
    
    private fun statements(): Statement {
        if(matchAndAdvance(TOKEN_TYPES.DISPLAY)) return printStatement()
        if(matchAndAdvance(TOKEN_TYPES.LEFT_BRACE)) return Statement.Block(block())
        if(matchAndAdvance(TOKEN_TYPES.IF)) return ifStatement()
        if(matchAndAdvance(TOKEN_TYPES.WHILE)) return whileLoop()
        if(matchAndAdvance(TOKEN_TYPES.LOOP)) return forLoop()
        if(matchAndAdvance(TOKEN_TYPES.RETURN)) return returnStatement()
        return expressionStatement()
    }

    private fun printStatement(): Statement {


        if (matchAndAdvance(TOKEN_TYPES.LEFT_PAREN)) {
            val value = expression()
            consume(TOKEN_TYPES.RIGHT_PAREN, "Expected ) after value to print")
            return Statement.Print(value)
        }

        throw ParseError.error(peek().line, "Expected ( before value to print")

    }

    private fun block() : MutableList<Statement> {
        return mutableListOf<Statement>().apply {
            //loop until it finds right brace or until hits end token
            while(!check(TOKEN_TYPES.RIGHT_BRACE) && !isAtEnd() ) {
                add(declaration())

            }

            consume(TOKEN_TYPES.RIGHT_BRACE, "unclosed scope, missing closing }")
        }
    }


    private fun ifStatement() : Statement {
        consume(TOKEN_TYPES.LEFT_PAREN, "No ( was found for expected if statement")
        val condition = expression()
        consume(TOKEN_TYPES.RIGHT_PAREN, "No ) was found for expected if statement")
        val thenBranch = statements()
        var elseStatement: Statement? = null
        if(matchAndAdvance(TOKEN_TYPES.ELSE)) {
            elseStatement = statements()
        }
        return Statement.If(condition, thenBranch, elseStatement)
    }
    private fun whileLoop() : Statement {
        consume(TOKEN_TYPES.LEFT_PAREN, "No ( was found for expected while loop")
        val condition = expression()
        consume(TOKEN_TYPES.RIGHT_PAREN, "No ) was found for expected while loop")
        val body = statements()
        return Statement.While(condition, body)
    }

    private fun forLoop(): Statement {
        consume(TOKEN_TYPES.LEFT_PAREN, " No ( found for expected loop")
        val init: Statement? = when {
            matchAndAdvance(TOKEN_TYPES.MUTABLE_VARIABLE) -> variableDecl().also { advance() }
            matchAndAdvance(TOKEN_TYPES.AS) -> null
            else -> expressionStatement().also { advance() }
        }
        val condition: Expression = if (!check(TOKEN_TYPES.COLON)) expression() else Expression.Literal(true)
        consume(TOKEN_TYPES.COLON, "No colon detected after condition")
        val increment: Expression? = if (!check(TOKEN_TYPES.RIGHT_PAREN)) expression() else null
        consume(TOKEN_TYPES.RIGHT_PAREN, "Enclosing ) expected")


        return statements().let {
            var block = it
            if(increment != null) {
             block = Statement.Block(
                    listOf(
                        it,
                        Statement.Expression(increment)
                    )
                )
            }
           var body : Statement = Statement.While(condition, block)

            if(init != null) {
                body = Statement.Block(listOf(init, body))
            }
            body
        }
    }

    private fun returnStatement(): Statement {
        val returnName = previous()
        val value: Expression? = if (!check(TOKEN_TYPES.SQUIGGLY)) expression() else null

        consume(TOKEN_TYPES.SEMICOLON, "No ; detected following returned expression")
        return Statement.Return(returnName, value)
    }

    private fun expressionStatement(): Statement {
        val value = expression()
        return Statement.Expression(value)
    }


    private fun expression(): Expression {

        return assignment()
    }


    private fun assignment() : Expression {
        val expr = ternary()
        if(matchAndAdvance(TOKEN_TYPES.ASSIGNMENT)) {
            val equals = previous()
            val value = assignment()
            if(expr is Expression.Variable) {
                val name = expr.name
                return Expression.Assignment(name, value)
            }
            piekLite.error(equals, "Invalid assignment target")
        }
        return expr
    }

    private fun ternary(): Expression {
        var ternary = or()
        while (matchAndAdvance(TOKEN_TYPES.QUESTION)) {
            val question = previous().apply {
                if (lexeme != "?") ParseError.error(line, "Expected ? character for expected ternary, not $lexeme")
            }

            val firstOption = or()

            if (matchAndAdvance(TOKEN_TYPES.COLON)) {
                val colon = previous().apply {
                    if (lexeme != ":") ParseError.error(line, "Expected : character for expected ternary, not $lexeme")
                }
                val secondOption = or()
                ternary = Expression.Ternary(ternary, question, firstOption, colon, secondOption)
            }
        }
        return ternary
    }
    private fun or() : Expression {
        var expr = and()
        while(matchAndAdvance(TOKEN_TYPES.OR)) {
        val token = previous()
        val rightSide = and()
            expr =  Expression.Logical(expr, token, rightSide)
        }
        return expr
    }
    private fun and() : Expression {
        var expr = equality()
        while(matchAndAdvance(TOKEN_TYPES.AND)) {
            val token = previous()
            val rightSide = equality()
            expr = Expression.Logical(expr, token, rightSide)
        }
        return expr
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
        return callee()
    }

    private fun callee() : Expression {
       var expr = primary()
       while (true) {
           if(matchAndAdvance(TOKEN_TYPES.LEFT_PAREN)) {
               expr = finishCall(expr)
           } else {
               break
           }
       }
        return expr
    }
    private fun finishCall(expression: Expression) : Expression {
        val listOfArgs = mutableListOf<Expression>()
        if(!check(TOKEN_TYPES.RIGHT_PAREN)) {
            do {
                listOfArgs.add(expression())
            } while (matchAndAdvance(TOKEN_TYPES.COMMA))
        }
        val rightParen = consume(TOKEN_TYPES.RIGHT_PAREN, "Closing right parenthesis expected")
        return Expression.Call(expression, rightParen, listOfArgs)
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


    private fun synchronize() {

    }

    //parser
    fun parse(): List<Statement> {
        val declaration = mutableListOf<Statement>()

            while (!isAtEnd()) {
               declaration.add(declaration())
            }
        return declaration

    }
}