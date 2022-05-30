package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import settings.CompilationSettings;
import settings.CompilationSettings.Target;
import types.DataType;

/**
 * Store of code necessary to interact with the OS of each architecture
 *
 */
public class LibFunctions {
	CompilationSettings.Target architecture;
	public LibFunctions(CompilationSettings.Target t) {
		architecture = t;
	}
	int gennum =0;
	private int fresh() {
		return gennum++;
	}
	/**
	 * Loads all necessary information regarding the signatures of library functions into the parser for the purpose of type checking 
	 * @param p the parser to load with library info
	 */
	public void loadLibraryFunctions(Parser p) {
		if(architecture==CompilationSettings.Target.REPL) {
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Byte, "deref_byte");
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Ubyte, "deref_ubyte");
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Int, "deref_int");
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Uint, "deref_uint");
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Ptr, "deref_ptr");
			
			p.registerLibFunction(Arrays.asList(DataType.Func,DataType.Uint), DataType.Uint, "");//call-by-pointer
			p.registerLibFunction(Arrays.asList(DataType.Op, DataType.Uint, DataType.Uint), DataType.Uint, "binop");
			
			p.registerLibFunction(Arrays.asList(DataType.Ptr,DataType.Byte),DataType.Void, "put_byte");
			p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Ubyte),DataType.Void, "put_ubyte");
			p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Int),DataType.Void, "put_int");
			p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Uint),DataType.Void, "put_uint");
			p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Ptr),DataType.Void, "put_ptr");

			p.requireLibFunction(Arrays.asList(DataType.Ptr, DataType.Ptr, DataType.Uint),DataType.Void, "memcpy");
			p.requireLibFunction(Arrays.asList(DataType.Ptr, DataType.Ptr),DataType.Ptr, "strcpy");
			p.registerLibFunction(Arrays.asList(DataType.Ptr),DataType.Void, "error");
			p.registerLibFunction(Arrays.asList(), DataType.Ubyte, "getc");
			p.registerLibFunction(Arrays.asList(DataType.Ubyte), DataType.Void, "putchar");
			p.registerLibFunction(Arrays.asList(), DataType.Void, "putln");

			p.requireLibFunction(Arrays.asList(DataType.Uint), DataType.Ptr, "malloc");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Void, "free");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Uint, "sizeof");
			
			return;
		}
		p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Byte, "deref_byte");
		p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Ubyte, "deref_ubyte");
		p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Int, "deref_int");
		p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Uint, "deref_uint");
		p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Ptr, "deref_ptr");
		
		p.registerLibFunction(Arrays.asList(DataType.Func,DataType.Uint), DataType.Uint, "");//call-by-pointer
		p.registerLibFunction(Arrays.asList(DataType.Op, DataType.Uint, DataType.Uint), DataType.Uint, "binop");
		
		p.registerLibFunction(Arrays.asList(DataType.Ptr,DataType.Byte),DataType.Void, "put_byte");
		p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Ubyte),DataType.Void, "put_ubyte");
		p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Int),DataType.Void, "put_int");
		p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Uint),DataType.Void, "put_uint");
		p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Ptr),DataType.Void, "put_ptr");

		p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Ptr, DataType.Uint),DataType.Void, "memcpy");
		p.registerLibFunction(Arrays.asList(DataType.Ptr, DataType.Ptr),DataType.Ptr, "strcpy");
		p.registerLibFunction(Arrays.asList(DataType.Ptr),DataType.Void, "error");
		p.aliasLibFunction(Arrays.asList(DataType.Listbyte),DataType.Void, "error");
		p.aliasLibFunction(Arrays.asList(DataType.Listubyte),DataType.Void, "error");
		p.registerLibFunction(Arrays.asList(), DataType.Void, "putln");
		if(p.settings.target==Target.LINx64) {
			p.requireLibFunction(Arrays.asList(), DataType.Ubyte, "getc");
			p.requireLibFunction(Arrays.asList(DataType.Ubyte), DataType.Void, "putchar");
			
		} else {
			p.registerLibFunction(Arrays.asList(), DataType.Ubyte, "getc");
			p.registerLibFunction(Arrays.asList(DataType.Ubyte), DataType.Void, "putchar");
			p.aliasLibFunction(Arrays.asList(DataType.Byte),DataType.Void, "putchar");
		}

		for(DataType t:DataType.values()) {
			if(t.getSize(p)>1) {
				p.aliasLibFunction(Arrays.asList(DataType.Func,t), t, "");
			}
		}
		
		
		p.requireLibFunction(Arrays.asList(DataType.Int,DataType.Range), DataType.Bool, "inrange");
		
		p.requireLibFunction(Arrays.asList(DataType.Byte,DataType.Brange), DataType.Bool, "inbrange");
		
		p.requireLibFunction(Arrays.asList(DataType.Uint,DataType.Urange), DataType.Bool, "inurange");
		
		p.requireLibFunction(Arrays.asList(DataType.Ubyte,DataType.Ubrange), DataType.Bool, "inubrange");
		
		p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Void, "puts");
		//architecture specific libraries
		switch(architecture) {
		case TI83pz80:
			p.requireLibFunction(Arrays.asList(DataType.Uint), DataType.Ptr, "malloc");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Void, "free");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Uint, "sizeof");
			
			p.requireLibFunction(Arrays.asList(DataType.Int,DataType.Int), DataType.Int, "sdiv");
			p.requireLibFunction(Arrays.asList(DataType.Int,DataType.Int), DataType.Int, "smod");
			p.requireLibFunction(Arrays.asList(DataType.Byte,DataType.Byte), DataType.Byte, "sbdiv");
			p.requireLibFunction(Arrays.asList(DataType.Byte,DataType.Byte), DataType.Byte, "sbmod");
			
			p.registerLibFunction(Arrays.asList(), DataType.Void, "draw");
			break;
			
		case LINx64:
			
			p.requireLibFunction(Arrays.asList(DataType.Uint), DataType.Ptr, "malloc");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Uint, "sizeof");
			p.requireLibFunction(Arrays.asList(DataType.Ptr,DataType.Uint), DataType.Ptr, "realloc");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Void, "free");
			
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.File, "fopen_r");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.File, "fopen_w");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.File, "fopen_a");
			
			p.requireLibFunction(Arrays.asList(DataType.File,DataType.Ubyte), DataType.Void, "fwrite");
			p.requireLibFunction(Arrays.asList(DataType.File), DataType.Void, "fflush");
			p.requireLibFunction(Arrays.asList(DataType.File), DataType.Void, "fclose");
			p.requireLibFunction(Arrays.asList(DataType.File), DataType.Int, "fread");
			
			//flops
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "exp");
			p.requireLibFunction(Arrays.asList(DataType.Float, DataType.Float), DataType.Float, "pow");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "log");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "ln");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "exp");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "lg");
			
			p.registerLibFunction(Arrays.asList(DataType.Float), DataType.Float, "sqrt");
			p.registerLibFunction(Arrays.asList(DataType.Float), DataType.Float, "sin");
			p.registerLibFunction(Arrays.asList(DataType.Float), DataType.Float, "cos");
			
			break;
		case WINx64:
		case WINx86:
			p.registerLibFunction(Arrays.asList(DataType.Uint), DataType.Ptr, "malloc");
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Uint, "sizeof");
			p.registerLibFunction(Arrays.asList(DataType.Ptr,DataType.Uint), DataType.Ptr, "realloc");
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.Void, "free");
			
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.File, "fopen");
			p.registerLibFunction(Arrays.asList(DataType.File,DataType.Ubyte), DataType.Void, "fwrite");
			p.registerLibFunction(Arrays.asList(DataType.File), DataType.Void, "fflush");
			p.registerLibFunction(Arrays.asList(DataType.File), DataType.Void, "fclose");
			p.registerLibFunction(Arrays.asList(DataType.File), DataType.Uint, "fread");
			
			//flops
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "exp");
			p.requireLibFunction(Arrays.asList(DataType.Float, DataType.Float), DataType.Float, "pow");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "log");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "ln");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "exp");
			p.requireLibFunction(Arrays.asList(DataType.Float), DataType.Float, "lg");
			
			p.registerLibFunction(Arrays.asList(DataType.Float), DataType.Float, "sqrt");
			p.registerLibFunction(Arrays.asList(DataType.Float), DataType.Float, "sin");
			p.registerLibFunction(Arrays.asList(DataType.Float), DataType.Float, "cos");
			
			break;
		case z80Emulator:
			p.registerLibFunction(Arrays.asList(DataType.Ptr), DataType.File, "fopen");
			p.registerLibFunction(Arrays.asList(DataType.File,DataType.Ubyte), DataType.Void, "fwrite");
			p.registerLibFunction(Arrays.asList(DataType.File), DataType.Void, "fflush");
			p.registerLibFunction(Arrays.asList(DataType.File), DataType.Void, "fclose");
			p.registerLibFunction(Arrays.asList(DataType.File), DataType.Ubyte, "fread");
			p.registerLibFunction(Arrays.asList(DataType.File), DataType.Ubyte, "favail");
			

			p.requireLibFunction(Arrays.asList(DataType.Uint), DataType.Ptr, "malloc");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Void, "free");
			p.requireLibFunction(Arrays.asList(DataType.Ptr), DataType.Uint, "sizeof");
			
			p.requireLibFunction(Arrays.asList(DataType.Int,DataType.Int), DataType.Int, "sdiv");
			p.requireLibFunction(Arrays.asList(DataType.Int,DataType.Int), DataType.Int, "smod");
			p.requireLibFunction(Arrays.asList(DataType.Byte,DataType.Byte), DataType.Byte, "sbdiv");
			p.requireLibFunction(Arrays.asList(DataType.Byte,DataType.Byte), DataType.Byte, "sbmod");
			break;
		default:
			break;
			
		}
		if(p.hasFunction("free")) {
			for(DataType t:DataType.values()) {
				if(t.isFreeable() && t!=DataType.Ptr) {
					p.aliasLibFunction(Arrays.asList(t), DataType.Void, "free");
				}
			}
		}
	}
	/**
	 * Does a simple inline-replacement to switch out library function calls with their implementation. The translators assume these functions behave exactly the same as fhidwfe functions in terms of stack operations, which may or may not be true. 
	 * @param instructions the list of instructions which may have calls to library functions
	 * @param p the parser the instructions were originally parsed with
	 */
	public void correct(List<Instruction> instructions, Parser p) {
		p.inlineReplace("deref_byte");
		p.inlineReplace("deref_ubyte");
		p.inlineReplace("deref_int");
		p.inlineReplace("deref_uint");
		p.inlineReplace("deref_ptr");
		p.inlineReplace("put_byte");
		p.inlineReplace("put_ubyte");
		p.inlineReplace("put_int");
		p.inlineReplace("put_uint");
		p.inlineReplace("put_ptr");
		p.inlineReplace("");
		p.inlineReplace("binop");
		if(architecture==CompilationSettings.Target.LINx64) {
			p.inlineReplace("memcpy");
			p.inlineReplace("strcpy");
			p.inlineReplace("cos");
			p.inlineReplace("sqrt");
			p.inlineReplace("sin");
			p.inlineReplace("putln");
			for(String s:p.getExternFunctions()) {
				instructions.add(0,InstructionType.rawinstruction.cv("extern "+s));
			}
		}
		if(architecture==CompilationSettings.Target.WINx64) {
			p.inlineReplace("malloc");
			p.inlineReplace("free");
			p.inlineReplace("realloc");
			p.inlineReplace("sizeof");
			p.inlineReplace("getc");
			p.inlineReplace("putchar");
			p.inlineReplace("memcpy");
			p.inlineReplace("strcpy");
			p.inlineReplace("putln");
			p.inlineReplace("fflush");
			p.inlineReplace("fwrite");
			p.inlineReplace("fclose");
			p.inlineReplace("fopen");
			p.inlineReplace("fread");
			//p.inlineReplace("favail");
			p.inlineReplace("cos");
			p.inlineReplace("sqrt");
			p.inlineReplace("sin");
		}
		if(architecture==CompilationSettings.Target.TI83pz80) {
			p.inlineReplace("putln");
			p.inlineReplace("memcpy");
			p.inlineReplace("putchar");
			p.inlineReplace("strcpy");
			p.inlineReplace("getc");
			
			p.inlineReplace("draw");
		}
		if(architecture==CompilationSettings.Target.z80Emulator) {
			p.inlineReplace("putln");
			p.inlineReplace("memcpy");
			p.inlineReplace("putchar");
			p.inlineReplace("strcpy");
			p.inlineReplace("getc");
			
			p.inlineReplace("fread");
			p.inlineReplace("fflush");
			p.inlineReplace("fopen");
			p.inlineReplace("fclose");
			p.inlineReplace("fwrite");
			p.inlineReplace("favail");
		}
		for(int loc = 0;loc < instructions.size(); loc++) {
			Instruction instr = instructions.get(loc);
			List<Instruction> replacement = new ArrayList<>();
			
			if(	p.settings.target==CompilationSettings.Target.LINx64
					&& instr.in==InstructionType.call_function 
					&& p.getExternFunctions().contains(instr.args[0])) {
				
				if(!p.getFunctionInputTypes(instr.args[0]).get(0).isEmpty()) {
					replacement.add(InstructionType.rawinstruction.cv("push rax"));
				}
				replacement.add(InstructionType.rawinstruction.cv("call "+instr.args[0]));

			}
			else if(instr.in==InstructionType.call_function) {
				if(p.isInlined(instr.args[0]))
				switch(instr.args[0]) {
					case "binop":
						//call function
						switch(architecture) {
						case LINx64:
						case WINx64:
							//the stack holds the function address then the first arg
							//rax holds the second argument
							replacement = Arrays.asList(
									InstructionType.swap23.cv(),
									InstructionType.rawinstruction.cv("pop rcx"),
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("call rcx"),
									InstructionType.notify_pop.cv(),
									InstructionType.notify_pop.cv());
							break;
						case TI83pz80:
						case z80Emulator:
							int lbl = fresh();
							replacement = Arrays.asList(
								InstructionType.swap23.cv(),
								InstructionType.rawinstruction.cv("ex (sp),hl"),
								InstructionType.rawinstruction.cv("ld bc,__indirection_"+lbl),
								InstructionType.rawinstruction.cv("push bc"),
								InstructionType.rawinstruction.cv("jp (hl)"),
								InstructionType.general_label.cv("__indirection_"+lbl),
								InstructionType.notify_pop.cv(),
								InstructionType.notify_pop.cv()
							);
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "":
						//call function
						switch(architecture) {
						case LINx64:
						case WINx64:
							//the stack holds the function address
							//rax holds the argument
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("pop rcx"),
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("call rcx"),
									InstructionType.notify_pop.cv());
							break;
						case TI83pz80:
						case z80Emulator:
							int lbl = fresh();
							replacement = Arrays.asList(
								InstructionType.rawinstruction.cv("ex (sp),hl"),
								InstructionType.rawinstruction.cv("ld bc,__indirection_"+lbl),
								InstructionType.rawinstruction.cv("push bc"),
								InstructionType.rawinstruction.cv("jp (hl)"),
								InstructionType.general_label.cv("__indirection_"+lbl),
								InstructionType.notify_pop.cv()
							);
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "sin":
						switch(architecture) {
						case WINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("fld QWORD PTR [rsp]"),
									InstructionType.rawinstruction.cv("fsin"),
									InstructionType.rawinstruction.cv("fstp QWORD PTR [rsp]"),
									InstructionType.rawinstruction.cv("pop rax"));
							break;
						case LINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("fld [rsp]"),
									InstructionType.rawinstruction.cv("fsin"),
									InstructionType.rawinstruction.cv("fstp [rsp]"),
									InstructionType.rawinstruction.cv("pop rax"));
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "cos":
						switch(architecture) {
						case WINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("fld QWORD PTR [rsp]"),
									InstructionType.rawinstruction.cv("fcos"),
									InstructionType.rawinstruction.cv("fstp QWORD PTR [rsp]"),
									InstructionType.rawinstruction.cv("pop rax"));
							break;
						case LINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("fld [rsp]"),
									InstructionType.rawinstruction.cv("fcos"),
									InstructionType.rawinstruction.cv("fstp [rsp]"),
									InstructionType.rawinstruction.cv("pop rax"));
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "sqrt":
						switch(architecture) {
						case WINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("fld QWORD PTR [rsp]"),
									InstructionType.rawinstruction.cv("fsqrt"),
									InstructionType.rawinstruction.cv("fstp QWORD PTR [rsp]"),
									InstructionType.rawinstruction.cv("pop rax"));
							break;
						case LINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("push rax"),
									InstructionType.rawinstruction.cv("fld [rsp]"),
									InstructionType.rawinstruction.cv("fsqrt"),
									InstructionType.rawinstruction.cv("fstp [rsp]"),
									InstructionType.rawinstruction.cv("pop rax"));
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "malloc":
						switch(architecture) {
						case WINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("mov rcx,rax"),
									InstructionType.syscall_noarg.cv("__malloc"));
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "sizeof":
						switch(architecture) {
						case WINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("mov rcx,rax"),
									InstructionType.syscall_noarg.cv("__sizeof"));
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "free":
						switch(architecture) {
						case WINx64:
							replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("mov rcx,rax"),
									InstructionType.syscall_noarg.cv("__free"),
									InstructionType.pop_discard.cv());
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "realloc":
						switch(architecture) {
						case WINx64:
							replacement = Arrays.asList(
									InstructionType.notify_pop.cv(),
									InstructionType.rawinstruction.cv("mov rdx,rax"),
									InstructionType.rawinstruction.cv("pop rcx"),
									InstructionType.syscall_noarg.cv("__realloc"));
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "deref_ubyte":
					case "deref_byte":
						replacement = Arrays.asList(InstructionType.load_b.cv());
						break;
					case "deref_int":
					case "deref_uint":
					case "deref_ptr":
						replacement = Arrays.asList(InstructionType.load_i.cv());
						break;
					case "put_ubyte":
					case "put_byte":
						replacement = Arrays.asList(InstructionType.store_b.cv());
						break;
					case "put_int":
					case "put_uint":
					case "put_ptr":
						replacement = Arrays.asList(InstructionType.store_i.cv());
						break;
					case "fclose":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then request a close
									InstructionType.rawinstruction.cv("ld a,0xc0"),
									InstructionType.rawinstruction.cv("or l"),
									InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									InstructionType.rawinstruction.cv("ld a,$82"),//close_constant
									InstructionType.rawinstruction.cv("out (1),a"),
									InstructionType.pop_discard.cv()
								);
								break;
							case WINx64:
								replacement = Arrays.asList(
										InstructionType.rawinstruction.cv("mov rcx,rax"),
										InstructionType.syscall_noarg.cv("__fclose"),
										InstructionType.pop_discard.cv());
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "fflush":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then request a flush
									InstructionType.rawinstruction.cv("ld a,0xc0"),
									InstructionType.rawinstruction.cv("or l"),
									InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									InstructionType.rawinstruction.cv("ld a,$81"),//flush_constant
									InstructionType.rawinstruction.cv("out (1),a"),
									InstructionType.pop_discard.cv()
								);
								break;
							case WINx64:
								replacement = Arrays.asList(
										InstructionType.rawinstruction.cv("mov rcx,rax"),
										InstructionType.syscall_noarg.cv("__fflush"),
										InstructionType.pop_discard.cv());
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "fread":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then read
									InstructionType.rawinstruction.cv("ld a,0xc0"),
									InstructionType.rawinstruction.cv("or l"),
									InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									InstructionType.rawinstruction.cv("in a,(1)"),//read_byte
									InstructionType.rawinstruction.cv("ld l,a")
								);
								break;
							case WINx64:
								replacement = Arrays.asList(
										InstructionType.rawinstruction.cv("mov rcx,rax"),
										InstructionType.syscall_noarg.cv("__fread"));
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "favail":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then read
									InstructionType.rawinstruction.cv("ld a,0xc0"),
									InstructionType.rawinstruction.cv("or l"),
									InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									InstructionType.rawinstruction.cv("ld a,$83"),//select available mode
									InstructionType.rawinstruction.cv("out (1),a"),
									InstructionType.rawinstruction.cv("in a,(1)"),//read_byte
									InstructionType.rawinstruction.cv("ld l,a")
								);
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "fwrite":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then request a write
									InstructionType.notify_pop.cv(),
									InstructionType.rawinstruction.cv("pop de"),
									InstructionType.rawinstruction.cv("ld a,0xc0"),
									InstructionType.rawinstruction.cv("or e"),
									InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									InstructionType.rawinstruction.cv("ld a,$80"),//write_constant
									InstructionType.rawinstruction.cv("out (1),a"),
									InstructionType.rawinstruction.cv("ld a,l"),//write the second arg
									InstructionType.rawinstruction.cv("out (1),a"),
									
									InstructionType.pop_discard.cv()
								);
								break;
							case WINx64:
								replacement = Arrays.asList(
										InstructionType.notify_pop.cv(),
										InstructionType.rawinstruction.cv("mov rdx,rax"),
										InstructionType.rawinstruction.cv("pop rcx"),
										InstructionType.syscall_noarg.cv("__fwrite"),
										InstructionType.pop_discard.cv());
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "fopen":
						int label = fresh();
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to file select mode, write my argument string (including terminating 0) and return the descriptor
									InstructionType.rawinstruction.cv("ld a,$84"),
									InstructionType.rawinstruction.cv("out (1),a"),//entered file select mode
									InstructionType.general_label.cv("__fopen_loop_"+label),
									InstructionType.rawinstruction.cv("ld a,(hl)"),
									InstructionType.rawinstruction.cv("out (1),a"),
									InstructionType.rawinstruction.cv("inc hl"),
									InstructionType.rawinstruction.cv("or a"),
									InstructionType.rawinstruction.cv("jr nz, __fopen_loop_"+label),
									InstructionType.rawinstruction.cv("in a,(1)"),
									InstructionType.rawinstruction.cv("ld l,a"),
									InstructionType.rawinstruction.cv("ld h,$0")
								);
								break;
							case WINx64:
								replacement = Arrays.asList(
										InstructionType.rawinstruction.cv("mov rcx,rax"),
										InstructionType.syscall_noarg.cv("__fopen"));
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "memcpy":
						replacement = Arrays.asList(
							InstructionType.copy_from_address.cv()
						);
						break;
					case "getc"://block for input
						replacement = Arrays.asList(
								InstructionType.getc.cv()
						);
						break;
					case "putchar":
						switch(architecture) {
							case TI83pz80:
								replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("ld a,l"),
									InstructionType.syscall_arg.cv("_PutC")//putchar does not take its argument in hl, but we need to pop it anyway
								);
								break;
							case z80Emulator:
								replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("ld a,l"),
									InstructionType.rawinstruction.cv("out (0),a"),
									InstructionType.pop_discard.cv());
								break;
							case WINx64:
								replacement = Arrays.asList(
									InstructionType.rawinstruction.cv("mov rcx,rax"),
									InstructionType.syscall_noarg.cv("__putchar"),
									InstructionType.pop_discard.cv()
								);
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
							}
						break;
					case "putln":
						switch(architecture) {
							case LINx64:
								replacement = Arrays.asList(
										InstructionType.retrieve_immediate_byte.cv("10"),
										InstructionType.call_function.cv("putchar")
								);
								break;
							case WINx64:
								replacement = Arrays.asList(
										InstructionType.rawinstruction.cv("mov rdi, rax"),
										InstructionType.rawinstruction.cv("mov rcx, 13"),
										InstructionType.syscall_noarg.cv("__putchar"),
										InstructionType.rawinstruction.cv("mov rcx, 10"),
										InstructionType.syscall_noarg.cv("__putchar"),
										InstructionType.rawinstruction.cv("mov rax, rdi")
								);
								break;
							case TI83pz80:
								replacement = Arrays.asList(
										InstructionType.rawinstruction.cv("ld de,curRow"),//preserve hl and ix
										InstructionType.rawinstruction.cv("ld a,(de)"),
										InstructionType.rawinstruction.cv("inc a"),
										InstructionType.rawinstruction.cv("and $07"),
										InstructionType.rawinstruction.cv("ld (de),a"),
										InstructionType.rawinstruction.cv("xor a"),
										InstructionType.rawinstruction.cv("inc de"),
										InstructionType.rawinstruction.cv("ld (de),a")
								);
								break;
							case z80Emulator:
								replacement = Arrays.asList(
										InstructionType.rawinstruction.cv("ld a,"+(byte)'\r'),//lol windows
										InstructionType.rawinstruction.cv("out (0),a"),
										InstructionType.rawinstruction.cv("ld a,"+(byte)'\n'),
										InstructionType.rawinstruction.cv("out (0),a"));
									break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
							}
						break;
					case "strcpy":
						replacement = Arrays.asList(
								InstructionType.strcpy.cv()
						);
						break;
					case "draw":
						switch(architecture) {
							case TI83pz80:
								replacement = Arrays.asList(InstructionType.syscall_noarg.cv("_GrBufCpy"));
								break;
							default:
								throw new UnsupportedOperationException("graphics on "+architecture+" not supported yet");
						}
					default:
						break;
				}
			}
			if(!replacement.isEmpty()) {
				instructions.remove(loc);
				instructions.addAll(loc,replacement);
			}
		}
		if(architecture.name().contains("x64")||architecture.name().contains("x86"))
			for(int i=0;i<instructions.size();i++) {
				Instruction instruction = instructions.get(i);
				int argc = instruction.args.length;
				if(instruction.in==InstructionType.rawinstruction) {
					instruction.args[0] = instruction.args[0].replaceAll("(\\s|,|^)__", "$1Fwf_internal_");
				} else
					for(int j=0;j<argc;j++) {
						if(instruction.args[j].startsWith("__"))
							instruction.args[j] = "Fwf_internal"+instruction.args[j].substring(1);
					}
			}
	}
	/**
	 * Notify the syntax tree that the given symbols refer to assemble-time constants, as well as their types
	 * @param tree
	 */
	public void loadCompiletimeConstants(BaseTree tree) {
		tree.addConstantValue("heaphead", DataType.Ptr);
		tree.addConstantValue("heap", DataType.Ptr);
		tree.addConstantValue("heaptail", DataType.Ptr);
		tree.addConstantValue("int_size", DataType.Uint);
		tree.notifyCalled("putchar");
		tree.notifyCalled("puts");
		if(architecture.intsize==8) {
			tree.addConstantValue("e", DataType.Float);
			tree.addConstantValue("pi", DataType.Float);
			tree.addConstantValue("qNaN", DataType.Float);
			tree.addConstantValue("sNaN", DataType.Float);
			tree.addConstantValue("NaN", DataType.Float);
			tree.addConstantValue("pos_infinity", DataType.Float);
			tree.addConstantValue("neg_infinity", DataType.Float);
		}
		if(architecture==Target.TI83pz80) {
			tree.addConstantValue("plotSScreen", DataType.Ptr);
		}
	}
}
