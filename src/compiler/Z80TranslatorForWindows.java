package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Z80TranslatorForWindows {
	int counter = 0;
	private int fresh() {
		return counter++;
	}
	public ArrayList<String> translate(List<IntermediateLang.Instruction> instructions, boolean useDSNotation, int stackDepth, Parser p) {
		
		boolean debug = false;
		
		
		ArrayList<String> comp = new ArrayList<String>();
		p.settings.target.addHeader(comp);
		
		for(IntermediateLang.Instruction instruction:instructions) {
			if(debug) {
				System.out.println(instruction);
			}
			String[] args = instruction.args;
			switch(instruction.in) {
			case notify_pop:
				stackDepth--;
				break;
			case branch_address:
				comp.addAll(Arrays.asList(
						"	ld a,l",
						stackDepth>1?"	pop hl":"	",
						"	or a",
						"	jp nz,"+args[0]));
				stackDepth--;
				break;
			case branch_not_address:
				comp.addAll(Arrays.asList(
						"	ld a,l",
						stackDepth>1?"	pop hl":"	",
						"	or a",
						"	jp z,"+args[0]));
				stackDepth--;
				break;
			case strcpy:
				int label = fresh();
				comp.add("	pop de");
				//write from hl to de until (hl)==0
				comp.add("__strcpy_loop_"+label);
				comp.add("	ld a,(hl)");
				comp.add("	or a");
				comp.add("	jr z,__strcpy_loop_exit_"+label);
				comp.add("	ldi");
				comp.add("	jp __strcpy_loop_"+label);
				comp.add("__strcpy_loop_exit_"+label);
				comp.add("	ex de,hl");
				stackDepth--;
				break;
			case call_function:
					
				if(p.getFunctionOutputType(args[0]).get(0)==Parser.Data.Void) {
					comp.add("	push hl");//save last value of stack
					comp.add("	call "+args[0]);//call the function
					//result is unused
					stackDepth-=p.getFunctionInputTypes(args[0]).get(0).size();
					//if we consumed all our arguments and there's still something left, pop it
					if(stackDepth>0)
						comp.add("	pop hl");
				} else {
					comp.add("	push hl");//save last value of stack
					comp.add("	call "+args[0]);//call the function
					stackDepth-=p.getFunctionInputTypes(args[0]).get(0).size();
					//we actually do use the result this time
					stackDepth++;
				}
				break;
			case copy:
				comp.add("	push hl");
				stackDepth++;
				break;
			case copy2:
				comp.addAll(Arrays.asList(
						"	pop bc",
						"	push bc",
						"	push hl",
						"	push bc"));
				stackDepth+=2;
				break;
			case copy_from_address:
				//stack is [dest] [src] [n]
				label = fresh();
				comp.addAll(Arrays.asList(
						"	ld b,h",
						"	ld c,l",
						"	pop hl",
						"	pop de",
						"	xor a",
						"	or b",
						"	or c",
						"	jr z,___skip_"+label,
						"	ldir",
						"___skip_"+label+":"));
				stackDepth-=3;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case decrement_by_pointer_b:
				comp.addAll(Arrays.asList(
						"	dec (hl)"));
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case decrement_by_pointer_i:
				label = fresh();
				comp.addAll(Arrays.asList(
						"	dec (hl)",
						"	ld a,(hl)",
						"	inc a",
						"	jr nz, ___skip_"+label,
						"	inc hl",
						"	dec (hl)",
						"___skip_"+label+":"));
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case define_symbolic_constant:
				comp.add(""+args[0]+"\t.equ "+args[1]);
				break;
			case enter_function:
				int spaceNeeded = Integer.parseInt(args[0]);
				int pushes = spaceNeeded/2;
				comp.addAll(Arrays.asList(
						"	push ix",
						"	ld ix,$0000",
						"	add ix,sp"));
				for(int x=0;x<pushes;x++) {
					comp.add("	push hl");//dumb as hell, but at least it preserves hl and ix
					//dec sp * 2 is 1 cycle slower and 1 byte longer
					//manually subtracting from sp is faster for any more than a few arguments, but preserving hl is tricky (I recommend exx)
				}
				if(stackDepth!=0)
					throw new RuntimeException("@@contact devs. Unknown stack-related error while translating "+"stack depth not 0 before function def "+args[0]);
				stackDepth=0;
				break;
			case enter_routine:
				//similar to enter_function, but we preserve de and instead use iy for exiting
				stackDepth+=Integer.parseInt(args[0]);
				comp.addAll(Arrays.asList(
						"	di",
						"	ld (___flag_save),iy",
						"	pop iy"));
				break;
			case equal_to_b:
				label = fresh();
				comp.addAll(Arrays.asList(
						"	pop bc",
						"	ld h,$00",
						"	ld a,l",
						"	xor c",
						"	jr z,___skip"+label,
						"	ld a,$ff",
						"___skip"+label+":",
						"	cpl",
						"	ld l,a"));
				stackDepth--;
				break;
			case equal_to_f:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case equal_to_i:
				label = fresh();
				comp.addAll(Arrays.asList(
						"	pop bc",
						"	xor a",
						"	sbc hl,bc",
						"	jr nz,___rtrue"+label,
						"	cpl",
						"___rtrue"+label+":",
						"	ld l,a",
						"	ld h,$00"));
				stackDepth--;
				break;
			case exit_function:
				int spaceRemoved = Integer.parseInt(args[0]);
				comp.addAll(Arrays.asList(
					"	ld sp,ix",
					"	pop ix",
					"	pop bc"));
				stackDepth=0;
				//remove [spaceRemoved] words from the stack
				//hl holds return value, bc holds return address, de holds previous return location ix holds previous local pointer
				// we can't pop into anything so we have to directly increment sp twice for each argument
				for(int x=0;x<spaceRemoved/2;x++) {
					comp.add("	pop de;removing "+(spaceRemoved/2)+(spaceRemoved==2?" arg":" args"));
				}
				comp.add("	push bc");
				comp.add("	ret");
				break;
			case notify_stack:
				stackDepth = Integer.parseInt(args[0]);
				break;
			case exit_routine:
				comp.addAll(Arrays.asList(
						"	ld b,iyh",
						"	ld c,iyl",
						"	ld iy,(___flag_save)",
						"	push bc",
						"	ret"));
				stackDepth=0;
				break;
			case function_label:
			case general_label:
				comp.add(args[0]+":");
				break;
			case goto_address:
				comp.add("	jp "+args[0]);
				break;
			case greater_equal_b:
				// add 0x80 (or xor 0x80) with both registers
				comp.add("  ld a,$80");
				comp.add("	xor l");
				comp.add("	ld l,a");
				comp.add("	ld a,$80");
				label = fresh();
				comp.add("	pop bc");
				comp.add("	xor c");// hl=$ff if c>=l    in other words   set l to $ff if  c-l does not carry
				comp.add("	sub l");
				comp.add("	ld l,$ff");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, inc l
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case greater_equal_f:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case greater_equal_i:
				label = fresh();
				comp.add("	pop bc");
				//swap sign bits of both b and h
				comp.add("	ld a,$80");
				comp.add("	xor h");
				comp.add("	ld h,a");
				comp.add("	ld a,$80");
				comp.add("	xor b");
				comp.add("	ld b,a");
				// set flag if bc>=hl   or    0>=hl-bc   or 0>hl-bc-1
				comp.add("	scf");// bc >= hl is equivalent to hl-bc<=0 or hl-bc-1 < 0.  This is a perfect use for scf
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$0");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, dec l
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case greater_than_b:
				label = fresh();
				comp.add("	pop bc");
				comp.add("  ld a,$80");
				comp.add("  xor c");
				comp.add("  ld c,a");
				comp.add("  ld a,$80");
				comp.add("	xor l");// hl=$ff if c>l    in other words   set l to $ff if  l-c carries
				comp.add("	sub c");
				comp.add("	ld l,$ff");
				comp.add("	jr c,___comparison_"+label);//if carry was set, inc l
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case greater_equal_ub:
				label = fresh();
				comp.add("	pop bc");
				comp.add("	ld a,c");// hl=$ff if c>=l    in other words   set l to $ff if  c-l does not carry
				comp.add("	sub l");
				comp.add("	ld l,$ff");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, inc l
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case greater_equal_ui:
				label = fresh();
				comp.add("	pop bc");
				comp.add("	scf");// bc >= hl is equivalent to hl-bc<=0 or hl-bc-1 < 0.  This is a perfect use for scf
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$0");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, dec l
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case greater_than_f:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case greater_than_i:
				label = fresh();
				comp.add("	pop bc");
				//swap sign bits of both b and h
				comp.add("	ld a,$80");
				comp.add("	xor h");
				comp.add("	ld h,a");
				comp.add("	ld a,$80");
				comp.add("	xor b");
				comp.add("	ld b,a");
				// set flag if bc>hl   or    0>hl-bc
				comp.add("	xor a");// bc >= hl is equivalent to hl-bc<=0 or hl-bc-1 < 0.  This is a perfect use for scf
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$0");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, dec l
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case greater_than_ub:
				label = fresh();
				comp.add("	pop bc");// hl=$ff if c>l    in other words   set l to $ff if  l-c carries
				comp.add("	ld a,l");
				comp.add("	sub c");
				comp.add("	ld l,$ff");
				comp.add("	jr c,___comparison_"+label);//if carry was set, inc l
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case greater_than_ui:
				label = fresh();
				comp.add("	pop bc");
				comp.add("	xor a");// bc > hl is equivalent to hl-bc<0
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$0");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, dec l
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case increment_by_pointer_b:
				comp.add("	inc (hl)");
				comp.add("	pop hl");
				stackDepth--;
				break;
			case increment_by_pointer_i:
				label = fresh();
				comp.add("	inc (hl)");
				comp.add("	jr nz,___increment"+label);
				comp.add("	inc hl");
				comp.add("	inc (hl)");
				comp.add("___increment"+label);
				comp.add("	pop hl");
				stackDepth--;
				break;
			case less_equal_b:
				label = fresh();
				comp.add("	pop bc");
				comp.add("  ld a,$80");
				comp.add("  xor c");
				comp.add("  ld c,a");
				comp.add("  ld a,$80");
				comp.add("	xor l");
				comp.add("	sub c");
				comp.add("	ld l,$00");
				comp.add("	jr c,___comparison_"+label);
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case less_equal_f:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case less_equal_i:
				label = fresh();
				comp.add("	pop bc");
				//swap sign bits of both b and h
				comp.add("	ld a,$80");
				comp.add("	xor h");
				comp.add("	ld h,a");
				comp.add("	ld a,$80");
				comp.add("	xor b");
				comp.add("	ld b,a");
				
				comp.add("	xor a");
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$ff");
				comp.add("	jr nc,___comparison_"+label);
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case less_equal_ub:
				label = fresh();
				comp.add("	pop bc");// hl=$ff if c>l    in other words   set l to $ff if  l-c carries
				comp.add("	ld a,l");
				comp.add("	sub c");
				comp.add("	ld l,$00");
				comp.add("	jr c,___comparison_"+label);
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case less_equal_ui:
				label = fresh();
				comp.add("	pop bc");
				comp.add("	xor a");// bc > hl is equivalent to hl-bc<0
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$ff");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, dec l
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case less_than_b:
				// add 0x80 (or xor 0x80) with both registers
				comp.add("  ld a,$80");
				comp.add("  xor l");
				comp.add("  ld l,a");
				comp.add("  ld a,$80");
				label = fresh();
				comp.add("	pop bc");
				comp.add("	xor c");// hl=$ff if c>=l    in other words   set l to $ff if  c-l does not carry
				comp.add("	sub l");
				comp.add("	ld l,$00");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, inc l
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case less_than_f:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case less_than_i:
				label = fresh();
				comp.add("	pop bc");
				//swap sign bits of both b and h
				comp.add("	ld a,$80");
				comp.add("	xor h");
				comp.add("	ld h,a");
				comp.add("	ld a,$80");
				comp.add("	xor b");
				comp.add("	ld b,a");
				// set flag if bc>=hl   or    0>=hl-bc   or 0>hl-bc-1
				comp.add("	scf");// bc >= hl is equivalent to hl-bc<=0 or hl-bc-1 < 0.  This is a perfect use for scf
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$ff");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, dec l
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case less_than_ub:
				label = fresh();
				comp.add("	pop bc");
				comp.add("	ld a,c");// hl=$ff if c>=l    in other words   set l to $ff if  c-l does not carry
				comp.add("	sub l");
				comp.add("	ld l,$00");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, inc l
				comp.add("	dec l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case less_than_ui:
				label = fresh();
				comp.add("	pop bc");
				comp.add("	scf");// bc >= hl is equivalent to hl-bc<=0 or hl-bc-1 < 0.  This is a perfect use for scf
				comp.add("	sbc hl,bc");
				comp.add("	ld l,$ff");
				comp.add("	jr nc,___comparison_"+label);//if carry was set, dec l
				comp.add("	inc l");
				comp.add("___comparison_"+label+":");
				comp.add("	ld h,$00");
				stackDepth--;
				break;
			case load_b:
				comp.add("	ld l,(hl)");
				comp.add("	ld h,$00");
				break;
			case load_i:
				comp.add("	ld c,(hl)");
				comp.add("	inc hl");
				comp.add("	ld h,(hl)");
				comp.add("	ld l,c");
				break;
			case nop:
				comp.add("	nop");//a very useful instruction
				break;
			case overwrite_immediate_byte:
				comp.add("	ld l,"+args[0]+"& 255");
				break;
			case overwrite_immediate_float:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case overwrite_immediate_int:
				comp.add("	ld hl,"+args[0]);
				break;
			case pop_discard:
				if(stackDepth>1)
				{
					comp.add("	pop hl");
					stackDepth--;
				} else if(stackDepth==1) {
					stackDepth--;
				}
				break;
			case put_global_byte:
				try {
					Integer.parseInt(args[0]);
					comp.add("	ld a,l");
					comp.add("	ld (__globals+"+args[0]+"),a");
				} catch(NumberFormatException e) {
					comp.add("	ld a,l");
					comp.add("	ld ("+args[0]+"),a");
				}
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case put_global_int:
				try {
					Integer.parseInt(args[0]);
					comp.add("	ld (__globals+"+args[0]+"),hl");
				} catch(NumberFormatException e) {
					comp.add("	ld ("+args[0]+"),hl");
				}
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case put_local_byte:
				comp.add("	ld (ix"+args[0]+"),l");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case put_local_int:
				comp.add("	ld (ix"+args[0]+"),l");
				comp.add("	ld (ix"+args[0]+"+1),h");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case put_param_byte://not a good idea to use these instructions, but they're here if you want them
				comp.add("	ld (ix+"+args[0]+"),l");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case put_param_int:
				comp.add("	ld (ix+"+args[0]+"),l");
				comp.add("	ld (ix+"+args[0]+"+1),h");
				stackDepth--;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case raw:
				comp.add("	.db "+args[0]);
				break;
			case rawinstruction:
				comp.add("	"+args[0]);
				break;
			case rawint:
				comp.add("	.dw "+args[0]);
				break;
			case rawspace:
				if(useDSNotation)
					comp.add("	.ds "+args[0]);
				else
					for(int i=0;i<Integer.parseInt(args[0]);i++) {
						comp.add("	.db $00");
					}
				break;
			case rawspaceints:
				if(useDSNotation)
					comp.add("	.ds int_size*("+args[0]+")");
				else
					for(int i=0;i<Integer.parseInt(args[0]);i++) {
						comp.add("	.dw $0000");
					}
				break;
			case retrieve_global_address:
				if(stackDepth++>0)
					comp.add("	push hl");
				try {
					Integer.parseInt(args[0]);
					comp.add("	ld hl, __globals+"+args[0]);
				} catch(NumberFormatException e) {
					comp.add("	ld hl, "+args[0]);
				}
				break;
			case retrieve_global_byte:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld h,$00");
				try {
					Integer.parseInt(args[0]);
					comp.add("	ld a, (__globals+"+args[0]+")");
					comp.add("	ld l,a");
				} catch(NumberFormatException e) {
					comp.add("	ld a, ("+args[0]+")");
					comp.add("	ld l,a");
				}
				break;
			case retrieve_global_int:
				if(stackDepth++>0)
					comp.add("	push hl");
				try {
					Integer.parseInt(args[0]);
					comp.add("	ld hl, (__globals+"+args[0]+")");
				} catch(NumberFormatException e) {
					comp.add("	ld hl, ("+args[0]+")");
				}
				break;
			case retrieve_immediate_byte:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld hl,255 & "+args[0]);
				break;
			case retrieve_immediate_float:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case retrieve_immediate_int:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld hl,"+args[0]);
				break;
			case retrieve_local_address://this is a bit complicated because ix doesn't like giving its own value up
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld hl,"+args[0]);
				comp.add("	ld b,ixh");
				comp.add("	ld c,ixl");
				comp.add("	add hl,bc");
				break;
			case retrieve_local_byte:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld h,$00");
				comp.add("	ld l,(ix"+args[0]+")");
				break;
			case retrieve_local_int:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld h,(ix"+args[0]+"+1)");
				comp.add("	ld l,(ix"+args[0]+")");
				break;
			case retrieve_param_address:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld hl,"+args[0]);
				comp.add("	ld b,ixh");
				comp.add("	ld c,ixl");
				comp.add("	add hl,bc");
				break;
			case retrieve_param_byte:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld h,$00");
				comp.add("	ld l,(ix+"+args[0]+")");
				break;
			case retrieve_param_int:
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld h,(ix+"+args[0]+"+1)");
				comp.add("	ld l,(ix+"+args[0]+")");
				break;
			case signextend:
				label = fresh();
				comp.add("	or l");
				comp.add("	jp p,___nosign"+label);
				comp.add("	ld h,$ff");
				comp.add("___nosign"+label+":");
				break;
			case stackadd:
				comp.add("	pop bc");
				comp.add("	add hl,bc");
				stackDepth--;
				break;
			case stackaddfloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stackand:
				stackDepth--;
				comp.add("	pop bc");
				comp.add("	ld a,h");
				comp.add("	and b");
				comp.add("	ld h,a");
				comp.add("	ld a,l");
				comp.add("	and c");
				comp.add("	ld l,a");
				break;
			case stackconvertbtofloat:
			case stackconverttobyte:
			case stackconverttofloat:
			case stackconverttoint:
			case stackconverttoubyte:
			case stackconverttouint:
			case stackconvertubtofloat:
			case stackconvertutofloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stackcpl:
				comp.add("	ld a,h");//unironically couldn't find anything better
				comp.add("	cpl");
				comp.add("	ld h,a");
				comp.add("	ld a,l");
				comp.add("	cpl");
				comp.add("	ld l,a");
				break;
			case stackdecrement:
				comp.add("	dec hl");
				break;
			case stackdecrement_byte:
				comp.add("	dec l");
				break;
			case stackdecrement_intsize:
				comp.add("	dec hl");
				comp.add("	dec hl");
				break;
			case stackdecrement_intsize_byte:
				comp.add("	dec l");
				comp.add("	dec l");
				break;
				//the z80 needs multiply, divide and modulo routines
				// the single division operation has some weird results.
				// dividing by zero returns a/0 = 0
				// 							a%0 = a
				
				// this second one is pretty objectionable, it should be zero
				// and the built-in division algorithms always return
			case stackdiv_unsigned:
			case stackdiv_unsigned_b:
				//divide top stack by hl
				comp.add("	pop de");
				comp.add("	call _Div16By16");
				//result is in de
				comp.add("	ex de,hl");
				stackDepth--;
				break;
			case stackmod_signed:
				if(p.hasFunction("smod")) {
					comp.add("	push hl");
					comp.add("	call smod");
					stackDepth--;
				} else {
					throw new RuntimeException("Implementing signed mod on the ti83 requires implementing a function smod");
				}
				break;
			case stackmod_signed_b:
				if(p.hasFunction("sbmod")) {
					comp.add("	push hl");
					comp.add("	call sbmod");
					stackDepth--;
				} else {
					throw new RuntimeException("Implementing signed mod on the ti83 requires implementing a function sbmod");
				}
				break;
			case stackdiv_signed:
				if(p.hasFunction("sdiv")) {
					comp.add("	push hl");
					comp.add("	call sdiv");
					stackDepth--;
				} else {
					throw new RuntimeException("Implementing signed division on the ti83 requires implementing a function sdiv");
				}
				
				break;
			case stackdiv_signed_b:
				if(p.hasFunction("sbdiv")) {
					comp.add("	push hl");
					comp.add("	call sbdiv");
					stackDepth--;
				} else {
					throw new RuntimeException("Implementing signed division on the ti83 requires implementing a function sbdiv");
				}
				break;
			case stackmod_unsigned_b:
			case stackmod_unsigned:
				//divide top stack by hl
				label = fresh();
				comp.add("	pop de");
				comp.add("	push de");
				comp.add("	call _Div16By16");
				comp.add("	pop bc");
				//result is in hl, but hl might be equal to what de was.
				comp.add("	xor a");
				comp.add("	sbc hl,bc");//carry flag should be set. Add it back in
				comp.add("	jr nc,"+"__stackmod"+label);
				comp.add("	add hl,bc");
				comp.add("__stackmod"+label+":");
				stackDepth--;
				break;
			case stackdivfloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stackincrement:
				comp.add("	inc hl");
				break;
			case stackincrement_byte:
				comp.add("	inc l");
				break;
			case stackincrement_intsize:
				comp.add("	inc hl");
				comp.add("	inc hl");
				break;
			case stackincrement_intsize_byte:
				comp.add("	inc l");
				comp.add("	inc l");
				break;
			case stackmodfloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stackmult:
				stackDepth--;
				
				//there is no good builtin routine for multiplying 2 ints, so I made this
				label = fresh(); 
				//mulyiply 12 * 10
				
				// 
				// hl =     
				// de = ret
				// hl' = 0078
				// bc' = 00a0
				// de' = 0000
				// a' = 01
				//
				//
				//
				//
				//
				
				comp.add("	di");//1
				comp.add("	push hl");//1
				comp.add("	exx");//1
				comp.add("	ld hl,$0000");//3
				comp.add("	pop bc");//1
				comp.add("	pop de");//1
				comp.add("mulLoopCondition"+label+":");
				comp.add("	xor a");//1
				comp.add("	or d");//1
				comp.add("	or e");//1
				comp.add("	jr z,mulLoopEnd"+label);//2
				
				comp.add("	srl d");//2
				comp.add("	rr e");//2
				comp.add("	jr nc,mulLoopSkip"+label);//2
				comp.add("	add hl,bc");//1
				comp.add("mulLoopSkip"+label+":");
				comp.add("	sla c");//2
				comp.add("	rl b");//2
				comp.add("	jr mulLoopCondition"+label);//2
				comp.add("mulLoopEnd"+label+":");
				comp.add("	push hl");//1
				comp.add("	exx");//1
				comp.add("	pop hl");//1
				//29 bytes
				
				break;
			case stackmultfloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stackneg:
				comp.add("	xor a");
				comp.add("	sub l");
				comp.add("	ld l,a");
				comp.add("	sbc a,a");
				comp.add("	sub h");
				comp.add("	ld h,a");
				break;
			case stacknegbyte:
				comp.add("	xor a");
				comp.add("	sub l");
				comp.add("	ld l,a");
				break;
			case stacknegfloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stacknot:
				comp.add("	ld a,l");
				comp.add("	cpl");
				comp.add("	ld l,a");
				break;
			case stackor:
				comp.add("	pop bc");
				comp.add("	ld a,h");
				comp.add("	or b");
				comp.add("	ld h,a");
				comp.add("	ld a,l");
				comp.add("	or c");
				comp.add("	ld l,a");
				stackDepth--;
				break;
			case stacksub:
				comp.add("	ld c,l");
				comp.add("	ld b,h");
				comp.add("	pop hl");
				comp.add("	xor a");
				comp.add("	sbc hl,bc");
				stackDepth--;
				break;
			case stacksub_opposite_order:
				comp.add("	pop bc");
				comp.add("	xor a");
				comp.add("	sbc hl,bc");
				stackDepth--;
				break;
			case stacksubfloat:
				throw new UnsupportedOperationException("z80 does not support floating point");
			case stackxor:
				comp.add("	pop bc");
				comp.add("	ld a,h");
				comp.add("	xor b");
				comp.add("	ld h,a");
				comp.add("	ld a,l");
				comp.add("	xor c");
				comp.add("	ld l,a");
				stackDepth--;
				break;
			case store_b:
				comp.add("	ex (sp),hl");
				comp.add("	pop bc");
				comp.add("	ld (hl),c");
				stackDepth-=2;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case store_i:
				comp.add("	ex (sp),hl");
				comp.add("	pop bc");
				comp.add("	ld (hl),c");
				comp.add("	inc hl");
				comp.add("	ld (hl),b");
				stackDepth-=2;
				if(stackDepth!=0)
					comp.add("	pop hl");
				break;
			case swap12:
				comp.add("	ex (sp),hl");
				break;
			case swap13:
				comp.add("	pop bc");
				comp.add("	ex (sp),hl");
				comp.add("	push bc");
				break;
			case swap23:
				comp.add("	di");//I hate to do it
				comp.add("	exx");
				comp.add("	pop hl");
				comp.add("	ex (sp),hl");
				comp.add("	push hl");
				comp.add("	exx");
				break;
			case getc:
				// read from in (0) until the result is not 0.
				// then store the result in l.
				label = fresh();
				if(stackDepth++>0)
					comp.add("	push hl");
				comp.add("	ld h,$00");
				comp.add("__getc_loop_"+label+":");
				comp.add("	in a,(0)");
				comp.add("	or a");
				comp.add("	jp z, __getc_loop_"+label);
				comp.add("	ld l,a");
				break;
			case syscall_noarg:
				throw new RuntimeException("Syscalls not available on emulator "+args[0]);
			case syscall_arg:
				throw new RuntimeException("Syscalls not available on emulator "+args[0]);
			case truncate:
				comp.add("	ld h,$00");
				break;//TODO add direct comparison conditional branches
				
				//since shifting is done once, in-place, on the stack, we only need to operate on hl.
			case shift_left_b:
				comp.add("	sla l");//weirdly more memory inefficient than its 2 byte counterpart
				break;
			case shift_left_i:
				comp.add("	add hl,hl");//not technically a shift, but more efficient
				break;
			case shift_right_b:
				comp.add("	sra l");//sra maintains the carry bit, which is apparently what you're supposed to do with signed shifts
				break;
			case shift_right_i:
				comp.add("	sra h");
				comp.add("	rr l");
				break;
			case shift_right_ub:
				comp.add("	srl l");
				break;
			case shift_right_ui:
				comp.add("	srl h");
				comp.add("	rr l");
				break;
			case exit_noreturn:
				comp.add("	di");
				comp.add("	halt");
				break;
			case write_sp:
				comp.add("	ld ("+args[0]+"),sp");
				break;
			case fix_index:
				comp.add("	add hl,hl");//ok then
				break;
			}
			if(stackDepth<0)
				throw new RuntimeException("@@contact devs. Unknown stack-related error while translating");
			if(debug)
				System.out.println(";;;;;;"+stackDepth);
		}
		if(stackDepth!=0)
		{
			throw new RuntimeException("@@contact devs. Unknown stack-related error while translating");
		}
		comp.add("	di");//exiting on windows requires the sequence di halt. This will freeze if run on hardware
		comp.add("	halt");
		comp.add("___flag_save:");
		comp.add("	.dw $0000");
		
		//divide de by hl.
		//store result in de, remainder in hl
		comp.add("_Div16By16:");
		comp.add("	ld a,h");//check divide by 0
		comp.add("	or l");
		comp.add("	jr nz,__actually_divide");
		comp.add("	ld de,$0000");//return 0 in both if we divided by 0.
		comp.add("	ret");
		comp.add("__actually_divide:");
		
		comp.add("	ex de,hl");
		comp.add("	ld a,h");
		comp.add("	ld c,l");
		//brandonw 16/16 division routine
		comp.add("	ld hl,$0000");
		comp.add("	ld b,16");
		comp.add("__divloop__:");
		comp.add("	sll c");
		comp.add("	rla");
		comp.add("	adc hl,hl");
		comp.add("	sbc hl,de");
		comp.add("	jr nc, $+4");
		comp.add("	add hl,de");
		comp.add("	dec c");
		comp.add("	djnz __divloop__");
		//move ac to de
		comp.add("	ld d,a");
		comp.add("	ld e,c");
		comp.add("	ret");
		//slight optimization
		final boolean optimize = !debug;
		
		if(optimize) {
			// hl specifically we can do this because we won't have to discard the top of the stack and duplicate what's beneath
			for(int i=0;i<comp.size()-2;i++) {
				if("	pop hl".equals(comp.get(i)) && "	push hl".equals(comp.get(i+1)) && (comp.get(i+2).contains("ld h")||comp.get(i+2).contains("ld l"))) {
					comp.remove(i);
					comp.remove(i);
					i--;
				}
			}
			// The only side effect of pop push is loading the top stack value into a register pair. If we immediately change it, there was no use.
			for(int i=0;i<comp.size()-2;i++) {
				for(String regpair:"hl bc de".split(" "))
				if(("	pop "+regpair).equals(comp.get(i)) && ("	push "+regpair).equals(comp.get(i+1)) && (comp.get(i+2).contains("ld "+regpair))) {
					comp.remove(i);
					comp.remove(i);
					i--;
				}
			}
			//push pop has the side effect of altering the space above the stack. According to our calling convention, this is unnecessary in any circumstance
			for(int i=0;i<comp.size()-2;i++) {
				for(String regpair:"hl bc de".split(" "))
				if(comp.get(i).equals("	push "+regpair) && comp.get(i+1).equals("	pop "+regpair)) {
					comp.remove(i);
					comp.remove(i);
					i--;
				}
			}
			
			String[] likelyLoadPositions = "b c d e h l (ix+4) (ix+5) (ix+6) (ix+7) (ix+8) (ix+9) (ix+10) (ix+11) (ix-2) (ix-2+1) (ix-4) (ix-4+1) (ix-6) (ix-6+1) (ix-8) (ix-8+1) (hl) (de) (bc)".split(" ");
			
			// I have seen circumstances like ld a,l    ld l,a   come up in compiled code.
			
			for(int iter=0;iter<2;iter++)//this is a very slow loop, best not to run it more than twice.
			{
				for(int i=0;i<comp.size()-2;i++) {
					if(!comp.get(i).startsWith("	ld"))//make the compiler faster, lol
						continue;
					for(String reg1:likelyLoadPositions)
						for(String reg2:likelyLoadPositions)
							if(comp.get(i).equals("	ld "+reg1+","+reg2) && comp.get(i+1).equals("	ld "+reg2+","+reg1)) {
								comp.remove(i+1);
							}
				}
			}
			
		}
		
		return comp;
	}
}
