package compiler.parser


import compiler.Expression
import compiler.interpreter.RuntimeError


import compiler.Statement.Statement
import compiler.interpreter.Init
import compiler.piekLite
import compiler.tokens.TOKEN_TYPES

import compiler.tokens.TOKEN_TYPES.*
import compiler.tokens.Token
import java.lang.RuntimeException
import kotlin.text.StringBuilder


/**
 * Recursive Descent Parser
 **/

class Parser(private val tokens: List<Token>) {
    private var current = 0



    init {
  //  println(tokens)
    }

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
            if(matchAndAdvance(CLASS)) return classDec()
            if(matchAndAdvance(TASK)) return task("task")
            if (matchAndAdvance(IMMUTABLE_VARIABLE, MUTABLE_VARIABLE))  return variableDecl()
            return statements()
        } catch (runtimeErr: RuntimeError) {
            synchronize()
        }
        throw ParseError.error(peek().line, "No top level declaration found")
    }
    private fun classDec() : Statement {
        val methods = mutableListOf<Statement.Function>()

        val className = consume(IDENTIFIER, "Expected class name after declaration")
        val identifier = if(matchAndAdvance(FROM)) {
            consume(IDENTIFIER, "No superclass name found")
            Expression.Variable(previous())
        } else null

        consume(LEFT_BRACE, "{ expected after class declaration")

       val init = if(matchAndAdvance(INIT_BLOCK)) {
            val name = previous()
            consume(LEFT_BRACE, "{ expected after init block")
            Init(name, Statement.Block(block()))
        } else null

        while (!isAtEnd() && !check(RIGHT_BRACE)) {
            methods.add(task("method"))
        }
        consume(RIGHT_BRACE, "} expected")
        return Statement.ClassDec(className, methods, identifier, init)

    }

    private fun task(kind: String) : Statement.Function {
        val fnName = consume(IDENTIFIER, "No $kind name provided!")
        consume(LEFT_PAREN, "( expected after ${fnName.lexeme}")
        val params = mutableListOf<Token>()
        if(!check(RIGHT_PAREN)) {
            do {
                params.add(consume(IDENTIFIER, "Parameters can only be alphanumeric"))
            } while (matchAndAdvance(COMMA))
        }
       consume(RIGHT_PAREN, "Closing \")\" expected after parameters")
       consume(LEFT_BRACE, "{ expected after )")

        return Statement.Function(fnName, params, block())

    }

    private fun variableDecl(): Statement {
        consume(IDENTIFIER, "No identifier for variable has been found")
        val tokenName = previous()
        val value: Expression?
        if (matchAndAdvance(ASSIGNMENT)) {
            value = expression()
            return Statement.Declaration(tokenName, value)
        }

        return  Statement.Declaration(tokenName, null)
    }
    
    private fun statements(): Statement {
        return when {
            matchAndAdvance(DISPLAY) -> printStatement()
            matchAndAdvance(LEFT_BRACE) -> Statement.Block(block())
            matchAndAdvance(IF) -> ifStatement()
            matchAndAdvance(WHILE) -> whileLoop()
            matchAndAdvance(LOOP) -> forLoop()
            matchAndAdvance(RETURN) -> returnStatement()
            else -> expressionStatement()
        }
    }

    private fun printStatement(): Statement {


        if (matchAndAdvance(LEFT_PAREN)) {
            val value = expression()
            consume(RIGHT_PAREN, "Expected ) after value to print")
            return Statement.Print(value)
        }

        throw ParseError.error(peek().line, "Expected ( before value to print")

    }

    private fun block() : MutableList<Statement> {
        return mutableListOf<Statement>().apply {
            //loop until it finds right brace or until hits end token
            while(!check(RIGHT_BRACE) && !isAtEnd() ) {
                add(declaration())
            }

            consume(RIGHT_BRACE, "unclosed scope, missing closing }")
        }
    }


    private fun ifStatement() : Statement {
        consume(LEFT_PAREN, "No ( was found for expected if statement")
        val condition = expression()
        consume(RIGHT_PAREN, "No ) was found for expected if statement")
        val thenBranch = statements()
        var elseStatement: Statement? = null
        if(matchAndAdvance(ELSE)) {
            elseStatement = statements()
        }
        return Statement.If(condition, thenBranch, elseStatement)
    }
    private fun whileLoop() : Statement {
        consume(LEFT_PAREN, "No ( was found for expected while loop")
        val condition = expression()
        consume(RIGHT_PAREN, "No ) was found for expected while loop")
        val body = statements()
        return Statement.While(condition, body)
    }

    private fun forLoop(): Statement {
        consume(LEFT_PAREN, " No ( found for expected loop")
        val init: Statement? = when {
            matchAndAdvance(MUTABLE_VARIABLE) -> variableDecl().also { advance() }
            matchAndAdvance(AS) -> null
            else -> expressionStatement().also { advance() }
        }
        val condition: Expression = if (!check(COLON)) expression() else Expression.Literal(true)
        consume(COLON, "No colon detected after condition")
        val increment: Expression? = if (!check(RIGHT_PAREN)) expression() else null
        consume(RIGHT_PAREN, "Enclosing ) expected")


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
        val value: Expression? = if (!check(SEMICOLON)) expression() else null

        consume(SEMICOLON, "No ; detected following returned expression")
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
        if(matchAndAdvance(ASSIGNMENT)) {
            val equals = previous()
            val value = assignment()
            if(expr is Expression.Variable) {
                return Expression.Assignment(expr.name, value)
            } else if(expr is Expression.Get) {
                return Expression.Set(expr.obj, expr.name, value)
            }
            piekLite.error(equals, "Invalid assignment target")
        }
        return expr
    }

    private fun ternary(): Expression {
        var ternary = or()
        while (matchAndAdvance(QUESTION)) {
            val question = previous().apply {
                if (lexeme != "?") ParseError.error(line, "Expected ? character for expected ternary, not $lexeme")
            }

            val firstOption = or()

            if (matchAndAdvance(COLON)) {
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
        while(matchAndAdvance(OR)) {
        val token = previous()
        val rightSide = and()
            expr =  Expression.Logical(expr, token, rightSide)
        }
        return expr
    }
    private fun and() : Expression {
        var expr = equality()
        while(matchAndAdvance(AND)) {
            val token = previous()
            val rightSide = equality()
            expr = Expression.Logical(expr, token, rightSide)
        }
        return expr
    }


    private fun equality(): Expression {
        var equality = comparison()
        while (matchAndAdvance(NOT_EQUAL, EQUAL_EQUAL)) {
            val token = previous()
            val secondComparison = comparison()
            equality = Expression.Binary(equality, token, secondComparison)
        }
        return equality

    }

    private fun comparison(): Expression {
        var left = term()
        while (matchAndAdvance(
                LESS_THAN_OR_EQUAL,
                GREAT_THAN_OR_EQUAL,
                LEFT_TRIANGLE,
                RIGHT_TRIANGLE
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
        while (matchAndAdvance(PLUS, MINUS)) {
            val token = previous()
            val right = factor()
            left = Expression.Binary(left, token, right)
        }
        return left

    }

    private fun factor(): Expression {
        var left = unary()
        while (matchAndAdvance(MULT, DIVIDE, MODULUS)) {
            val token = previous()
            val right = unary()
            left = Expression.Binary(left, token, right)
        }
        return left

    }

    private fun unary(): Expression {

        if (matchAndAdvance(MINUS, NOT)) {
            return Expression.Unary(previous(), unary())
        }
        return callee()
    }

    private fun callee() : Expression {
       var expr = primary()
       while (true) {
           expr = when {
               matchAndAdvance(LEFT_PAREN) -> finishCall(expr)
               matchAndAdvance(DOT) -> {
                   val name: Token = consume(IDENTIFIER, "Expect property name after \".\".")
                   Expression.Get(expr, name)
               }
               else -> break
           }
       }
        return expr
    }
    private fun finishCall(expression: Expression) : Expression {
        val listOfArgs = mutableListOf<Expression>()
        if(!check(RIGHT_PAREN)) {
            do {
                listOfArgs.add(expression())
            } while (matchAndAdvance(COMMA))
        }
        val rightParen = consume(RIGHT_PAREN, "Closing right parenthesis expected")
        return Expression.Call(expression, rightParen, listOfArgs)
    }

    //primary Expressions are Literals
    private fun primary(): Expression {

        //returns Expression given a token type
        return when {
            matchAndAdvance(TRUE) -> Expression.Literal(true)
            matchAndAdvance(NULL) -> Expression.Literal(null)
            matchAndAdvance(FALSE) -> Expression.Literal(false)
            matchAndAdvance(NaN) -> Expression.Literal(Double.NaN)
            matchAndAdvance(INSTANCE) -> Expression.Instance(previous())
            matchAndAdvance(NUMBER, STRING) -> Expression.Literal(previous().literalValue)
            matchAndAdvance(LEFT_PAREN) -> {
                val expression: Expression = expression()
                consume(RIGHT_PAREN, "expected ) after expression")
                Expression.Grouping(expression)
            }
            matchAndAdvance(SUPER) -> {
                val expression = previous()
                consume(DOT, "No \".\" found after super call")
                val method = consume(IDENTIFIER, "No method found after super call!")
                Expression.Supe(expression, method )
            }
            matchAndAdvance(IDENTIFIER) -> {
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
        while (!isAtEnd()) {
            if(previous().type == SEMICOLON) return
            when(peek().type) {
                CLASS -> return
                TASK -> return
                MUTABLE_VARIABLE -> return
                LOOP -> return
                WHILE -> return
                IF -> return
                RETURN -> return
                DISPLAY -> return
            }
            advance()
        }
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

