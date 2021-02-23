import java.util.Arrays;
import java.util.List;

public class CompilationSettings {
	enum Target {
		WINx86("libwin86"),
		WINx64("libwin64"),
		z80Emulator("libemuz80"),
		TI83pz80("libti83pz80");
		public final String libLoc;
		private Target(String libLoc) {
			this.libLoc=libLoc;
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
				break;
			case WINx86:
				break;
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
