package compiler

import tokens.*
interface ExprVisitor<R> { 

 fun <R> visitBinary(visitor : Expression.Binary) {

 }
 fun <R> visitUnary(visitor : Expression.Unary) {

 }
 fun <R> visitGrouping(visitor : Expression.Grouping) {

 }
 fun <R> visitLiteral(visitor : Expression.Literal) {

 }
 fun <R> visitTernary(visitor : Expression.Ternary) {

 }
abstract class Expression {

class Binary(left:Expression, operator:Token, right:Expression) : Expression() {   
override fun <R> accept(visitor: ExprVisitor<R> ) {

visitor.visitBinary<R>(this)

    }

}


class Unary(prefix:Token?, expression:Expression, postfix:Token?) : Expression() {   
override fun <R> accept(visitor: ExprVisitor<R> ) {

visitor.visitUnary<R>(this)

    }

}


class Grouping(expression:Expression) : Expression() {   
override fun <R> accept(visitor: ExprVisitor<R> ) {

visitor.visitGrouping<R>(this)

    }

}


class Literal(value:Any) : Expression() {   
override fun <R> accept(visitor: ExprVisitor<R> ) {

visitor.visitLiteral<R>(this)

    }

}


class Ternary(left:Expression, questionMark:String, middle:Expression, colon:String, right:Expression) : Expression() {   
override fun <R> accept(visitor: ExprVisitor<R> ) {

visitor.visitTernary<R>(this)

    }

}



abstract fun <R> accept(visitor : ExprVisitor<R>)
    }
}
