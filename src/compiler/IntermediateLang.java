package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import settings.CompilationSettings;
/**
 * The Fhidwfe intermediate language (generated directly from source)
 *
 */
public class IntermediateLang {
	static String shifts = "top of stack is: [value]";
	/**
	 * If the given syntaxtree represents a type token, this returns that type
	 * @param t the tree
	 * @return the type
	 */
	private static DataType typeFromTree(SyntaxTree t) {
		return DataType.fromLowerCase(t.getTokenString());
	}
	/**
	 * Converts a string into one of the 4 possible data segments a variable can be in
	 * @param resolved the string
	 * @return the segment
	 */
	private static SyntaxTree.Location loc(String resolved) {
		return SyntaxTree.Location.valueOf(resolved);
	}
	long fres = 0;
	/**
	 * Generates a fresh number that can safely be used for loop labels
	 * @return
	 */
	private long fresh()
	{
		return fres++;
	}
	
	BaseTree base;
	/**
	 * Takes a syntax tree base, a lexer (used for string info) and generates a program in the intermediate language
	 * @param tree
	 * @param lex
	 * @return the list of instructions
	 */
	public ArrayList<Instruction> generateInstructions(BaseTree tree, Lexer lex) {
		
		ArrayList<Instruction> results = new ArrayList<Instruction>();
		int stringCount = 0;
		results.add(InstructionType.define_symbolic_constant.cv("int_size",""+tree.theParser.settings.intsize));
		if(tree.theParser.settings.target==CompilationSettings.Target.LINx64)
		{
			results.add(InstructionType.rawinstruction.cv("global __main"));
		}
		Predicate<CompilationSettings.Target> startsAtBegin =
				sett -> sett!=CompilationSettings.Target.WINx64
				&& sett!=CompilationSettings.Target.WINx86
				&& sett!=CompilationSettings.Target.LINx64;
		if(startsAtBegin.test(tree.theParser.settings.target)) {
			results.add(InstructionType.write_sp.cv("__ExitLocation"));
			results.add(InstructionType.goto_address.cv("__main"));//jump immediately to the main
		}
		//equivalent of the error(err_message) function
		tree.theParser.inlineReplace("error");
		results.add(InstructionType.function_label.cv("error"));
		results.add(InstructionType.enter_function.cv("0"));//we don't need any locals
		//^ according to the calling convention, enter_function isn't exactly needed for the error handler, but it's nice for consistency
		results.add(InstructionType.retrieve_param_int.cv(""+(tree.theParser.settings.intsize*2)));//retrieve string argument
		results.add(InstructionType.call_function.cv("puts"));//print the error message
		results.add(InstructionType.overwrite_immediate_int.cv("-1"));//return -1
		results.add(InstructionType.exit_noreturn.cv("__ExitLocation"));
		results.add(InstructionType.data_label.cv("__ExitLocation"));
		results.add(InstructionType.rawspace.cv(""+(tree.theParser.settings.intsize+(tree.theParser.settings.intsize>2?8:0))));
		for(Byte[] b:lex.stringConstants()) {//create string constants table
			results.add(InstructionType.data_label.cv("__String_"+stringCount++));
			for(byte c:b) {
				results.add(InstructionType.raw.cv("$"+Integer.toHexString((c+256)%256)));
			}
			results.add(InstructionType.raw.cv("$00"));
		}
		
		results.add(InstructionType.data_label.cv("__ErrMessageArray"));
		for(byte c:"Array Error!".getBytes()) {
			results.add(InstructionType.raw.cv("$"+Integer.toHexString((c+256)%256)));
		}
		results.add(InstructionType.raw.cv("$00"));
		
		for(SyntaxTree outer:tree.getChildren()) {//compile functions before anything else
			if(outer.getTokenType()==Token.Type.FUNCTION)
			{
				if(tree.functionIsEverCalled(outer.getChild(1).getTokenString())) {//only compile functions that are called somewhere in the code
					ArrayList<Instruction> instructions = generateSubInstructions(outer);
					results.addAll(instructions);
				}
				
			}
				
		}
		
		results.addAll(tree.getGlobalSymbols());
		
		//we need to define everything registered by LibFunctions.
		// first of all, the heap variables
		// heaphead
		// heap
		// heaptail

		results.add(InstructionType.data_label.cv("heaphead"));
		results.add(InstructionType.rawint.cv("0"));
		results.add(InstructionType.rawint.cv("heaptail"));
		results.add(InstructionType.rawint.cv("0"));
		results.add(InstructionType.data_label.cv("heap"));
		results.add(InstructionType.rawspaceints.cv(""+tree.theParser.settings.heapSpace));

		results.add(InstructionType.rawint.cv("heap"));
		results.add(InstructionType.rawint.cv("0"));
		results.add(InstructionType.rawint.cv("0"));
		results.add(InstructionType.data_label.cv("heaptail"));
		
		
		results.add(InstructionType.general_label.cv("__main"));
		if(!startsAtBegin.test(tree.theParser.settings.target)) {
			results.add(InstructionType.write_sp.cv("__ExitLocation"));
		}
		for(SyntaxTree outer:tree.getChildren()) {//compile functions before anything else
			if(outer.getTokenType()!=Token.Type.FUNCTION)
				results.addAll(generateSubInstructions(outer));
		}
		return results;
	}
	private ArrayList<Instruction> deferDeletion(ArrayList<Instruction> instructions) {
		
		ArrayList<Instruction> deletions = new ArrayList<>();
		for(int i=0;i<instructions.size();i++) {
			if(instructions.get(i).in==InstructionType.deffered_delete) {				
				deletions.add(InstructionType.retrieve_local_int.cv(instructions.get(i).getArgs()[0]));
				deletions.add(InstructionType.call_function.cv("free"));
				instructions.remove(i--);
			}
		}
		for(int i=0;i<instructions.size();i++) {
			if(instructions.get(i).in==InstructionType.exit_function) {				
				instructions.addAll(i,deletions);
				i+=deletions.size();
			}
		}
		instructions.addAll(deletions);
		if(deletions.size()!=0)
			System.out.println(instructions);
		return instructions;
	}
	/**
	 * Generates instructions to represent something that resides in the global code rather than the global code itself
	 * @param tree the syntax tree for which to generate instructions
	 * @return a list of instructions
	 */
	private ArrayList<Instruction> generateSubInstructions(SyntaxTree tree)
	{
		
		ArrayList<Instruction> results = new ArrayList<>();
		DataType Float = DataType.Float;
		DataType Uint = DataType.Uint;
		DataType Int = DataType.Int;
		DataType Ubyte = DataType.Ubyte;
		DataType Byte = DataType.Byte;
		DataType Ptr = DataType.Ptr;
		
		CompilationSettings settings = tree.getParser().settings;
		
		Token tok = tree.getToken();
		Token.Type toktype = tok.t;
		DataType type = tree.getType(); 
		String str = tok.s;
		//TODO implement pipelined if branching
		
		
		switch(toktype) {
		case SHIFT_LEFT:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(type==DataType.Float) 
				throw new RuntimeException("Cannot bit shift a float");
			if(type.getSize(settings)==1) {
				results.add(InstructionType.shift_left_b.cv());
			} else {
				results.add(InstructionType.shift_left_i.cv());
			}
			break;
		case SHIFT_RIGHT:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(type==DataType.Float) 
				throw new RuntimeException("Cannot bit shift a float");
			if(type.getSize(settings)==1) {
				if(type.signed())
					results.add(InstructionType.shift_right_b.cv());
				else
					results.add(InstructionType.shift_right_ub.cv());
			} else {
				if(type.signed())
					results.add(InstructionType.shift_right_i.cv());
				else
					results.add(InstructionType.shift_right_ui.cv());
			}
			break;
		case ADD:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(type==DataType.Float) 
				results.add(InstructionType.stackaddfloat.cv());
			else 
				results.add(InstructionType.stackadd.cv());
			if(type.getSize(settings)==1) {
				results.add(new Instruction(InstructionType.truncate));
			}
			break;
		case AS:
			
			DataType typeFrom = tree.getChildren().get(0).getType();
			results.addAll(this.generateSubInstructions(tree.getChild(0)));
			DataType typeTo = typeFromTree(tree.getChildren().get(1));
			if(typeFrom == typeTo) {
				
			} else if(typeFrom==Int && typeTo==Byte) {
				results.add(InstructionType.truncate.cv());
			} else if(typeFrom==Int && typeTo==Ubyte) {
				results.add(InstructionType.truncate.cv());
			} else if(typeFrom==Uint && typeTo==Byte) {
				results.add(InstructionType.truncate.cv());
			} else if(typeFrom==Uint && typeTo==Ubyte) {
				results.add(InstructionType.truncate.cv());
			} else if(typeFrom==Ptr && typeTo==Byte) {
				results.add(InstructionType.truncate.cv());
			} else if(typeFrom==Ptr && typeTo==Ubyte) {
				results.add(InstructionType.truncate.cv());
			}else if(typeFrom==Byte && typeTo==Float) {
				results.add(InstructionType.stackconvertbtofloat.cv());
			} else if(typeFrom==Ubyte && typeTo==Float) {
				results.add(InstructionType.stackconvertubtofloat.cv());
			} else if(typeFrom==Int && typeTo==Float) {
				results.add(InstructionType.stackconverttofloat.cv());
			} else if(typeFrom==Uint && typeTo==Float) {
				results.add(InstructionType.stackconvertutofloat.cv());
			} else if(typeTo==Byte && typeFrom==Float) {
				results.add(InstructionType.stackconverttobyte.cv());
			} else if(typeTo==Ubyte && typeFrom==Float) {
				results.add(InstructionType.stackconverttoubyte.cv());
			} else if(typeTo==Int && typeFrom==Float) {
				results.add(InstructionType.stackconverttoint.cv());
			} else if(typeTo==Uint && typeFrom==Float) {
				results.add(InstructionType.stackconverttouint.cv());
			} else if(typeTo==Int && typeFrom==Byte) {
				//the last case is if a signed byte is casted to a signed int
				//extend the sign.
				results.add(InstructionType.signextend.cv());
			}
			//otherwise, there is no conversion to be done
			
			break;
		case BITWISE_AND:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(new Instruction(InstructionType.stackand));
			break;
		case BITWISE_OR:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(new Instruction(InstructionType.stackor));
			break;
		case BITWISE_XOR:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(new Instruction(InstructionType.stackxor));
			break;
		case BYTE_LITERAL:
			try {
				int x;
				if(str.endsWith("ub")||str.endsWith("bu"))
					x = Integer.parseInt(str.substring(0,str.length()-2));
				else
					x = Integer.parseInt(str.substring(0,str.length()-1));
				if(x<-128||x>255)
					throw new RuntimeException("byte value: "+str+" out of range at line "+tree.getToken().linenum);
				results.add(new Instruction(InstructionType.retrieve_immediate_byte,x+""));
			} catch(NumberFormatException e) {
				throw new RuntimeException("byte value: "+str+" cannot be parsed at line "+tree.getToken().linenum);
			}
		case CLOSE_BRACE:
			break;
		case TEMP:
		case CLOSE_RANGE_EXCLUSIVE:
		case CLOSE_RANGE_INCLUSIVE:
			break;
		case COMPLEMENT:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(type.getSize(settings)==1) {
				results.add(new Instruction(InstructionType.stacknot));
			} else
				results.add(new Instruction(InstructionType.stackcpl));
			break;
		case DECREMENT_LOC:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(new Instruction(InstructionType.decrement_by_pointer_b));
			break;
		case DIVIDE:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			switch(type) {
			case Byte:
				results.add(new Instruction(InstructionType.stackdiv_signed_b));
				break;
			case Float:
				results.add(new Instruction(InstructionType.stackdivfloat));
				break;
			case Int:
				results.add(InstructionType.stackdiv_signed.cv());
				break;
			case Ptr:
			case Uint:
				results.add(InstructionType.stackdiv_unsigned.cv());
				break;
			case Ubyte:
				results.add(InstructionType.stackdiv_unsigned_b.cv());
				break;
			default:
				throw new UnsupportedOperationException("cannot divide type "+type);
			}
			
			break;
		case EMPTY_BLOCK:
			break;
		case EQ_SIGN:
			if(str.equals("assign")) {
				//assign value to a variable
				String find = tree.resolveVariableLocation(tree.getChild(0).getTokenString());
				int typeSize = tree.getChild(1).getType().getSize(settings);
				SyntaxTree.Location location = loc(find.split(" ")[0]);
				String placement = find.split(" ")[1];
				results.addAll(generateSubInstructions(tree.getChild(1)));
				
				switch(location) {
				case ARG:
					if(typeSize==1)
						results.add(new Instruction(InstructionType.put_param_byte,placement));
					else
						results.add(new Instruction(InstructionType.put_param_int,placement));
					break;
				case GLOBAL:
					if(typeSize==1)
						results.add(new Instruction(InstructionType.put_global_byte,placement));
					else
						results.add(new Instruction(InstructionType.put_global_int,placement));
					break;
				case LOCAL:
					if(typeSize==1)
						results.add(new Instruction(InstructionType.put_local_byte,placement));
					else
						results.add(new Instruction(InstructionType.put_local_int,placement));
					break;
				case NONE:
					throw new RuntimeException("attempt to assign to constant symbol at line "+tree.getToken().linenum);
				default:
					throw new RuntimeException(tree.getChild(0).getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
				}
				if(tree.getChildren().size()==3 && tree.getChild(2).getToken().t==Token.Type.TEMP) {
					if(tree.getChild(1).getType().isFreeable())
						results.add(new Instruction(InstructionType.deffered_delete,placement));
					else
						throw new RuntimeException("only pointer-type variables can be temp at line "+tree.getToken().linenum);
				}
				
			} else {
				//equality test
				for(SyntaxTree child:tree.getChildren()) {
					results.addAll(generateSubInstructions(child));
				}
				
				if(tree.getChild(0).getType().getSize(settings)>1) {
					if(tree.getChild(0).getType()!=DataType.Float)
						results.add(InstructionType.equal_to_i.cv());
					else
						results.add(InstructionType.equal_to_f.cv());
				} else {
					results.add(InstructionType.equal_to_b.cv());
				}
			}
			break;
		case FALSE:
			results.add(new Instruction(InstructionType.retrieve_immediate_byte,"0"));
			break;
		case FLOAT_LITERAL:
			if(str.endsWith("f")||str.endsWith("F"))
				str = str.substring(0,str.length()-1);
			results.add(new Instruction(InstructionType.retrieve_immediate_float,str));
			break;
		case FUNCTION_ARG:
		case FUNCTION_ARG_COLON:
		case FUNCTION_ARG_TYPE:
		case FUNCTION_COMMA:
		case FUNCTION_PAREN_L:
		case FUNCTION_NAME:
		case FUNCTION_PAREN_R:
		case FUNCTION_RETTYPE:
			break;
		case FUNC_CALL_NAME:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(new Instruction(InstructionType.call_function,str));
			if(tree.getParser().getFunctionOutputType(str).get(0)!=DataType.Void)
				if(tree.getParent() instanceof SyntaxTree) {
					SyntaxTree parent =(SyntaxTree) tree.getParent();
					if(parent.getTokenType()==Token.Type.OPEN_BRACE) {
						results.add(InstructionType.pop_discard.cv());
					}
				} else {
					results.add(InstructionType.pop_discard.cv());
				}
			break;
		case GEQUAL:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			switch(tree.getChild(0).getType()) {
				case Ptr:
				case Uint:
					results.add(InstructionType.greater_equal_ui.cv());
					break;
				case Int:
					results.add(InstructionType.greater_equal_i.cv());
					break;
				case Ubyte:
					results.add(InstructionType.greater_equal_ub.cv());
					break;
				case Byte:
					results.add(InstructionType.greater_equal_b.cv());
					break;
				case Float:
					results.add(InstructionType.greater_equal_f.cv());
					break;
			default:
				break;
			}
			break;
		case GTHAN:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			switch(tree.getChild(0).getType()) {
				case Ptr:
				case Uint:
					results.add(InstructionType.greater_than_ui.cv());
					break;
				case Int:
					results.add(InstructionType.greater_than_i.cv());
					break;
				case Ubyte:
					results.add(InstructionType.greater_than_ub.cv());
					break;
				case Byte:
					results.add(InstructionType.greater_than_b.cv());
					break;
				case Float:
					results.add(InstructionType.greater_than_f.cv());
					break;
			default:
				break;
			}
			break;
		case IDENTIFIER:
			String find = tree.resolveVariableLocation(tree.getTokenString());
			int typeSize = tree.getType().getSize(settings);
			SyntaxTree.Location location = loc(find.split(" ")[0]);
			String placement = find.split(" ")[1];
			switch(location) {
			case ARG:
				if(typeSize==1)
					results.add(new Instruction(InstructionType.retrieve_param_byte,placement));
				else
					results.add(new Instruction(InstructionType.retrieve_param_int,placement));
				break;
			case GLOBAL:
				if(typeSize==1)
					results.add(new Instruction(InstructionType.retrieve_global_byte,placement));
				else
					results.add(new Instruction(InstructionType.retrieve_global_int,placement));
				break;
			case LOCAL:
				if(typeSize==1)
					results.add(new Instruction(InstructionType.retrieve_local_byte,placement));
				else
					results.add(new Instruction(InstructionType.retrieve_local_int,placement));
				break;
			case NONE:
				if(typeSize==1)
					results.add(new Instruction(InstructionType.retrieve_immediate_byte,placement));
				else
					results.add(new Instruction(InstructionType.retrieve_immediate_int,placement));
				break;
			default:
				throw new RuntimeException(tree.getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
			
			}
			break;
		case IN:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			type = tree.getChild(1).getType();
			results.add(InstructionType.call_function.cv("in"+type.name().toLowerCase()));//in is too complicated to be inlined
			break;
		case INCREMENT_LOC:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(InstructionType.increment_by_pointer_b.cv());
			break;
		case INT_LITERAL:
			try {
				long x;
				if(str.endsWith("u"))
					x = Long.parseLong(str.substring(0,str.length()-1));
				else
					x = Long.parseLong(str);
				final long MAX_INT = (1l << (settings.intsize*8)) - 1;//maximum possible unsigned value
				final long MIN_INT = (~MAX_INT)>>1;
				if(MAX_INT>0)
					if(x<MIN_INT||x>MAX_INT)
						throw new RuntimeException("int value: "+str+" out of range "+MIN_INT+" to "+MAX_INT+" at line "+tree.getToken().linenum);
			} catch(NumberFormatException e) {
				throw new RuntimeException("int value: "+str+" cannot be parsed at line "+tree.getToken().linenum);
			}
			results.add(new Instruction(InstructionType.retrieve_immediate_int,str));
			break;
		case IS:
			throw new RuntimeException("'is' is deprecated. Why did you find this at translation?");
		case LEQUAL:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			switch(tree.getChild(0).getType()) {
				case Int:
					results.add(InstructionType.less_equal_i.cv());
					break;
				case Float:
					results.add(InstructionType.less_equal_f.cv());
					break;
				case Ubyte:
					results.add(InstructionType.less_equal_ub.cv());
					break;
				case Ptr:
				case Uint:
					results.add(InstructionType.less_equal_ui.cv());
					break;
				case Byte:
					results.add(InstructionType.less_equal_b.cv());
			default:
				break;
			}
			break;
		case LOGICAL_AND:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(InstructionType.stackand.cv());
			break;
		case LOGICAL_OR:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			results.add(InstructionType.stackor.cv());
			break;
		case LTHAN:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			switch(tree.getChild(0).getType()) {
				case Int:
					results.add(InstructionType.less_than_i.cv());
					break;
				case Float:
					results.add(InstructionType.less_than_f.cv());
					break;
				case Ubyte:
					results.add(InstructionType.less_than_ub.cv());
					break;
				case Ptr:
				case Uint:
					results.add(InstructionType.less_than_ui.cv());
					break;
				case Byte:
					results.add(InstructionType.less_than_b.cv());
					break;
			default:
				break;
			}
			break;
		case MODULO:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			switch(tree.getType()) {
			case Byte:
				results.add(new Instruction(InstructionType.stackmod_signed_b));
				break;
			case Float:
				results.add(new Instruction(InstructionType.stackmodfloat));
				break;
			case Int:
				results.add(InstructionType.stackmod_signed.cv());
				break;
			case Ptr:
			case Uint:
				results.add(InstructionType.stackmod_unsigned.cv());
				break;
			case Ubyte:
				results.add(InstructionType.stackmod_unsigned_b.cv());
				break;
			default:
				throw new UnsupportedOperationException("cannot divide type "+tree.getType());
			}
			
			break;
		case NEGATE:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(type==DataType.Float) 
			{
				results.add(InstructionType.stacknegfloat.cv());
			}
			else if(type.getSize(settings)>1)
				results.add(InstructionType.stackneg.cv());
			else if(type.getSize(settings)==1) {
				results.add(InstructionType.stacknegbyte.cv());
			}
			break;
		case OPEN_BRACE:
			ArrayList<Instruction> blockElems = new ArrayList<>();

			for(SyntaxTree child:tree.getChildren()) {
				blockElems.addAll(generateSubInstructions(child));
			}
			results.addAll(this.deferDeletion(blockElems));
			break;

		case OPEN_RANGE_EXCLUSIVE:
		case OPEN_RANGE_INCLUSIVE:
			//making a list
			int elements = tree.getChildren().size();
			int elemCounter = 0;
			int elemSize = 2;
			if(elements>=1)
				elemSize = tree.getChild(0).getType().getSize(settings);
			
			results.add(InstructionType.retrieve_immediate_int.cv(""+elements*elemSize));
			results.add(InstructionType.call_function.cv("malloc"));//this value is on the top of the stack. we want to keep it there.
			results.add(InstructionType.copy.cv());//make a copy of our pointer for counting up
			
			for(SyntaxTree child:tree.getChildren()) {
				elemCounter++;
				if(elemCounter!=elements)
					results.add(InstructionType.copy.cv());//this pointer will be used for placing the number
				results.addAll(generateSubInstructions(child));
				if(elemSize==1) {
					results.add(InstructionType.store_b.cv());
					if(elemCounter!=elements)
						results.add(InstructionType.stackincrement.cv());
				} else {
					results.add(InstructionType.store_i.cv());
					if(elemCounter!=elements)
						results.add(InstructionType.stackincrement_intsize.cv());
				}
			}
			break;
		case RANGE_COMMA:

			//we also have to make a call to malloc here too.
			elements = 2;
			elemSize = tree.getChild(1).getType().getSize(settings);
			
			results.add(InstructionType.retrieve_immediate_int.cv(""+elements*elemSize));
			results.add(InstructionType.call_function.cv("malloc"));//this value is on the top of the stack. we want to keep it there.
			results.add(InstructionType.copy.cv());//make a copy of our pointer for counting up
			for(int i=1;i<3;i++) {
				SyntaxTree child = tree.getChild(i);
				if(i==1)
					results.add(InstructionType.copy.cv());//this pointer will be used for placing the number
				results.addAll(generateSubInstructions(child));
				if(elemSize==1) {
					results.add(InstructionType.store_b.cv());
					if(i==1)
						results.add(InstructionType.stackincrement.cv());
				} else {
					results.add(InstructionType.store_i.cv());
					if(i==1)
						results.add(InstructionType.stackincrement_intsize.cv());
				}
			}
			break;
		case POINTER_TO:
			String descriptor;
			if(tree.getToken().guarded())
				descriptor = tree.resolveVariableLocation(tree.getTokenString());
			else
				descriptor = tree.resolveVariableLocation(tree.getTokenString().substring(1));
			String at = descriptor.split(" ")[1];
			switch(IntermediateLang.loc(descriptor.split(" ")[0])) {
				case ARG:
					results.add(InstructionType.retrieve_param_address.cv(at));
					break;
				case LOCAL:
					results.add(InstructionType.retrieve_local_address.cv(at));
					break;
				case GLOBAL:
					results.add(InstructionType.retrieve_global_address.cv(at));
					break;
				case NONE:
					throw new RuntimeException("cannot create pointer to constant at line "+tree.getToken().linenum);
				default:
					throw new RuntimeException("variable is "+descriptor);
			}
			break;
		case RESET:
			SyntaxTree ident = tree.getChild(0);
			find = tree.resolveVariableLocation(ident.getTokenString());
			location = loc(find.split(" ")[0]);
			placement = find.split(" ")[1];
			results.add(InstructionType.retrieve_immediate_byte.cv("0"));
			switch(location) {
			case ARG:
				results.add(new Instruction(InstructionType.put_param_byte,placement));
				break;
			case GLOBAL:
				results.add(new Instruction(InstructionType.put_global_byte,placement));
				break;
			case LOCAL:
				results.add(new Instruction(InstructionType.put_local_byte,placement));
				break;
			case NONE:
				throw new RuntimeException("cannot create pointer to constant at line "+tree.getToken().linenum);
			default:
				throw new RuntimeException(tree.getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
			}
			break;
		case SET:
			ident = tree.getChild(0);
			find = tree.resolveVariableLocation(ident.getTokenString());
			location = loc(find.split(" ")[0]);
			placement = find.split(" ")[1];
			results.add(InstructionType.retrieve_immediate_byte.cv("255"));
			switch(location) {
			case ARG:
				results.add(new Instruction(InstructionType.put_param_byte,placement));
				break;
			case GLOBAL:
				results.add(new Instruction(InstructionType.put_global_byte,placement));
				break;
			case LOCAL:
				results.add(new Instruction(InstructionType.put_local_byte,placement));
				break;
			case NONE:
				throw new RuntimeException("cannot create pointer to constant at line "+tree.getToken().linenum);
			default:
				throw new RuntimeException(tree.getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
			}
			break;
		case RETURN:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(tree.functionIn()==null) {
				//returning from global scope
				results.add(InstructionType.exit_global.cv("__ExitLocation"));
			} else {
				int popping = tree.getParser().getFunctionInputTypes(tree.functionIn()).get(0).size()*settings.intsize;
				results.add(InstructionType.exit_function.cv(""+popping));
			}
			break;
		case STRING_LITERAL:
			String stringnum = str.replace("#", "");
			results.add(InstructionType.retrieve_immediate_int.cv("__String_"+stringnum));
			break;
		case SUBTRACT:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(type==DataType.Float) 
				results.add(InstructionType.stacksubfloat.cv());
			else 
				results.add(InstructionType.stacksub.cv());
			if(type.getSize(settings)==1) {
				results.add(new Instruction(InstructionType.truncate));
			}
			break;
		case TIMES:
			for(SyntaxTree child:tree.getChildren()) {
				results.addAll(generateSubInstructions(child));
			}
			if(type==DataType.Float) 
				results.add(InstructionType.stackmultfloat.cv());
			else 
				results.add(InstructionType.stackmult.cv());
			if(type.getSize(settings)==1) {
				results.add(new Instruction(InstructionType.truncate));
			}
			break;
		case TRUE:
			results.add(InstructionType.retrieve_immediate_byte.cv("255"));
			break;
		case TYPE:
			break;
		case UBYTE_LITERAL:
			results.add(InstructionType.retrieve_immediate_byte.cv(str.replace("ub", "").replace("bu", "")));
			break;
		case UINT_LITERAL:
			results.add(InstructionType.retrieve_immediate_int.cv(str.replace("u", "")));
			break;
		case WHILE:
			long id;
			
			id = fresh();
			results.add(InstructionType.general_label.cv("__while_"+id+"_cond"));
			results.addAll(generateSubInstructions(tree.getChild(0)));
			results.add(InstructionType.branch_not_address.cv("__while_"+id+"_exit"));
			results.addAll(generateSubInstructions(tree.getChild(1)));
			results.add(InstructionType.goto_address.cv("__while_"+id+"_cond"));
			results.add(InstructionType.general_label.cv("__while_"+id+"_exit"));
			
			break;
		case WHILENOT:
			id = fresh();
			results.add(InstructionType.general_label.cv("__whilenot_"+id+"_cond"));
			results.addAll(generateSubInstructions(tree.getChild(0)));
			results.add(InstructionType.branch_address.cv("__whilenot_"+id+"_exit"));
			results.addAll(generateSubInstructions(tree.getChild(1)));
			results.add(InstructionType.goto_address.cv("__whilenot_"+id+"_cond"));
			results.add(InstructionType.general_label.cv("__whilenot_"+id+"_exit"));
			
			break;
		case WITH:
			break;
		case FOR:
			//f'n complicated
			
			results.addAll(this.generateSubInstructions(tree.getChild(1)));
			boolean freeResult = tree.getChild(1).getTokenType()==Token.Type.OPEN_RANGE_EXCLUSIVE || tree.getChild(1).getTokenType()==Token.Type.OPEN_RANGE_INCLUSIVE || tree.getChild(1).getTokenType()==Token.Type.RANGE_COMMA;

			//now the top of the stack is a pointer to either a range or list object.
			//let's find out which
			
			SyntaxTree block = tree.getChild(3);
			SyntaxTree returnloc = block.scanReturn();
			if(returnloc!=null) {
				System.err.println("WARNING: Memory leak at line "+returnloc.getToken().linenum+". Do not return from for loop");
			}
			DataType loopType = tree.getChild(1).getType();
			
			String loopingLocation = block.resolveVariableLocation(tree.getChild(2).getTokenString());
			location = loc(loopingLocation.split(" ")[0]);
			String index = loopingLocation.split(" ")[1];
			boolean byteType = loopType.assignable().getSize(settings)==1;
			id = fresh();
			
			if(loopType.isList) {//list type iterable
				
				//get size of list

				//stack depth				
				
				
				results.add(InstructionType.copy.cv());//2
				results.add(InstructionType.stackdecrement_intsize.cv());
				results.add(InstructionType.load_i.cv());//2
				results.add(InstructionType.retrieve_immediate_int.cv("0"));//3
				//top of stack is [void*] [size_in_bytes] [index]
				
				//start looping
				results.add(InstructionType.general_label.cv("__for_loop_start_"+id));
				results.add(InstructionType.copy2.cv());
				// ptr size_in_bytes index size_in_bytes index
				results.add(InstructionType.less_equal_ui.cv());
				// ptr size_in_bytes index exitFlag
				
				// index has gone beyond the list if exitFlag is set
				results.add(InstructionType.branch_address.cv("__for_loop_exit_"+id));
				// ptr size_in_bytes index
				results.add(InstructionType.swap23.cv());
				// size_in_bytes ptr index
				results.add(InstructionType.copy2.cv());
				// size_in_bytes ptr index ptr index
				results.add(InstructionType.stackadd.cv());
				// size_in_bytes ptr index elemaddress
				//now we dereference elemaddress and mov the result into our looping variable
				
				//then increment index
				if(byteType)
				{
					results.add(InstructionType.load_b.cv());
					if(location==SyntaxTree.Location.GLOBAL)
						results.add(InstructionType.put_global_byte.cv(index));
					else
						results.add(InstructionType.put_local_byte.cv(index));
					results.add(InstructionType.stackincrement.cv());
				}
				else
				{
					results.add(InstructionType.load_i.cv());
					if(location==SyntaxTree.Location.GLOBAL)
						results.add(InstructionType.put_global_int.cv(index));
					else
						results.add(InstructionType.put_local_int.cv(index));
					results.add(InstructionType.stackincrement_intsize.cv());
				}
				// size_in_bytes ptr nextindex
				results.add(InstructionType.swap23.cv());
				// ptr size_in_bytes nextindex
				//this stack setup is perfect for the next iteration
				results.addAll(this.generateSubInstructions(block));
				results.add(InstructionType.goto_address.cv("__for_loop_start_"+id));
				results.add(InstructionType.general_label.cv("__for_loop_exit_"+id));
				//pop 3 items
				results.add(InstructionType.pop_discard.cv());
				results.add(InstructionType.pop_discard.cv());
				if(freeResult)
				{
					results.add(InstructionType.call_function.cv("free"));
				} else {
					results.add(InstructionType.pop_discard.cv());
				}
				
			} else {//range type iterable
				//top of stack is ptr to a range object
				// [rnglow]
				if(freeResult)//hold on to the pointer so we can free it later
				{
					results.add(InstructionType.copy.cv());
				}
				results.add(InstructionType.copy.cv());
				// [rnglow] [rnglow]
				
				//dereference rnghigh and decrement it if it's open above
				
				if(byteType)
				{
					results.add(InstructionType.stackincrement.cv());
					results.add(InstructionType.load_b.cv());
					if(!loopType.closedHigh)
						results.add(InstructionType.stackdecrement_byte.cv());
				}
				else
				{
					results.add(InstructionType.stackincrement_intsize.cv());
					results.add(InstructionType.load_i.cv());
					if(!loopType.closedHigh)
						results.add(InstructionType.stackdecrement.cv());
				}
				// [rnglow] (rnghigh - (0 or 1))
				
				//dereference rnglow and increment if it's open below
				results.add(InstructionType.swap12.cv());
				if(byteType)
				{
					results.add(InstructionType.load_b.cv());
					if(!loopType.closedLow)
						results.add(InstructionType.stackincrement_byte.cv());
					//rnghigh rnglow
					
					
					
					
					//start with the looping variable = rnglow and repeat until variable > rnghigh
					
					
					//rnghigh var
					
					
					//start looping
					results.add(InstructionType.general_label.cv("__for_loop_start_"+id));
					//check if my variable is greater than rnghigh
					results.add(InstructionType.copy2.cv());
					//rnghigh var rnghigh var
					
					//rnghigh rnghigh loopvar
					//if rnghigh < loopvar exit
					if(loopType.assignable().signed())
						results.add(InstructionType.less_than_b.cv());
					else
						results.add(InstructionType.less_than_ub.cv());
					results.add(InstructionType.branch_address.cv("__for_loop_exit_"+id));
					//rnghigh var
					results.add(InstructionType.copy.cv());
					if(location==SyntaxTree.Location.GLOBAL)
						results.add(InstructionType.put_global_byte.cv(index));
					else
						results.add(InstructionType.put_local_byte.cv(index));
					results.addAll(this.generateSubInstructions(block));
					results.add(InstructionType.stackincrement_byte.cv());
					results.add(InstructionType.goto_address.cv("__for_loop_start_"+id));
					results.add(InstructionType.general_label.cv("__for_loop_exit_"+id));
					results.add(InstructionType.pop_discard.cv());
					results.add(InstructionType.pop_discard.cv());
					
				}
				else
				{
					results.add(InstructionType.load_i.cv());
					if(!loopType.closedLow)
						results.add(InstructionType.stackincrement.cv());
					//rnghigh rnglow
					
					
					
					
					//start with the looping variable = rnglow and repeat until variable > rnghigh
					
					
					//rnghigh var
					
					
					//start looping
					results.add(InstructionType.general_label.cv("__for_loop_start_"+id));
					//check if my variable is greater than rnghigh
					results.add(InstructionType.copy2.cv());
					//rnghigh var rnghigh var
					
					//rnghigh rnghigh loopvar
					//if rnghigh < loopvar exit
					if(loopType.assignable().signed())
						results.add(InstructionType.less_than_i.cv());
					else
						results.add(InstructionType.less_than_ui.cv());
					results.add(InstructionType.branch_address.cv("__for_loop_exit_"+id));
					//rnghigh var
					results.add(InstructionType.copy.cv());
					if(location==SyntaxTree.Location.GLOBAL)
						results.add(InstructionType.put_global_int.cv(index));
					else
						results.add(InstructionType.put_local_int.cv(index));
					results.addAll(this.generateSubInstructions(block));
					results.add(InstructionType.stackincrement.cv());
					results.add(InstructionType.goto_address.cv("__for_loop_start_"+id));
					results.add(InstructionType.general_label.cv("__for_loop_exit_"+id));
					results.add(InstructionType.pop_discard.cv());
					results.add(InstructionType.pop_discard.cv());
				}
				if(freeResult)
				{
					results.add(InstructionType.call_function.cv("free"));
				}
				
			}
			
			
			
			
			
			
			break;
		case FUNCTION:
			id = fresh();
			//results.add(InstructionType.goto_address.cv("__function_"+id+"_end"));
			
			String localSpace = tree.getLocalSpaceNeeded()+""; 
			String argumentSpace = ""+tree.theParser.getFunctionInputTypes(tree.functionIn()).get(0).size()*settings.intsize;
			results.add(InstructionType.function_label.cv(tree.getChild(1).getTokenString()));
			results.add(InstructionType.enter_function.cv(localSpace));
			ArrayList<Instruction> functionBody = generateSubInstructions(tree.getChild(tree.getChildren().size()-1));
			
			if(results.get(results.size()-1).in!=InstructionType.exit_function)
				functionBody.add(InstructionType.exit_function.cv(argumentSpace));
			results.addAll(functionBody);
			//results.add(InstructionType.function_label.cv("__function_"+id+"_end"));
			break;
		case IF:
			boolean hasElseBlock = !tree.getChild(2).getTokenType().equals(Token.Type.EMPTY_BLOCK);
			
			id = fresh();
			results.addAll(generateSubInstructions(tree.getChild(0)));
			results.add(InstructionType.branch_not_address.cv("__if_"+id+"_else"));
			results.addAll(generateSubInstructions(tree.getChild(1)));
			if(hasElseBlock)
				results.add(InstructionType.goto_address.cv("__if_"+id+"_exit"));
			results.add(InstructionType.general_label.cv("__if_"+id+"_else"));
			if(hasElseBlock)
				results.addAll(generateSubInstructions(tree.getChild(2)));
			if(hasElseBlock)
				results.add(InstructionType.general_label.cv("__if_"+id+"_exit"));
			
			break;
		case IFNOT:
			hasElseBlock = !tree.getChild(2).getTokenType().equals(Token.Type.EMPTY_BLOCK);
			
			id = fresh();
			results.addAll(generateSubInstructions(tree.getChild(0)));
			results.add(InstructionType.branch_address.cv("__ifnot_"+id+"_else"));
			results.addAll(generateSubInstructions(tree.getChild(1)));
			if(hasElseBlock)
				results.add(InstructionType.goto_address.cv("__ifnot_"+id+"_exit"));
			results.add(InstructionType.general_label.cv("__ifnot_"+id+"_else"));
			if(hasElseBlock)
				results.addAll(generateSubInstructions(tree.getChild(2)));
			if(hasElseBlock)
				results.add(InstructionType.general_label.cv("__ifnot_"+id+"_exit"));
			
			break;
		case ALIAS:
			break;
		case CORRECT:
			for(SyntaxTree child:tree.getChildren())
				results.addAll(this.generateSubInstructions(child));
			results.add(InstructionType.fix_index.cv());
			break;
		
		}
		return results;
	}
}
