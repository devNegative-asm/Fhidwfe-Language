Enjoy Fhidwfe!
This is a C-like language which takes some syntax and feature inspiration from functional languages and python.
This is not a functional language at all though, so be warned.
This software is provided as-is. This compiler is does not come with any warranty, implied or otherwise.

Assembling for z80 requires tniasm, so go download that first.

To use the compiler, build it into a jar then run
java -jar comp.jar
and follow the directions
or simply run "make" on linux

architecture may be any of
1. WINx64:
2. TI83pz80:
3. z80Emulator:
4. LINx64

Compiler modes:
 x64: Generates a file .asm which can be linked with bootstrap.c and assembled with MASM / NASM
 Ti83+Z80: Generates a files outputfile.asm (assembly) and outputfile.prc (preprocessed). outputfile.prc can be assembled by tniasm.
  The resulting file will run on a Ti83+
 emuZ80: Z80 emulated in Java, with support for command line & file IO operations.


Credits to:
	Alberto Sánchez Terrén (z80 emulator)
	Brandonw (creator of ti83plus.inc)
	detachedsolutions (creator of mirage.inc)
	The New Image (creators of tniasm, the assembler this compiler targets)



Syntax:
In most cases, tokens are space delimited. Tokens are also delimited by special characters except for characters used as a prefix or suffix in an operation
	(+ - $ @ = & | < >)
The following rules generally describe the syntax:


file:
private variables:	guard
import globals:		import [var_name]

block:
function calling:	func_name$ args...
conditional:		if cond {blockTrue} {blockFalse}
while loop:			while cond {block}
iteration:			for [type] [iterable_expr] with var_name {block}
assignment:			var = [expr]
assignment:			temp var = [expr]
flag set:			set flag_name
flag reset:			reset flag_name
return:				return [expr] OR return

expr:
casting:			as [expr] [type]
					@[type] [expr]
unary operator:		[op] [expr_1]
binary operator:	[op] [expr_1] [expr_2]
function call:		func_name$ args...
construct type:		TypeName$
int literal:		[0-9]+
uint literal:		[0-9]+u
byte literal:		[0-9]+b
ubyte literal:		[0-9]+ub
float literal:		[0-9]+[fF]|[0-9]*\.[0-9]+|[0-9]+\.[0-9]*
range literal:		[([] [expr_lower],[expr_upper] [)\]]
list literal:		[([] expr... [)\]]
variable:			var_name
pointer-to:			@var_name
field access:		var.fieldname

type definition:	type TypeName (
						fieldname:fieldType ...
						function...)

functions defined inside a type are accessible using the same syntax as fields, and are passed an implicit "this" parameter
instance.methodname$ args...


Temp variables are automatically freed when the block ends or the function returns.

String literals are the same as C except the only escape sequences are \\ \" \r \n \t \0
characters are the same as C except they share the escape sequence limitations and they are of type ubyte.
The syntax #n can be used to refer to the string that comes nth in the program, starting at 0. If the string is not edited and appears multiple times,
	this is more memory efficient than writing it out several times.
 
	
	
; can be used in place of {} for slight performance enhancement and readability


Operation:
Builtin operators are called with prefix order. Thus, there is no order of operations
.		Field access (must be put after an identifier)
!		Unary numeric negation
~		Unary complement
%		Modulo arithmetic 
			Contrary to C's convention defining modulo with  (a/b)*b+(a%b) == a
			This is a more mathematical operation defined such that a%b is always between 0 and b
/		Division (round towards zero)
*		Multiplication
+		Addition
-		subtraction
&		bitwise and
|		bitwise or
&&		logical and
||		logical or
>>		unary shift right (divide by two, round down) (preserves sign bit when operating on signed types)
<<		unary shift left (multiply by 2)
=		bitwise equality comparison
<		less than
>		greater than
<=		less than or equal
>=		greater than or equal
in		test whether a number is inside a range
?		multiplies its argument by int_size. This is faster than actually multiplying and is useful for creating list indicies.
$		(technically a function, not an operator) runs its first argument as a function, passing its second argument as the only parameter to that function
binop$  same as $, but for 2 argument functions

Saved for future use:
`		(unimplemented) Used to create format strings for printf-like behavior




Type system
There are several builtin types. byte and ubyte take 1 byte each and the byte size of an integer is stored in the global variable int_size
uint	unsigned int. The bit size of int is such that (in C terms) sizeof(int)==sizeof(void*)
int		signed int.
ubyte	unsigned byte, also usable as a character.
byte	signed byte.
ptr		same as uint, but used to access memory locations.
file	a file index. This is NOT equivalent to C's FILE*. File is a 1 byte type with a valid range of 0-62. Any values above 62 represent invalid files.
			Internally, this byte is used as the index of a FILE*[]
func	a function pointer. Only single arg uint -> uint functions can be used this way.
float	defined to be of size sizeof(float)==sizeof(void*). Float operations are only available on architectures that natively support flops.
			Conversion from uint -> float is lower accuracy than int -> float.
list	One of the variant types. This acts like a pointer except it can be iterated over in a for loop. It takes a suffix like listbyte to determine its elements.
			Only the types listed above can be put into a list.
range	Another variant type. A range can only hold numeric types, and only integer-like ranges can be iterated in a for loop.
op		same as func, but for 2 argument funcions

casting is done by the keyword "as" which can do the following conversions. Anything not listed here has undefined behavior
any type other than float -> ptr	(no conversion)
ptr or uint or int -> byte or ubyte	(drop higher order bytes)
numeric type -> numeric type		(drop higher order bytes if target is smaller)
byte -> int							(sign extend)
numeric type -> float				(convert value)
float -> numeric type				(depends on hardware rounding implementation, no error checking)
an alternative to code such as
	as signed_number uint
would be to instead write
	@uint signed_number

iteration
A for loop can iterate over a list or a range. When iterating over the elements of a list, any changes to the list other than its size will be reflected in further
	loop iterations.
A for loop iterates over a range with the following protocol. Any changes made to the range variable inside the loop will be ignored for the purposes of iteration.
	1. treating the range like a list, retrieve its first and second elements into r_min and r_max
	2. if the range is exclusive below, increment r_min without checking overflow
	3. if the range is exclusive above, decrement r_max without checking overflow
	4. if r_max is the largest value for its data type, the loop will never end as the internal test x>r_max will never fail. Use a while loop, or
		unravel the for instead.
	4. if r_min > r_max, the number of iterations is 0. 
	5. The for loop will then run the code block r_max-r_min+1 times with the looping variable starting at r_min and increasing by 1 each iteration.
		changes either to the range or to the looping variable made inside the for loop are ignored by the iterator,
		so if r_min<=r_max and r_max<Type.MAX_VALUE there will always be exactly r_max-r_min+1 iterations.
		
Functions:
functions are defined as
	function [return_type] func_name(arg1:type1 arg2:type2 ...) {body}
	with "void" denoting no return value.
	All functions must have a return statement be the final statement in their body unless they are void
	Functions can only be defined in a global scope
	
	functions can be called with func_name$ args...
	without the trailing $, func_name generates a pointer to that function
	function pointers can be called as such:
		$ func_variable arg
	which evaluates to func_name$ arg
	This can only be done with single or 2 argument functions which have a signature or alias which is uint -> uint
	
Types:
	Types can be defined in a similar syntax to functions.
	Types are essentially the same as C structs, with the exception that their fields are not necessarily kept in the same order as they are written.
	Pointers to fields are illegal.
	To use the output of a function as an object and access its fields, it must first be assigned to a variable.
	instances of types are not guaranteed to start off with valid data, so fields can hold arbitrary data until they are assigned.
	

Aliases:
	Functions cannot be overloaded, but they can be given alias type signatures. This is done with the same syntax as the function header but with 'alias'
		instead of 'function', and with no function body. Variable names for aliases are ignored.
	Functions with an alias will not behave any differently than the original, but they will do an data-unaltering cast to the target return type.
	Aliases can only be made between types of the same size, so bytes cannot be converted to ints, etc. this way.
	When creating an alias with a different return type, it is necessary that its combination of input types be distinct from that function's other aliases.
		Otherwise, a call to the function would have an ambiguous return type and not compile.

	Aliases are meant to be a safer version of union types for functions that move or write data. They are absolutely NOT meant for polymorphic behavior,
		and will give unexpected results if used for such.
	
Variables:
	Variables are technically allocated when they enter scope, but are unusable until their first assignment.
	Type inference is used to give types to local and global variables, but they retain that same type for future uses
	Like C, variables local to a function will become unusable once the function exits, and pointers to them will give junk data
	Likewise, variables used for loop control will become unusable when the loop finishes.
	A raw function name will evaluate to a pointer to that function, with type func. Thus puts$ as puts ptr is possible and will print the assembled code
		of puts as if it were a string.
	Using a raw function name for a function imported from the library such as deref_byte may generate an error at the linking stage if the function is inlined. 
	
Scoping rules:
	Variable names used in a local scope will be unusable in other sub-scopes of its parent scope. A "parent" scope in this context meaning either a global or
		function scope.
	In most cases, attempting to shadow a variable will either result in an error, or erroneously use the variable of the same name from the parent scope.
	Only function arguments can shadow globals.
	
	Assigning a value to globals meant to be used in a local context in another function will result in those variables being clobbered when the function
		is called, or in a parsing error.
	
	To use global variables in a safer way, give all the variables in a project a unique prefix
	There is an automated way of doing this, by using the "guard" keyword. This keyword can be inserted anywhere without affecting parsing and will
		add a file-specific prefix to all non-function identifiers after it.
	
	Adding the guard keyword will cause all further variables used in that file to have a long string prepended to them to almost entirely prevent
		cross-file name clashes.
	writing import [var_name] afterward will specifically allow access to that global variable, even if it's in another file.
	when used properly, a guard will prevent your code from accessing compiler constants and globals from other files, and prevent other files from
		accessing your globals.
	(The following may be changed later)
		It is possible to bypass another file's guard by prepending its prefix to the name of a global variable. This is very strongly discouraged.
	
	
Calling convention:
	Under normal operation, the default int-sized register (rax in x64, hl in z80) holds the value that can be considered the "top" of the stack.
	This register should be considered undefined when entering a program or a function. and at any point therein where the stack is empty.
	
	prior to a function call, all arguments must be pushed onto the actual stack in the order given by the source code, followed by a call instruction.
	Upon entering a function, the frame pointer for the caller will be saved (rbp in x64, ix in z80)
	Every argument on the stack must take all int_size bytes, even if the type of the argument would have made it smaller. This is not to say that sp
		must be aligned, but that sp%int_size must stay constant.
	So after entering a function, the stack will look like:
		... [local 2] [local 1] [frame_ptr] [ret_addr] [arg_n] [arg_n-1] ... [arg 0]
		
	On return, all of these entries will be popped off and the default register will hold the return value if there was one.
	If the error$ function is called, sp will be restored to what it was at the beginning of the program, an error message will be printed, and the program
		will exit without returning.
	No guarantees are made about in what order locals will be allocated on the stack
	
	In order to write an assembly function which can be called, it is the callee's responsibility to save the frame pointer.
	
Heap:
	(for z80 only, x86 implementations use C's malloc and free)

	The heap is maintained with similar usage requirements as C's malloc and free.
	The builtin library uses O(n^2) time for mallocing n objects due to its linked list implementation.
	
	(for all implementations)	

	All ranges and lists created are stored on the heap and must be manually free'd after use. List or range literals used as the immediate argument of a for
		loop do not have to be freed.
	An anonymous range or list object not used in a for loop will create a memory leak.
	Alternative implementations of malloc and free can be provided after disabling the library.
	
	(for z80 only)
	
	As it is implemented now, restoring both the head and tail of the heap's linked list will effectively free every object.
	
	
	
	
	
	
(Credit to the maker of the z80 emulator used in this project)
Emulador en Java de un Sinclair ZX Spectrum 48K
1.0 B
Copyright (c) 2004
Alberto Sánchez Terrén
 - edited 3/24/2022
