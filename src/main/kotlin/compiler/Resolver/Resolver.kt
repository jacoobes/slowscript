package compiler.Resolver

import compiler.Expression
import compiler.Statement.Statement
import compiler.piekLite
import tokens.Token
import java.util.*

class Resolver(private val interpreter: compiler.Interpreter.InterVisitor) : Statement.StateVisitor<Unit>,
    Expression.Visitor<Unit> {
    private val scopes = Stack<HashMap<String, Boolean>>()
    private var currentFn = FunctionType.NONE

    private fun resolve(statement: Statement) {
        statement.accept(this)
    }

    private fun resolve(expression: Expression) {
        expression.accept(this)
    }

    fun resolve(statements: List<Statement>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    override fun <R> visit(arg: Statement.Return) {
        if(currentFn == FunctionType.NONE) {
            piekLite.error(arg.name, "Cannot return at top level")
        }
        arg.value?.let {
            resolve(it)
        }
    }

    override fun <R> visit(declaration: Statement.Declaration): R? {
        declare(declaration.name)
        if (declaration.expr != null) {
            resolve(declaration.expr)
        }
        define(declaration.name)
        return null
    }

    override fun <R> visit(block: Statement.Block) {
        beginScope()
        resolve(block.statementList)
        endScope()
    }

    override fun <R> visit(function: Statement.Function) {
        declare(function.fnName)
        define(function.fnName)

        resolveFunction(function, FunctionType.Function)
    }
    private fun resolveFunction(function: Statement.Function, type: FunctionType) {
        val enclosing = currentFn
        currentFn = type
        beginScope()
        for(params in function.parameters) {
            declare(params)
            define(params)
        }
        resolve(function.body)
        endScope()
        currentFn = enclosing
    }

    override fun <R> visit(loop: Statement.While) {
        resolve(loop.expr)
        resolve(loop.body)
    }

    override fun <R> visit(tree: Statement.If) {
        resolve(tree.ifBranch)
        tree.acted?.let { resolve(it) }
        tree.elseBranch?.let { resolve(it) }
    }

    override fun <R> visit(visitor: Statement.Expression) {
        resolve(visitor.expr)
    }

    override fun <R> visit(visitor: Statement.Print) {
        resolve(visitor.expr)
    }

    override fun <R> visit(assignment: Expression.Assignment) {
        resolve(assignment.right)
        resolveLocal(assignment, assignment.name)
    }

    override fun <R> visit(call: Expression.Call) {
        resolve(call.callee)
        for (args in call.args) {
            resolve(args)
        }
    }

    override fun <R> visit(expression: Expression.Binary) {
        resolve(expression.right)
        resolve(expression.left)
    }

    override fun <R> visit(expression: Expression.Grouping) {
        resolve(expression.expression)
    }

    override fun <R> visit(expression: Expression.Literal): Any? {
        return null
    }

    override fun <R> visit(expression: Expression.Ternary) {
        resolve(expression.left)
        resolve(expression.middle)
        resolve(expression.right)
    }

    override fun <R> visit(expression: Expression.Unary) {
        resolve(expression.expression)
    }

    override fun <R> visit(logical: Expression.Logical) {
        resolve(logical.right)
        resolve(logical.left)
    }

    override fun <R> visit(variable: Expression.Variable) {
        if (scopes.isNotEmpty() && scopes.peek()[variable.name.lexeme] == false) {
            piekLite.error(variable.name, "Variable has not been properly defined")
        }
        resolveLocal(variable, variable.name)
    }

    private fun resolveLocal(variable: Expression, name: Token) {
        var i = scopes.size - 1
        while (i >= 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(variable, scopes.size - 1 - i)
                return
            }
            i--
        }
    }

    private fun beginScope() {
        scopes.push(HashMap())
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        if (scopes.peek().containsKey(name.lexeme)) {
            piekLite.error(name, "A variable has already been declared ")
        }
        scopes.peek()[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

    private fun endScope() {
        scopes.pop()
    }


}

private enum class FunctionType {
    NONE,
    Function
}