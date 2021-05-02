package compiler

import tokens.*
abstract class Expression {

class Binary(left:Expression, operator:Token, right:Expression) {

}
class Unary(prefix:Token?, expression:Expression, postfix:Token?) {

}
class grouping(expression:Expression) {

}
class literal(value:Any) {

}
class ternary(left:Expression, questionMark:String, middle:Expression, colon:String, right:Expression) {

}
}
