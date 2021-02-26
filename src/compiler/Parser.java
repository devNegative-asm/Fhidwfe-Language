package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
	final CompilationSettings settings;
	public Parser(CompilationSettings s) {
		settings = s;
		s.getLibrary().loadLibraryFunctions(this);
	}
	public enum Data{
		Flag(1,false,null,false,false,false),
		Bool(1,false,null,false,false,false),
		Byte(1,false,null,false,false,false),
		Int(2,false,null,false,false,false),
		Float(2,false,null,false,false,false),
		Uint(2,false,null,false,false,false),
		Ubyte(1,false,null,false,false,false),
		Ptr(2,false,null,false,false,false),
		Void(0,false,null,false,false,false),
		Relptr(1,false,null,false,false,false),
		
		Listbyte(2,false,Byte,false,false,true),
		Listint(2,false,Int,false,false,true),
		Listubyte(2,false,Ubyte,false,false,true),
		Listuint(2,false,Uint,false,false,true),
		Listfloat(2,false,Float,false,false,true),
		Listptr(2,false,Ptr,false,false,true),
		
		Rangecc(2,true,Int,true,true,false),//int ranges
		Rangeco(2,true,Int,true,false,false),
		Rangeoc(2,true,Int,false,true,false),
		Rangeoo(2,true,Int,false,false,false),
		
		Urangecc(2,true,Uint,true,true,false),//unsigned int ranges
		Urangeco(2,true,Uint,true,false,false),
		Urangeoc(2,true,Uint,false,true,false),
		Urangeoo(2,true,Uint,false,false,false),
		
		Brangecc(2,true,Uint,true,true,false),//byte ranges
		Brangeco(2,true,Uint,true,false,false),
		Brangeoc(2,true,Uint,false,true,false),
		Brangeoo(2,true,Uint,false,false,false),
		
		Ubrangecc(2,true,Uint,true,true,false),//unsigned byte ranges
		Ubrangeco(2,true,Uint,true,false,false),
		Ubrangeoc(2,true,Uint,false,true,false),
		Ubrangeoo(2,true,Uint,false,false,false),
		
		Frangecc(2,true,Uint,true,true,false),//float ranges
		Frangeco(2,true,Uint,true,false,false),
		Frangeoc(2,true,Uint,false,true,false),
		Frangeoo(2,true,Uint,false,false,false),
		
		SYNTAX(0,false,null,false,false,false),
		
		File(1,false,null,false,false,false);
		
		private final int size;
		private final boolean range;
		private final Data assignable;
		
		
		public final boolean closedLow;
		public final boolean closedHigh;
		public final boolean isList;
		
		
		private Data(int siz, boolean Range, Data assignable, boolean cllow, boolean clhigh, boolean list)
		{
			size=siz;
			this.range=Range;
			this.assignable=assignable;
			isList = list;
			closedLow = cllow;
			closedHigh=clhigh;
		}
		public Data assignable() {
			return assignable;
		}
		public int getSize(Parser p)
		{
			if(size==1)
				return 1;
			if(size==0)
				return 0;
			return p.settings.intsize;
		}
		public int getSize(CompilationSettings p)
		{
			if(size==1)
				return 1;
			if(size==0)
				return 0;
			return p.intsize;
		}
		public boolean assignable(Data x)
		{
			return x==assignable;
		}
		public boolean isRange()
		{
			return range;
		}
		public static Data fromLowerCase(String s)
		{
			return Data.valueOf(Character.toUpperCase(s.charAt(0))+s.substring(1));
		}
		public boolean signed() {
			return this==Int || this==Byte;
		}
	}
	public void registerLibFunction(List<Data> inputs, Data output, String name) {
		fnInputTypes.put(name, new ArrayList<ArrayList<Parser.Data>>(Arrays.asList(new ArrayList<Data>(inputs))));
		fnOutputTypes.put(name, new ArrayList<Parser.Data>(Arrays.asList(output)));
		libAllocated.add(name);
	}
	public void aliasLibFunction(List<Data> inputs, Data output, String name) {
		fnInputTypes.get(name).add(new ArrayList<Data>(inputs));
		fnOutputTypes.get(name).add(output);
	}
	
	private Set<String> libAllocated = new HashSet<String>();
	private Set<String> inlined = new HashSet<String>();
	public void inlineReplace(String fnName) {
		inlined.add(fnName);
	}
	public void verify(ArrayList<IntermediateLang.Instruction> ins) {
		ArrayList<String> alloc = new ArrayList<>();
		alloc.addAll(libAllocated);
		alloc.removeAll(inlined);
		for(IntermediateLang.Instruction in:ins) {
			if(in.in==IntermediateLang.InstructionType.general_label || in.in==IntermediateLang.InstructionType.function_label)
			{
				String labelName = in.args[0];
				alloc.remove(labelName);
			}
			if(in.in==IntermediateLang.InstructionType.define_symbolic_constant)
			{
				String labelName = in.args[0];
				alloc.remove(labelName);
			}
		}
		for(String name:fnInputTypesReq.keySet()) {
			int index = 0;
			if(!fnInputTypes.containsKey(name)) {
				throw new RuntimeException("Required function "+name+" not defined");
			}
			if(!fnInputTypes.get(name).get(0).equals(fnInputTypesReq.get(name).get(0))) {
				throw new RuntimeException("Required function "+name+" must match input type signature "+fnInputTypesReq.get(name).get(0));
			}
			if(!fnOutputTypes.get(name).get(0).equals(fnOutputTypesReq.get(name).get(0))) {
				throw new RuntimeException("Required function "+name+" must match output type signature "+fnOutputTypesReq.get(name).get(0));
			}
			alloc.remove(name);
		}
		if(alloc.isEmpty())
			return;
		throw new RuntimeException("imported constants and/or functions not resolved: "+alloc.toString());
	}
	public ArrayList<String> functionNames() {
		ArrayList<String> names = new ArrayList<>();
		names.addAll(fnInputTypes.keySet());
		names.sort(String::compareTo);
		return names;
	}
	private HashMap<String, ArrayList<ArrayList<Data>>> fnInputTypes = new HashMap<>();
	private HashMap<String, ArrayList<Data>> fnOutputTypes = new HashMap<>();
	public ArrayList<ArrayList<Data>> getFunctionInputTypes(String functionName)
	{
		if(!fnInputTypes.containsKey(functionName))
			throw new RuntimeException("Function "+functionName+" not found");
		return (ArrayList<ArrayList<Data>>) fnInputTypes.get(functionName).clone();
	}
	public List<Data> getFunctionOutputType(String functionName)
	{
		return fnOutputTypes.get(functionName);
	}
	public boolean hasFunction(String functionName)
	{
		return fnOutputTypes.containsKey(functionName);
	}
	
	
	
	private HashMap<String, ArrayList<ArrayList<Data>>> fnInputTypesReq = new HashMap<>();
	private HashMap<String, ArrayList<Data>> fnOutputTypesReq = new HashMap<>();
	public void requireLibFunction(List<Data> asList, Data ptr, String string) {
		fnInputTypesReq.put(string, new ArrayList<ArrayList<Parser.Data>>(Arrays.asList(new ArrayList<Data>(asList))));
		fnOutputTypesReq.put(string, new ArrayList<Parser.Data>(Arrays.asList(ptr)));
	}
	public BaseTree parse(ArrayList<Token> t)
	{
		functionSignatures(t);
		BaseTree tree = new BaseTree(this);
		while(!t.isEmpty())
		{
			tree.addChild(parseOuter(t,tree));
		}
		
		return tree;
	}
	private void functionSignatures(ArrayList<Token> t) {
		try {
			for(int i=0;i<t.size();i++)
			{
				if(t.get(i).t==Token.Type.FUNCTION)
				{
					if(t.get(i+1).t==Token.Type.FUNCTION_RETTYPE)
					{
						String rettype = t.get(i+1).s;
						String rttype = Character.toUpperCase(rettype.charAt(0))+rettype.substring(1);
						Data returnType = null;
						try{
							returnType = Data.valueOf(rttype);
						} catch(Exception e)
						{
							printFuncError("not a valid type",t.get(i+1));
						}
						if(t.get(i+2).t==Token.Type.FUNCTION_NAME)
						{
							String name = t.get(i+2).s;
							if(this.hasFunction(name)){
								throw new RuntimeException("Function "+name+" defined in multiple places at line "+t.get(i+2).linenum);
							}
							fnOutputTypes.put(name, new ArrayList<Parser.Data>(Arrays.asList(returnType)));
							if(t.get(i+3).t==Token.Type.FUNCTION_PAREN_L)
							{
								int argCount = 0;
								ArrayList<Data> args = new ArrayList<Data>();
								while(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_ARG &&
										t.get(i+5+argCount*3).t==Token.Type.FUNCTION_ARG_COLON &&
										t.get(i+6+argCount*3).t==Token.Type.FUNCTION_ARG_TYPE)
								{
									Data argtype = null;
									String type =  t.get(i+6+argCount*3).s;
									type = Character.toUpperCase(type.charAt(0))+type.substring(1);
									try{
										argtype = Data.valueOf(type);
									} catch(Exception e)
									{
										printFuncError("not a valid type",t.get(i+6+argCount*3));
									}
									args.add(argtype);
									argCount++;
								}
								if(t.get(i+5+argCount*3).t==Token.Type.FUNCTION_ARG_COLON && t.get(i+6+argCount*3).t==Token.Type.FUNCTION_ARG_TYPE) {
									printFuncError("used incorrect token type. Expected FUNCTION_ARG, found instead "+t.get(i+4+argCount*3).t,t.get(i+1));
								}
								if(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_ARG && t.get(i+6+argCount*3).t==Token.Type.FUNCTION_ARG_TYPE) {
									printFuncError("used incorrect token type. Expected FUNCTION_ARG_COLON, found instead "+t.get(i+4+argCount*3).t,t.get(i+1));
								}
								if(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_PAREN_R)
								{
									fnInputTypes.put(name, new ArrayList<ArrayList<Parser.Data>>(Arrays.asList(args)));
								} else {
									printFuncError("missing function close paren",t.get(i+1));
								}
							} else {
								printFuncError("missing function open paren",t.get(i+1));
							}
						} else {
							printFuncError("missing function name",t.get(i+1));
						}
					} else {
						printFuncError("missing function return type",t.get(i+1));
					}
				}
			}
			for(int i=0;i<t.size();i++)
			{
				if(t.get(i).t==Token.Type.ALIAS)//alias syntax is the same as function syntax, but the function already has to be defined and its types have to be the same size
				{
					if(t.get(i+1).t==Token.Type.FUNCTION_RETTYPE)
					{
						String rettype = t.get(i+1).s;
						String rttype = Character.toUpperCase(rettype.charAt(0))+rettype.substring(1);
						Data returnType = null;
						try{
							returnType = Data.valueOf(rttype);
						} catch(Exception e)
						{
							printFuncError("not a valid type",t.get(i+1));
						}
						if(t.get(i+2).t==Token.Type.FUNCTION_NAME)
						{
							String name = t.get(i+2).s;
							if(!this.hasFunction(name)){
								throw new RuntimeException("Aliasing nonexistant function "+name+" at line "+t.get(i+2).linenum);
							}
							List<Parser.Data> outTypes = fnOutputTypes.get(name);
							if(outTypes.get(0).size!=returnType.size)
								throw new RuntimeException("Attempted to alias "+name+" which returns "+outTypes.get(0)+" to different sized type "+returnType+" at line "+t.get(i+2).linenum);
							
							if(t.get(i+3).t==Token.Type.FUNCTION_PAREN_L)
							{
								int argCount = 0;
								ArrayList<Data> args = new ArrayList<Data>();
								while(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_ARG &&
										t.get(i+5+argCount*3).t==Token.Type.FUNCTION_ARG_COLON &&
										t.get(i+6+argCount*3).t==Token.Type.FUNCTION_ARG_TYPE)
								{
									Data argtype = null;
									String type =  t.get(i+6+argCount*3).s;
									type = Character.toUpperCase(type.charAt(0))+type.substring(1);
									try{
										argtype = Data.valueOf(type);
									} catch(Exception e)
									{
										printFuncError("not a valid type",t.get(i+6+argCount*3));
									}
									args.add(argtype);
									argCount++;
								}
								if(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_PAREN_R)
								{
									//check that the input types do not clash
									if(fnInputTypes.get(name).contains(args))
										throw new RuntimeException("Aliased function "+name+" must have a different input signature than its alias at line "+t.get(i+2).linenum);
									int argCounter = 0;
									for(Data typeIn:args) {
										if(typeIn.size!=fnInputTypes.get(name).get(0).get(argCounter++).size) {
											throw new RuntimeException("Function alias "+name+"'s inputs must be equivalently sized to the original. First failure at arg #"+argCounter+" of type "+typeIn+" at line "+t.get(i+2).linenum);
										}
									}
									fnInputTypes.get(name).add(args);
									fnOutputTypes.get(name).add(returnType);
								} else {
									printFuncError("missing function close paren",t.get(i+1));
								}
							} else {
								printFuncError("missing function open paren",t.get(i+1));
							}
						} else {
							printFuncError("missing function name",t.get(i+1));
						}
					} else {
						printFuncError("missing function return type",t.get(i+1));
					}
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("expected properly formed function. Found instead EOF");
		}
	}
	private ArrayList<Token> cache;
	private void printParseError(String s) {
		throw new RuntimeException("parsing error on line "+tok.linenum+" starting with "+tok.s+" : "+s);
	}
	private void printFuncError(String s, Token t) {
		throw new RuntimeException("function parsing error on line "+t.linenum+" starting with "+t.s+" : "+s);
	}
	private void pe(String e)
	{
		cache.add(0, tok);
		printParseError(e);
	}
	Token tok;
	private SyntaxTree parseOuter(ArrayList<Token> t, BaseTree parent) {
		final boolean ENABLE_IF_PIPELINING = false;
		//not implemented yet
		if(ENABLE_IF_PIPELINING) {
			if(1>0)
				throw new UnsupportedOperationException("pipelined branching not supported yet");
			
			boolean replacement = false;
			if(t.get(0).t==Token.Type.IF) {
				replacement = true;
				switch(t.get(1).t) {
				case GTHAN:
					t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_GT,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
					break;
				case LTHAN:
					t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_LT,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
					break;
				case EQ_SIGN:
					t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_EQ,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
					break;
				case GEQUAL:
					t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_GE,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
					break;
				case LEQUAL:
					t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_LE,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
					break;
					default:
						replacement=false;
						break;
				}
			}
			if(t.get(0).t==Token.Type.IFNOT) {
				replacement = true;
				switch(t.get(1).t) {
					case GTHAN:
						t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_LE,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
						break;
					case LTHAN:
						t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_GE,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
						break;
					case EQ_SIGN:
						t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_NE,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
						break;
					case GEQUAL:
						t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_LT,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
						break;
					case LEQUAL:
						t.set(0, new Token(t.get(0).s+" "+t.get(1).s,Token.Type.IF_GT,t.get(0).guarded(),t.get(0).srcFile()).setLineNum(t.get(0).linenum));
						break;
					default:
						replacement=false;
						break;
				}
			}
			if(replacement)
				t.remove(1);
		}
		
		
		
		
		cache = t;
		SyntaxTree root = new SyntaxTree(t.get(0),this,parent);
		tok = t.remove(0);
		switch(tok.t) {
			case ALIAS:
				//similar to function parsing except that we don't use a body
				Token retType = t.remove(0);
				if(retType.t!=Token.Type.FUNCTION_RETTYPE)
					pe("expected return type");
				root.addChild(retType);
				
				Token fnname = t.remove(0);
				if(fnname.t!=Token.Type.FUNCTION_NAME)
					pe("expected function name");
				
				root.addChild(fnname);
				
				if(t.remove(0).t!=Token.Type.FUNCTION_PAREN_L)
					pe("expected (");
				
				for(int i=0;i<fnInputTypes.get(fnname.s).get(0).size(); i++) {
					Token param = t.remove(0);
					if(param.t!=Token.Type.FUNCTION_ARG)
						pe("expected function argument");
					
					Token colon = t.remove(0);
					if(colon.t!=Token.Type.FUNCTION_ARG_COLON)
						pe("expected type marker :");
					
					Token ttype = t.remove(0);
					if(ttype.t!=Token.Type.FUNCTION_ARG_TYPE)
						pe("expected argument type");
					
					root.addChild(new SyntaxTree(param,this,root).addChild(ttype));
				}
				if(t.remove(0).t!=Token.Type.FUNCTION_PAREN_R)
					pe("expected ) to end alias definition");
				break;
			case CORRECT:
				pe("result from index corrector '?' unused");
				break;
			case ADD:
				pe("result from + unused");
				break;
			case AS:
				pe("result from as unused");
				break;
			case BITWISE_AND:
				pe("result from & unused");
				break;
			case BITWISE_OR:
				pe("result from | unused");
				break;
			case BITWISE_XOR:
				pe("result from ^ unused");
				break;
			case BYTE_LITERAL:
				pe("cannot use bare bytes");
				break;
			case CLOSE_BRACE:
				pe("no { to close");
				break;
			case CLOSE_RANGE_EXCLUSIVE:
				pe("no range to close");
				break;
			case CLOSE_RANGE_INCLUSIVE:
				pe("no range to close");
				break;
			case COMPLEMENT:
				pe("result from ~ unused");
				break;
			case DECREMENT_LOC:
				root.addChild(parseExpr(t,root,false));
				break;
			case DIVIDE:
				pe("result from / unused");
				break;
			case EMPTY_BLOCK:
				pe("cannot use bare block");
				break;
			case EQ_SIGN:
				pe("result from = unused");
				break;
			case FALSE:
				pe("cannot use bare booleans");
				break;
			case FLOAT_LITERAL:
				pe("cannot use bare floats");
				break;
			case FOR:
				Token forLoopType = t.remove(0);
				switch(forLoopType.s) {
					case "int":
					case "uint":
					case "byte":
					case "ubyte":
						//parse some sort of range
						// then "with"
						// then an identifier
						// then an inner block
							
						root.addChild(forLoopType).addChild(parseExpr(t,root,false));
						/*
						 * For loop possibilities:
						 * range: discrete values that fall in the range
						 * function void -> int: generator, run repeatedly until generates 0
						 * function int -> int: reentrant function, init with 0, run repeatedly until generates 0
						 * range(int, range(int ...) linked list traversal
						 * 
						 * 
						 */
						
						
						
						if(t.remove(0).t!=Token.Type.WITH)
							pe("expected with after for");
						if(t.get(0).t!=Token.Type.IDENTIFIER)
							pe("need identifier to loop over");
						root.addChild(t.remove(0));
						root.addChild(parseBlock(t,root));
						break;
					default:
						pe("type other than int, byte, or their unsigned versions cannot be used in a for loop");
						break;
				}
				
				break;
			case FUNCTION:
				retType = t.remove(0);
				if(retType.t!=Token.Type.FUNCTION_RETTYPE)
					pe("expected return type");
				root.addChild(retType);
				
				fnname = t.remove(0);
				if(fnname.t!=Token.Type.FUNCTION_NAME)
					pe("expected function name");
				
				root.addChild(fnname);
				
				if(t.remove(0).t!=Token.Type.FUNCTION_PAREN_L)
					pe("expected (");
				
				for(int i=0;i<fnInputTypes.get(fnname.s).get(0).size(); i++) {
					Token param = t.remove(0);
					if(param.t!=Token.Type.FUNCTION_ARG)
						pe("expected function argument");
					
					Token colon = t.remove(0);
					if(colon.t!=Token.Type.FUNCTION_ARG_COLON)
						pe("expected type marker :");
					
					Token ttype = t.remove(0);
					if(ttype.t!=Token.Type.FUNCTION_ARG_TYPE)
						pe("expected argument type");
					
					root.addChild(new SyntaxTree(param,this,root).addChild(ttype));
				}
				if(t.remove(0).t!=Token.Type.FUNCTION_PAREN_R)
					pe("expected ) to end function definition");
				root.addChild(parseBlock(t,root));
				break;
			case FUNCTION_ARG:
				pe("cannot place function argument in outer block");
				break;
			case FUNCTION_ARG_COLON:
				pe("cannot specify type here");
				break;
			case FUNCTION_ARG_TYPE:
				pe("cannot specify type here");
				break;
			case FUNCTION_COMMA:
				pe("no range to delimit");
				break;
			case FUNCTION_NAME:
				pe("function keyword must precede name");
				break;
			case FUNCTION_PAREN_L:
				pe("no function identified");
				break;
			case FUNCTION_PAREN_R:
				pe("no function identified");
				break;
			case FUNCTION_RETTYPE:
				pe("no function identified");
				break;
			case FUNC_CALL_NAME:
				String callname = tok.s.substring(0, tok.s.length()-1);
				root = new SyntaxTree(new Token(callname,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				if(this.fnInputTypes.containsKey(callname))	{
					int args = fnInputTypes.get(callname).get(0).size();
					for(int i=0;i<args;i++)
					{
						root.addChild(parseExpr(t,root,false));
					}
				} else {
					pe("function not found");
				}
				break;
			case GEQUAL:
				pe("result from >= unused");
				break;
			case GTHAN:
				pe("result from > unused");
				break;
			case IDENTIFIER:
				Token secondToken = t.remove(0);
				if(secondToken.t!=Token.Type.EQ_SIGN)
				{
					if(this.fnInputTypes.containsKey(root.getToken().s))
					{
						pe("bare identifier. Function calls must be suffixed with $");
					} else {
						pe("bare identifier not used for assignment");
					}
				} else {
					SyntaxTree newRoot = new SyntaxTree(new Token("assign",Token.Type.EQ_SIGN,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
					return newRoot.addChild(new SyntaxTree(root.getToken(),this,newRoot)).addChild(parseExpr(t,newRoot,true));
				}
				break;
			case IF_GE:
			case IF_GT:
			case IF_EQ:
			case IF_LT:
			case IF_NE:
			case IF_LE:
				root.addChild(parseExpr(t,root,false)).addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case SHIFT_LEFT:
				pe("result from shift unused");
				break;
			case SHIFT_RIGHT:
				pe("result from shift unused");
				break;
			case IF:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IFNOT:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IN:
				pe("result from in unused");
				break;
			case INCREMENT_LOC:
				root.addChild(parseExpr(t,root,false));
				break;
			case INT_LITERAL:
				pe("cannot use bare ints");
				break;
			case IS:
				if(t.get(0).t==Token.Type.IDENTIFIER&&t.get(1).t==Token.Type.TYPE) {
					root.addChild(t.remove(0)).addChild(t.remove(0));
				} else {
					pe("cast with is [identifier] [type]");
				}
				break;
			case LEQUAL:
				pe("result from <= unused");
				break;
			case LOGICAL_AND:
				pe("result from && unused");
				break;
			case LOGICAL_OR:
				pe("result from || unused");
				break;
			case LTHAN:
				pe("result from < unused");
				break;
			case MODULO:
				pe("result from % unused");
				break;
			case NEGATE:
				pe("result from ! unused");
				break;
			case OPEN_BRACE:
				pe("cannot use bare block");
				break;
			case OPEN_RANGE_EXCLUSIVE:
				pe("cannot use bare ranges");
				break;
			case OPEN_RANGE_INCLUSIVE:
				pe("cannot use bare ranges");
				break;
			case POINTER_TO:
				pe("cannot use bare pointers");
				break;
			case RANGE_COMMA:
				pe("no range to delimit");
				break;
			case RESET:
				Token ident = t.remove(0);
				if(ident.t==Token.Type.IDENTIFIER) {
					root.addChild(ident);
				} else {
					pe("can only set or reset flags");
				}
				break;
			case RETURN:
				root.addChild(parseExpr(t,root,true)); //return from this
				if(t.size()!=0)
				{
					pe("should not be returning before end of program");
				}
				break;
			case SET:
				ident = t.remove(0);
				if(ident.t==Token.Type.IDENTIFIER) {
					root.addChild(ident);
				} else {
					pe("can only set or reset flags");
				}
				break;
			case STRING_LITERAL:
				pe("cannot use bare strings");
				break;
			case SUBTRACT:
				pe("result from - unused");
				break;
			case TIMES:
				pe("result from * unused");
				break;
			case TRUE:
				pe("cannot use bare booleans");
				break;
			case TYPE:
				pe("cannot specify type here");
				break;
			case UBYTE_LITERAL:
				pe("cannot use bare bytes");
				break;
			case UINT_LITERAL:
				pe("cannot use bare ints");
				break;
			case WHILE:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root));
				break;
			case WHILENOT:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root));
				break;
			case WITH:
				pe("expected with after for");
				break;
		
		}
		return root;
	}
	private SyntaxTree parseInner(ArrayList<Token> t, BaseTree parent) {
		cache = t;
		SyntaxTree root = new SyntaxTree(t.get(0),this,parent);
		tok = t.remove(0);
		switch(tok.t) {
			case CORRECT:
				pe("result from index corrector '?' unused");
				break;
			case ADD:
				pe("result from + unused");
				break;
			case AS:
				pe("result from as unused");
				break;
			case BITWISE_AND:
				pe("result from & unused");
				break;
			case BITWISE_OR:
				pe("result from | unused");
				break;
			case BITWISE_XOR:
				pe("result from ^ unused");
				break;
			case BYTE_LITERAL:
				pe("cannot use bare bytes");
				break;
			case CLOSE_BRACE:
				pe("no { to close");
				break;
			case CLOSE_RANGE_EXCLUSIVE:
				pe("no range to close");
				break;
			case CLOSE_RANGE_INCLUSIVE:
				pe("no range to close");
				break;
			case COMPLEMENT:
				pe("result from ~ unused");
				break;
			case DECREMENT_LOC:
				root.addChild(parseExpr(t,root,false));
				break;
			case DIVIDE:
				pe("result from / unused");
				break;
			case EMPTY_BLOCK:
				pe("cannot use bare block");
				break;
			case EQ_SIGN:
				pe("result from = unused");
				break;
			case FALSE:
				pe("cannot use bare booleans");
				break;
			case FLOAT_LITERAL:
				pe("cannot use bare floats");
				break;
			case FOR:
				Token forLoopType = t.remove(0);
				switch(forLoopType.s) {
					case "int":
					case "uint":
					case "ptr":
					case "byte":
					case "ubyte":
						//parse some sort of range
						// then "with"
						// then an identifier
						// then an inner block
						root.addChild(forLoopType).addChild(parseExpr(t,root,false));
						if(t.remove(0).t!=Token.Type.WITH)
							pe("expected with after for");
						if(t.get(0).t!=Token.Type.IDENTIFIER)
							pe("need identifier to loop over");
						root.addChild(t.remove(0));
						root.addChild(parseBlock(t,root));
						break;
					default:
						pe("type other than ptr, int, byte, or their unsigned versions cannot be used in a for loop");
						break;
				}
				
				break;
			case FUNCTION:
				pe("cannot declare inner functions");
				break;
			case ALIAS:
				pe("cannot declare inner alias");
				break;
			case FUNCTION_ARG:
				pe("cannot place function argument in outer block");
				break;
			case FUNCTION_ARG_COLON:
				pe("cannot specify type here");
				break;
			case FUNCTION_ARG_TYPE:
				pe("cannot specify type here");
				break;
			case FUNCTION_COMMA:
				pe("no range to delimit");
				break;
			case FUNCTION_NAME:
				pe("function keyword must precede name");
				break;
			case FUNCTION_PAREN_L:
				pe("no function identified");
				break;
			case FUNCTION_PAREN_R:
				pe("no function identified");
				break;
			case FUNCTION_RETTYPE:
				pe("no function identified");
				break;
			case FUNC_CALL_NAME:
				String callname = tok.s.substring(0, tok.s.length()-1);
				root = new SyntaxTree(new Token(callname,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				if(this.fnInputTypes.containsKey(callname))	{
					int args = fnInputTypes.get(callname).get(0).size();
					for(int i=0;i<args;i++)
					{
						root.addChild(parseExpr(t,root,false));
					}
				} else {
					pe("function not found");
				}
				break;
			case GEQUAL:
				pe("result from >= unused");
				break;
			case GTHAN:
				pe("result from > unused");
				break;
			case IDENTIFIER:
				Token secondToken = t.remove(0);
				if(secondToken.t!=Token.Type.EQ_SIGN)
				{
					if(this.fnInputTypes.containsKey(root.getToken().s))
					{
						pe("bare identifier. Function calls must be suffixed with $");
					} else {
						pe("bare identifier not used for assignment");
					}
				} else {
					SyntaxTree newRoot = new SyntaxTree(new Token("assign",Token.Type.EQ_SIGN,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
					return newRoot.addChild(new SyntaxTree(root.getToken(),this,newRoot)).addChild(parseExpr(t,newRoot,true));
				}
				break;
			case IF_GE:
			case IF_GT:
			case IF_EQ:
			case IF_LT:
			case IF_NE:
			case IF_LE:
				root.addChild(parseExpr(t,root,false)).addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IF:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IFNOT:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IN:
				pe("result from in unused");
				break;
			case INCREMENT_LOC:
				root.addChild(parseExpr(t,root,false));
				break;
			case INT_LITERAL:
				pe("cannot use bare ints");
				break;
			case IS:
				if(t.get(0).t==Token.Type.IDENTIFIER&&t.get(1).t==Token.Type.TYPE) {
					root.addChild(t.remove(0)).addChild(t.remove(0));
				} else {
					pe("cast with is [identifier] [type]");
				}
				break;
			case LEQUAL:
				pe("result from <= unused");
				break;
			case LOGICAL_AND:
				pe("result from && unused");
				break;
			case LOGICAL_OR:
				pe("result from || unused");
				break;
			case LTHAN:
				pe("result from < unused");
				break;
			case MODULO:
				pe("result from % unused");
				break;
			case NEGATE:
				pe("result from ! unused");
				break;
			case OPEN_BRACE:
				pe("cannot use bare block");
				break;
			case OPEN_RANGE_EXCLUSIVE:
				pe("cannot use bare ranges");
				break;
			case OPEN_RANGE_INCLUSIVE:
				pe("cannot use bare ranges");
				break;
			case POINTER_TO:
				pe("cannot use bare pointers");
				break;
			case RANGE_COMMA:
				pe("no range to delimit");
				break;
			case RESET:
				Token ident = t.remove(0);
				if(ident.t==Token.Type.IDENTIFIER) {
					root.addChild(ident);
				} else {
					pe("can only set or reset flags");
				}
				break;
			case RETURN:
				String function = root.functionIn();
				if(function==null)
				{
					try{
						root.addChild(parseExpr(t,root,true));
					} catch(Exception e){
						//use no return value
					}
				} else {
					Data retType = fnOutputTypes.get(function).get(0);
					if(retType!=Data.Void)
					{
						root.addChild(parseExpr(t,root,true)); //return from this
					}
				}
				break;
			case SET:
				ident = t.remove(0);
				if(ident.t==Token.Type.IDENTIFIER) {
					root.addChild(ident);
				} else {
					pe("can only set or reset flags");
				}
				break;
			case SHIFT_LEFT:
				pe("result from shift unused");
				break;
			case SHIFT_RIGHT:
				pe("result from shift unused");
				break;
			case STRING_LITERAL:
				pe("cannot use bare strings");
				break;
			case SUBTRACT:
				pe("result from - unused");
				break;
			case TIMES:
				pe("result from * unused");
				break;
			case TRUE:
				pe("cannot use bare booleans");
				break;
			case TYPE:
				pe("cannot specify type here");
				break;
			case UBYTE_LITERAL:
				pe("cannot use bare bytes");
				break;
			case UINT_LITERAL:
				pe("cannot use bare ints");
				break;
			case WHILE:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root));
				break;
			case WHILENOT:
				root.addChild(parseExpr(t,root,false)).addChild(parseBlock(t,root));
				break;
			case WITH:
				pe("expected with after for");
				break;
		
		}
		return root;
	}
	private SyntaxTree parseExpr(ArrayList<Token> t, BaseTree parent, boolean inAssignment) {
		tok = t.remove(0);
		SyntaxTree root = new SyntaxTree(tok,this,parent);
		switch(tok.t)
		{
			case ADD:
			case SUBTRACT:
			case TIMES:
			case BITWISE_AND:
			case BITWISE_OR:
			case GEQUAL:
			case LEQUAL:
			case LOGICAL_AND:
			case LOGICAL_OR:
			case LTHAN:
			case MODULO:
			case GTHAN:
			case BITWISE_XOR:
			case DIVIDE:
			case IN:
			case EQ_SIGN:
				root.addChild(parseExpr(t,root,inAssignment)).addChild(parseExpr(t,root,inAssignment));
				break;
			case BYTE_LITERAL:
			case FLOAT_LITERAL:
			case INT_LITERAL:
			case FALSE:
			case TRUE:
			case UBYTE_LITERAL:
			case UINT_LITERAL:
			case IDENTIFIER:
			case STRING_LITERAL:
			case POINTER_TO:
				break;
			case CORRECT:
			case COMPLEMENT:
			case SHIFT_LEFT:
			case SHIFT_RIGHT:
				root.addChild(parseExpr(t,root,inAssignment));
				break;
			case NEGATE:
				root.addChild(parseExpr(t,root,inAssignment));
				break;
			case AS:
				root.addChild(parseExpr(t,root,inAssignment));
				if(t.get(0).t==Token.Type.TYPE) {
					root.addChild(t.remove(0));
				} else {
					pe("cast with as [value] [type]");
				}
				break;
			case FUNC_CALL_NAME:
				String callname = tok.s.substring(0, tok.s.length()-1);
				root = new SyntaxTree(new Token(callname,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				if(this.fnInputTypes.containsKey(callname))	{
					int args = fnInputTypes.get(callname).get(0).size();
					for(int i=0;i<args;i++)
					{
						root.addChild(parseExpr(t,root,inAssignment));
					}
				} else {
					pe("function not found");
				}
				break;
			case OPEN_RANGE_EXCLUSIVE:
			case OPEN_RANGE_INCLUSIVE:
				SyntaxTree possibleRoot = new SyntaxTree(new Token(",",Token.Type.RANGE_COMMA,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				SyntaxTree openBracket = new SyntaxTree(root.getToken(),this,possibleRoot);
				SyntaxTree result1 = parseExpr(t,possibleRoot,inAssignment);
				if(t.get(0).t==Token.Type.RANGE_COMMA) {
					t.remove(0);
					root = possibleRoot;
					root.addChild(openBracket);
					root.addChild(result1);
					root.addChild(parseExpr(t,root,inAssignment));
					Token endChoice = t.remove(0);
					if(endChoice.t!=Token.Type.CLOSE_RANGE_EXCLUSIVE && endChoice.t!=Token.Type.CLOSE_RANGE_INCLUSIVE)
						pe("not a valid way to end a range");
					root.addChild(endChoice);
				} else {
					//this is a list
					root.addChild(result1.copyWithDifferentParent(root));
					while(t.get(0).t!=Token.Type.CLOSE_RANGE_EXCLUSIVE && t.get(0).t!=Token.Type.CLOSE_RANGE_INCLUSIVE) {
						root.addChild(parseExpr(t,root,inAssignment));
					}
					t.remove(0);
				}
				
				
			break;
		}
		return root;
	}
	private SyntaxTree parseBlock(ArrayList<Token> t, SyntaxTree parent) {
		tok = t.remove(0);
		SyntaxTree root = new SyntaxTree(tok,this,parent);
		switch(tok.t)
		{
			case OPEN_BRACE:
				//parseinner repeatedly
				while(t.get(0).t!=Token.Type.CLOSE_BRACE)
				{
					root.addChild(parseInner(t,root));
				}
				t.remove(0);
			case EMPTY_BLOCK:
				break;
			default:
				pe("expected block");
		}
		return root;
	}
	
}
