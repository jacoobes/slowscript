package compiler.resolver

import compiler.Sscript
import compiler.inputTypes.Expression
import compiler.inputTypes.Statement
import compiler.tokens.Token
import java.util.*

/**
 * Semantic analysis
 * - Provides more readable interpreter and binds variable so that they are used once
 * - Resolves local variable's distance in the environment chain and uses that one
 */
class Resolver(private val interpreter: compiler.interpreter.InterVisitor) : Statement.StateVisitor<Unit>,
    Expression.Visitor<Unit> {

    private enum class FunctionType {
        NONE,
        Function,
        METHOD,
        NEW,
        INIT
    }

    private enum class ClassType {
        CLASS,
        NO_CLASS,
        SUBCLASS
    }

    private val scopes = Stack<HashMap<String, Boolean>>()
    private var currentFn = FunctionType.NONE
    private var currentClass = ClassType.NO_CLASS

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

    override fun visit(arg: Statement.Return) {
        if (currentFn == FunctionType.INIT) {
            Sscript.error(arg.name.line, "Cannot ${arg.name.lexeme} in init block")
        }
        if (currentFn == FunctionType.NONE) {
            Sscript.error(arg.name, "Cannot return at top level")
        }
        if (currentFn == FunctionType.NEW) {
            Sscript.error(arg.name, "Cannot return inside an new object function")
        }
        arg.value?.let {
            resolve(it)
        }
    }

    override fun visit(declaration: Statement.Declaration): Any? {
        declare(declaration.name)
        if (declaration.expr != null) {
            resolve(declaration.expr)
        }
        define(declaration.name)
        return null
    }

    override fun visit(block: Statement.Block) {
        beginScope()
        resolve(block.statementList)
        endScope()
    }

    override fun visit(function: Statement.Function) {
        declare(function.fnName)
        define(function.fnName)

        resolveFunction(function, FunctionType.Function)
    }

    private fun resolveFunction(function: Statement.Function, type: FunctionType) {
        val enclosing = currentFn
        currentFn = type
        beginScope()
        for (params in function.parameters) {
            declare(params)
            define(params)
        }
        resolve(function.body)
        endScope()
        currentFn = enclosing
    }

    override fun visit(loop: Statement.While) {
        resolve(loop.expr)
        resolve(loop.body)
    }

    override fun visit(tree: Statement.If) {
        resolve(tree.ifBranch)
        tree.acted?.let { resolve(it) }
        tree.elseBranch?.let { resolve(it) }
    }

    override fun visit(visitor: Statement.Expression) {
        resolve(visitor.expr)
    }

    override fun visit(visitor: Statement.Print) {
        resolve(visitor.expr)
    }

    override fun visit(assignment: Expression.Assignment) {
        resolve(assignment.right)
        resolveLocal(assignment, assignment.name)
    }

    override fun visit(call: Expression.Call) {
        resolve(call.callee)
        for (args in call.args) {
            resolve(args)
        }
    }

    override fun visit(expression: Expression.Binary) {
        resolve(expression.right)
        resolve(expression.left)
    }

    override fun visit(expression: Expression.Grouping) {
        resolve(expression.expression)
    }

    override fun visit(expression: Expression.Literal): Any? {
        return null
    }

    override fun visit(expression: Expression.Ternary) {
        resolve(expression.left)
        resolve(expression.middle)
        resolve(expression.right)
    }

    override fun visit(expr: Expression.Unary) {
        resolve(expr.value)
    }

    override fun visit(logical: Expression.Logical) {
        resolve(logical.right)
        resolve(logical.left)
    }

    override fun visit(variable: Expression.Variable) {
        if (scopes.isNotEmpty() && scopes.peek()[variable.name.lexeme] == false) {
            Sscript.error(variable.name, "Variable has not been properly defined")
        }
        resolveLocal(variable, variable.name)
    }

    override fun visit(classDec: Statement.ClassDec) {
        currentClass = ClassType.CLASS
        declare(classDec.name)
        define(classDec.name)

        /*
        *  If there is a super class, being a new scope and add the "super" keyword to true
        *  If a variable in the scope is set to true, it is declared and defined automatically
        */

        classDec.superClass?.let {
            if (classDec.name.lexeme == classDec.superClass.name.lexeme) {
                return Sscript.error(classDec.name, "Cannot inherit from self!")
            }
            currentClass = ClassType.SUBCLASS
            resolve(it)
            beginScope()
            scopes.peek()["super"] = true

        }

        beginScope()

        if (classDec.init != null) {
            currentFn = FunctionType.INIT
            beginScope()
            resolve(classDec.init.executeBlock)
            endScope()
            currentFn = FunctionType.NONE
        }

        scopes.peek()["this"] = true

        for (methods in classDec.methods) {
            val declaration = if (methods.fnName.lexeme == "object") FunctionType.NEW else FunctionType.METHOD
            resolveFunction(methods, declaration)
        }


        endScope()

        if (classDec.superClass != null) endScope()

        currentClass = ClassType.NO_CLASS
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
            Sscript.error(name, "A variable has already been declared ")
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

    override fun visit(get: Expression.Get) {
        resolve(get.obj)
    }

    override fun visit(set: Expression.Set) {
        resolve(set.value)
        resolve(set.expr)
    }

    override fun visit(instance: Expression.Instance) {
        if (currentClass == ClassType.NO_CLASS) {
            Sscript.error(instance.inst, "Cannot use \"instance\" outside of classes")
            return
        }
        resolveLocal(instance, instance.inst)
    }

    override fun visit(expr: Expression.Supe) {
        when (currentClass) {
            ClassType.NO_CLASS -> Sscript.error(expr.supe, "Cannot use super outside of a class")
            ClassType.CLASS -> Sscript.error(expr.supe, "Cannot use super in a class with no super class")
            else -> resolveLocal(expr, expr.supe)
        }

    }




}

