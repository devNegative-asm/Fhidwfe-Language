package translators;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import compiler.Instruction;
import compiler.Parser;
import types.DataType;
public class Unix64Translator {
	int counter = 0;
	private int fresh() {
		return counter++;
	}
	public ArrayList<String> translate(List<Instruction> instructions, boolean useDSNotation, int stackDepth, Parser p) {
		
		boolean debug = false;
		
		
		ArrayList<String> comp = new ArrayList<String>();
		ArrayList<String> externs = new ArrayList<String>();
		ArrayList<String> data = new ArrayList<String>();
		data.add("Fwf_internal_mxmode:");
		data.add("	dd 01111111111000000b");
		HashMap<String,Integer> depths = new HashMap<String,Integer>();
		p.getSettings().target.addHeader(comp);
		for(Instruction instruction:instructions) {
			if(debug) {
				System.out.println(instruction);
			}
			String[] args = instruction.getArgs();
			String[] orig = Arrays.copyOf(args, args.length);
			for(int i=0;i<args.length;i++) {
				if(args[i].matches("-?[0-9]*"))
					continue;
				if(args[i].matches("-?[0-9]*\\.[0-9]+"))
					continue;
				if(args[i].matches("-?[0-9]+\\.[0-9]*"))
					continue;
				if(p.isSymbol(args[i]))
					continue;
				if(args[i].startsWith("Fwf_internal")||args[i].equals("error"))
					continue;
				args[i] = "Fwf_us_"+args[i];
				
			}
			
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
				depths.put(orig[0],stackDepth);
				break;
			case branch_not_address:
				comp.addAll(Arrays.asList(
						"	or al, al",
						stackDepth>1?"	pop rax":"	",
						"	jz "+args[0]));
				stackDepth--;
				depths.put(orig[0],stackDepth);
				break;
			case strcpy:
				//arguments on stack: dest
				//arg in rax: src
				int label = fresh();
				comp.add("	cld");
				comp.add("	mov r8, rdi");
				comp.add("	mov r9, rsi");
				comp.add("	pop rdi");
				comp.add("	mov rsi, rax");
				comp.add("Fwf_internal_strcpy_"+label+"_imp:");
				comp.add("	lodsb");
				comp.add("	test al, al");
				comp.add("	stosb");
				comp.add("	jnz Fwf_internal_strcpy_"+label+"_imp");
				comp.add("	mov rax, rdi");
				comp.add("	dec rax");
				comp.add("	mov rdi, r8");
				comp.add("	mov rsi, r9");
				stackDepth--;
				break;
			case call_function:
				
				if(p.getFunctionOutputType(orig[0]).get(0)==DataType.Void) {
					if(stackDepth>0||p.getFunctionInputTypes(orig[0]).get(0).size()>0)
						comp.add("	push rax");//save last value of stack
					comp.add("	call "+args[0]);//call the function
					//result is unused
					stackDepth-=p.getFunctionInputTypes(orig[0]).get(0).size();
					//if we consumed all our arguments and there's still something left, pop it
					if(stackDepth>0)
						comp.add("	pop rax");
				} else {
					if(stackDepth>0||p.getFunctionInputTypes(orig[0]).get(0).size()>0)
						comp.add("	push rax");//save last value of stack
					comp.add("	call "+args[0]);//call the function
					stackDepth-=p.getFunctionInputTypes(orig[0]).get(0).size();
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
						"	pop rcx",
						"	push rcx",
						"	push rax",
						"	push rcx"));
				stackDepth+=2;
				break;
			case copy_from_address:
				//stack is [dest] [src] [n]
				comp.add("	cld");
				comp.addAll(Arrays.asList(
						"	mov r10, rsi",
						"	mov r11, rdi",
						"	pop rsi",
						"	pop rdi",
						"	mov rcx, rax",
						"	rep movsb",
						"	mov rsi, r10",
						"	mov rdi, r11"));
				stackDepth-=3;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case decrement_by_pointer_b:
				comp.addAll(Arrays.asList(
						"	dec [rax]"));
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case decrement_by_pointer_i:
				label = fresh();
				comp.addAll(Arrays.asList(
						"	dec [rax]"));
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case define_symbolic_constant:
				comp.add(""+orig[0]+"\tequ "+orig[1]);
				break;
			case enter_function:
				int spaceNeeded = Integer.parseInt(args[0]);
				comp.addAll(Arrays.asList(
						"	push rbp",
						"	mov rbp, rsp"));
				int pushes = spaceNeeded / 8;
				for(int i=0;i<pushes;i++)
					comp.add("	push 0");
				int space = spaceNeeded & 7;
				if(space!=0) {
					comp.add("	mov al, 0");
					comp.add("	sub rsp, "+space);
					for(int i=0;i<space;i++)
						comp.add("	mov [rsp+"+i+"], al");
				}
				if(stackDepth!=0)
					throw new RuntimeException("@@contact devs. Unknown stack-related error while translating "+"stack depth not 0 before function def "+args[0]);
				stackDepth=0;
				break;
			case equal_to_b:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp al, cl",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmove ax, cx"));
				stackDepth--;
				break;
			case equal_to_f:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	movsd xmm1, [rsp]",
						"	comisd xmm1, [rsp+"+p.getSettings().intsize+"]",
						"	cmove ax, cx",
						"	add rsp, 16"));
				stackDepth--;
				break;
			case equal_to_i:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rax, rcx",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmove ax, cx"));
				stackDepth--;
				break;
			case exit_function:
				int spaceRemoved = Integer.parseInt(args[0]);
				comp.addAll(Arrays.asList(
					"	leave",
					"	ret "+spaceRemoved));
				stackDepth=0;
				break;
			case function_label:
				comp.add(args[0]+":");
				break;
			case general_label:
				if(orig[0].equals("Fwf_internal_main"))
				{
					comp.add("Fwf_internal_main:");
					comp.add("	ldmxcsr [Fwf_internal_mxmode]");
				}
				else
				{
					if(depths.containsKey(orig[0]))
						stackDepth = depths.get(orig[0]);
					comp.add(args[0]+":");
				}
				break;
			case goto_address:
				comp.add("	jmp "+args[0]);
				break;
			case greater_equal_b:
				// add 0x80 (or xor 0x80) with both registers
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovge ax, cx"));
				stackDepth--;
				break;
			case greater_equal_f:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	movsd xmm1, [rsp]",
						"	comisd xmm1, [rsp+"+p.getSettings().intsize+"]",
						"	cmovbe ax, cx",
						"	add rsp, 16"));
				stackDepth--;
				break;
			case greater_equal_i:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovge ax, cx"));
				stackDepth--;
				break;
			case greater_than_b:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovg ax, cx"));
				stackDepth--;
				break;
			case greater_equal_ub:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovae ax, cx"));
				stackDepth--;
				break;
			case greater_equal_ui:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovae ax, cx"));
				stackDepth--;
				break;
			case greater_than_f:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	movsd xmm1, [rsp]",
						"	comisd xmm1, [rsp+"+p.getSettings().intsize+"]",
						"	cmovb ax, cx",
						"	add rsp, 16"));
				stackDepth--;
				break;
			case greater_than_i:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovg ax, cx"));
				stackDepth--;
				break;
			case greater_than_ub:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmova ax, cx"));
				stackDepth--;
				break;
			case greater_than_ui:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmova ax, cx"));
				stackDepth--;
				break;
			case increment_by_pointer_b:
				comp.add("	inc [rax]");
				if(--stackDepth!=0)
					comp.add("	pop rax");
				break;
			case increment_by_pointer_i:
				label = fresh();
				comp.add("	inc [rax]");
				if(--stackDepth!=0)
					comp.add("	pop rax");
				break;
			case less_equal_b:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovle ax, cx"));
				stackDepth--;
				break;
			case less_equal_f:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	movsd xmm1, [rsp]",
						"	comisd xmm1, [rsp+"+p.getSettings().intsize+"]",
						"	cmovae ax, cx",
						"	add rsp, 16"));
				stackDepth--;
				break;
			case less_equal_i:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovle ax, cx"));
				stackDepth--;
				break;
			case less_equal_ub:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovbe ax, cx"));
				stackDepth--;
				break;
			case less_equal_ui:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovbe ax, cx"));
				stackDepth--;
				break;
			case less_than_b:
				// add 0x80 (or xor 0x80) with both registers
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovl ax, cx"));
				stackDepth--;
				break;
			case less_than_f:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	movsd xmm1, [rsp]",
						"	comisd xmm1, [rsp+"+p.getSettings().intsize+"]",
						"	cmova ax, cx",
						"	add rsp, 16"));
				stackDepth--;
				break;
			case less_than_i:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovl ax, cx"));
				stackDepth--;
				break;
			case less_than_ub:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp cl, al",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovb ax, cx"));
				stackDepth--;
				break;
			case less_than_ui:
				comp.addAll(Arrays.asList(
						"	pop rcx",
						"	cmp rcx, rax",
						"	mov eax, 0",
						"	mov cx, 255",
						"	cmovb ax, cx"));
				stackDepth--;
				break;
			case load_b:
				comp.add("	mov cl, [rax]");
				comp.add("	xor rax,rax");
				comp.add("	mov al, cl");
				break;
			case load_i:
				comp.add("	mov rax, [rax]");
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
					comp.add("	mov [Fwf_internal_globals+"+args[0]+"], al");
				} catch(NumberFormatException e) {
					comp.add("	mov ["+args[0]+"], al");
				}
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_global_int:
				try {
					Integer.parseInt(args[0]);
					comp.add("	mov [Fwf_internal_globals+"+args[0]+"], rax");
				} catch(NumberFormatException e) {
					comp.add("	mov ["+args[0]+"], rax");
				}
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_local_byte:
				comp.add("	mov [rbp"+args[0]+"], al");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_local_int:
				comp.add("	mov [rbp"+args[0]+"], rax");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_param_byte://not a good idea to use these instructions, but they're here if you want them
				comp.add("	mov [rbp+"+args[0]+"], al");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case put_param_int:
				comp.add("	mov [rbp+"+args[0]+"], rax");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case raw:
				if(orig[0].startsWith("$"))
					data.add("	db "+(orig[0].length()==2?"0":"")+orig[0].substring(1)+"h");
				else
					data.add("	db "+orig[0]);
				break;
			case rawinstruction:
				comp.add("	"+orig[0]);
				break;
			case rawint:
				if(p.getSettings().target.needsAlignment)
					data.add(data.size()-1,"ALIGN "+p.getSettings().intsize);
				if(orig[0].startsWith("$"))
					data.add("	dq "+orig[0].substring(1)+"h");
				else
					data.add("	dq "+orig[0]);
				break;
			case rawspace:
				if(Integer.parseInt(orig[0])%p.getSettings().intsize==0)
					if(p.getSettings().target.needsAlignment)
						data.add(data.size()-1,"ALIGN "+p.getSettings().intsize);
				if(useDSNotation)
					data.add("	times "+orig[0]+" db 0 ");
				else
					for(int i=0;i<Integer.parseInt(orig[0]);i++) {
						data.add("	db 0");
					}
				break;
			case rawspaceints:
				if(p.getSettings().target.needsAlignment)
					data.add(data.size()-1,"ALIGN "+p.getSettings().intsize);
				if(useDSNotation)
					data.add("	times "+orig[0]+" dq 0 ");
				else
					for(int i=0;i<Integer.parseInt(orig[0]);i++) {
						data.add("	dq 0");
					}
				break;
			case retrieve_global_address:
				if(stackDepth++>0)
					comp.add("	push rax");
				try {
					Integer.parseInt(args[0]);
					comp.add("	lea rax, [Fwf_internal_globals+"+args[0]+"]");
				} catch(NumberFormatException e) {
					comp.add("	lea rax, ["+args[0]+"]");
				}
				break;
			case retrieve_global_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	xor rax, rax");
				try {
					Integer.parseInt(args[0]);
					comp.add("	mov al, [Fwf_internal_globals+"+args[0]+"]");
				} catch(NumberFormatException e) {
					comp.add("	mov al, ["+args[0]+"]");
				}
				break;
			case retrieve_global_int:
				if(stackDepth++>0)
					comp.add("	push rax");
				try {
					Integer.parseInt(args[0]);
					comp.add("	mov rax, [Fwf_internal_globals+"+args[0]+"]");
				} catch(NumberFormatException e) {
					comp.add("	mov rax, ["+args[0]+"]");
				}
				break;
			case retrieve_immediate_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, 255 & "+args[0]);
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
				comp.add("	lea rax, [Fwf_internal_getc]");
				comp.add("	call Fwf_internal_syscall_0");
				if(!externs.contains("extern Fwf_internal_getc")) {
					externs.add("extern Fwf_internal_getc");
				}
				break;
			case retrieve_local_address:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	lea rax, [rbp"+args[0]+"]");
				break;
			case retrieve_local_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	xor rax, rax");
				comp.add("	mov al, [rbp"+args[0]+"]");
				break;
			case retrieve_local_int:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, [rbp"+args[0]+"]");
				break;
			case retrieve_param_address:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	lea rax, [rbp+"+args[0]+"]");
				break;
			case retrieve_param_byte:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	xor rax, rax");
				comp.add("	mov al, [rbp+"+args[0]+"]");
				break;
			case retrieve_param_int:
				if(stackDepth++>0)
					comp.add("	push rax");
				comp.add("	mov rax, [rbp+"+args[0]+"]");
				break;
			case signextend:
				label = fresh();
				comp.add("	movsx rax, al");
				break;
			case stackadd:
				comp.add("	pop rcx");
				comp.add("	add rax, rcx");
				stackDepth--;
				break;
			case stackaddfloat:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	movsd xmm1, [rsp]",
						"	addsd xmm1, [rsp+"+p.getSettings().intsize+"]",//add second arg to first
						"	movsd [rsp], xmm1",
						"	pop rax",
						"	pop rcx"));
				stackDepth--;
				break;
			case stackand:
				comp.add("	pop rcx");
				comp.add("	and rax, rcx");
				stackDepth--;
				break;
			case stackconvertbtofloat:
				comp.add("	movsx rax, al");
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	movsd [rsp], xmm1");
				comp.add("	pop rax");
				break;
			case stackconverttofloat:
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	movsd [rsp], xmm1");
				comp.add("	pop rax");
				break;
			case stackconverttoint:
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				break;
			case stackconverttobyte:
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				comp.add("	and rax, 255");
				break;
			case stackconverttoubyte:
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				comp.add("	and rax, 255");
				break;
			case stackconverttouint:
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp]");
				comp.add("	pop rax");
				comp.add("	cvtsd2si rax, xmm1");
				break;
			case stackconvertubtofloat:
				comp.add("	and rax, 255");
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	movsd [rsp], xmm1");
				comp.add("	pop rax");
				break;
			case stackconvertutofloat:
				//lose a bit of precision
				comp.add("	shr rax, 1");
				comp.add("	cvtsi2sd xmm1, rax");
				comp.add("	push rax");
				comp.add("	addsd xmm1, xmm1");
				comp.add("	movsd [rsp], xmm1");
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
				comp.add("	sub rax, "+p.getSettings().intsize);
				break;
			case stackdecrement_intsize_byte:
				comp.add("	sub al, "+p.getSettings().intsize);
				break;
			case stackdiv_unsigned://technically we have defined division by 0, so this can no longer fault
				stackDepth--;
				label=fresh();
				comp.add("	pop rcx");
				comp.add("	test rax, rax");
				comp.add("	jz Fwf_internal_div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	xchg rax, rcx");
				comp.add("	div rcx");
				comp.add("Fwf_internal_div_by_0"+label+":");
				break;
			case stackdiv_unsigned_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rcx");
				comp.add("	test al, al");
				comp.add("	jz Fwf_internal_div_by_0"+label);
				comp.add("	mov ah, 0");
				comp.add("	xchg rax, rcx");
				comp.add("	div cl");
				comp.add("Fwf_internal_div_by_0"+label+":");
				comp.add("	mov ah, 0");
				break;
			case stackmod_signed:
				stackDepth--;
				label=fresh();
				comp.add("	pop r8");
				comp.add("	mov rcx, r8");
				comp.add("	sar rcx, 63");
				comp.add("	mov rdx, rcx");
				comp.add("	mov r9, rax");
				comp.add("	sar r9, 63");
				comp.add("	test rax, rax");
				comp.add("	jz Fwf_internal_div_by_0"+label);
				
				comp.add("	xchg rax, r8");
				comp.add("	idiv r8");
				//if the numerator and denominator have mismatching signs, add the denom
				comp.add("	cmp rcx, r9");
				comp.add("	mov rcx, 0");
				comp.add("	cmovne rcx, r8");
				comp.add("	add rdx, rcx");

				comp.add("Fwf_internal_div_by_0"+label+":");
				comp.add("	mov rax, rdx");
				break;
			case stackmod_signed_b:
				stackDepth--;
				label = fresh();
				comp.add("	mov r8, rbx");
				comp.add("	pop rbx");
				comp.add("	test al, al");
				comp.add("	jz Fwf_internal_div_by_0"+label);
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

				comp.add("Fwf_internal_div_by_0"+label+":");
				comp.add("	mov rbx, r8");
				break;
			case stackdiv_signed:
				stackDepth--;
				label=fresh();
				comp.add("	pop r8");
				comp.add("	test rax, rax");
				comp.add("	jz Fwf_internal_div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	mov rcx, -1");
				comp.add("	bt r8, 63");
				comp.add("	cmovc rdx, rcx");
				comp.add("	xchg rax, r8");
				comp.add("	idiv r8");
				comp.add("Fwf_internal_div_by_0"+label+":");
				break;
			case stackdiv_signed_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rcx");
				comp.add("	test al, al");
				comp.add("	jz Fwf_internal_div_by_0"+label);
				comp.add("	movsx cx, cl");
				comp.add("	xchg rax, rcx");
				comp.add("	idiv cl");
				comp.add("Fwf_internal_div_by_0"+label+":");
				comp.add("	mov ah, 0");
				break;
			case stackmod_unsigned_b:
				stackDepth--;
				label=fresh();
				comp.add("	pop rcx");
				comp.add("	test al, al");
				comp.add("	jz Fwf_internal_div_by_0"+label);
				comp.add("	mov ah, 0");
				comp.add("	xchg rax, rcx");
				comp.add("	div cl");
				comp.add("Fwf_internal_div_by_0"+label+":");
				comp.add("	mov al, ah");
				comp.add("	mov ah, 0");
				break;
			case stackmod_unsigned:
				stackDepth--;
				label=fresh();
				comp.add("	pop rcx");
				comp.add("	test rax, rax");
				comp.add("	jz Fwf_internal_div_by_0"+label);
				comp.add("	xor rdx, rdx");
				comp.add("	xchg rax, rcx");
				comp.add("	div rcx");
				comp.add("Fwf_internal_div_by_0"+label+":");
				comp.add("	mov rax, rdx");
				break;
			case stackdivfloat:
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp+"+p.getSettings().intsize+"]");
				comp.add("	divsd xmm1, [rsp]");
				comp.add("	movsd [rsp], xmm1");
				comp.add("	pop rax");
				comp.add("	pop rcx");
				stackDepth--;
				break;
			case stackincrement:
				comp.add("	inc rax");
				break;
			case stackincrement_byte:
				comp.add("	inc al");
				break;
			case stackincrement_intsize:
				comp.add("	add rax, "+p.getSettings().intsize);
				break;
			case stackincrement_intsize_byte:
				comp.add("	add al, "+p.getSettings().intsize);
				break;
			case stackmodfloat:
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp+"+p.getSettings().intsize+"]");// numerator
				comp.add("	divsd xmm1, [rsp]");//denominator
				//xmm1 holds quotient. We need to round this 
				comp.add("	roundsd xmm1, xmm1, 1");//1 specifies rounding towards -inf
				comp.add("	mulsd xmm1, [rsp]");//multiply by denom
				//subtract numerator - the result (xmm1)
				comp.add("	movapd xmm2, xmm1");
				comp.add("	movsd xmm1, [rsp+"+p.getSettings().intsize+"]");
				comp.add("	subsd xmm1, xmm2");
				comp.add("	movsd [rsp], xmm1");
				comp.add("	pop rax");
				comp.add("	pop rcx");
				stackDepth--;
				break;
			case stackmult:
				stackDepth--;
				comp.add("	pop rcx");
				comp.add("	mul rcx");//much better than the z80 version
				break;
			case stackmultfloat:
				comp.addAll(Arrays.asList(
						"	push rax",
						"	movsd xmm1, [rsp]",
						"	mulsd xmm1, [rsp+"+p.getSettings().intsize+"]",//mult second arg to first
						"	movsd [rsp], xmm1",
						"	pop rax",
						"	pop rcx"));
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
				comp.add("	pop rcx");
				comp.add("	or rax, rcx");
				stackDepth--;
				break;
			case stacksub:
				comp.add("	pop rcx");
				comp.add("	xchg rax, rcx");
				comp.add("	sub rax, rcx");
				stackDepth--;
				break;
			case stacksub_opposite_order:
				comp.add("	pop rcx");
				comp.add("	sub rax, rcx");
				stackDepth--;
				break;
			case stacksubfloat:
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp+"+p.getSettings().intsize+"]");
				comp.add("	subsd xmm1, [rsp]");
				comp.add("	movsd [rsp], xmm1");
				comp.add("	pop rax");
				comp.add("	pop rcx");
				stackDepth--;
				break;
			case stackxor:
				comp.add("	pop rcx");
				comp.add("	xor rax, rcx");
				stackDepth--;
				break;
			case store_b:
				comp.add("	pop rcx");
				comp.add("	mov [rcx], al");
				stackDepth-=2;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case store_i:
				comp.add("	pop rcx");
				comp.add("	mov [rcx], rax");
				stackDepth-=2;
				if(stackDepth!=0)
					comp.add("	pop rax");
				break;
			case swap12:
				comp.add("	xchg [rsp], rax");
				break;
			case swap13:
				comp.add("	xchg [rsp+8], rax");//this is so much easier
				break;
			case swap23:
				comp.add("	mov rdx, [rsp]");
				comp.add("	xchg rdx, [rsp+8]");
				comp.add("	mov [rsp], rdx");
				break;
			case syscall_noarg:
				comp.add("	mov rax, "+args[0]+"");
				comp.add("	call Fwf_internal_syscall_0");
				if(!externs.contains("extern "+args[0])) {
					externs.add("extern "+args[0]);
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
				comp.add("	mov rsp, ["+args[0]+"]");
				comp.add("	mov rbp, ["+args[0]+"+8]");
				comp.add("	ret");
				break;
			case exit_global:
				comp.add("	mov rsp, ["+args[0]+"]");
				comp.add("	mov rbp, ["+args[0]+"+8]");
				comp.add("	ret");
				stackDepth--;
				break;
			case write_sp:
				comp.add("	mov ["+args[0]+"], rsp");
				comp.add("	mov ["+args[0]+"+8], rbp");
				break;
			case fix_index:
				comp.add("	shl rax, 3");
				break;
				
			case branch_equal_to_ub:
			case branch_equal_to_b:
			case branch_equal_to_i:
			case branch_equal_to_ui:
			case branch_not_equal_b:
			case branch_not_equal_ub:
			case branch_not_equal_i:
			case branch_not_equal_ui:
			case branch_greater_equal_b:
			case branch_greater_equal_i:
			case branch_greater_equal_ub:
			case branch_greater_equal_ui:
			case branch_greater_than_b:
			case branch_greater_than_i:
			case branch_greater_than_ub:
			case branch_greater_than_ui:
			case branch_less_equal_b:
			case branch_less_equal_i:
			case branch_less_equal_ub:
			case branch_less_equal_ui:
			case branch_less_than_b:
			case branch_less_than_i:
			case branch_less_than_ub:
			case branch_less_than_ui:
				String istring = instruction.in.toString();
				comp.add("	pop rdx");
				if(istring.endsWith("b"))
					comp.add("	sub dl, al");
				else
					comp.add("	sub rdx, rax");
				if(stackDepth>2)
					comp.add("	pop rax");
				String jump = "\tj";
				if(istring.startsWith("branch_not_equal"))
					jump+="ne";
				else if(istring.startsWith("branch_equal_to"))
					jump+="e";
				else {
					if(istring.contains("greater"))
						if(istring.contains("_u"))
							jump+="a";
						else
							jump+="g";
					if(istring.contains("less"))
						if(istring.contains("_u"))
							jump+="b";
						else
							jump+="l";
					if(istring.contains("equal"))
						jump+="e";
				}
				jump+=" "+instruction.getArgs()[0];
				stackDepth-=2;
				comp.add(jump);
				depths.put(orig[0],stackDepth);
				break;
			case branch_less_than_f:
			case branch_less_equal_f:
			case branch_greater_equal_f:
			case branch_greater_than_f:
			case branch_equal_to_f:
			case branch_not_equal_f:
				istring = instruction.in.toString();
				comp.add("	push rax");
				comp.add("	movsd xmm1, [rsp+"+p.getSettings().intsize+"]");
				comp.add("	comisd xmm1, [rsp]");
				comp.add("	pop rax");
				comp.add("	pop rax");
				if(stackDepth>2)
					comp.add("	pop rax");
				jump = "\tj";
				if(istring.startsWith("branch_not_equal"))
					jump+="ne";
				else if(istring.startsWith("branch_equal_to"))
					jump+="e";
				else {
					if(istring.contains("greater"))
						jump+="a";
					if(istring.contains("less"))
						jump+="b";
					if(istring.contains("equal"))
						jump+="e";
				}
				jump+=" "+instruction.getArgs()[0];
				stackDepth-=2;
				comp.add(jump);
				depths.put(orig[0],stackDepth);
				break;
			case deffered_delete:
				throw new RuntimeException("@@contact devs. deffered delete should be replaced");
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
		comp.add("	mov eax, 0");
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
				if(comp.get(i).startsWith("\tret") && (comp.get(i+1).startsWith("\tret")||comp.get(i+1).startsWith("\tjmp"))) {
					comp.remove(i+1);
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
		
		externs.add("section .data");
		externs.addAll(data);
		externs.add("section .text");
		comp.addAll(0, externs);
		return comp;
	}
	ArrayList<String> cache;
}
