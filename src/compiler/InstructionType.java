package compiler;

/**
 * Instructions supported by the intermediate language
 *
 */
public enum InstructionType{
	//System use
	
	nop(0,"does nothing"),
	raw(1,"insert raw bytes into the code"),
	rawint(1,"insert raw ints into the code"),
	rawspace(1,"insert n bytes of null bytes"),
	rawspaceints(1,"insert n ints of 0 ints"),
	rawinstruction(1,"raw instruction to copy to the asm code"),
	define_symbolic_constant(2,"constant name, and value"),
	
	//error handling
	
	write_sp(1,"save the stack pointer to an address"),
	exit_noreturn(1,"read from the given address into the stack pointer, then return"),
	
	//Variable operations
	
	//push operations
	//retrieve_xx_yy pushes a variable specified by the argument from the xx segment with yy type. Ints and addresses differ in the same way mov and lea differ
	retrieve_global_byte(1, "global index"),
	retrieve_global_int(1, "global int index"),
	retrieve_global_address(1, "global pointer"),
	retrieve_param_byte(1, "param index"),
	retrieve_param_int(1, "param int index"),
	retrieve_param_address(1, "param pointer"),
	retrieve_local_byte(1, "local index"),
	retrieve_local_int(1, "local int index"),
	retrieve_local_address(1,"local pointer"),
	
	//push a literal
	retrieve_immediate_int(1, "literal int"),
	retrieve_immediate_byte(1, "literal byte"),
	retrieve_immediate_float(1,"literal float"),
	
	//pops and pushes the new immediate value more efficiently
	overwrite_immediate_int(1, "literal int"),
	overwrite_immediate_byte(1, "literal byte"),
	overwrite_immediate_float(1,"literal float"),
	
	//the following are used under the assumption only the top 2 stack values are easily accessible, which is true on certain 8 or 16 bit machines. They are much less useful in x86 and may be optimized out 
	copy(0,"pushes top of stack again, making a copy"),
	copy2(0,"pushes top 2 elements of stack again, making a copy and alternating abab"),
	swap12(0,"swaps top 2 elements of the stack"),
	swap23(0,"swaps second and third elements of the stack"),
	swap13(0,"swaps first and third elements of the stack"),
	
	//pop operations
	put_global_byte(1, "global index"),
	put_global_int(1, "global int index"),
	put_param_byte(1, "param index"),
	put_param_int(1, "param int index"),
	put_local_byte(1, "local index"),
	put_local_int(1, "local int index"),

	pop_discard(0,"pop off the top of the stack"),
	
	//math operations
	stackincrement(0,"adds 1 to top stack"),
	stackdecrement(0,"adds 1 to top stack"),
	stackincrement_byte(0,"adds 1 to top stack"),
	stackdecrement_byte(0,"adds 1 to top stack"),
	stackincrement_intsize(0,"adds sizeof(ptr) to top stack"),
	stackdecrement_intsize(0,"subs sizeof(ptr) from top stack"),
	stackincrement_intsize_byte(0,"adds sizeof(ptr) to top stack"),
	stackdecrement_intsize_byte(0,"subs sizeof(ptr) from top stack"),
	stackadd(0,"adds top 2 ints on stack"),
	stackand(0,"ands top 2 ints on stack"),
	stackor(0,"ors top 2 ints on stack"),
	stacksub(0,"subs top 2 ints on stack"),
	stacksub_opposite_order(0,"subs top 2 ints on stack in reverse order"),
	stackneg(0,"negates top int on stack"),
	stacknegbyte(0,"negates top byte on stack"),
	stackxor(0,"xors top 2 ints on stack"),
	stackcpl(0,"complements top int on stack"),
	stacknot(0,"complements top byte on stack"),
	stackmult(0,"multiplies top 2 ints on stack"),
	truncate(0,"truncates top stack entry to be a byte"),
	signextend(0,"converts top stack entry from signed byte to signed int"),

	//these are single-argument unary shifts by 1
	shift_left_b(0,IntermediateLang.shifts),
	shift_left_i(0,IntermediateLang.shifts),
	shift_right_b(0,IntermediateLang.shifts),
	shift_right_ub(0,IntermediateLang.shifts),
	shift_right_i(0,IntermediateLang.shifts),
	shift_right_ui(0,IntermediateLang.shifts),//signed shift right copies the sign bit instead of shifting it
	
	//operations where signed or unsigned matter
	stackdiv_unsigned(0,"divides top 2 ints on stack"),
	stackdiv_signed(0,"divides top 2 ints on stack"),
	stackdiv_unsigned_b(0,"divides top 2 bytes on stack"),
	stackdiv_signed_b(0,"divides top 2 bytes on stack"),
	stackmod_unsigned(0,"modulo's top 2 ints on stack"),
	stackmod_signed(0,"modulo's top 2 ints on stack"),
	stackmod_unsigned_b(0,"modulo's top 2 bytes on stack"),
	stackmod_signed_b(0,"modulo's top 2 bytes on stack"),
	
	//floatint point ops
	
	stackaddfloat(0,"adds top 2 floats on stack"),
	stacksubfloat(0,"subs top 2 floats on stack"),
	stackmultfloat(0,"adds top 2 floats on stack"),
	stackdivfloat(0,"subs top 2 floats on stack"),
	stackmodfloat(0,"adds top 2 floats on stack"),
	stacknegfloat(0,"negates top float on stack"),

	stackconverttobyte(0,"casts a float to an byte"),
	stackconverttoubyte(0,"casts a float to a ubyte"),
	stackconverttoint(0,"casts a float to an int"),
	stackconverttouint(0,"casts a float to a uint"),
	
	stackconverttofloat(0,"casts an int to a float"),
	stackconvertbtofloat(0,"casts an byte to a float"),
	stackconvertutofloat(0,"casts a uint to a float"),
	stackconvertubtofloat(0,"casts a ubyte to a float"),
	
	//library use. These do not generate code on their own, but they do affect how code is generated in surrounding regions based on assumptions about the stack
	notify_pop(0,"Notify the translator that a pop occured in an library's inline replacement"),
	notify_stack(1,"Notify the translator of what the stack depth actually is"),
	
	//control flow

	//function specific
	function_label(1, "func name"),
	enter_function(1, "local space in bytes"),
	exit_function(1,"arg space in bytes"),
	call_function(1,"func name"),
	
	//other flow
	goto_address(1,"address"),
	branch_address(1,"address, only goes if true."),
	branch_not_address(1,"address, only goes if false."),
	exit_global(1,"exits the global routine"),

	syscall_2arg(1,"uses 2 stack tops as the argument to a syscall"),
	syscall_arg(1,"uses stack top as the argument to a syscall"),
	syscall_noarg(1,"performs a syscall without touching the stack"),
	//loops
	general_label(1,"loop name"),
	data_label(1,"data name"),
	//conditional
	//stores the result as a boolean on the stack
	less_than_b(0,"compare 2 bytes on stack"),
	less_than_i(0,"compare 2 ints on stack"),
	less_than_ub(0,"compare 2 ubytes on stack"),
	less_than_ui(0,"compare 2 uints on stack"),
	less_equal_b(0,"compare 2 bytes on stack"),
	less_equal_i(0,"compare 2 ints on stack"),
	less_equal_ub(0,"compare 2 ubytes on stack"),
	less_equal_ui(0,"compare 2 uints on stack"),
	greater_than_b(0,"compare 2 bytes on stack"),
	greater_than_i(0,"compare 2 ints on stack"),
	greater_than_ub(0,"compare 2 ubytes on stack"),
	greater_than_ui(0,"compare 2 uints on stack"),
	greater_equal_b(0,"compare 2 bytes on stack"),
	greater_equal_i(0,"compare 2 ints on stack"),
	greater_equal_ub(0,"compare 2 ubytes on stack"),
	greater_equal_ui(0,"compare 2 uints on stack"),
	equal_to_b(0,"compare 2 bytes on stack"),
	equal_to_i(0,"compare 2 ints on stack"),
	equal_to_f(0,"compare 2 floats on stack"),
	
	//TODO implement these guys
	/*
	branch_less_than_b(0,"compare 2 bytes on stack"),
	branch_less_than_i(0,"compare 2 ints on stack"),
	branch_less_than_ub(0,"compare 2 ubytes on stack"),
	branch_less_than_ui(0,"compare 2 uints on stack"),
	branch_less_equal_b(0,"compare 2 bytes on stack"),
	branch_less_equal_i(0,"compare 2 ints on stack"),
	branch_less_equal_ub(0,"compare 2 ubytes on stack"),
	branch_less_equal_ui(0,"compare 2 uints on stack"),
	branch_greater_than_b(0,"compare 2 bytes on stack"),
	branch_greater_than_i(0,"compare 2 ints on stack"),
	branch_greater_than_ub(0,"compare 2 ubytes on stack"),
	branch_greater_than_ui(0,"compare 2 uints on stack"),
	branch_greater_equal_b(0,"compare 2 bytes on stack"),
	branch_greater_equal_i(0,"compare 2 ints on stack"),
	branch_greater_equal_ub(0,"compare 2 ubytes on stack"),
	branch_greater_equal_ui(0,"compare 2 uints on stack"),
	branch_equal_to_b(0,"compare 2 bytes on stack"),
	branch_equal_to_i(0,"compare 2 ints on stack"),
	branch_not_equal_b(0,"compare 2 bytes on stack"),
	branch_not_equal_i(0,"compare 2 ints on stack"),
	branch_equal_to_f(0,"compare 2 floats on stack"),
	branch_not_equal_f(0,"compare 2 floats on stack"),
	*/
	fix_index(0,"convert an index number to a pointer offset for int size arrays"),

	less_than_f(0,"compare 2 floats on stack"),
	less_equal_f(0,"compare 2 floats on stack"),
	greater_than_f(0,"compare 2 floats on stack"),
	greater_equal_f(0,"compare 2 floats on stack"),
	
	//memory operations
	increment_by_pointer_i(0,"increments int pointed to on stack"),
	decrement_by_pointer_i(0,"decrements int pointed to on stack"),
	increment_by_pointer_b(0,"increments byte pointed to on stack"),
	decrement_by_pointer_b(0,"decrements byte pointed to on stack"),
	copy_from_address(0, "if stack is arranged as [dest] [src] [n], will copy n bytes from [src] to [dest]"),
	strcpy(0,"copy null terminated string"),
	
	load_b(0,"loads a byte pointed to by the top of the stack"),
	store_b(0,"if stack is arranged as [dest] [val], will store val into [dest]"),
	load_i(0,"loads an int pointed to by the top of the stack"),
	store_i(0,"if stack is arranged as [dest] [val], will store val into [dest]"), 
	getc(0,"blocks for input from user input stream and returns the character entered"),
	
	// The intermediate language does not make assumptions about how the stack is actually represented in memory. In all implementations so far, the top of the stack is kept as a register
	;
	
	/**
	 * Generates an instruction object with this type
	 * @return the instruction
	 */
	public Instruction cv() {
		if(argc>0)
			throw new RuntimeException("instruction "+InstructionType.this+" constructed without argument");
		return new Instruction(this);
	}
	/**
	 * Generates an instruction object with this type and the given arguments
	 * @return the instruction
	 */
	public Instruction cv(String... args) {
		if(argc!=args.length)
			throw new RuntimeException("instruction "+InstructionType.this+" constructed with "+args.length+" arguments instead of "+argc);
		return new Instruction(this,args);
	}
	/**
	 * Show the string representation of this instruction
	 * @return the representation
	 */
	public String toString() {
		return this.name();
	}
	/**
	 * The description of this instruction or its usage
	 * @return the description
	 */
	public String descString() {
		return desc;
	}
	
	private InstructionType(int argnum, String desc)
	{
		argc=argnum;
		this.desc=this.name()+": "+desc;
	}
	private final int argc;
	private final String desc;
}