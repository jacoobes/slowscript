##slowScript.spt

a tree-walk interpreter implemented in Kotlin that was created to learn how programming languages worked </br>
Although not very practical to use in a real project, it is Turing complete and provides basic scripting needs. </br>
Yes, its <ins>slow</ins>.


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
//Native function asking question and the type of the answer
var ans = responseTo("What is 10 - 10?", "number")

if (ans != 0) {
 log("Idiot")
 return;
}
```

###Features
- Implementation of Visitor Pattern
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
- Closures
- Lexically scoped variables
- if / else (No else if)
- Comments and comment blocks
- Short-hands for expressions ( +=, ++, --)
- return statements
- ternary operators  
- loops (loop, while)
- Kotlin / JS syntax inspiration
- Object getters and setters
- multi-line strings  
- Native functions :
    - asking for user response in terminal
    - stopwatch
    - exitProcess
### Not Featured 
- Module system
- standard library
- type system
- immutability
- speed
- syntax highlighting (yet) 
- bitwise operators
 

#Syntax

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
    
### characters 
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
And many more! (characters are similar to C / JS syntax)
 
## Quick Docs

declare a variable -> ` var hi = "Hello" ` </br>
 - declared but not assigned variables are null by default </br>
 - variables that have not been declared nor defined but accessed throw undefined variable error </br>
 - semicolons are not optional and omitted </br> 

declare function -> `task() {}` </br>
 - returns must end in semicolon </br>
 - nested functions are okay (closures) </br>

declare class -> `class T {}` </br>
  - classes have optional init blocks like Kotlin. use `init{}` </br>

extending class -> `class E from T{}` </br>
constructor -> `object( #parameters#) {}` </br>
while -> `while(expression) {}` </br>
loop -> `loop(as:) #infinite loop#`, `loop (var i = 0 as i < 10: i++)  ` </br>
comments -> `// line specific` </br>
comment blocks -> `# comments #` </br>
print to terminal -> `log()`
 - Automatically appends line </br>








