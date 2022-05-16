package settings;
import java.util.Arrays;
import java.util.List;

import compiler.LibFunctions;

public class CompilationSettings {
	public enum Target {
		REPL("librepl",3,false),
		WINx86("libwin86",4,true),
		WINx64("libwin64",8,true),
		z80Emulator("libemuz80",2,false),
		TI83pz80("libti83pz80",2,false),
		LINx64("liblin64",8,true);
		public final String libLoc;
		public final int intsize;
		public final boolean needsAlignment;
		private Target(String libLoc, int intsize, boolean align) {
			this.libLoc=libLoc;
			this.intsize=intsize;
			this.needsAlignment=align;
		}
		
		
		
		public void addHeader(List<String> instructions) {
			switch(this) {
			case z80Emulator:
				instructions.addAll(0,Arrays.asList(
						";allow equate lines to not require colons",
						"#define ([a-zA-Z_0-9]*)[\\t ]+\\.equ",
						"$1:	.equ",
						"",
						";mnemonics from other assemblers",
						"#define \\.equ",
						"EQU",
						"",
						"#define \\.db",
						"DB",
						"",
						"#define \\.dw",
						"DW",
						"",
						"#define \\.org",
						"ORG",
						"",
						"#define \\.ds",
						"DS",
						"",
						"#define #include",
						"INCLUDE",
						"#define &",
						"AND",
						"",
						"",
						";introduce bcall macro",
						"#define bcall\\((.*?)\\)",
						"DB bcall | DW $1",
						"",
						"#define b_call\\((.*?)\\)",
						"DB bcall | DW $1",
						"bcall					.equ $EF",
						"",
						".org	$0000				;Origin",
						"",
						""));
				break;
			case TI83pz80:
				instructions.addAll(0,Arrays.asList(
						";allow equate lines to not require colons",
						"#define ([a-zA-Z_0-9]*)[\\t ]+\\.equ",
						"$1:	.equ",
						"",
						";mnemonics from other assemblers",
						"#define \\.equ",
						"EQU",
						"",
						"#define \\.db",
						"DB",
						"",
						"#define \\.dw",
						"DW",
						"",
						"#define \\.org",
						"ORG",
						"",
						"#define \\.ds",
						"DS",
						"",
						"#define #include",
						"INCLUDE",
						"#define &",
						"AND",
						"",
						"",
						";introduce bcall macro",
						"#define bcall\\((.*?)\\)",
						"DB bcall | DW $1",
						"",
						"#define b_call\\((.*?)\\)",
						"DB bcall | DW $1",
						"bcall					.equ $EF",
						"",
						"",
						"#include		\"ti83plus.inc\"",
						"#include		\"mirage.inc\"",
						"",
						".org	$9d93				;Origin (set back two to account for AsmPrgm)",
						"	.db	$BB,$6D				;Compiled AsmPrgm token",
						"",
						""));
				break;
			case WINx64:
				instructions.addAll(0,Arrays.asList(
						"Fwf_internal_syscall_0 PROC",
						"	mov r12, rsp",
						"	sub rsp, 32",
						"	and rsp, -16",
						"	call rax",
						"	mov rsp, r12",
						"	ret",
						"Fwf_internal_syscall_0 ENDP",
						"e	equ "+Double.doubleToLongBits(Math.E),
						"pi	equ "+Double.doubleToLongBits(Math.PI),
						"NaN	equ "+ Double.doubleToLongBits(Double.NaN),
						"pos_infinity	equ "+Double.doubleToLongBits(Double.POSITIVE_INFINITY),
						"neg_infinity	equ "+Double.doubleToLongBits(Double.NEGATIVE_INFINITY)
						));
				break;
			case WINx86:
				break;
			case LINx64:
				instructions.addAll(0,Arrays.asList(
						"%include \"linuxasm/unistd_64.asm\"",
						"extern Fwf_internal_linux_syscall",
						"global error",
						"Fwf_internal_syscall_0:",
						"	mov r12, rsp",
						"	and rsp, -16",
						"	call rax",
						"	mov rsp, r12",
						"	ret",
						"e:	equ "+Double.doubleToLongBits(Math.E),
						"pi:	equ "+Double.doubleToLongBits(Math.PI),
						"NaN:	equ "+ Double.doubleToLongBits(Double.NaN),
						"pos_infinity:	equ "+Double.doubleToLongBits(Double.POSITIVE_INFINITY),
						"neg_infinity:	equ "+Double.doubleToLongBits(Double.NEGATIVE_INFINITY)
						));
			}
		}
	}
	public static A setIntByteSize(int len) {
		return new A(len);
	}
	public static class A{
		int size = 0;
		private A(int intsize) {
			size = intsize;
		}
		public C setHeapSpace(int heapSpace) {
			return new C(size, heapSpace);
		}
	}
	public static class C {
		int size;
		int heapspace;
		private C(int sz,int hsp)
		{
			size=sz;
			heapspace=hsp;
		}
		public CompilationSettings useTarget(Target t) {
			return new CompilationSettings(size,heapspace,t,new LibFunctions(t));
		}
	}
	public final int intsize;
	public final int heapSpace;
	public final Target target;
	public final LibFunctions library;
	public CompilationSettings(int size, int heapSpace, Target t, LibFunctions library) {
		intsize = size;
		this.heapSpace = heapSpace;
		target=t;
		this.library=library;
	}
	public LibFunctions getLibrary() {
		return library;
	}
}
