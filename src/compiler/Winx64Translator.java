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
		
		boolean debug = false;
		
		
		ArrayList<String> comp = new ArrayList<String>();
		ArrayList<String> externs = new ArrayList<String>();
		ArrayList<String> data = new ArrayList<String>();
		data.add("__mxmode:");
		data.add("	DD 01111111111000000b");
		HashMap<String,Integer> depths = new HashMap<String,Integer>();
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
				depths.put(args[0],stackDepth);
				break;
			case branch_not_address:
				comp.addAll(Arrays.asList(
						"	or al, al",
						stackDepth>1?"	pop rax":"	",
						"	jz "+args[0]));
				stackDepth--;
				depths.put(args[0],stackDepth);
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
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov rax, 0",
						"	mov bx, 255",
						"	movsd xmm1, QWORD PTR [rsp]",
						"	comisd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]",
						"	cmove ax, bx",
						"	add rsp, 16"));
				stackDepth--;
				break;
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
				{
					comp.add("__main PROC");
					comp.add("	ldmxcsr DWORD PTR [__mxmode]");
				}
				else
				{
					if(depths.containsKey(args[0]))
						stackDepth = depths.get(args[0]);
					comp.add(args[0]+":");
				}
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
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov rax, 0",
						"	mov bx, 255",
						"	movsd xmm1, QWORD PTR [rsp]",
						"	comisd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]",
						"	cmovbe ax, bx",
						"	add rsp, 16"));
				stackDepth--;
				break;
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
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov rax, 0",
						"	mov bx, 255",
						"	movsd xmm1, QWORD PTR [rsp]",
						"	comisd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]",
						"	cmovb ax, bx",
						"	add rsp, 16"));
				stackDepth--;
				break;
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
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov rax, 0",
						"	mov bx, 255",
						"	movsd xmm1, QWORD PTR [rsp]",
						"	comisd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]",
						"	cmovae ax, bx",
						"	add rsp, 16"));
				stackDepth--;
				break;
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
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov rax, 0",
						"	mov bx, 255",
						"	movsd xmm1, QWORD PTR [rsp]",
						"	comisd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]",
						"	cmova ax, bx",
						"	add rsp, 16"));
				stackDepth--;
				break;
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
				comp.add("	mov al, "+args[0]);
				break;
			case overwrite_immediate_float:
				comp.add("	mov rax, "+Double.doubleToRawLongBits(Double.parseDouble(args[0])));
				break;
			case overwrite_immediate_int:
				comp.add("	mov rax, "+args[0]);
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
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, "+Double.doubleToRawLongBits(Double.parseDouble(args[0])));
				break;
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
				comp.add("	xor rax, rax");
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
				comp.add("	xor rax, rax");
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
				comp.add("	add rax, rbx");
				stackDepth--;
				break;
			case stackaddfloat:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	movsd xmm1, QWORD PTR [rsp]",
						"	addsd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]",//add second arg to first
						"	movsd QWORD PTR [rsp], xmm1",
						"	pop rax",
						"	pop rbx"));
				stackDepth--;
				break;
			case stackand:
				comp.add("	pop rbx");
				comp.add("	and rax, rbx");
				stackDepth--;
				break;
			case stackconvertbtofloat:
				comp.add("	movsx rax, al");
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	movsd QWORD PTR [rsp], xmm1");
				comp.add("	pop rax");
				break;
			case stackconverttofloat:
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	movsd QWORD PTR [rsp], xmm1");
				comp.add("	pop rax");
				break;
			case stackconverttoint:
				comp.add("	push rax");
				comp.add("	movsd xmm1, QWORD PTR [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				break;
			case stackconverttobyte:
				comp.add("	push rax");
				comp.add("	movsd xmm1, QWORD PTR [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				comp.add("	and rax, 255");
				break;
			case stackconverttoubyte:
				comp.add("	push rax");
				comp.add("	movsd xmm1, QWORD PTR [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				comp.add("	and rax, 255");
				break;
			case stackconverttouint:
				comp.add("	push rax");
				comp.add("	movsd xmm1, QWORD PTR [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				break;
			case stackconvertubtofloat:
				comp.add("	and rax, 255");
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	movsd QWORD PTR [rsp], xmm1");
				comp.add("	pop rax");
				break;
			case stackconvertutofloat:
				//lose a bit of precision
				comp.add("	shr rax, 1");
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	addsd xmm1, xmm1");
				comp.add("	movsd QWORD PTR [rsp], xmm1");
				comp.add("	pop rax");
				break;
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
				comp.add("	test rax, rax");
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
				comp.add("	test al, al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	mov ah, 0");
				comp.add("	xchg rax, rbx");
				comp.add("	div bl");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov ah, 0");
				break;
			case stackmod_signed:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	mov rcx, rbx");
				comp.add("	sar rcx, 63");
				comp.add("	mov rdx, rcx");
				comp.add("	mov rdi, rax");
				comp.add("	sar rdi, 63");
				comp.add("	test rax, rax");
				comp.add("	jz __div_by_0"+label);
				
				comp.add("	xchg rax, rbx");
				comp.add("	idiv rbx");
				//if the numerator and denominator have mismatching signs, add the denom
				comp.add("	cmp rcx, rdi");
				comp.add("	mov rcx, 0");
				comp.add("	cmovne rcx, rbx");
				comp.add("	add rdx, rcx");

				comp.add("__div_by_0"+label+":");
				comp.add("	mov rax, rdx");
				break;
			case stackmod_signed_b:
				stackDepth--;
				label = fresh();
				comp.add("	pop rbx");
				comp.add("	test al, al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	movsx bx, bl");
				comp.add("	mov dl, bh");//dl holds one sign
				comp.add("	mov cl, al");
				comp.add("	sar cl, 7");
				comp.add("	xchg rax, rbx");
				comp.add("	idiv bl");
				comp.add("	mov al, ah");
				comp.add("	mov ah, 0");
				//if the numerator and denominator have mismatching signs, add the denom
				comp.add("	cmp cl, dl");
				comp.add("	mov cl, 0");
				comp.add("	cmovne cx, bx");
				comp.add("	add al,cl");

				comp.add("__div_by_0"+label+":");
				break;
			case stackdiv_signed:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test rax, rax");
				comp.add("	jz __div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	mov rcx, -1");
				comp.add("	bt rbx, 63");
				comp.add("	cmovc rdx, rcx");
				comp.add("	xchg rax, rbx");
				comp.add("	idiv rbx");
				comp.add("__div_by_0"+label+":");
				break;
			case stackdiv_signed_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test al, al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	movsx bx, bl");
				comp.add("	xchg rax, rbx");
				comp.add("	idiv bl");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov ah, 0");
				break;
			case stackmod_unsigned_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test al, al");
				comp.add("	jz __div_by_0"+label);
				comp.add("	mov ah, 0");
				comp.add("	xchg rax, rbx");
				comp.add("	div bl");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov al, ah");
				comp.add("	mov ah, 0");
				break;
			case stackmod_unsigned:
				stackDepth--;
				label=fresh();
				comp.add("	pop rbx");
				comp.add("	test rax, rax");
				comp.add("	jz __div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	xchg rax, rbx");
				comp.add("	div rbx");
				comp.add("__div_by_0"+label+":");
				comp.add("	mov rax, rdx");
				break;
			case stackdivfloat:
				comp.add("	push rax");
				comp.add("	movsd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]");
				comp.add("	divsd xmm1, QWORD PTR [rsp]");
				comp.add("	movsd QWORD PTR [rsp], xmm1");
				comp.add("	pop rax");
				comp.add("	pop rbx");
				stackDepth--;
				break;
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
				comp.add("	push rax");
				comp.add("	movsd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]");// numerator
				comp.add("	divsd xmm1, QWORD PTR [rsp]");//denominator
				//xmm1 holds quotient. We need to round this 
				comp.add("	roundsd xmm1, xmm1, 1");//1 specifies rounding towards -inf
				comp.add("	mulsd xmm1, QWORD PTR [rsp]");//multiply by denom
				//subtract numerator - the result (xmm1)
				comp.add("	movapd xmm2, xmm1");
				comp.add("	movsd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]");
				comp.add("	subsd xmm1, xmm2");
				comp.add("	movsd QWORD PTR [rsp], xmm1");
				comp.add("	pop rax");
				comp.add("	pop rbx");
				stackDepth--;
				break;
			case stackmult:
				stackDepth--;
				comp.add("	pop rbx");
				comp.add("	mul rbx");//much better than the z80 version
				break;
			case stackmultfloat:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	movsd xmm1, QWORD PTR [rsp]",
						"	mulsd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]",//mult second arg to first
						"	movsd QWORD PTR [rsp], xmm1",
						"	pop rax",
						"	pop rbx"));
				stackDepth--;
				break;
			case stackneg:
				comp.add("	neg rax");
				break;
			case stacknegbyte:
				comp.add("	neg al");
				break;
			case stacknegfloat:
				comp.add("	btc rax, 63");//flip the sign bit
				break;
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
				comp.add("	push rax");
				comp.add("	movsd xmm1, QWORD PTR [rsp+"+p.settings.intsize+"]");
				comp.add("	subsd xmm1, QWORD PTR [rsp]");
				comp.add("	movsd QWORD PTR [rsp], xmm1");
				comp.add("	pop rax");
				comp.add("	pop rbx");
				stackDepth--;
				break;
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
				comp.add("	mov rdx, QWORD PTR [rsp]");
				comp.add("	xchg rdx, QWORD PTR [rsp+8]");
				comp.add("	mov QWORD PTR [rsp], rdx");
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
				comp.add("	mov rbp, QWORD PTR ["+args[0]+"+8]");
				comp.add("	ret");
				comp.add("error ENDP");
				break;
			case exit_global:
				comp.add("	mov rsp, QWORD PTR ["+args[0]+"]");
				comp.add("	mov rbp, QWORD PTR ["+args[0]+"+8]");
				comp.add("	ret");
				stackDepth--;
				break;
			case write_sp:
				comp.add("	mov QWORD PTR ["+args[0]+"], rsp");
				comp.add("	mov QWORD PTR ["+args[0]+"+8], rbp");
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
		comp.add("	mov rax, 0");
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
				for(String regs:"rax rbx rcx rdx r8 r9 r10".split(" "))
				if(comp.get(i).equals("	push "+regs) && comp.get(i+1).equals("	pop "+regs)) {
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
			for(int i=0;i<comp.size()-2;i++) {
				//System.out.println(comp.get(i));
				if(!comp.get(i).startsWith("\tret"))//make the compiler faster, lol
					continue;
				if(comp.get(i).startsWith("\tret") && comp.get(i+1).endsWith("ENDP") && (comp.get(i+2).startsWith("\tret")||comp.get(i+2).startsWith("\tjmp"))) {
					comp.remove(i+2);
				}
			}
			for(int i=0;i<comp.size()-1;i++) {
				if(!comp.get(i).startsWith("\tret"))
					continue;
				if(comp.get(i).startsWith("\tret") && (comp.get(i+1).startsWith("\tret")||comp.get(i+1).startsWith("\tjmp"))) {
					comp.remove(i+1);
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
