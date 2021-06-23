package compiler.interpreter

import compiler.Expression
import compiler.Statement.Env
import compiler.piekLite
import compiler.tokens.TOKEN_TYPES
import compiler.Statement.Statement
import compiler.tokens.Token
import java.lang.RuntimeException
import kotlin.math.exp
import kotlin.system.exitProcess


class InterVisitor : Expression.Visitor<Any>, Statement.StateVisitor<Unit> {
    private val globals = Env()
    private val locals = HashMap<Expression, Int>()
    private var env = globals

    /*
    * Visitor Pattern implementation to evaluate Expressions and Statements
    *
    * */

    init {
        globals.define("clockMS", object : Callee {
            override fun call(interpreter: InterVisitor, arguments: List<Any?>) : Any {
                return System.currentTimeMillis().toDouble() / 1000
            }
            override fun arity(): Int {
                return 0
            }
            override fun toString(): String {
                return "native function"
            }

        })
      globals.define("endProcess", object : Callee {
          override fun call(interpreter: InterVisitor, arguments: List<Any?>): Any? {
              if(arguments[0] is Double) {
                  exitProcess(arguments[0].toString().dropLast(2).toInt() )
              }
              throw RuntimeException("Expected number for exit code")
          }

          override fun arity(): Int {
              return 1
          }

      })
    globals.define("responseTo", object : Callee {
        override fun call(interpreter: InterVisitor, arguments: List<Any?>): Any? {
          println(arguments[0])
          return when(arguments[1]) {
                "string" -> readLine()
                "number" ->  readLine().toString().toDouble()
                "boolean" -> readLine().toString().toBoolean()
                else -> throw RuntimeException("Invalid argument for second parameter")
            }

        }

        override fun arity(): Int {
            return 2
        }


    })

    }

    fun interpret(listOfStatements: List<Statement>) {
        try {
            listOfStatements.forEach { execute(it) }
        } catch (error: RuntimeError) {
            piekLite.error(error)
        }
    }


    override fun <R> visit(expr: Expression.Unary): Any? {
        val unary: Any? = evaluate(expr.value)

        return when (expr.prefix.type) {
            TOKEN_TYPES.MINUS -> {
                if (unary is Double) {
                    return -unary
                }
                null
            }
            TOKEN_TYPES.NOT -> {
                if (!isTruthy(unary)) {
                    return true
                }
                false
            }
            TOKEN_TYPES.DECREMENT -> {
                evaluateCompoundUnary(true, expr, expr)
            }
            TOKEN_TYPES.INCREMENT -> {
                evaluateCompoundUnary(false, unary, expr)

            }
            else -> null
        }

    }
    private fun evaluateCompoundUnary(isNegated: Boolean = true, value: Any?, expr: Expression.Unary  ) : Double {
        if(value !is Double) throw RuntimeError("Value is not a number! on ", expr.prefix)
        val unary = if(isNegated) value - 1 else value + 1
        if(expr.value is Expression.Variable) {

            val distance = locals[expr.value]
            if(distance != null) {
                env.assignAt(distance, expr.value.name, unary)
            } else {
                globals.assign(expr.value.name, unary)
            }
            return unary
        }

        throw RuntimeError("Not a valid operand for decrement operator -- on ", expr.prefix)
    }

    override fun <R> visit(expression: Expression.Ternary): Any? {
        val left: Any? = evaluate(expression.left)
        return evalTernary(left, expression)
    }

    override fun <R> visit(expression: Expression.Binary): Any? {
        val left: Any? = evaluate(expression.left)
        val right: Any? = evaluate(expression.right)

        return when (expression.operator.type) {

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
            TOKEN_TYPES.LEFT_TRIANGLE -> {
                lessThan(left, right, expression)
            }
            TOKEN_TYPES.GREAT_THAN_OR_EQUAL -> {
                greaterOrEqual(left, right, expression)
            }
            TOKEN_TYPES.LESS_THAN_OR_EQUAL -> {
                lesserOrEqual(left, right, expression)
            }
            TOKEN_TYPES.EQUAL_EQUAL -> {
                isEqual(left, right)
            }
            TOKEN_TYPES.NOT_EQUAL -> {
                !isEqual(left, right)
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

    override fun <R> visit(visitor: Statement.Expression) {
        evaluate(visitor.expr)
    }

    override fun <R> visit(visitor: Statement.Print) {
        val expression = evaluate(visitor.expr)
        println(stringify(expression))
    }

    override fun <R> visit(variable: Expression.Variable): Any? {
        return lookUpVariable(variable.name, variable)
    }

    override fun <R> visit(declaration: Statement.Declaration){
        declaration.expr?.let { env.define(declaration.name,evaluate(it)) } ?: env.define(declaration.name, null)
    }
    override fun <R> visit(assignment: Expression.Assignment) : Any? {
        val distance = locals[assignment]
        val value = evaluate(assignment.right)
        if(distance != null) {
            env.assignAt(distance, assignment.name, value)
        } else {
            globals.assign(assignment.name, value)
        }
        return value
    }

    override fun <R> visit(block: Statement.Block) {
       executeBlock(block.statementList, Env(this.env))
    }

    override fun <R> visit(tree: Statement.If): Any? {
      return if(isTruthy(evaluate(tree.ifBranch))) {
            tree.acted?.let { execute(it) }
            }
            else {
               tree.elseBranch?.let{execute(it)}
            }

    }

    override fun <R> visit(logical: Expression.Logical): Any? {
        val leftSide = evaluate(logical.left)

        if(logical.operator.type === TOKEN_TYPES.OR) {
            if(isTruthy(leftSide)) return leftSide
       } else {
            if(!isTruthy(leftSide)) {
                return leftSide
            }
        }

        return evaluate(logical.right)
    }

    override fun <R> visit(loop: Statement.While) {

        while(isTruthy( evaluate(loop.expr))) {

            execute(loop.body)
        }
    }

    override fun <R> visit(call: Expression.Call): Any? {
        val callee : Any = evaluate(call.callee) ?: piekLite.error(call.paren.line,"Undefined function" )
        val listResolved = mutableListOf<Any?>()
        for (arguments in call.args) {
            listResolved.add(evaluate(arguments))
        }

        if(callee !is Callee) {
            throw RuntimeError("$callee cannot be called", call.paren)
        }

        if(callee.arity() != listResolved.size ) {
            throw RuntimeError("Expected ${callee.arity()} but got ${listResolved.size} arguments", call.paren)
        }
        return callee.call(this, listResolved)
    }
    override fun <R> visit(function: Statement.Function): R? {

        val task = Callable(function, env, false)
        env.define(function.fnName, task)

        return null
    }

    override fun <R> visit(arg: Statement.Return): R? {
        val value = if (arg.value != null) evaluate(arg.value) else null
        throw Return(value)
    }
    override fun <R> visit(classDec: Statement.ClassDec) {

        val superClass = if(classDec.superClass != null) evaluate(classDec.superClass) else null

        if(superClass !is Entity?) {
            throw RuntimeError("Cannot inherit from non class ${classDec.superClass}", classDec.name)
        }

        env.define(classDec.name, null)

        if(classDec.init != null) {
            env = Env(env)
            env.define(classDec.init.name, classDec.init.executeBlock)
            env.enclosed?.also {
                env = it
            }
        }

        if(superClass != null) {
            env = Env(env)
            env.define("super", superClass)
        }

            val allMethods = classDec.methods.associate {
                Pair(it.fnName.lexeme,  Callable(it, env, it.fnName.lexeme == "object") ) } as HashMap

            val klass = Entity(classDec.name.lexeme, superClass, allMethods, classDec.init)

        if(superClass != null) {
            env.enclosed?.also {
                env = it
            }
        }
        env.assign(classDec.name, klass)
    }
    override fun <R> visit(get: Expression.Get): Any? {
        val value = evaluate(get.obj)
        if(value is InstanceOf) {
            return value.get(get.name)
        }
        throw RuntimeError("Only instances have properties", get.name)
    }
    override fun <R> visit(instance: Expression.Instance): Any? {
        return lookUpVariable(instance.inst, instance)
    }

    override fun <R> visit(expr: Expression.Supe): Any {
        val distance = locals[expr] ?: 0

        val superKlass = env.getAt(distance, "super")
        val instanceOf = env.getAt(distance - 1, "instance")
        if(superKlass == null) {
            throw RuntimeError("Superclass cannot be null", expr.supe)
        }
        val method : Callable;
        if(superKlass is Entity) {
            if(instanceOf is InstanceOf) {
                method = superKlass.findMethod(expr.method.lexeme) ?: throw RuntimeError("No method found on super class", expr.method)
               return method.bind(instanceOf)
            }
        }
       throw RuntimeError("Superclass is not callable on ${expr.method}", expr.supe )
    }


    fun executeBlock(listOfStatements: List<Statement>, currentEnv: Env ) {
        val previous = this.env
        try {
            this.env = currentEnv
            for(statement in listOfStatements) {
                execute(statement)
            }
        }
        finally {
            this.env = previous
        }
    }
    private fun evaluate(expression: Expression): Any? {
        return expression.accept(this)
    }

    private fun isTruthy(unary: Any?): Boolean {
    return when(unary) {
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
        if(distance != null) {
            return env.getAt(distance, name.lexeme)
        }
        return globals.get(name)

    }

    override fun <R> visit(set: Expression.Set): Any {
        val property = evaluate(set.expr)
        if(property !is InstanceOf) {
            throw RuntimeError("Cannot assign to a non property of object!", set.name)
        }
        val value = evaluate(set.value)
        value?.let {
            property.set(set.name, value)
        } ?: throw RuntimeError("Cannot assign null values to a property", set.name)
        return value
    }




}