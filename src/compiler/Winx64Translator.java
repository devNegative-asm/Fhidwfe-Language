package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
public class Winx64Translator {
	int counter = 0;
	private int fresh() {
		return counter++;
	}
	public ArrayList<String> translateWin64(List<IntermediateLang.Instruction> instructions, boolean useDSNotation, int stackDepth, Parser p) {
		
		boolean debug = true;
		
		
		ArrayList<String> comp = new ArrayList<String>();
		ArrayList<String> externs = new ArrayList<String>();
		ArrayList<String> data = new ArrayList<String>();
		p.settings.target.addHeader(comp);
		String fndef = "";
		for(IntermediateLang.Instruction instruction:instructions) {
			if(debug) {
				System.out.println(instruction);
			}
			String[] args = instruction.args;
			switch(instruction.in) {
			case data_label:
				data.add(args[0]+":");
				break;
			case notify_stack:
				stackDepth = Integer.parseInt(args[0]);
				break;
			case notify_pop:
				stackDepth--;
				break;
			case branch_address:
				comp.addAll(Arrays.asList(
						"	test al, al",
						stackDepth>1?"	pop rax":"	",
						"	jnz "+args[0]));
				stackDepth--;
				break;
			case branch_not_address:
				comp.addAll(Arrays.asList(
						"	or al, al",
						stackDepth>1?"	pop rax":"	",
						"	jz "+args[0]));
				stackDepth--;
				break;
			case strcpy:
				//arguments on stack: dest
				//arg in rax: src
				int label = fresh();
				comp.add("	cld");
				comp.add("	pop rdi");
				comp.add("	ld rsi, rax");
				comp.add("__strcpy_"+label+"_imp:");
				comp.add("	lodsb");
				comp.add("	test %al, %al");
				comp.add("	stosb");
				comp.add("	jnz __strcpy_"+label+"_imp");
				comp.add("	mov rax, rdi");
				comp.add("	dec rax");
				stackDepth--;
				break;
			case call_function:
				
				if(p.getFunctionOutputType(args[0]).get(0)==Parser.Data.Void) {
					comp.add("	push rax");//save last value of stack
					comp.add("	call "+args[0]);//call the function
					//result is unused
					stackDepth-=p.getFunctionInputTypes(args[0]).get(0).size();
					//if we consumed all our arguments and there's still something left, pop it
					if(stackDepth>0)
						comp.add("	pop rax");
				} else {
					comp.add("	push rax");//save last value of stack
					comp.add("	call "+args[0]);//call the function
					stackDepth-=p.getFunctionInputTypes(args[0]).get(0).size();
					//we actually do use the result this time
					stackDepth++;
				}
				break;
			case copy:
				comp.add("	push rax");
				stackDepth++;
				break;
			case copy2:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	push rbx",
						"	push rax",
						"	push rbx"));
				stackDepth+=2;
				break;
			case copy_from_address:
				//stack is [dest] [src] [n]
				comp.add("	cld");
				comp.addAll(Arrays.asList(
						"	pop rsi",
						"	pop rdi",
						"	mov rcx, rax",
						"	rep movsb"));
				stackDepth-=3;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case decrement_by_pointer_b:
				comp.addAll(Arrays.asList(
						"	dec BYTE PTR [rax]"));
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case decrement_by_pointer_i:
				label = fresh();
				comp.addAll(Arrays.asList(
						"	dec QWORD PTR [rax]"));
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case define_symbolic_constant:
				comp.add(""+args[0]+"\tequ "+args[1]);
				break;
			case enter_function:
				int spaceNeeded = Integer.parseInt(args[0]);
				comp.addAll(Arrays.asList(
						"	push rbp",
						"	mov rbp, rsp",
						"	sub rsp, "+spaceNeeded));
				if(stackDepth!=0)
					throw new RuntimeException("@@contact devs. Unknown stack-related error while translating "+"stack depth not 0 before function def "+args[0]);
				stackDepth=0;
				break;
			case enter_routine:
				throw new RuntimeException("@@contact devs. Routines not available on x64");
			case equal_to_b:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp al, bl",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmove ax, bx"));
				stackDepth--;
				break;
			case equal_to_f:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case equal_to_i:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rax, rbx",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmove ax, bx"));
				stackDepth--;
				break;
			case exit_function:
				int spaceRemoved = Integer.parseInt(args[0]);
				comp.addAll(Arrays.asList(
					"	leave",
					"	ret "+spaceRemoved));
				comp.add(fndef+" ENDP");
				stackDepth=0;
				break;
			case exit_routine:
				throw new RuntimeException("@@contact devs. Routines not available on x64");
			case function_label:
				comp.add(args[0]+" PROC");
				fndef = args[0];
				break;
			case general_label:
				if(args[0].equals("__main"))
					comp.add("__main PROC");
				else
					comp.add(args[0]+":");
				break;
			case goto_address:
				comp.add("	jmp "+args[0]);
				break;
			case greater_equal_b:
				// add 0x80 (or xor 0x80) with both registers
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovge ax, bx"));
				stackDepth--;
				break;
			case greater_equal_f:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case greater_equal_i:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovge ax, bx"));
				stackDepth--;
				break;
			case greater_than_b:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovg ax, bx"));
				stackDepth--;
				break;
			case greater_equal_ub:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovae ax, bx"));
				stackDepth--;
				break;
			case greater_equal_ui:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovae ax, bx"));
				stackDepth--;
				break;
			case greater_than_f:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case greater_than_i:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovg ax, bx"));
				stackDepth--;
				break;
			case greater_than_ub:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmova ax, bx"));
				stackDepth--;
				break;
			case greater_than_ui:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmova ax, bx"));
				stackDepth--;
				break;
			case increment_by_pointer_b:
				comp.add("	inc BYTE PTR [rax]");
				if(--stackDepth!=0)
					comp.add("	pop rax");
				break;
			case increment_by_pointer_i:
				label = fresh();
				comp.add("	inc QWORD PTR [rax]");
				if(--stackDepth!=0)
					comp.add("	pop rax");
				break;
			case less_equal_b:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovle ax, bx"));
				stackDepth--;
				break;
			case less_equal_f:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case less_equal_i:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovle ax, bx"));
				stackDepth--;
				break;
			case less_equal_ub:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovbe ax, bx"));
				stackDepth--;
				break;
			case less_equal_ui:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovbe ax, bx"));
				stackDepth--;
				break;
			case less_than_b:
				// add 0x80 (or xor 0x80) with both registers
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovl ax, bx"));
				stackDepth--;
				break;
			case less_than_f:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case less_than_i:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovl ax, bx"));
				stackDepth--;
				break;
			case less_than_ub:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp bl, al",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovb ax, bx"));
				stackDepth--;
				break;
			case less_than_ui:
				comp.addAll(Arrays.asList(
						"	pop rbx",
						"	cmp rbx, rax",
						"	mov ax, 0",
						"	mov bx, 255",
						"	cmovb ax, bx"));
				stackDepth--;
				break;
			case load_b:
				comp.add("	mov al, BYTE PTR [rax]");
				break;
			case load_i:
				comp.add("	mov rax, QWORD PTR [rax]");
				break;
			case nop:
				comp.add("	nop");//a very useful instruction
				break;
			case overwrite_immediate_byte:
				comp.add("	mov al,"+args[0]);
				break;
			case overwrite_immediate_float:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case overwrite_immediate_int:
				comp.add("	mov rax,"+args[0]);
				break;
			case pop_discard:
				if(stackDepth>1)
				{
					comp.add("	pop rax");
					stackDepth--;
				} else if(stackDepth==1) {
					stackDepth--;
				}
				break;
			case put_global_byte:
				try {
					Integer.parseInt(args[0]);
					comp.add("	mov BYTE PTR [__globals+"+args[0]+"], al");
				} catch(NumberFormatException e) {
					comp.add("	mov BYTE PTR ["+args[0]+"], al");
				}
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_global_int:
				try {
					Integer.parseInt(args[0]);
					comp.add("	mov QWORD PTR [__globals+"+args[0]+"], rax");
				} catch(NumberFormatException e) {
					comp.add("	mov QWORD PTR ["+args[0]+"], rax");
				}
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_local_byte:
				comp.add("	mov BYTE PTR [rbp"+args[0]+"], al");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_local_int:
				comp.add("	mov QWORD PTR [rbp"+args[0]+"], rax");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_param_byte://not a good idea to use these instructions, but they're here if you want them
				comp.add("	mov BYTE PTR [rbp+"+args[0]+"], al");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_param_int:
				comp.add("	mov QWORD PTR [rbp+"+args[0]+"], rax");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case raw:
				if(args[0].startsWith("$"))
					data.add("	DB "+(args[0].length()==2?"0":"")+args[0].substring(1)+"h");
				else
					data.add("	DB "+args[0]);
				break;
			case rawinstruction:
				comp.add("	"+args[0]);
				break;
			case rawint:
				if(args[0].startsWith("$"))
					data.add("	DQ "+args[0].substring(1)+"h");
				else
					data.add("	DQ "+args[0]);
				break;
			case rawspace:
				if(useDSNotation)
					data.add("	TIMES "+args[0]+" DB 0 ");
				else
					for(int i=0;i<Integer.parseInt(args[0]);i++) {
						data.add("	DB 0");
					}
				break;
			case rawspaceints:
				if(useDSNotation)
					data.add("	TIMES "+args[0]+" DQ 0 ");
				else
					for(int i=0;i<Integer.parseInt(args[0]);i++) {
						data.add("	DQ 0");
					}
				break;
			case retrieve_global_address:
				if(stackDepth++>0)
					comp.add("	push rax");
				try {
					Integer.parseInt(args[0]);
					comp.add("	lea rax, __globals+"+args[0]);
				} catch(NumberFormatException e) {
					comp.add("	lea rax, "+args[0]);
				}
				break;
			case retrieve_global_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	xor rax, rax");
				try {
					Integer.parseInt(args[0]);
					comp.add("	mov al, BYTE PTR [__globals+"+args[0]+"]");
				} catch(NumberFormatException e) {
					comp.add("	mov al, BYTE PTR ["+args[0]+"]");
				}
				break;
			case retrieve_global_int:
				if(stackDepth++>0)
					comp.add("	push rax");
				try {
					Integer.parseInt(args[0]);
					comp.add("	mov rax, QWORD PTR [__globals+"+args[0]+"]");
				} catch(NumberFormatException e) {
					comp.add("	mov rax, QWORD PTR ["+args[0]+"]");
				}
				break;
			case retrieve_immediate_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, 255 AND "+args[0]);
				break;
			case retrieve_immediate_float:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case retrieve_immediate_int:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, "+args[0]);
				break;
			case getc:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	lea rax, __getc");
				comp.add("	call __syscall_0");
				if(!externs.contains("EXTERN __getc:PROC")) {
					externs.add("EXTERN __getc:PROC");
				}
				break;
			case retrieve_local_address:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	lea rax, QWORD PTR [rbp"+args[0]+"]");
				break;
			case retrieve_local_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	xor rax,rax");
				comp.add("	mov al, BYTE PTR [rbp"+args[0]+"]");
				break;
			case retrieve_local_int:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, QWORD PTR [rbp"+args[0]+"]");
				break;
			case retrieve_param_address:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	lea rax, QWORD PTR [rbp+"+args[0]+"]");
				break;
			case retrieve_param_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	xor rax,rax");
				comp.add("	mov al, BYTE PTR [rbp+"+args[0]+"]");
				break;
			case retrieve_param_int:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, QWORD PTR [rbp+"+args[0]+"]");
				break;
			case signextend:
				label = fresh();
				comp.add("	movsx rax, al");
				break;
			case stackadd:
				comp.add("	pop rbx");
				comp.add("	add rax,rbx");
				stackDepth--;
				break;
			case stackaddfloat:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case stackand:
				comp.add("	pop rbx");
				comp.add("	and rax,rbx");
				stackDepth--;
				break;
			case stackconvertbtofloat:
			case stackconverttobyte:
			case stackconverttofloat:
			case stackconverttoint:
			case stackconverttoubyte:
			case stackconverttouint:
			case stackconvertubtofloat:
			case stackconvertutofloat:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case stackcpl:
				comp.add("	not rax");
				break;
			case stackdecrement:
				comp.add("	dec rax");
				break;
			case stackdecrement_byte:
				comp.add("	dec al");
				break;
			case stackdecrement_intsize:
				comp.add("	sub rax, "+p.settings.intsize);
				break;
			case stackdecrement_intsize_byte:
				comp.add("	sub al, "+p.settings.intsize);
				break;
			case stackdiv_unsigned://technically we have defined division by 0, so this can no longer fault
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test rax,rax");
				comp.add("	jz __div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	xchg rax, rbx");
				comp.add("	div rbx");
				comp.add("__div_by_0"+label+":");
				break;
			case stackdiv_unsigned_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test al,al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	mov ah,0");
				comp.add("	xchg rax, rbx");
				comp.add("	div bl");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov ah,0");
				break;
			case stackmod_signed:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test rax,rax");
				comp.add("	jz __div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	xchg rax, rbx");
				comp.add("	idiv rbx");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov rax, rdx");
				break;
			case stackmod_signed_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test al,al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	mov ah,0");
				comp.add("	xchg rax, rbx");
				comp.add("	idiv bl");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov al,ah");
				comp.add("	mov ah,0");
				break;
			case stackdiv_signed:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test rax,rax");
				comp.add("	jz __div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	xchg rax, rbx");
				comp.add("	idiv rbx");
				comp.add("__div_by_0"+label+":");
				break;
			case stackdiv_signed_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test al,al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	mov ah,0");
				comp.add("	xchg rax, rbx");
				comp.add("	idiv bl");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov ah,0");
				break;
			case stackmod_unsigned_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test al,al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	mov ah,0");
				comp.add("	xchg rax, rbx");
				comp.add("	div bl");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov al,ah");
				comp.add("	mov ah,0");
				break;
			case stackmod_unsigned:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test rax,rax");
				comp.add("	jz __div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	xchg rax, rbx");
				comp.add("	div rbx");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov rax, rdx");
				break;
			case stackdivfloat:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case stackincrement:
				comp.add("	inc rax");
				break;
			case stackincrement_byte:
				comp.add("	inc al");
				break;
			case stackincrement_intsize:
				comp.add("	add rax, "+p.settings.intsize);
				break;
			case stackincrement_intsize_byte:
				comp.add("	add al, "+p.settings.intsize);
				break;
			case stackmodfloat:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case stackmult:
				stackDepth--;
				comp.add("	pop rbx");
				comp.add("	mul rbx");//much better than the z80 version
				break;
			case stackmultfloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stackneg:
				comp.add("	neg rax");
				break;
			case stacknegbyte:
				comp.add("	neg al");
				break;
			case stacknegfloat:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case stacknot:
				comp.add("	not al");
				break;
			case stackor:
				comp.add("	pop rbx");
				comp.add("	or rax, rbx");
				stackDepth--;
				break;
			case stacksub:
				comp.add("	pop rbx");
				comp.add("	xchg rax, rbx");
				comp.add("	sub rax, rbx");
				stackDepth--;
				break;
			case stacksub_opposite_order:
				comp.add("	pop rbx");
				comp.add("	sub rax, rbx");
				stackDepth--;
				break;
			case stacksubfloat:
				throw new UnsupportedOperationException("x64 does not support floating point yet");
			case stackxor:
				comp.add("	pop rbx");
				comp.add("	xor rax, rbx");
				stackDepth--;
				break;
			case store_b:
				comp.add("	pop rbx");
				comp.add("	mov BYTE PTR [rbx], al");
				stackDepth-=2;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case store_i:
				comp.add("	pop rbx");
				comp.add("	mov QWORD PTR [rbx], rax");
				stackDepth-=2;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case swap12:
				comp.add("	xchg QWORD PTR [rsp], rax");
				break;
			case swap13:
				comp.add("	xchg QWORD PTR [rsp+8], rax");//this is so much easier
				break;
			case swap23:
				comp.add("	pop r9");
				comp.add("	pop r10");
				comp.add("	push r9");
				comp.add("	push r10");
				break;
			case syscall_noarg:
				comp.add("	mov rax, "+args[0]+"");
				comp.add("	call __syscall_0");
				if(!externs.contains("EXTERN "+args[0]+":PROC")) {
					externs.add("EXTERN "+args[0]+":PROC");
				}
				break;
			case syscall_arg:
				throw new RuntimeException("@@contact devs. syscall system not used on x64");
			case syscall_2arg:
				throw new RuntimeException("@@contact devs. syscall system not used on x64");
			case truncate:
				comp.add("	and rax, 255");
				break;
			case shift_left_b:
				comp.add("	sal al, 1");
				break;
			case shift_left_i:
				comp.add("	sal rax, 1");
				break;
			case shift_right_b:
				comp.add("	sar al, 1");
				break;
			case shift_right_i:
				comp.add("	sar rax, 1");
				break;
			case shift_right_ub:
				comp.add("	shr al, 1");
				break;
			case shift_right_ui:
				comp.add("	shr rax, 1");
				break;
			case exit_noreturn:
				comp.add("	mov rsp, QWORD PTR ["+args[0]+"]");
				comp.add("	ret");
				comp.add("error ENDP");
				break;
			case write_sp:
				comp.add("	mov QWORD PTR ["+args[0]+"], rsp");
				break;
			case fix_index:
				comp.add("	shl rax, 3");
				break;
			}
			cache=comp;
			if(stackDepth<0)
				throw new RuntimeException("@@contact devs. Unknown stack-related error while translating");
			if(debug)
				comp.add(";;;;"+stackDepth);
			if(debug)
				System.out.println(";;;;"+stackDepth);
		}
		if(stackDepth!=0)
		{
			throw new RuntimeException("@@contact devs. Unknown stack-related error while translating");
		}
		comp.add("	ret");
		
		//slight optimization
		final boolean optimize = !debug;
		
		if(optimize) {
			// rax specifically we can do this because we won't have to discard the top of the stack and duplicate what's beneath
			for(int i=0;i<comp.size()-2;i++) {
				if("	pop rax".equals(comp.get(i)) && "	push rax".equals(comp.get(i+1)) && (comp.get(i+2).contains("mov rax")||comp.get(i+2).contains("mov al"))) {
					comp.remove(i);
					comp.remove(i);
					i--;
				}
			}
			// The only side effect of pop push is loading the top stack value into a register pair. If we immediately change it, there was no use.
			for(int i=0;i<comp.size()-2;i++) {
				for(String regs:"rax rbx rcx rdx r8 r9 r10".split(" "))
				if(("	pop "+regs).equals(comp.get(i)) && ("	push "+regs).equals(comp.get(i+1)) && (comp.get(i+2).contains("lea "+regs)||comp.get(i+2).contains("mov "+regs))) {
					comp.remove(i);
					comp.remove(i);
					i--;
				}
			}
			//push pop has the side effect of altering the space above the stack. According to our calling convention, this is unnecessary in any circumstance
			for(int i=0;i<comp.size()-2;i++) {
				for(String regpair:"rax rbx rcx rdx r8 r9 r10".split(" "))
				if(comp.get(i).equals("	push "+regpair) && comp.get(i+1).equals("	pop "+regpair)) {
					comp.remove(i);
					comp.remove(i);
					i--;
				}
			}
			
			String[] likelyLoadPositions = "rax rbx rcx rdx al bl [rbp+16] [rbp+24] [rbp+32] [rbp-8] [rbp-16] [rbp-24] [rax] [rbx]".split(" ");
			
			// I have seen circumstances like ld a,l    ld l,a   come up in compiled code.
			
			for(int iter=0;iter<2;iter++)//this is a very slow loop, best not to run it more than twice.
			{
				for(int i=0;i<comp.size()-2;i++) {
					if(!comp.get(i).startsWith("	mov"))//make the compiler faster, lol
						continue;
					for(String reg1:likelyLoadPositions)
						for(String reg2:likelyLoadPositions)
							if(comp.get(i).equals("	mov "+reg1+", "+reg2) && comp.get(i+1).equals("	mov "+reg2+", "+reg1)) {
								comp.remove(i+1);
							}
				}
			}
			
		}
		//not an optimization, this is just getting the assembler to stop complaining
		ArrayList<Integer> removeLocations = new ArrayList<Integer>();
		HashMap<String,Integer> lastExit = new HashMap<>();
		for(int i=0;i<comp.size();i++) {
			if(comp.get(i).endsWith(" ENDP")) {
				if(lastExit.containsKey(comp.get(i))) {
					removeLocations.add(lastExit.get(comp.get(i)));
				}
				lastExit.put(comp.get(i), i);
			}
		}
		for(int i=removeLocations.size()-1;i>=0;i--) {
			comp.remove(removeLocations.get(i).intValue());//remove redundant (incorrect) ENDP labels
		}
		externs.add(".DATA");
		externs.addAll(data);
		externs.add(".CODE");
		comp.addAll(0, externs);
		comp.add("__main ENDP");
		comp.add("END");
		return comp;
	}
	ArrayList<String> cache;
}
