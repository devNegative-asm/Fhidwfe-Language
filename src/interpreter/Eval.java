package interpreter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import compiler.DataType;
import compiler.Instruction;
import compiler.InstructionType;
import compiler.Lexer;
import compiler.LibFunctions;
import compiler.SyntaxTree;
import compiler.Token;
import interfaceCore.Main;
import settings.CompilationSettings;
import java.util.function.Function;

public class Eval {
	private abstract class Func {
		String[] args;
		public Func(String... args){
			this.args=args;
		}
		abstract void call();
	}
	public final static CompilationSettings replSettings =
			new CompilationSettings(3,1<<23,CompilationSettings.Target.REPL,
					new LibFunctions(CompilationSettings.Target.REPL));
	
	
	private Value globalAllocator, stackPointer;
	private HashMap<String,Value> globalPointers = new HashMap<>();
	private Stack<HashMap<String,Value>> argPointers = new Stack<>();
	private Stack<HashMap<String,Value>> localPointers = new Stack<>();
	private Stack<ArrayList<Value>> temps = new Stack<>();
	private Stack<Value> stackPointers = new Stack<>(); 
	private Value returnValue = Value.VOID;
	private HashMap<String,Func> functions = new HashMap<>();
	boolean justReturned = false;

	public void readGlobals() {
		System.out.println(globalPointers);
	}
	public void readStackFrame() {
		if(!localPointers.empty()) {
			System.out.println(localPointers);
			System.out.println(argPointers);
		}
		System.out.println();
	}
	
	
	private Value call(String func, Value... args) {
		HashMap<String,Value> argMap = new HashMap<>();
		if(!functions.containsKey(func))
			throw new RuntimeException("required library function "+func+" not found");
		Func callingFunc = functions.get(func);
		int argPosition = 0;
		stackPointers.push(stackPointer);
		for(Value arg:args) {
			String argumentName = callingFunc.args[argPosition++];
			stackPointer = stackPointer.sub(new Value(DataType.Ptr,replSettings.intsize,ram));
			argMap.put(argumentName, stackPointer);
			stackPointer.useSelfAsPointerToWriteArgToRAM(arg);
		}
		argPointers.push(argMap);
		localPointers.push(new HashMap<>());
		callingFunc.call();
		justReturned = false;
		stackPointer = stackPointers.pop();
		argPointers.pop();
		localPointers.pop();
		return this.returnValue;
	}
	private Ram24Bit ram;
	private Lexer lexer;
	public Eval(Lexer lex) {
		ram = new Ram24Bit();
		globalAllocator = new Value(DataType.Ptr,0,ram);
		stackPointer = new Value(DataType.Ptr,0,ram);
		this.lexer = lex;
		
		functions.put("put_byte", new Func("location","value"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				Value byteValue = argPointers.peek().get("value").dereference(DataType.Byte);
				targetLocation.useSelfAsPointerToWriteArgToRAM(byteValue);
				returnValue = Value.VOID;
			}
		});
		functions.put("put_ubyte", new Func("location","value"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				Value byteValue = argPointers.peek().get("value").dereference(DataType.Ubyte);
				targetLocation.useSelfAsPointerToWriteArgToRAM(byteValue);
				returnValue = Value.VOID;
			}
		});
		functions.put("put_int", new Func("location","value"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				Value byteValue = argPointers.peek().get("value").dereference(DataType.Int);
				targetLocation.useSelfAsPointerToWriteArgToRAM(byteValue);
				returnValue = Value.VOID;
			}
		});
		functions.put("put_uint", new Func("location","value"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				Value byteValue = argPointers.peek().get("value").dereference(DataType.Uint);
				targetLocation.useSelfAsPointerToWriteArgToRAM(byteValue);
				returnValue = Value.VOID;
			}
		});
		functions.put("put_ptr", new Func("location","value"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				Value byteValue = argPointers.peek().get("value").dereference(DataType.Ptr);
				targetLocation.useSelfAsPointerToWriteArgToRAM(byteValue);
				returnValue = Value.VOID;
			}
		});
		functions.put("deref_ptr", new Func("location"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				returnValue = targetLocation.dereference(DataType.Ptr);
			}
		});
		functions.put("deref_uint", new Func("location"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				returnValue = targetLocation.dereference(DataType.Uint);
			}
		});
		functions.put("deref_int", new Func("location"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				returnValue = targetLocation.dereference(DataType.Int);
			}
		});
		functions.put("deref_byte", new Func("location"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				returnValue = targetLocation.dereference(DataType.Byte);
			}
		});
		functions.put("deref_ubyte", new Func("location"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				returnValue = targetLocation.dereference(DataType.Ubyte);
			}
		});
		functions.put("deref_ubyte", new Func("location"){
			@Override
			void call() {
				Value targetLocation = argPointers.peek().get("location").dereference(DataType.Ptr);
				returnValue = targetLocation.dereference(DataType.Ubyte);
			}
		});
		functions.put("", new Func("location", "number"){
			@Override
			void call() {
				Value func = argPointers.peek().get("location").dereference(DataType.Func);
				Value argument = argPointers.peek().get("number").dereference(DataType.Uint);
				String functionName = func.getFunctionNameString();
				Eval.this.call(functionName, argument);
			}
		});
		functions.put("binop", new Func("location", "arg1", "arg2"){
			@Override
			void call() {
				Value func = argPointers.peek().get("location").dereference(DataType.Func);
				Value argument = argPointers.peek().get("arg1").dereference(DataType.Uint);
				Value argument2 = argPointers.peek().get("arg2").dereference(DataType.Uint);
				String functionName = func.getFunctionNameString();
				Eval.this.call(functionName, argument, argument2);
			}
		});
		/*p.registerLibFunction(Arrays.asList(DataType.Ptr),DataType.Void, "error");
			p.registerLibFunction(Arrays.asList(), DataType.Ubyte, "getc");
			p.registerLibFunction(Arrays.asList(DataType.Ubyte), DataType.Void, "putchar");
			p.registerLibFunction(Arrays.asList(), DataType.Void, "putln");*/
		functions.put("putchar", new Func("char"){
			@Override
			void call() {
				Value letter = argPointers.peek().get("char").dereference(DataType.Byte);
				System.out.write((byte)letter.signedByteValue());
				System.out.flush();
				returnValue = Value.VOID;
			}
		});
		functions.put("putln", new Func(){
			@Override
			void call() {
				System.out.println();
				returnValue = Value.VOID;
			}
		});
		functions.put("getc", new Func(){
			@Override
			void call() {
				int l = Main.getC();
				Eval.this.returnValue = new Value(DataType.Byte,l,ram);
			}
		});
		functions.put("error", new Func("errorString"){
			@Override
			void call() {
				String errorString = argPointers.peek().get("errorString").dereference(DataType.Ptr).derefAsString();
				System.err.println(errorString);
				returnValue = Value.VOID;
			}
		});
	}
	public Value evaluate(SyntaxTree tree) {
		
		Token.Type toktype = tree.getTokenType();
		DataType type = tree.getType();
		

		Token tok = tree.getToken();
		String str = tok.s;
		
		switch(toktype) {
		case SHIFT_LEFT:
			Value v = evaluate(tree.getChild(0));
			return v.shiftLeft();
		case SHIFT_RIGHT:
			v = evaluate(tree.getChild(0));
			return v.shiftRight();
		case ADD:
			return evaluate(tree.getChild(0)).add(evaluate(tree.getChild(1)));
		case AS:
			
			DataType typeTo = typeFromTree(tree.getChildren().get(1));
			v = evaluate(tree.getChild(0));
			DataType typeFrom = v.type;
			
			if(typeFrom.numeric() && typeTo.numeric()) {
				return new Value(typeTo,v.getNative(),ram);
			} else if(typeFrom.numeric() && typeTo==DataType.Float) {
				return new Value((float)v.getNative(),ram);
			} else if(typeFrom==DataType.Float && typeTo.numeric()) {
				return new Value(typeTo,(int)v.floatValue(),ram);
			}
			return new Value(typeTo,v.unsignedIntValue(),ram);
		case BITWISE_AND:
			return evaluate(tree.getChild(0)).and(evaluate(tree.getChild(1)));
		case BITWISE_OR:
			return evaluate(tree.getChild(0)).or(evaluate(tree.getChild(1)));
		case BITWISE_XOR:
			return evaluate(tree.getChild(0)).xor(evaluate(tree.getChild(1)));
		case BYTE_LITERAL:
			try {
				int x;
				if(str.endsWith("ub")||str.endsWith("bu"))
					x = Integer.parseInt(str.substring(0,str.length()-2));
				else
					x = Integer.parseInt(str.substring(0,str.length()-1));
				if(x<-128||x>255)
					return Value.createException("byte value: "+str+" out of range");
				return new Value(str.endsWith("ub")||str.endsWith("bu")?DataType.Ubyte:DataType.Byte,x,ram);
			} catch(NumberFormatException e) {
				Value.createException("byte value: "+str+" cannot be parsed");
			}
		case CLOSE_BRACE:
			break;
		case TEMP:
		case CLOSE_RANGE_EXCLUSIVE:
		case CLOSE_RANGE_INCLUSIVE:
			break;
		case COMPLEMENT:
			return evaluate(tree.getChild(0)).complement();
		case DECREMENT_LOC:
			v = evaluate(tree.getChild(0));
			ram.write(v.unsignedIntValue(), (byte)(ram.access(v.unsignedIntValue())-1));
			return Value.VOID;
		case DIVIDE:
			return evaluate(tree.getChild(0)).div(evaluate(tree.getChild(1)));
		case EMPTY_BLOCK:
			return Value.SYNTAX;
		case RESET:
			String find = resolveVariableLocation(tree,tree.getChild(0).getTokenString());
			SyntaxTree.Location location = loc(find.split(" ")[0]);
			String placement = find.split(" ")[1];
			String varname = tree.getChild(0).getTokenString();
			v = Value.FALSE(ram);
			
			switch(location) {
			case NONE:
			case GLOBAL:
				if(!globalPointers.containsKey(varname)) {
					globalPointers.put(varname, globalAllocator);
					globalAllocator = globalAllocator.add(new Value(DataType.Ptr,1,ram));
				}
				globalPointers.get(varname).useSelfAsPointerToWriteArgToRAM(v);
				break;
			case LOCAL:
				if(!localPointers.peek().containsKey(varname)) {
					stackPointer = stackPointer.sub(new Value(DataType.Ptr,1,ram));
					localPointers.peek().put(varname, stackPointer);
				}
				localPointers.peek().get(varname).useSelfAsPointerToWriteArgToRAM(v);
				break;
			default:
				throw new RuntimeException(tree.getChild(0).getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
			}
			return Value.SYNTAX;
		case SET:
			find = resolveVariableLocation(tree,tree.getChild(0).getTokenString());
			location = loc(find.split(" ")[0]);
			placement = find.split(" ")[1];
			varname = tree.getChild(0).getTokenString();
			v = Value.TRUE(ram);
			
			switch(location) {
			case NONE:
			case GLOBAL:
				if(!globalPointers.containsKey(varname)) {
					globalPointers.put(varname, globalAllocator);
					globalAllocator = globalAllocator.add(new Value(DataType.Ptr,1,ram));
				}
				globalPointers.get(varname).useSelfAsPointerToWriteArgToRAM(v);
				break;
			case LOCAL:
				if(!localPointers.peek().containsKey(varname)) {
					stackPointer = stackPointer.sub(new Value(DataType.Ptr,1,ram));
					localPointers.peek().put(varname, stackPointer);
				}
				localPointers.peek().get(varname).useSelfAsPointerToWriteArgToRAM(v);
				break;
			default:
				throw new RuntimeException(tree.getChild(0).getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
			}
			return Value.SYNTAX;
		case EQ_SIGN:
			if(str.equals("assign")) {
				//assign value to a variable
				find = resolveVariableLocation(tree,tree.getChild(0).getTokenString());
				int typeSize = tree.getChild(1).getType().getSize(Eval.replSettings);
				location = loc(find.split(" ")[0]);
				placement = find.split(" ")[1];
				varname = tree.getChild(0).getTokenString();
				v = evaluate(tree.getChild(1));
				
				switch(location) {
				case ARG:
					argPointers.peek().get(varname).useSelfAsPointerToWriteArgToRAM(v);
					break;
				case NONE:
				case GLOBAL:
					if(!globalPointers.containsKey(varname)) {
						globalPointers.put(varname, globalAllocator);
						globalAllocator = globalAllocator.add(new Value(DataType.Ptr,typeSize,ram));
					}
					globalPointers.get(varname).useSelfAsPointerToWriteArgToRAM(v);
					break;
				case LOCAL:
					if(!localPointers.peek().containsKey(varname)) {
						stackPointer = stackPointer.sub(new Value(DataType.Ptr,typeSize,ram));
						localPointers.peek().put(varname, stackPointer);
					}
					localPointers.peek().get(varname).useSelfAsPointerToWriteArgToRAM(v);
					break;
				default:
					throw new RuntimeException(tree.getChild(0).getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
				}
				if(tree.getChildren().size()==3 && tree.getChild(2).getToken().t==Token.Type.TEMP) {
					if(tree.getChild(1).getType().isFreeable())
						{
							temps.peek().add(localPointers.peek().get(varname));
						}
					else
						throw new RuntimeException("only pointer-type variables can be temp");
				}
				
			} else {
				return evaluate(tree.getChild(0)).equalTo(evaluate(tree.getChild(1)));
			}
			break;
		case FALSE:
			return new Value(DataType.Bool,0,ram);
		case FLOAT_LITERAL:
			if(str.endsWith("f")||str.endsWith("F"))
				str = str.substring(0,str.length()-1);
			Float f = Float.parseFloat(str);
			return new Value(f,ram);
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
			HashMap<String,Value> args = new HashMap<>();
			ArrayList<SyntaxTree> subexprs = tree.getChildren();
			Value[] inputs = new Value[subexprs.size()];
			for(int i=0;i<inputs.length;i++) {
				inputs[i] = evaluate(subexprs.get(i));
			}
			return call(str,inputs);
		case GEQUAL:
			int compare = evaluate(tree.getChild(0)).compareTo(evaluate(tree.getChild(1)));
			if(compare>=0)
				return Value.TRUE(ram);
			else
				return Value.FALSE(ram);
		case GTHAN:
			compare = evaluate(tree.getChild(0)).compareTo(evaluate(tree.getChild(1)));
			if(compare>0)
				return Value.TRUE(ram);
			else
				return Value.FALSE(ram);
		case IDENTIFIER:
			find = resolveVariableLocation(tree,tree.getTokenString());
			int typeSize = tree.getType().getSize(replSettings);
			DataType idType = tree.getType();
			location = loc(find.split(" ")[0]);
			placement = find.split(" ")[1];
			varname = str;
			
			switch(location) {
				case ARG:
					return argPointers.peek().get(varname).dereference(idType);
				case GLOBAL:
					if(typeSize==1)
						return globalPointers.get(varname).dereference(idType);
					else {
						if((tree.getType()==DataType.Func||tree.getType()==DataType.Op)&&functions.containsKey(tree.getTokenString())) {
							return new Value(tree.getTokenString(),ram);//a function pointer
						} else
							return globalPointers.get(varname).dereference(idType);
					}
				case LOCAL:
					return localPointers.peek().get(varname).dereference(idType);
				case NONE:
					if((tree.getType()==DataType.Func||tree.getType()==DataType.Op)&&functions.containsKey(tree.getTokenString())) {
						return new Value(tree.getTokenString(),ram);//a function pointer
					} else
						return Value.resolveConstant(varname,ram);
				default:
					throw new RuntimeException(tree.getTokenString()+"@@contact devs variable somehow evaluated to NONE location");
			
			}
		case IN:
			return call("in"+tree.getChild(1).getType().name().toLowerCase(),evaluate(tree.getChild(0)),evaluate(tree.getChild(1)));
			
		case INCREMENT_LOC:

			v = evaluate(tree.getChild(0));
			ram.write(v.unsignedIntValue(), (byte)(ram.access(v.unsignedIntValue())+1));
			return Value.VOID;
		case INT_LITERAL:
			try {
				long x;
				if(str.endsWith("u"))
					x = Long.parseLong(str.substring(0,str.length()-1));
				else
					x = Long.parseLong(str);
				final long MAX_INT = (1l << (replSettings.intsize*8)) - 1;//maximum possible unsigned value
				final long MIN_INT = (~MAX_INT)>>1;
				if(MAX_INT>0)
					if(x<MIN_INT||x>MAX_INT)
						throw new RuntimeException("int value: "+str+" out of range "+MIN_INT+" to "+MAX_INT+" at line "+tree.getToken().linenum);
				return new Value(str.endsWith("u")?DataType.Uint:DataType.Int,(int)x,ram);
			} catch(NumberFormatException e) {
				throw new RuntimeException("int value: "+str+" cannot be parsed at line "+tree.getToken().linenum);
			}
		case IS:
			throw new RuntimeException("'is' is deprecated. Why did you find this at translation?");
		case LEQUAL:
			compare = evaluate(tree.getChild(0)).compareTo(evaluate(tree.getChild(1)));
			if(compare<=0)
				return Value.TRUE(ram);
			else
				return Value.FALSE(ram);
		case LOGICAL_AND:
			Value a = evaluate(tree.getChild(0));
			Value b = evaluate(tree.getChild(1));
			if(a.type==DataType.Bool && a.type==b.type) {
				return a.and(b);
			} else {
				return Value.createException("cannot logical and types "+a.type+" and "+b.type);
			}
		case LOGICAL_OR:
			a = evaluate(tree.getChild(0));
			b = evaluate(tree.getChild(1));
			if(a.type==DataType.Bool && a.type==b.type) {
				return a.or(b);
			} else {
				return Value.createException("cannot logical and types "+a.type+" and "+b.type);
			}
		case LTHAN:
			compare = evaluate(tree.getChild(0)).compareTo(evaluate(tree.getChild(1)));
			if(compare<0)
				return Value.TRUE(ram);
			else
				return Value.FALSE(ram);
		case MODULO:
			return evaluate(tree.getChild(0)).modulo(evaluate(tree.getChild(1)));
		case NEGATE:
			return evaluate(tree.getChild(0)).negate();
		case OPEN_BRACE:
			temps.push(new ArrayList<>());
			for(SyntaxTree child:tree.getChildren()) {
				evaluate(child);
				if(justReturned)
					break;
			}
			Value saveReturn = this.returnValue;
			for(Value freePtr:temps.pop()) {
				call("free",freePtr);
			}
			returnValue = saveReturn;
			return Value.SYNTAX;
		case OPEN_RANGE_EXCLUSIVE:
		case OPEN_RANGE_INCLUSIVE:
			//making a list
			int elements = tree.getChildren().size();
			int elemSize = 2;
			if(elements>=1)
				elemSize = tree.getChild(0).getType().getSize(Eval.replSettings);
			
			Value list = call("malloc",new Value(DataType.Uint,elements*elemSize,ram));
			Value writer = list;
			for(SyntaxTree child:tree.getChildren()) {
				Value element = evaluate(child);
				writer.useSelfAsPointerToWriteArgToRAM(element);
				writer = writer.add(new Value(DataType.Ptr,elemSize,ram));
			}
			return new Value(tree.getType(),list.unsignedIntValue(),ram);
		case RANGE_COMMA:

			//we also have to make a call to malloc here too.
			elements = 2;
			elemSize = tree.getChild(1).getType().getSize(Eval.replSettings);
			Value totalSize = new Value(DataType.Uint,(elements*elemSize+1),ram);
			list = call("malloc",totalSize);
			writer = list;
			
			
			boolean exclusiveLow = false;
			boolean exclusiveHigh = false;
			if(tree.getChild(0).getTokenType() == Token.Type.OPEN_RANGE_EXCLUSIVE)
				exclusiveLow = true;
			if(tree.getChild(3).getTokenType() == Token.Type.CLOSE_RANGE_EXCLUSIVE)
				exclusiveHigh = true;
			final byte finalByte =(byte) ((exclusiveLow? 0:1) | (exclusiveHigh?0:2));
			
			Value lowerBound = evaluate(tree.getChild(1));
			Value upperBound = evaluate(tree.getChild(2));
			writer.useSelfAsPointerToWriteArgToRAM(lowerBound);
			writer = writer.add(new Value(DataType.Ptr,elemSize,ram));
			writer.useSelfAsPointerToWriteArgToRAM(upperBound);
			writer = writer.add(new Value(DataType.Ptr,elemSize,ram));
			writer.useSelfAsPointerToWriteArgToRAM(new Value(DataType.Byte,finalByte,ram));
			return list;
		case POINTER_TO:
			String descriptor;
			
			str = tree.getTokenString().substring(1);
			descriptor = resolveVariableLocation(tree,tree.getTokenString().substring(1));
			switch(loc(descriptor.split(" ")[0])) {
				case ARG:
					return this.argPointers.peek().get(str);
				case LOCAL:
					return this.localPointers.peek().get(str);
				case GLOBAL:
					return this.globalPointers.get(str);
				case NONE:
					throw new RuntimeException("cannot create pointer to constant");
				default:
					throw new RuntimeException("variable is "+descriptor);
			}
		
		case RETURN:
			if(tree.getChildren().size()==0) {
				returnValue = Value.VOID;
			} else {
				v = evaluate(tree.getChild(0));
				this.returnValue = v;
			}
				this.justReturned =true;
			return Value.SYNTAX;
		case STRING_LITERAL:
			String stringnum = str.replace("#", "");
			if(globalPointers.containsKey("__String_"+stringnum)) {
				
			} else {
				globalPointers.put("__String_"+stringnum, globalAllocator);
				Byte[] bytes = lexer.stringConstants().get(Integer.parseInt(stringnum));
				for(int i=0;i<bytes.length;i++) {
					globalAllocator.useSelfAsPointerToWriteArgToRAM(new Value(DataType.Ubyte,bytes[i],ram));
					globalAllocator = globalAllocator.add(new Value(DataType.Ptr,1,ram));
				}
				globalAllocator.useSelfAsPointerToWriteArgToRAM(new Value(DataType.Ubyte,0,ram));
				globalAllocator = globalAllocator.add(new Value(DataType.Ptr,1,ram));
			}
			
			return globalPointers.get("__String_"+stringnum);
		case SUBTRACT:
			return evaluate(tree.getChild(0)).sub(evaluate(tree.getChild(1)));
		case TIMES:
			return evaluate(tree.getChild(0)).mul(evaluate(tree.getChild(1)));
		case TRUE:
			return Value.TRUE(ram);
		case TYPE:
			break;
		case UBYTE_LITERAL:
			int byteval = Integer.parseInt(str.replace("ub", "").replace("bu", ""));
			return new Value(DataType.Ubyte,byteval,ram);
		case UINT_LITERAL:
			int intval = Integer.parseInt(str.replace("u", ""));
			return new Value(DataType.Uint,intval,ram);
		case WHILE:
			SyntaxTree condition = tree.getChild(0);
			while(evaluate(condition).isTruthy()) {
				evaluate(tree.getChild(1));
				if(this.justReturned)
					break;
			}
			return Value.SYNTAX;
		case WHILENOT:
			condition = tree.getChild(0);
			while(evaluate(condition).isFalsy()) {
				evaluate(tree.getChild(1));
				if(this.justReturned)
					break;
			}
			return Value.SYNTAX;
		case WITH:
			break;
		case FOR:
			//f'n complicated
			Value rangeOrList = evaluate(tree.getChild(1));
			boolean freeResult = tree.getChild(1).getTokenType()==Token.Type.OPEN_RANGE_EXCLUSIVE || tree.getChild(1).getTokenType()==Token.Type.OPEN_RANGE_INCLUSIVE || tree.getChild(1).getTokenType()==Token.Type.RANGE_COMMA; 
			
			//now the top of the stack is a pointer to either a range or list object.
			//let's find out which
			
			SyntaxTree block = tree.getChild(3);
			SyntaxTree returnloc = block.scanReturn();
			if(returnloc!=null) {
				System.err.println("WARNING: Memory leak at line "+returnloc.getToken().linenum+". Do not return from for loop");
			}
			DataType loopType = tree.getChild(1).getType();
			
			String loopingLocation = resolveVariableLocation(block,tree.getChild(2).getTokenString());
			location = loc(loopingLocation.split(" ")[0]);
			HashMap<String,Value> scope = this.currentScope(location);
			
			
			int iterableSize = loopType.assignable().getSize(Eval.replSettings);
			boolean byteType = iterableSize==1;
			
			
			if(loopType.isList) {//list type iterable
				int length = rangeOrList.dereference(DataType.Ptr, -replSettings.intsize).unsignedIntValue() / loopType.assignable().getSize(replSettings);
				
				for(int loopIndex=0;loopIndex<length;loopIndex++) {
					scope.put(tree.getChild(2).getTokenString(), rangeOrList.add(loopIndex*iterableSize));
					evaluate(block);
					if(this.justReturned)
						return Value.SYNTAX;
				}
			} else {//range type iterable
				int low = rangeOrList.dereference(loopType.assignable()).getNative();
				int high = rangeOrList.dereference(loopType.assignable(),iterableSize).getNative();
				int flags = rangeOrList.dereference(loopType.assignable(),iterableSize*2).signedByteValue();
				exclusiveHigh = (flags&2)==0;
				exclusiveLow = (flags&1)==0;
				this.stackPointer = stackPointer.add(-3);
				this.stackPointers.push(stackPointer);
				scope.put(tree.getChild(2).getTokenString(), stackPointer);
				for(int actualValue = exclusiveLow? low+1:low;
						exclusiveHigh?(actualValue<high):(actualValue<=high);
						actualValue++) {
					stackPointer = stackPointers.peek();
					stackPointer.useSelfAsPointerToWriteArgToRAM(new Value(loopType.assignable(), actualValue, ram));
					evaluate(block);
					if(this.justReturned)
						return Value.SYNTAX;
				}
				this.stackPointer = stackPointers.pop().add(3);
				
				
			}
			if(freeResult) {
				call("free",rangeOrList);
			}
			return Value.SYNTAX;
			
		case FUNCTION:
			
			//children: (rettype) (fnname) args... (block)
			List<SyntaxTree> fnArgs = tree.getChildren().subList(2, tree.getChildren().size()-1);
			String[] fnArgArray = new String[fnArgs.size()];
			final SyntaxTree functionBody = tree.getChild(tree.getChildren().size()-1);
			String functionName = tree.getChild(1).getTokenString();
			for(int i=0;i<fnArgArray.length;i++) {
				fnArgArray[i] = fnArgs.get(i).getTokenString();
			}
			
			Func funcDefining = new Func(fnArgArray){
				@Override
				void call() {
					evaluate(functionBody);
				}
			};
			functions.put(functionName, funcDefining);
			
			
			return Value.SYNTAX;
			
		case IF_LE:
		case IF_GE:
		case IF_GT:
		case IF_LT:
		case IF_EQ:
		case IF_NE:
			SyntaxTree mainBlock = tree.getChild(2);
			SyntaxTree elseBlock = tree.getChild(3);
			DataType comparisonType = tree.getChild(0).getType();
			Function<Value,Function<Value,Boolean>> comparator;
			switch(tok.t.toString().substring(3)) {
				case "LE":
					comparator = x -> y -> x.getNative()<=y.getNative();
					break;
				case "GE":
					comparator = x -> y -> x.getNative()>=y.getNative();
					break;
				case "GT":
					comparator = x -> y -> x.getNative()>y.getNative();
					break;
				case "LT":
					comparator = x -> y -> x.getNative()<y.getNative();
					break;
				case "EQ":
					comparator = x -> y -> x.getNative()==y.getNative();
					break;
				case "NE":
					comparator = x -> y -> x.getNative()!=y.getNative();
					break;
				default:
					comparator = x->{throw new RuntimeException("unknown comparison operator "+tok.t.toString());};
			}
			if(comparator.apply(evaluate(tree.getChild(0))).apply(evaluate(tree.getChild(1)))) {
				evaluate(mainBlock);
			} else {
				evaluate(elseBlock);
			}
			return Value.SYNTAX;
		case IF:
			mainBlock = tree.getChild(1);
			elseBlock = tree.getChild(2);

			condition = tree.getChild(0);
			
			if(evaluate(condition).isTruthy()) {
				evaluate(mainBlock);
			} else {
				evaluate(elseBlock);
			}
			return Value.SYNTAX;
		case IFNOT:
			condition = tree.getChild(0);
			mainBlock = tree.getChild(1);
			elseBlock = tree.getChild(2);
			
			if(evaluate(condition).isFalsy()) {
				evaluate(mainBlock);
			} else {
				evaluate(elseBlock);
			}
			return Value.SYNTAX;
		case ALIAS:
			break;
		case CORRECT:
			return evaluate(tree.getChild(0)).mul(3);
		}
		return Value.SYNTAX;
	}
	private static SyntaxTree.Location loc(String resolved) {
		return SyntaxTree.Location.valueOf(resolved);
	}
	private HashMap<String,Value> currentScope(SyntaxTree.Location l) {
		switch(l) {
		case ARG:
			return this.argPointers.peek();
		case LOCAL:
			return this.localPointers.peek();
		case GLOBAL:
			return this.globalPointers;
		}
		return null;
	}
	private DataType typeFromTree(SyntaxTree t) {
		return DataType.fromLowerCase(t.getTokenString());
	}
	private String resolveVariableLocation(SyntaxTree tree, String var) {
		if(!localPointers.empty())
			if(localPointers.peek().containsKey(var))
				return "LOCAL "+var;
		if(!argPointers.empty())
			if(argPointers.peek().containsKey(var))
				return "ARG "+var;
		
		try {
			String attempt = tree.resolveVariableLocation(var);
			if(attempt.startsWith("NONE") && globalPointers.containsKey(var)) {
				return "GLOBAL "+var;
			}
			return attempt;
		} catch(Exception e) {
			if(tree.inFunction()) {
				return "LOCAL "+var;
			} else {
				return "GLOBAL "+var;
			}
		}
	}
}
