## slowScript(.spt)

a tree-walk interpreter implemented in Kotlin that was created to learn how programming languages worked </br>
Although not very practical to use in a real project, it is Turing complete and provides basic scripting needs. </br>
Yes, its <ins>slow</ins>. </br>
Made possible with http://craftinginterpreters.com/
### References

- [Features](#Features)
- [Quick Docs](#Quick-Docs)
- [Syntax](#Syntax)
- [More to come](#A-few-things-I-want-to-complete) 


```txt
log ("Hello world")
```
```txt
loop(var i = 0 as i < 10: i++) {
    log("Hello world")   
}
```
```text
class A {

}

class littleA from A {

}
```
```txt
task fib(n) {
    if (num <= 1) return 1;
    return fib(n - 1) + fib( n - 2); 
}
```
```text
//Native function asking question and the type of response
var ans = responseTo("What is 10 - 10?", "number")

if (ans != 0) {
 log("Idiot")
 return;
}
```

### Features
- HashMaps for environments
- Implementation of Visitor Pattern
- separate parser and lexer
  - No modes   
- primitives 
   - boolean, 
   - number (64 bit)
   - strings
- Semantic Analysis  
- Recursive Descent Parser
- Classes
  - Inheritance
  - super keyword
  - this keyword  
- Functions
- Lexical Scoping  
- Global variables  
- Closures
- Lexically scoped variables
- if / else (No else if)
- Comments and comment blocks
- Short-hands for expressions ( +=, ++, --)
- return statements
- ternary operators  
- loops (loop, while)
- Kotlin and JS syntax inspiration
- Object getters and setters
- multi-line strings
- truthy values  
- short circuiting evaluation  
- Native functions :
    - asking for user response in terminal
    - stopwatch
    - exitProcess
- No implicit primitive type conversions (number to number, string to string only)  
### Not Featured 
- Module system
- multi-file support  
- standard library
- type system
- immutability
- speed
- syntax highlighting (yet) 
- bitwise operators
- `typeof` or `instanceof` operators</br>
- dead code elimination
- static methods in classes
- escape characters
- template literals
- no data structures 

<b><ins>As stated above, this language was created to learn language design and is not meant for practical use </b><ins>

# Syntax

### keywords 
 -  ``
super``

 -  ``
class``

 -  ``
return``

 -  ``
var``
 
 -  ``
task``
 
 -  ``
false``

 -  ``
true``
 
 -  ``
null``
 
 -  ``
if``
 
 -  ``
else``
 
 -  ``
loop``
 
 -  ``
while``

 -  ``
from``
 
 -  ``
super``

 -  ``
this``
 
 -  ``
NaN``
 
 -  ``
log``
 
 -  ``
as``
 
 -  ``
this``
 
 -  ``
init``
 - ``
 object  ``   
    
### tokens
`
{`
`
}`
`
(`
`
)`
`
;`
`
.`
`
,`
`
:`
`
?`
`#`
`!`
`=`
`>` `<=` `>=` `<` `-` `.` `+` `++` `--` `"` `%` `/` `/=` </br>
And many more! (tokens are similar to C / JS syntax)
 
## Quick Docs

declare a variable -> ` var hi = "Hello" ` </br>
 - declared but not assigned variables are null by default </br>
 - variables that have not been declared nor defined but accessed throw undefined variable error </br>
 - semicolons are always omitted </br> 

declare function -> `task name() {}` </br>
 - returns must end in semicolon </br>
 - nested functions are okay (closures) </br>

declare class -> `class T {}` </br>
  - classes have optional init blocks like Kotlin. use `init{ #stmts | expressions #}` </br>
  -  ```txt
     class joeWithInit {
        init {
          log("a new joe class was instatiated")
          //does not have access to 'this' properties
          //cannot return anything
          
        }
     //constructor
        object(a,b) {
          this.a = a
          this.b = b
          }
        eatChicken() {
          log(this.a + " ate" + " chicken") 
        }
     
     }
     
     ```

extending class -> `class E from T{}` </br>
constructor -> `object( #parameters#) {}` </br>
while -> `while(expression) {}` </br>
loop -> `loop(as:) #infinite loop#`, simple loop ->`loop (var i = 0 as i < 10: i++)  ` </br>
comments -> `// line specific` </br>
comment blocks -> `# comments #` </br>
print to terminal -> `log()`
 - Automatically appends new line (\n) (equivalent to `println()`) </br>
 
declare method in class -> `name(#parameters#){}` </br>
ternary operators -> `condition expression ? expression evaluated if true : expression evaluated if false`
 - Cannot compound ternary operators </br>
 
Short Circuiting -> `expression || expression`
 - Evaluates first truthy values
 - NaN and null are falsy


## A few things I want to complete
  - VScode extension (in the process)[https://github.com/jacoobes/slowscript-langExtension]
  - logo
  - a cmdlet or something to make this easier to run then direct jar
  - usable download on this repo



