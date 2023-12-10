package settings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import compiler.LibFunctions;
import types.DataType;

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
		
	}
	public void addHeader(List<String> instructions) {
		switch(this.target) {
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
					""));
			for(DataType type:DataType.values()) {
				if(!type.builtin())
					instructions.add(0,type.getHeapSizeString()+": EQU "+type.getActualHeapSize(this));
			}
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
					"	ret					;So TIOS wont run the program",
					"	.db	1				;Identifier as MirageOS program",
					"	.db	%11101000, %00101110		;15x15 button",
					"	.db	%10001000, %00101000",
					"	.db	%11101001, %00101110",
					"	.db	%10000101, %01001000",
					"	.db	%10000010, %10001000",
					"	.db	%00000000, %00000000",
					"	.db	%00000000, %00000000",
					"	.db	%11101110, %00011100",
					"	.db	%10101001, %00100000",
					"	.db	%11101110, %00101110",
					"	.db	%10001001, %00100010",
					"	.db	%10001000, %10011100",
					"	.db	%00000000, %00000000",
					"	.db	%00000000, %00000000",
					"	.db	%00000000, %00000000",
					"	.db	70, 119, 102, 32, 112, 114, 103, 109, 0",
					""));
			for(DataType type:DataType.values()) {
				if(!type.builtin())
					instructions.add(0,type.getHeapSizeString()+": EQU "+type.getActualHeapSize(this));
			}
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
			for(DataType type:DataType.values()) {
				if(!type.builtin())
					instructions.add(0,type.getHeapSizeString()+": equ "+type.getActualHeapSize(this));
			}
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
			for(DataType type:DataType.values()) {
				if(!type.builtin())
					instructions.add(0,type.getHeapSizeString()+": equ "+type.getActualHeapSize(this));
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
	public void addFooter(ArrayList<String> comp) {
		int midiPort = 0;
		switch(target) {
		case LINx64:
			break;
		case REPL:
			break;
		case z80Emulator:
			midiPort = 21;
		case TI83pz80:
			// stack holds [note, &inners, &outers, &durations, beats]
			// basically just a bunch of nested loops with the second to bottom outputing to port 0
			/*
			 * push ix
				ld ix,$0000
				add ix,sp
				
				...
				
				ld sp,ix
				pop ix
				pop bc
				pop de;removing 1 arg
				push bc
				ret
			 */
			comp.add("play_midi_silence:");
			comp.add("	ld a, 0");
			comp.add("	ld (__midi_output_port_num_minus_1 + 1), a");
			comp.add("	jr __play_midi_common");

			comp.add("play_midi_note:");
			comp.add("	ld a, 3");
			comp.add("	ld (__midi_output_port_num_minus_1 + 1), a");
			
			comp.add("__play_midi_common:");
			comp.add("	pop hl");
			comp.add("	ld (__midi_return + 1), hl");
			comp.add("	pop hl");
			comp.add("	ld (__midi_data), hl");
			// stack holds [note, &inners, &outers, &durations]
			comp.add("	ld hl,6");
			comp.add("	add hl,sp");
			comp.add("	ld a,(hl)");
			comp.add("	add a,a");//turn note # into index into int[]
			// a = note. I don't care about note anymore except to pop it off the stack when we return 
			comp.add("	ld c,a");
			comp.add("	ld b,0");
			
			comp.add("	pop hl");
			comp.add("	add hl,bc");
			comp.add("	ld a, (hl)");
			comp.add("	ld (__midi_data+2), a");
			comp.add("	inc hl");
			comp.add("	ld a, (hl)");// de is durations[note]
			comp.add("	ld (__midi_data+3), a");
			
			comp.add("	pop hl");
			comp.add("	add hl,bc");
			comp.add("	ld e, (hl)");
			comp.add("	inc hl");
			comp.add("	ld d, (hl)");// de is outers[note]
			
			comp.add("	pop hl");
			comp.add("	add hl,bc");
			comp.add("	ld c, (hl)");
			comp.add("	inc hl");
			comp.add("	ld b, (hl)");// bc is inners[note]
			comp.add("	ld hl, (__midi_data + 2)");//hl = duration, de = outers, bc = inners
			comp.add("	exx");
			comp.add("	ld a, (__midi_data)");
			comp.add("	ld b,a");//b' = beats
			comp.add("	xor a");
			comp.add("	ex af, af'");//a' = status

			comp.add("__midi_note_loop1:");
			comp.add("	exx");
			comp.add("	push hl");
			comp.add("__midi_note_loop2:");
			comp.add("	push de");
			comp.add("__midi_note_loop3:");
			comp.add("	push bc");
			comp.add("__midi_note_loop4:");
			//for some stupid reason, dec bc,de,hl don't set flags, but the 1-reg instructions do
			comp.add("	dec bc");
			comp.add("	ld a,b");
			comp.add("	or c");
			comp.add("	jp nz, __midi_note_loop4");
			comp.add("	pop bc");
			comp.add("	dec de");
			comp.add("	ld a,d");
			comp.add("	or e");
			comp.add("	jp nz, __midi_note_loop3");
			comp.add("	pop de");
			comp.add("	ex af, af'");
			comp.add("__midi_output_port_num_minus_1:");
			comp.add("	xor $03");
			comp.add("	out ("+midiPort+"), a");
			comp.add("	ex af, af'");
			comp.add("	dec hl");
			comp.add("	ld a,h");
			comp.add("	or l");
			comp.add("	jp nz, __midi_note_loop2");
			comp.add("	pop hl");
			comp.add("	exx");
			comp.add("	djnz __midi_note_loop1");
			comp.add("	exx");
			
			comp.add("__midi_end:");
			comp.add("  pop hl");
			comp.add("__midi_return:");
			comp.add("  jp $0");
			
			comp.add("__midi_data:");
			comp.add("  .dw 0");//beats
			comp.add("  .dw 0");//duration
			comp.add("  .dw 0");//outer
			comp.add("  .dw 0");//inner
			break;
		case WINx64:
			break;
		case WINx86:
			break;
		default:
			break;
		
		}
	}
}
