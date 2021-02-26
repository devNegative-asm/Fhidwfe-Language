package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibFunctions {
	CompilationSettings.Target architecture;
	public LibFunctions(CompilationSettings.Target t) {
		architecture = t;
	}
	int gennum =0;
	private int fresh() {
		return gennum++;
	}
	public void loadLibraryFunctions(Parser p) {
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.Byte, "deref_byte");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.Ubyte, "deref_ubyte");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.Int, "deref_int");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.Uint, "deref_uint");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.Ptr, "deref_ptr");
		
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr,Parser.Data.Byte),Parser.Data.Void, "put_byte");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr, Parser.Data.Ubyte),Parser.Data.Void, "put_ubyte");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr, Parser.Data.Int),Parser.Data.Void, "put_int");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr, Parser.Data.Uint),Parser.Data.Void, "put_uint");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr, Parser.Data.Ptr),Parser.Data.Void, "put_ptr");

		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr, Parser.Data.Ptr, Parser.Data.Uint),Parser.Data.Void, "memcpy");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr, Parser.Data.Ptr),Parser.Data.Ptr, "strcpy");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ptr),Parser.Data.Void, "error");
		p.registerLibFunction(Arrays.asList(), Parser.Data.Ubyte, "getc");
		p.registerLibFunction(Arrays.asList(Parser.Data.Ubyte), Parser.Data.Void, "putchar");
		p.registerLibFunction(Arrays.asList(), Parser.Data.Void, "putln");
		
		
		p.requireLibFunction(Arrays.asList(Parser.Data.Uint), Parser.Data.Ptr, "malloc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.Void, "free");
		
		p.requireLibFunction(Arrays.asList(Parser.Data.Int,Parser.Data.Rangecc), Parser.Data.Bool, "inrangecc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Int,Parser.Data.Rangeco), Parser.Data.Bool, "inrangeco");
		p.requireLibFunction(Arrays.asList(Parser.Data.Int,Parser.Data.Rangeoc), Parser.Data.Bool, "inrangeoc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Int,Parser.Data.Rangeoo), Parser.Data.Bool, "inrangeoo");
		
		p.requireLibFunction(Arrays.asList(Parser.Data.Byte,Parser.Data.Brangecc), Parser.Data.Bool, "inbrangecc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Byte,Parser.Data.Brangeco), Parser.Data.Bool, "inbrangeco");
		p.requireLibFunction(Arrays.asList(Parser.Data.Byte,Parser.Data.Brangeoc), Parser.Data.Bool, "inbrangeoc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Byte,Parser.Data.Brangeoo), Parser.Data.Bool, "inbrangeoo");
		
		p.requireLibFunction(Arrays.asList(Parser.Data.Uint,Parser.Data.Urangecc), Parser.Data.Bool, "inurangecc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Uint,Parser.Data.Urangeco), Parser.Data.Bool, "inurangeco");
		p.requireLibFunction(Arrays.asList(Parser.Data.Uint,Parser.Data.Urangeoc), Parser.Data.Bool, "inurangeoc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Uint,Parser.Data.Urangeoo), Parser.Data.Bool, "inurangeoo");
		
		p.requireLibFunction(Arrays.asList(Parser.Data.Ubyte,Parser.Data.Ubrangecc), Parser.Data.Bool, "inubrangecc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Ubyte,Parser.Data.Ubrangeco), Parser.Data.Bool, "inubrangeco");
		p.requireLibFunction(Arrays.asList(Parser.Data.Ubyte,Parser.Data.Ubrangeoc), Parser.Data.Bool, "inubrangeoc");
		p.requireLibFunction(Arrays.asList(Parser.Data.Ubyte,Parser.Data.Ubrangeoo), Parser.Data.Bool, "inubrangeoo");
		//p.registerLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.Void, "puts");
		
		
		//architecture specific libraries
		switch(architecture) {
		case TI83pz80:
			break;
		case WINx64:
			break;
		case WINx86:
			break;
		case z80Emulator:
			p.registerLibFunction(Arrays.asList(Parser.Data.Ptr), Parser.Data.File, "fopen");
			p.registerLibFunction(Arrays.asList(Parser.Data.File,Parser.Data.Ubyte), Parser.Data.Void, "fwrite");
			p.registerLibFunction(Arrays.asList(Parser.Data.File), Parser.Data.Void, "fflush");
			p.registerLibFunction(Arrays.asList(Parser.Data.File), Parser.Data.Void, "fclose");
			p.registerLibFunction(Arrays.asList(Parser.Data.File), Parser.Data.Ubyte, "fread");
			p.registerLibFunction(Arrays.asList(Parser.Data.File), Parser.Data.Ubyte, "favail");
			break;
		default:
			break;
			
		}
	}
	public void correct(List<IntermediateLang.Instruction> instructions, Parser p) {
		
		for(int loc = 0;loc < instructions.size(); loc++) {
			IntermediateLang.Instruction instr = instructions.get(loc);
			List<IntermediateLang.Instruction> replacement = new ArrayList<>();
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
			
			if(architecture==CompilationSettings.Target.TI83pz80) {
				p.inlineReplace("putln");
				p.inlineReplace("memcpy");
				p.inlineReplace("putchar");
				p.inlineReplace("strcpy");
				p.inlineReplace("getc");
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
			
			
			
			if(instr.in==IntermediateLang.InstructionType.call_function) {
				switch(instr.args[0]) {
					case "deref_ubyte":
					case "deref_byte":
						replacement = Arrays.asList(IntermediateLang.InstructionType.load_b.cv());
						break;
					case "deref_int":
					case "deref_uint":
					case "deref_ptr":
						replacement = Arrays.asList(IntermediateLang.InstructionType.load_i.cv());
						break;
					case "put_ubyte":
					case "put_byte":
						replacement = Arrays.asList(IntermediateLang.InstructionType.store_b.cv());
						break;
					case "put_int":
					case "put_uint":
					case "put_ptr":
						replacement = Arrays.asList(IntermediateLang.InstructionType.store_i.cv());
						break;
					case "fclose":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then request a close
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,0xc0"),
									IntermediateLang.InstructionType.rawinstruction.cv("or l"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,$82"),//close_constant
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),
									IntermediateLang.InstructionType.pop_discard.cv()
								);
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "fflush":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then request a flush
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,0xc0"),
									IntermediateLang.InstructionType.rawinstruction.cv("or l"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,$81"),//flush_constant
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),
									IntermediateLang.InstructionType.pop_discard.cv()
								);
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "fread":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then read
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,0xc0"),
									IntermediateLang.InstructionType.rawinstruction.cv("or l"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									IntermediateLang.InstructionType.rawinstruction.cv("in a,(1)"),//read_byte
									IntermediateLang.InstructionType.rawinstruction.cv("ld l,a")
								);
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "favail":
						switch(architecture) {
							case z80Emulator:
								replacement = Arrays.asList(//switch to the given descriptor, then read
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,0xc0"),
									IntermediateLang.InstructionType.rawinstruction.cv("or l"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,$83"),//select available mode
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),
									IntermediateLang.InstructionType.rawinstruction.cv("in a,(1)"),//read_byte
									IntermediateLang.InstructionType.rawinstruction.cv("ld l,a")
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
									IntermediateLang.InstructionType.notify_pop.cv(),
									IntermediateLang.InstructionType.rawinstruction.cv("pop de"),
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,0xc0"),
									IntermediateLang.InstructionType.rawinstruction.cv("or e"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),//selected the correct file
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,$80"),//write_constant
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,l"),//write the second arg
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),
									
									IntermediateLang.InstructionType.pop_discard.cv()
								);
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
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,$84"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),//entered file select mode
									IntermediateLang.InstructionType.general_label.cv("__fopen_loop_"+label),
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,(hl)"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (1),a"),
									IntermediateLang.InstructionType.rawinstruction.cv("inc hl"),
									IntermediateLang.InstructionType.rawinstruction.cv("or a"),
									IntermediateLang.InstructionType.rawinstruction.cv("jr nz, __fopen_loop_"+label),
									IntermediateLang.InstructionType.rawinstruction.cv("in a,(1)"),
									IntermediateLang.InstructionType.rawinstruction.cv("ld l,a"),
									IntermediateLang.InstructionType.rawinstruction.cv("ld h,$0")
								);
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "memcpy":
						switch(architecture) {
						case z80Emulator:
						case TI83pz80:
							
							replacement = Arrays.asList(
								IntermediateLang.InstructionType.copy_from_address.cv()
							);
							break;
						default:
							throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
						}
						break;
					case "getc"://block for input
						replacement = Arrays.asList(
								IntermediateLang.InstructionType.getc.cv()
						);
						break;
					case "putchar":
						switch(architecture) {
							case TI83pz80:
								
								replacement = Arrays.asList(
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,l"),
									IntermediateLang.InstructionType.syscall_arg.cv("_PutC")//putchar does not take its argument in hl, but we need to pop it anyway
								);
								break;
							case z80Emulator:
								replacement = Arrays.asList(
									IntermediateLang.InstructionType.rawinstruction.cv("ld a,l"),
									IntermediateLang.InstructionType.rawinstruction.cv("out (0),a"),
									IntermediateLang.InstructionType.pop_discard.cv());
								break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
							}
						break;
					case "putln":
						switch(architecture) {
							case TI83pz80:
								replacement = Arrays.asList(
										IntermediateLang.InstructionType.rawinstruction.cv("ld de,curRow"),//preserve hl and ix
										IntermediateLang.InstructionType.rawinstruction.cv("ld a,(de)"),
										IntermediateLang.InstructionType.rawinstruction.cv("inc a"),
										IntermediateLang.InstructionType.rawinstruction.cv("and $07"),
										IntermediateLang.InstructionType.rawinstruction.cv("ld (de),a"),
										IntermediateLang.InstructionType.rawinstruction.cv("xor a"),
										IntermediateLang.InstructionType.rawinstruction.cv("inc de"),
										IntermediateLang.InstructionType.rawinstruction.cv("ld (de),a")
								);
								break;
							case z80Emulator:
								replacement = Arrays.asList(
										IntermediateLang.InstructionType.rawinstruction.cv("ld a,"+(byte)'\r'),//lol windows
										IntermediateLang.InstructionType.rawinstruction.cv("out (0),a"),
										IntermediateLang.InstructionType.rawinstruction.cv("ld a,"+(byte)'\n'),
										IntermediateLang.InstructionType.rawinstruction.cv("out (0),a"));
									break;
							default:
								throw new UnsupportedOperationException("Compilation to architecture "+architecture+" does not support "+instr.args[0]+" yet.");
							}
						break;
					case "strcpy":
						replacement = Arrays.asList(
								IntermediateLang.InstructionType.strcpy.cv()
						);
					default:
						break;
				}
			}
			if(!replacement.isEmpty()) {
				instructions.remove(loc);
				instructions.addAll(loc,replacement);
			}
		}
	}
	public void loadCompiletimeConstants(BaseTree tree) {
		tree.addConstantValue("heaphead", Parser.Data.Ptr);
		tree.addConstantValue("heap", Parser.Data.Ptr);
		tree.addConstantValue("heaptail", Parser.Data.Ptr);
		tree.addConstantValue("int_size", Parser.Data.Uint);
	}
}
