package compiler.interpreter

import compiler.Sscript
import compiler.env.Entity
import compiler.env.Env
import compiler.inputTypes.Expression
import compiler.inputTypes.Statement
import compiler.tokens.TOKEN_TYPES
import compiler.tokens.TOKEN_TYPES.*
import compiler.tokens.Token
import kotlin.system.exitProcess


class InterVisitor : Expression.Visitor<Any>, Statement.StateVisitor<Unit> {
    private val globals = Env()
    private val locals = HashMap<Expression, Int>()
    private var env = globals

    /*
    * Interpreter - 2021
    * Visitor Pattern implementation to evaluate Expressions and Statements
    */

    init {
        /**
         * These are native functions that work on the global scope.
         * They all implement the Callee interface, which is the base for all callable expressions
         */

        globals.define("clockMS", object : Callee {
            override fun call(interpreter: InterVisitor, arguments: List<Any?>): Double =
                (System.currentTimeMillis().toDouble() / 1000)

            override fun arity(): Int = 0
            override fun toString(): String = "clockMS -> native function"


        })
        globals.define("endProcess", object : Callee {
            override fun call(interpreter: InterVisitor, arguments: List<Any?>): Any? {
                if (arguments[0] is Double) {
                    exitProcess(arguments[0].toString().dropLast(2).toInt())
                }
                throw RuntimeException("Expected number for exit code, got ${arguments[0]}")
            }

            override fun arity(): Int = 1
            override fun toString(): String = "endProcess -> native function"


        })
        globals.define("responseTo", object : Callee {
            override fun call(interpreter: InterVisitor, arguments: List<Any?>): Any? {
                println(arguments[0])
                return when (arguments[1]) {
                    "string" -> readLine()
                    "number" -> readLine().toString().toDouble()
                    "boolean" -> readLine().toString().toBoolean()
                    else -> throw RuntimeException("Invalid argument ${arguments[1]} for second parameter")
                }
            }

            override fun arity(): Int {
                return 2
            }

            override fun toString(): String = "responseTo -> native function"
        })

    }

    fun interpret(listOfStatements: List<Statement>) {
        try {
            listOfStatements.forEach { execute(it) }
        } catch (error: RuntimeError) {
            Sscript.error(error)
        }
    }

    override fun visit(expr: Expression.Unary): Any? {
        val unary: Any? = evaluate(expr.value)

        return when (expr.prefix.type) {
            MINUS -> {
                if (unary is Double) {
                    -unary
                } else null
            }
            NOT -> !isTruthy(unary)
            DECREMENT -> evaluateCompoundUnary(true, expr, expr)
            INCREMENT -> evaluateCompoundUnary(false, unary, expr)
            else -> null
        }

    }

    private fun evaluateCompoundUnary(isNegated: Boolean = true, value: Any?, expr: Expression.Unary): Double {
        if (value !is Double) throw RuntimeError("Value is not a number! on ", expr.prefix)
        val unary = if (isNegated) value - 1 else value + 1
        if (expr.value is Expression.Variable) {

            locals[expr.value]?.let {
                env.assignAt(it, expr.value.name, unary)
            } ?: globals.assign(expr.value.name, unary)

            return unary
        }

        throw RuntimeError("Not a valid operand for decrement operator -- on ", expr.prefix)
    }

    override fun visit(expression: Expression.Ternary): Any? {
        val left: Any? = evaluate(expression.left)
        return evalTernary(left, expression)
    }

    override fun visit(expression: Expression.Binary): Any? {
        val left: Any? = evaluate(expression.left)
        val right: Any? = evaluate(expression.right)

        return when (expression.operator.type) {
            PLUS -> add(left, right)
            MINUS -> subtract(left, right)
            MULT -> multiply(left, right)
            MODULUS -> mod(left, right)
            DIVIDE -> divide(left, right)
            RIGHT_TRIANGLE -> greaterThan(left, right, expression)
            LEFT_TRIANGLE -> lessThan(left, right, expression)
            GREAT_THAN_OR_EQUAL -> greaterOrEqual(left, right, expression)
            LESS_THAN_OR_EQUAL -> lesserOrEqual(left, right, expression)
            EQUAL_EQUAL -> isEqual(left, right)
            NOT_EQUAL -> !isEqual(left, right)
            PLUS_EQUALS -> eqAssign(expression, PLUS_EQUALS)
            MINUS_EQUALS -> eqAssign(expression, MINUS_EQUALS)
            MULT_EQUAL -> eqAssign(expression, MULT_EQUAL)
            DIV_EQUALS -> eqAssign(expression, DIV_EQUALS)
            MOD_EQUALS -> eqAssign(expression, MOD_EQUALS)
            else -> null
        }
    }


    private fun eqAssign(expression: Expression.Binary, tokenType: TOKEN_TYPES): Any {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)
        if (left == null || right == null) throw RuntimeError("Cannot add null", expression.operator)

        if (left is String && right is String && tokenType == PLUS_EQUALS) {
            val value = left + right
            if (expression.left is Expression.Variable) {
                locals[expression.left]?.let { env.assignAt(it, expression.left.name, value) } ?: globals.assign(
                    expression.left.name,
                    value
                )
                return value
            }
            throw RuntimeError("${expression.left} is not a variable that can be assigned", expression.operator)
        }

        if (left is Double && right is Double) {

            val value = when (tokenType) {
                PLUS_EQUALS -> left + right
                MINUS_EQUALS -> left - right
                MULT_EQUAL -> left * right
                DIV_EQUALS -> left / right
                MOD_EQUALS -> left % right
                else -> throw RuntimeException("Incorrect token sign")
            }

            if (expression.left is Expression.Variable) {
                locals[expression.left]?.let { env.assignAt(it, expression.left.name, value) } ?: globals.assign(
                    expression.left.name,
                    value
                )
                return value
            }
            throw RuntimeError("${expression.left} is not a variable that can be assigned", expression.operator)
        }

        throw RuntimeError(
            "Expected two numbers and got ${left::class.simpleName} and ${right::class.simpleName}",
            expression.operator
        )
    }


    override fun visit(expression: Expression.Literal): Any? {
        return expression.value
    }

    override fun visit(expression: Expression.Grouping): Any? {
        return evaluate(expression.expression)
    }

    override fun visit(visitor: Statement.Expression) {
        evaluate(visitor.expr)
    }

    override fun visit(visitor: Statement.Print) {
        val expression = evaluate(visitor.expr)
        println(stringify(expression))
    }

    override fun visit(variable: Expression.Variable): Any? {
        return lookUpVariable(variable.name, variable)
    }

    override fun visit(declaration: Statement.Declaration) {
        declaration.expr?.let { env.define(declaration.name, evaluate(it)) } ?: env.define(declaration.name, null)
    }

    override fun visit(assignment: Expression.Assignment): Any? {
        val value = evaluate(assignment.right)
        locals[assignment]?.let {
            env.assignAt(it, assignment.name, value)
        } ?: globals.assign(assignment.name, value)

        return value
    }

    override fun visit(block: Statement.Block) {
        executeBlock(block.statementList, Env(this.env))
    }

    override fun visit(tree: Statement.If): Any? {
        return if (isTruthy(evaluate(tree.ifBranch))) {
            tree.acted?.let { execute(it) }
        } else {
            tree.elseBranch?.let { execute(it) }
        }

    }

    override fun visit(logical: Expression.Logical): Any? {
        val leftSide = evaluate(logical.left)

        if (logical.operator.type === OR) {
            if (isTruthy(leftSide)) return leftSide
        } else {
            if (!isTruthy(leftSide)) {
                return leftSide
            }
        }

        return evaluate(logical.right)
    }

    override fun visit(loop: Statement.While) {

        while (isTruthy(evaluate(loop.expr))) {

            execute(loop.body)
        }
    }

    override fun visit(call: Expression.Call): Any? {
        val callee: Any = evaluate(call.callee) ?: Sscript.error(call.paren.line, "Undefined function")
        val listResolved = call.args.map { evaluate(it) }

        if (callee !is Callee) {
            throw RuntimeError("$callee cannot be called", call.paren)
        }

        if (callee.arity() != listResolved.size) {
            throw RuntimeError("Expected ${callee.arity()} but got ${listResolved.size} arguments", call.paren)
        }
        return callee.call(this, listResolved)
    }

    override fun visit(function: Statement.Function): Any? {

        val task = Callable(function, env, false)
        env.define(function.fnName, task)

        return null
    }

    override fun visit(arg: Statement.Return): Any? {
        val value = if (arg.value != null) evaluate(arg.value) else null
        throw Return(value)
    }

    override fun visit(classDec: Statement.ClassDec) {

        val superClass = if (classDec.superClass != null) evaluate(classDec.superClass) else null

        if (superClass !is Entity?) {
            throw RuntimeError("Cannot inherit from non class ${classDec.superClass}", classDec.name)
        }

        env.define(classDec.name, null)

        if (classDec.init != null) {
            env = Env(env)
            env.define(classDec.init.name, classDec.init.executeBlock)
            env.enclosed?.also {
                env = it
            }
        }

        if (superClass != null) {
            env = Env(env)
            env.define("super", superClass)
        }

        val allMethods = classDec.methods.associate {
            Pair(it.fnName.lexeme, Callable(it, env, it.fnName.lexeme == "object"))
        } as HashMap

        val klass = Entity(classDec.name.lexeme, superClass, allMethods, classDec.init)

        if (superClass != null) {
            env.enclosed?.also {
                env = it
            }
        }
        env.assign(classDec.name, klass)
    }

    override fun visit(get: Expression.Get): Any? {
        val value = evaluate(get.obj)
        if (value is InstanceOf) {
            return value.get(get.name)
        }
        throw RuntimeError("Only instances have properties", get.name)
    }

    override fun visit(instance: Expression.Instance): Any? {
        return lookUpVariable(instance.inst, instance)
    }

    override fun visit(expr: Expression.Supe): Any {
        val distance = locals[expr] ?: 0

        val superKlass = env.getAt(distance, "super")
        val instanceOf = env.getAt(distance - 1, "this")
        if (superKlass == null) {
            throw RuntimeError("Superclass cannot be null", expr.supe)
        }
        val method: Callable
        if (superKlass is Entity) {
            if (instanceOf is InstanceOf) {
                method = superKlass.findMethod(expr.method.lexeme) ?: throw RuntimeError(
                    "No method found on super class",
                    expr.method
                )
                return method.bind(instanceOf)
            }
        }
        throw RuntimeError("Superclass is not callable on ${expr.method}", expr.supe)
    }


    fun executeBlock(listOfStatements: List<Statement>, currentEnv: Env) {
        val previous = this.env
        try {
            this.env = currentEnv
            for (statement in listOfStatements) {
                execute(statement)
            }
        } finally {
            this.env = previous
        }
    }

    private fun evaluate(expression: Expression): Any? {
        return expression.accept(this)
    }

    /**
     * Null is falsy along with NaN
     * */

    private fun isTruthy(unary: Any?): Boolean {
        return when (unary) {
            null -> false
            is Double -> !unary.isNaN()
            is Boolean -> unary
            else -> true
        }
    }


    private fun add(left: Any?, right: Any?): Any {
        if (left is Double && right is Double) {
            return left + right
        }
        if ((left is String && right is String)) {
            return "$left$right"
        }
        return Double.NaN
    }

    private fun multiply(left: Any?, right: Any?): Any {
        if (left is Double && right is Double) {
            return left * right
        }
        return Double.NaN
    }

    private fun subtract(left: Any?, right: Any?): Double {
        if (left is Double && right is Double) {
            return left - right
        }
        return Double.NaN

    }

    private fun mod(left: Any?, right: Any?): Double {
        if (left is Double && right is Double) {
            return left % right
        }
        return Double.NaN
    }

    private fun divide(left: Any?, right: Any?): Double {
        if (left is Double && right is Double) {
            return left / right
        }
        return Double.NaN
    }

    private fun greaterThan(left: Any?, right: Any?, expression: Expression.Binary): Boolean {
        if (left is Double && right is Double) {
            return left > right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }

    private fun lessThan(left: Any?, right: Any?, expression: Expression.Binary): Boolean {
        if (left is Double && right is Double) {
            return left < right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }

    private fun greaterOrEqual(left: Any?, right: Any?, expression: Expression.Binary): Boolean {
        if (left is Double && right is Double) {
            return left >= right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }

    private fun lesserOrEqual(left: Any?, right: Any?, expression: Expression.Binary): Boolean {
        if (left is Double && right is Double) {
            return left <= right
        }
        throw RuntimeError("Both operators must be a number", expression.operator)
    }

    private fun isEqual(left: Any?, right: Any?): Boolean {
        if (left == null && right == null) return true
        if (left == null) return false

        return left == right
    }

    private fun evalTernary(toBeOperated: Any?, expression: Expression.Ternary): Any? {
        if (toBeOperated == null || toBeOperated == false) return evaluate(expression.right)

        return evaluate(expression.middle)
    }

    private fun stringify(value: Any?): Any? {
        val text = value.toString()
        if (text.endsWith(".0")) {
            return text.substring(0, text.length - 2)

        }

        return value
    }

    private fun execute(statement: Statement) {
        statement.accept(this)
    }

    fun resolve(expr: Expression, depth: Int) {
        locals[expr] = depth
    }

    private fun lookUpVariable(name: Token, variable: Expression): Any? {
        val distance: Int? = locals[variable]
        if (distance != null) {
            return env.getAt(distance, name.lexeme)
        }
        return globals.get(name)

    }

    override fun visit(set: Expression.Set): Any {
        val property = evaluate(set.expr)
        if (property !is InstanceOf) {
            throw RuntimeError("Cannot assign to a non property of object!", set.name)
        }
        val value = evaluate(set.value)
        value?.let {
            property.set(set.name, value)
        } ?: throw RuntimeError("Cannot assign null values to a property", set.name)
        return value
    }




}


