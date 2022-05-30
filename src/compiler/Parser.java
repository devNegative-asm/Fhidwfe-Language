package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import settings.CompilationSettings;
import types.DataType;

public class Parser {
	public final CompilationSettings settings;
	public Parser(CompilationSettings s) {
		settings = s;
		s.getLibrary().loadLibraryFunctions(this);
	}
	public CompilationSettings getSettings() {
		return settings;
	}
	/**
	 * Register an external or inlined function with some type signature
	 * @param inputs input types
	 * @param output output type
	 * @param name function name
	 */
	public void registerLibFunction(List<DataType> inputs, DataType output, String name) {
		fnInputTypes.put(name, new ArrayList<ArrayList<DataType>>(Arrays.asList(new ArrayList<DataType>(inputs))));
		fnOutputTypes.put(name, new ArrayList<DataType>(Arrays.asList(output)));
		libAllocated.add(name);
	}
	/**
	 * Give a type alias to library functions
	 * @param inputs aliased input types
	 * @param output aliased output type
	 * @param name function name
	 */
	public void aliasLibFunction(List<DataType> inputs, DataType output, String name) {
		fnInputTypes.get(name).add(new ArrayList<DataType>(inputs));
		fnOutputTypes.get(name).add(output);
	}
	
	private Set<String> libAllocated = new HashSet<String>();
	private Set<String> inlined = new HashSet<String>();
	public void inlineReplace(String fnName) {
		inlined.add(fnName);
	}
	/**
	 * @param fnName the function name
	 * @return whether this parser has been notified of an inline implementation of the given function
	 */
	public boolean isInlined(String fnName) {
		return inlined.contains(fnName);
	}
	/**
	 * Verify that the given list of instructions has implementations for each required function that was not already inlined, throws runtimeexception otherwise
	 * @param ins the list of instructions
	 */
	public void verify(ArrayList<Instruction> ins) {
		ArrayList<String> alloc = new ArrayList<>();
		alloc.addAll(libAllocated);
		alloc.removeAll(inlined);
		for(Instruction in:ins) {
			if(in.in==InstructionType.general_label || in.in==InstructionType.function_label)
			{
				String labelName = in.args[0];
				alloc.remove(labelName);
			}
			if(in.in==InstructionType.define_symbolic_constant)
			{
				String labelName = in.args[0];
				alloc.remove(labelName);
			}
		}
		for(String name:fnInputTypesReq.keySet()) {
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
	/**
	 * Get a list of all the function names
	 * @return the function names
	 */
	public ArrayList<String> functionNames() {
		ArrayList<String> names = new ArrayList<>();
		names.addAll(fnInputTypes.keySet());
		names.sort(String::compareTo);
		return names;
	}
	private HashMap<String, ArrayList<ArrayList<DataType>>> fnInputTypes = new HashMap<>();
	private HashMap<String, ArrayList<DataType>> fnOutputTypes = new HashMap<>();
	/**
	 * Get the set of each possible list of input types as defined by this function's aliases
	 * @param functionName the function
	 * @return a list of aliased input type signatures
	 */
	public ArrayList<ArrayList<DataType>> getFunctionInputTypes(String functionName)
	{
		if(!fnInputTypes.containsKey(functionName))
			throw new RuntimeException("Function "+functionName+" not found");
		return new ArrayList<ArrayList<DataType>>(fnInputTypes.get(functionName));
	}
	/**
	 * Get the list of each possible output type as defined by this function's aliases
	 * @param functionName the function
	 * @return a list of aliased output types
	 */
	public List<DataType> getFunctionOutputType(String functionName)
	{
		if(fnOutputTypes.containsKey(functionName))
			return fnOutputTypes.get(functionName);
		return this.fnOutputTypesReq.get(functionName);
	}
	/**
	 * Get the collapsed set of output types, removing duplicates, as defined by this function's aliases
	 * @param functionName the function
	 * @return a set of aliased output types
	 */
	public Set<DataType> getCollapsedFunctionOutputTypes(String functionName) {
		HashSet<DataType> result = new HashSet<>();
		getFunctionOutputType(functionName).forEach(result::add);
		return result;
	}
	/**
	 * Whether the given function exists
	 * @param functionName the function
	 */
	public boolean hasFunction(String functionName)
	{
		return fnOutputTypes.containsKey(functionName);
	}
	
	
	
	private HashMap<String, ArrayList<ArrayList<DataType>>> fnInputTypesReq = new HashMap<>();
	private HashMap<String, ArrayList<DataType>> fnOutputTypesReq = new HashMap<>();
	/**
	 * Notify the parser that the given function must be implemented and that the implementation must be given in .fwf rather than be external or inlined
	 * @param asList the required input types
	 * @param ptr the required output type
	 * @param string the function name
	 */
	public void requireLibFunction(List<DataType> asList, DataType ptr, String string) {
		fnInputTypesReq.put(string, new ArrayList<ArrayList<DataType>>(Arrays.asList(new ArrayList<DataType>(asList))));
		fnOutputTypesReq.put(string, new ArrayList<DataType>(Arrays.asList(ptr)));
	}
	/**
	 * Parse the given token list into a syntaxtree
	 * @param t the token list
	 * @return the syntax tree
	 */
	public BaseTree parse(ArrayList<Token> t)
	{
		findTypes(t);
		functionSignatures(t);
		BaseTree tree = new BaseTree(this);
		while(!t.isEmpty())
		{
			SyntaxTree st = parseOuter(t,tree);
			if(st!=null)
				tree.addChild(st);
		}
		
		return tree;
	}
	private void findTypes(ArrayList<Token> t) {
		for(int i=0;i<t.size()-1;i++) {
			if(t.get(i).t==Token.Type.TYPE_DEFINITION) {
				DataType.makeUserType(t.get(i+1).tokenString());
			}
		}
	}
	public ArrayList<SyntaxTree> parseAdditional(BaseTree originalBase, ArrayList<Token> t){
		findTypes(t);
		functionSignatures(t);
		ArrayList<SyntaxTree> trees = new ArrayList<>();
		while(!t.isEmpty()) {
			SyntaxTree sub = null;
			ArrayList<Token> backup = new ArrayList<>(t);
			try {
				sub = parseOuter(t,originalBase);
			} catch(Exception e) {
				try {
					sub = parseExpr(backup, originalBase);
				} catch(Exception b) {
					b.initCause(e);
					throw b;
				}
				t = backup;
			}
			trees.add(sub);
			originalBase.addChild(sub);
		}
		return trees;
	}
	/**
	 * To allow mutually recursive functions and defining functions out of order with the code that calls them, this method scans the token list for functions and stores their signatures
	 * @param t token list
	 */
	private void functionSignatures(ArrayList<Token> t) {
		try {
			int typeDepth = 0;
			DataType typeIn = null;
			String type = "";
			for(int i=0;i<t.size();i++)
			{
				if(typeDepth>=1 && t.get(i).t==Token.Type.TYPE_DEFINITION) {
					pe("cannot define nested types");
				}
				if(typeDepth==0 && t.get(i).t==Token.Type.TYPE_DEFINITION) {
					typeDepth++;
					String typename = t.get(i+1).s;
					typeIn = DataType.makeUserType(typename);
					type = typename+".";
					i+=2;
					continue;
				}
				if(typeDepth > 0 && (t.get(i).t==Token.Type.OPEN_RANGE_EXCLUSIVE || t.get(i).t==Token.Type.OPEN_RANGE_INCLUSIVE))
					typeDepth++;
				if(typeDepth > 0 && (t.get(i).t==Token.Type.CLOSE_RANGE_EXCLUSIVE || t.get(i).t==Token.Type.CLOSE_RANGE_INCLUSIVE))
					typeDepth--;
				if(typeDepth<=0) {
					type="";
					typeDepth = 0;
				}
			}
			typeDepth = 0;
			typeIn = null;
			type = "";
			
			for(int i=0;i<t.size();i++)
			{
				if(typeDepth>=1 && t.get(i).t==Token.Type.TYPE_DEFINITION) {
					pe("cannot define nested types");
				}
				if(typeDepth==0 && t.get(i).t==Token.Type.TYPE_DEFINITION) {
					typeDepth++;
					String typename = t.get(i+1).s;
					typeIn = DataType.makeUserType(typename);
					type = typename+".";
					i+=2;
					continue;
				}
				if(typeDepth > 0 && (t.get(i).t==Token.Type.OPEN_RANGE_EXCLUSIVE || t.get(i).t==Token.Type.OPEN_RANGE_INCLUSIVE))
					typeDepth++;
				if(typeDepth > 0 && (t.get(i).t==Token.Type.CLOSE_RANGE_EXCLUSIVE || t.get(i).t==Token.Type.CLOSE_RANGE_INCLUSIVE))
					typeDepth--;
				if(typeDepth<=0) {
					type="";
					typeDepth = 0;
				}
				
				if(typeDepth==1 && t.get(i).t==Token.Type.IDENTIFIER) {
					if(t.get(i+1).t==Token.Type.FUNCTION_ARG_COLON)
						if(t.get(i+2).t==Token.Type.TYPE) {
							String fieldName = t.get(i).unguardedVersion().tokenString();
							String fieldType = t.get(i+2).unguardedVersion().tokenString();
							String properTypeName = Character.toUpperCase(fieldType.charAt(0)) + fieldType.substring(1);
							DataType fieldDataType = DataType.valueOf(properTypeName);
							DataType.valueOf(type.substring(0,type.length()-1)).addField(fieldName, fieldDataType);
							i++;
							continue;
						} else {
							throw new RuntimeException("Expected proper field type in definition of "+t.get(i).unguardedVersion().tokenString()+" at line "+t.get(i).linenum);
						}
				}
				if(t.get(i).t==Token.Type.EXTERN) {
					if(t.get(i+1).t==Token.Type.FUNCTION_RETTYPE || t.get(i+1).t==Token.Type.TYPE)
					{
						String rettype = t.get(i+1).s;
						String rttype = Character.toUpperCase(rettype.charAt(0))+rettype.substring(1);
						DataType returnType = null;
						try{
							returnType = DataType.valueOf(rttype);
						} catch(Exception e)
						{
							printFuncError("not a valid type",t.get(i+1));
						}
						if(t.get(i+2).t==Token.Type.FUNCTION_NAME)
						{
							String name = type+t.get(i+2).s;
							if(this.hasFunction(name)){
								throw new RuntimeException("Function "+name+" defined in multiple places at line "+t.get(i+2).linenum);
							}
							//fnOutputTypes.put(name, new ArrayList<DataType>(Arrays.asList(returnType)));
							if(t.get(i+3).t==Token.Type.FUNCTION_PAREN_L)
							{
								int argCount = 0;
								ArrayList<DataType> args = new ArrayList<DataType>();
								while(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_ARG &&
										t.get(i+5+argCount*3).t==Token.Type.FUNCTION_ARG_COLON &&
										t.get(i+6+argCount*3).t==Token.Type.FUNCTION_ARG_TYPE)
								{
									DataType argtype = null;
									String argType =  t.get(i+6+argCount*3).s;
									argType = Character.toUpperCase(argType.charAt(0))+argType.substring(1);
									try{
										argtype = DataType.valueOf(argType);
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
									this.asmExternFunction(args, returnType, name);
									//fnInputTypes.put(name, new ArrayList<ArrayList<DataType>>(Arrays.asList(args)));
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
				} else if(t.get(i).t==Token.Type.FUNCTION)
				{
					if(t.get(i+1).t==Token.Type.FUNCTION_RETTYPE || t.get(i+1).t==Token.Type.TYPE)
					{
						String rettype = t.get(i+1).s;
						String rttype = Character.toUpperCase(rettype.charAt(0))+rettype.substring(1);
						DataType returnType = null;
						try{
							returnType = DataType.valueOf(rttype);
						} catch(Exception e)
						{
							printFuncError("not a valid type",t.get(i+1));
						}
						if(t.get(i+2).t==Token.Type.FUNCTION_NAME)
						{
							String name = type+t.get(i+2).s;
							if(this.hasFunction(name)){
								throw new RuntimeException("Function "+name+" defined in multiple places at line "+t.get(i+2).linenum);
							}
							fnOutputTypes.put(name, new ArrayList<DataType>(Arrays.asList(returnType)));
							if(t.get(i+3).t==Token.Type.FUNCTION_PAREN_L)
							{
								int argCount = 0;
								ArrayList<DataType> args = new ArrayList<DataType>();
								while(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_ARG &&
										t.get(i+5+argCount*3).t==Token.Type.FUNCTION_ARG_COLON &&
										t.get(i+6+argCount*3).t==Token.Type.FUNCTION_ARG_TYPE)
								{
									DataType argtype = null;
									String argType =  t.get(i+6+argCount*3).s;
									argType = Character.toUpperCase(argType.charAt(0))+argType.substring(1);
									try{
										argtype = DataType.valueOf(argType);
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
									if(typeDepth>0) {
										argCount++;
										args.add(0,typeIn);
									}
									fnInputTypes.put(name, new ArrayList<ArrayList<DataType>>(Arrays.asList(args)));
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
				if(typeDepth==0 && t.get(i).t==Token.Type.TYPE_DEFINITION) {
					typeDepth++;
					String typename = t.get(i+1).s;
					typeIn = DataType.makeUserType(typename);
					type = typename+".";
					i+=2;
					continue;
				}
				if(typeDepth > 0 && (t.get(i).t==Token.Type.OPEN_RANGE_EXCLUSIVE || t.get(i).t==Token.Type.OPEN_RANGE_INCLUSIVE))
					typeDepth++;
				if(typeDepth > 0 && (t.get(i).t==Token.Type.CLOSE_RANGE_EXCLUSIVE || t.get(i).t==Token.Type.CLOSE_RANGE_INCLUSIVE))
					typeDepth--;
				if(typeDepth<=0) {
					type="";
					typeDepth = 0;
				}
				
				if(t.get(i).t==Token.Type.ALIAS)//alias syntax is the same as function syntax, but the function already has to be defined and its types have to be the same size
				{
					if(t.get(i+1).t==Token.Type.FUNCTION_RETTYPE || t.get(i+1).t==Token.Type.TYPE)
					{
						String rettype = t.get(i+1).s;
						String rttype = Character.toUpperCase(rettype.charAt(0))+rettype.substring(1);
						DataType returnType = null;
						try{
							returnType = DataType.valueOf(rttype);
						} catch(Exception e)
						{
							printFuncError("not a valid type",t.get(i+1));
						}
						if(t.get(i+2).t==Token.Type.FUNCTION_NAME)
						{
							String name = type+t.get(i+2).s;
							if(!this.hasFunction(name)){
								throw new RuntimeException("Aliasing nonexistant function "+name+" at line "+t.get(i+2).linenum);
							}
							List<DataType> outTypes = fnOutputTypes.get(name);
							if(outTypes.get(0).size!=returnType.size)
								throw new RuntimeException("Attempted to alias "+name+" which returns "+outTypes.get(0)+" to different sized type "+returnType+" at line "+t.get(i+2).linenum);
							
							if(t.get(i+3).t==Token.Type.FUNCTION_PAREN_L)
							{
								int argCount = 0;
								ArrayList<DataType> args = new ArrayList<DataType>();
								while(t.get(i+4+argCount*3).t==Token.Type.FUNCTION_ARG &&
										t.get(i+5+argCount*3).t==Token.Type.FUNCTION_ARG_COLON &&
										t.get(i+6+argCount*3).t==Token.Type.FUNCTION_ARG_TYPE)
								{
									DataType argtype = null;
									String argType =  t.get(i+6+argCount*3).s;
									argType = Character.toUpperCase(argType.charAt(0))+argType.substring(1);
									try{
										argtype = DataType.valueOf(argType);
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
									if(typeDepth>0) {
										argCount++;
										args.add(0,typeIn);
									}
									if(fnInputTypes.get(name).contains(args))
										throw new RuntimeException("Aliased function "+name+" must have a different input signature than its alias at line "+t.get(i+2).linenum);
									int argCounter = 0;
									for(DataType fnArgType:args) {
										if(
												fnArgType.size
												!=fnInputTypes
												.get(name)
												.get(0)
												.get(argCounter++)
												.size) {
											throw new RuntimeException("Function alias "+name+"'s inputs must be equivalently sized to the original. First failure at arg #"+argCounter+" of type "+fnArgType+" at line "+t.get(i+2).linenum);
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
	/**
	 * parse code in the global scope
	 * @param t token list
	 * @param parent the base of the syntax tree
	 * @return a syntax tree representing this expression or statement
	 */
	private SyntaxTree parseOuter(ArrayList<Token> t, BaseTree parent) {
		final boolean ENABLE_IF_PIPELINING = true;
		if(ENABLE_IF_PIPELINING) {
			
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
		Token myTok = tok;
		DataType userType;
		switch(tok.t) {
			case EXTERN:
				while(t.remove(0).t!=Token.Type.FUNCTION_PAREN_R);
				return null;
			case ALIAS:
				//similar to function parsing except that we don't use a body
				Token retType = t.remove(0);
				if(retType.t!=Token.Type.FUNCTION_RETTYPE && retType.t!=Token.Type.TYPE)
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
				return null;
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
				root.addChild(parseExpr(t,root));
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
					case "ptr":
					case "uint":
					case "byte":
					case "ubyte":
					case "op":
					case "func":
						//parse some sort of range
						// then "with"
						// then an identifier
						// then an inner block
							
						root.addChild(forLoopType).addChild(parseExpr(t,root));
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
						if(!parent.hasVariable(root.getTokenString()))
							root.addVariableToScope(forLoopType, root.getChild(2).getTokenString(), DataType.valueOf(forLoopType.s));
						root.addChild(parseBlock(t,root));
						break;
					default:
						pe("type other than int, byte, or their unsigned versions cannot be used in a for loop");
						break;
				}
				
				break;
			case FUNCTION:
				retType = t.remove(0);
				if(retType.t!=Token.Type.FUNCTION_RETTYPE && retType.t!=Token.Type.TYPE)
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
					root.addVariableToScope(fnname, param.s, DataType.valueOf(ttype.unguardedVersion().tokenString()));
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
			case CONSTRUCTOR_CALL:
				pe("result from "+tok.s+" unused");
				break;
			case FUNC_CALL_NAME:
				String callname = myTok.s.substring(0, myTok.s.length()-1);
				root = new SyntaxTree(new Token(callname,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				if(root.functionIn()==null) {
					root.notifyCalled(callname);
				}
				if(this.fnInputTypes.containsKey(callname)) {
					int args = fnInputTypes.get(callname).get(0).size();
					for(int i=0;i<args;i++)
					{
						root.addChild(parseExpr(t,root));
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
			case TEMP:
				pe("global variables cannot be temp");
				break;
			case IDENTIFIER:
				while((!t.isEmpty()) && t.get(0).t==Token.Type.FIELD_ACCESS) {
					SyntaxTree lastRoot = root;
					root = new SyntaxTree(t.remove(0),this,lastRoot.getParent());
					root.addChild(lastRoot.copyWithDifferentParent(root));
				}
				Token secondToken = t.remove(0);
				if(secondToken.t==Token.Type.CLASS_FUNC_CALL) {
					String classFunc = root.getType()+secondToken.s.substring(0, secondToken.s.length()-1);
					parent.notifyCalled(classFunc);
					SyntaxTree call = new SyntaxTree(new Token(classFunc,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
					call.addChild(root);
					root = call;
					if(this.fnInputTypes.containsKey(classFunc)) {
						int args = fnInputTypes.get(classFunc).get(0).size() - 1;
						for(int i=0;i<args;i++)
						{
							root.addChild(parseExpr(t,root));
						}
					} else {
						pe("function not found");
					}
				} else if(secondToken.t!=Token.Type.EQ_SIGN)
				{
					if(this.fnInputTypes.containsKey(root.getToken().s))
					{
						pe("bare identifier. Function calls must be suffixed with $");
					} else {
						pe("bare identifier not used for assignment");
					}
				} else {
					SyntaxTree newRoot = new SyntaxTree(new Token("assign",Token.Type.EQ_SIGN,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
					
					newRoot.addChild(root.copyWithDifferentParent(newRoot)).addChild(parseExpr(t,newRoot));
					if(!parent.hasVariable(root.getTokenString()))
						parent.addVariableToScope(secondToken, root.getTokenString(), newRoot.getChild(1).getType());
					return newRoot;
				}
				
				break;
			case IF_GE:
			case IF_GT:
			case IF_EQ:
			case IF_LT:
			case IF_NE:
			case IF_LE:
				root.addChild(parseExpr(t,root)).addChild(parseExpr(t,root)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case SHIFT_LEFT:
				pe("result from shift unused");
				break;
			case SHIFT_RIGHT:
				pe("result from shift unused");
				break;
			case IF:
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IFNOT:
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IN:
				pe("result from in unused");
				break;
			case INCREMENT_LOC:
				root.addChild(parseExpr(t,root));
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
				// array assignment (index) list = input_val
				// reformat to assign_something$ list (index) input_val
				SyntaxTree indexVal = parseExpr(t,root);
				if(t.remove(0).t!=Token.Type.CLOSE_RANGE_EXCLUSIVE) {
					pe("list assignments must be done using (index)list = ...");
				}
				SyntaxTree listValue = parseExpr(t,root);
				if(t.remove(0).t!=Token.Type.EQ_SIGN) {
					pe("list assignments must be done using (index)list = ...");
				}
				SyntaxTree rightSide = parseExpr(t,root);
				String calledFunction = "assign_";
				if(listValue.getType().assignable().size>1) {
					calledFunction+="word";
				} else {
					calledFunction+="byte";
				}
				if(root.functionIn()==null) {
					root.notifyCalled(calledFunction);
				} else {
					root.addDependent(root.functionIn(), calledFunction);
				}
				// rearrange
				root = new SyntaxTree(new Token(calledFunction,Token.Type.FUNC_CALL_NAME,false,root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				root.addChild(listValue.copyWithDifferentParent(root));
				root.addChild(indexVal.copyWithDifferentParent(root));
				root.addChild(rightSide.copyWithDifferentParent(root));
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
				root.addChild(parseExpr(t,root)); //return from this
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
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root));
				break;
			case WHILENOT:
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root));
				break;
			case TYPE_DEFINITION:
				/*
				 * type Point (
				 * 		x:int
						y:int
					)
				 */
				Token typeNameToken = t.remove(0).unguardedVersion();
				if(typeNameToken.t!=Token.Type.TYPE) {
					throw new RuntimeException("type name must be a valid identifier "
							+typeNameToken.tokenString()+" at line "+typeNameToken.linenum);
				}
				root.addChild(typeNameToken);
				String typeName = typeNameToken.tokenString();
				
				if(!Character.isUpperCase(typeName.charAt(0))) {
					throw new RuntimeException("type definition name must start with an uppercase letter at line "
							+typeNameToken.linenum);
				}
				
				userType = DataType.makeUserType(typeName);
				Token openRange = t.remove(0);
				if(openRange.t!=Token.Type.OPEN_RANGE_EXCLUSIVE) {
					throw new RuntimeException("Expected ( after type definition of "+typeName+" at line "+openRange.linenum);
				}
				
				Token fieldTok = t.remove(0);
				while(fieldTok.t!=Token.Type.CLOSE_RANGE_EXCLUSIVE) {
					if(fieldTok.t==Token.Type.IDENTIFIER) {
						if(t.remove(0).t!=Token.Type.FUNCTION_ARG_COLON
								|| t.remove(0).t!=Token.Type.TYPE) {
							pe("Expected name:type in field definition");
						}
					} else {
						while(fieldTok.t==Token.Type.FUNCTION || fieldTok.t==Token.Type.ALIAS) {
							if (fieldTok.t==Token.Type.ALIAS) {
								if(t.get(0).t!=Token.Type.FUNCTION_RETTYPE && t.get(0).t!=Token.Type.TYPE) {
									throw new RuntimeException("invalid alias syntax at line "+fieldTok.linenum);
								}
								t.remove(0);
								if(t.remove(0).t!=Token.Type.FUNCTION_NAME) {
									throw new RuntimeException("invalid alias syntax at line "+fieldTok.linenum);
								}
								if(t.remove(0).t!=Token.Type.FUNCTION_PAREN_L) {
									throw new RuntimeException("invalid alias syntax at line "+fieldTok.linenum);
								}
								while(t.get(0).t!=Token.Type.FUNCTION_PAREN_R) {
									if(t.remove(0).t!=Token.Type.FUNCTION_ARG) {
										throw new RuntimeException("invalid alias syntax at line "+fieldTok.linenum);
									}
									if(t.remove(0).t!=Token.Type.FUNCTION_ARG_COLON) {
										throw new RuntimeException("invalid alias syntax at line "+fieldTok.linenum);
									}
									if(t.remove(0).t!=Token.Type.FUNCTION_ARG_TYPE) {
										throw new RuntimeException("invalid alias syntax at line "+fieldTok.linenum);
									}
								}
								t.remove(0);
								fieldTok = t.remove(0);
							} else {
								SyntaxTree funcTree = parseClassFunction(fieldTok,t,userType, root);
								root.addChild(funcTree);
								fieldTok = t.remove(0);
							}
						}
						if(fieldTok.t!=Token.Type.CLOSE_RANGE_EXCLUSIVE)
							throw new RuntimeException("Expected proper field name or function in definition of "
									+typeName+" at line "+fieldTok.linenum
									+" instead found "+fieldTok);
						break;
					}
					if(this.fnInputTypes.containsKey("free")) {
						fnInputTypes.get("free").add(new ArrayList<>(Arrays.asList(userType)));
						fnOutputTypes.get("free").add(DataType.Void);
					} else if(this.fnInputTypesReq.containsKey("free")) {
						fnInputTypesReq.get("free").add(new ArrayList<>(Arrays.asList(userType)));
						fnOutputTypes.get("free").add(DataType.Void);
					} else {
						throw new RuntimeException("no free$ function found before defining type "+userType.name());
					}
					fieldTok = t.remove(0);
				}
				return root;
			case WITH:
				pe("expected with after for");
				break;
		
		}
		return root;
	}
	private SyntaxTree parseClassFunction(Token functionToken, ArrayList<Token> t, DataType memberClass, SyntaxTree parent) {
		SyntaxTree root = new SyntaxTree(functionToken,this,parent);
		Token retType = t.remove(0);
		if(retType.t!=Token.Type.FUNCTION_RETTYPE && retType.t!=Token.Type.TYPE)
			pe("expected return type");
		root.addChild(retType);
		
		Token fnname = t.remove(0);
		if(fnname.t!=Token.Type.FUNCTION_NAME)
			pe("expected function name");
		
		root.addChild(new Token(memberClass.name()+"."+fnname.tokenString(),Token.Type.FUNCTION_NAME,false,fnname.srcFile()));
		
		if(t.remove(0).t!=Token.Type.FUNCTION_PAREN_L)
			pe("expected (");
		
		ArrayList<ArrayList<DataType>> inputsAndAliases = fnInputTypes.get(memberClass.name()+"."+fnname.s);
		ArrayList<DataType> outputs = fnOutputTypes.get(memberClass.name()+"."+fnname.s);
		//oh no, how do we deal with functions by the same name in multiple types?
		Token thisToken = new Token("this",Token.Type.IDENTIFIER,fnname.guarded(),functionToken.srcFile());
		root.addChild(
				new SyntaxTree(thisToken,this,root)
				.addChild(new Token(memberClass.name(),Token.Type.FUNCTION_ARG_TYPE,false,functionToken.srcFile())));

		root.addVariableToScope(thisToken, thisToken.s, memberClass);
		for(int i=0;i<inputsAndAliases.get(0).size()-1; i++) {
			Token param = t.remove(0);
			if(param.t!=Token.Type.FUNCTION_ARG)
			{
				System.err.println(param);
				pe("expected function argument");
			}
			Token colon = t.remove(0);
			if(colon.t!=Token.Type.FUNCTION_ARG_COLON)
				pe("expected type marker :");
			
			Token ttype = t.remove(0);
			if(ttype.t!=Token.Type.FUNCTION_ARG_TYPE)
				pe("expected argument type");
			
			root.addChild(new SyntaxTree(param,this,root).addChild(ttype));

			root.addVariableToScope(fnname, param.s, DataType.valueOf(ttype.unguardedVersion().tokenString()));
		}
		if(t.remove(0).t!=Token.Type.FUNCTION_PAREN_R)
			pe("expected ) to end function definition");
		root.addChild(parseBlock(t,root));
		return root;
		
	}
	private SyntaxTree parseInner(ArrayList<Token> t, BaseTree parent) {
		final boolean ENABLE_IF_PIPELINING = true;
		if(ENABLE_IF_PIPELINING) {
			
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
		Token myTok = tok;
		
		switch(tok.t) {
			case EXTERN:
				pe("cannot declare extern functions in an inner block");
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
				root.addChild(parseExpr(t,root));
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
						root.addChild(forLoopType).addChild(parseExpr(t,root));
						if(t.remove(0).t!=Token.Type.WITH)
							pe("expected with after for");
						if(t.get(0).t!=Token.Type.IDENTIFIER)
							pe("need identifier to loop over");
						root.addChild(t.remove(0));
						if(!parent.hasVariable(root.getTokenString()))
							root.addVariableToScope(forLoopType, root.getChild(2).getTokenString(), DataType.valueOf(forLoopType.s));
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
			case CONSTRUCTOR_CALL:
				pe("result from "+tok.s+" unused");
				break;
			case FUNC_CALL_NAME:
				String callname = tok.s.substring(0, tok.s.length()-1);
				root = new SyntaxTree(new Token(callname,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				if(root.functionIn()==null) {
					root.notifyCalled(callname);
				}
				if(this.fnInputTypes.containsKey(callname)) {
					int args = fnInputTypes.get(callname).get(0).size();
					for(int i=0;i<args;i++)
					{
						root.addChild(parseExpr(t,root));
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
			case TEMP:
				String function = root.functionIn();
				if(function==null)
				{
					pe("cannot declare temp without an enclosing function");
				}
				Token varname = t.remove(0);
				root = new SyntaxTree(varname,this,parent);
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
					newRoot.addChild(root.copyWithDifferentParent(newRoot))
							.addChild(parseExpr(t,newRoot))
							.addChild(new SyntaxTree(myTok,this,newRoot));
					if(!parent.hasVariable(root.getTokenString()))
						parent.addVariableToScope(secondToken, root.getTokenString(), newRoot.getChild(1).getType());
					return newRoot;
				}
				break;
			case IDENTIFIER:
				while((!t.isEmpty()) && t.get(0).t==Token.Type.FIELD_ACCESS) {
					SyntaxTree lastRoot = root;
					root = new SyntaxTree(t.remove(0),this,lastRoot.getParent());
					root.addChild(lastRoot.copyWithDifferentParent(root));
				}
				secondToken = t.remove(0);
				if(secondToken.t==Token.Type.CLASS_FUNC_CALL) {
					String classFunc = root.getType()+secondToken.s.substring(0, secondToken.s.length()-1);
					parent.notifyCalled(classFunc);
					SyntaxTree call = new SyntaxTree(new Token(classFunc,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
					call.addChild(root);
					root = call;
					if(this.fnInputTypes.containsKey(classFunc)) {
						int args = fnInputTypes.get(classFunc).get(0).size() - 1;
						for(int i=0;i<args;i++)
						{
							root.addChild(parseExpr(t,root));
						}
					} else {
						pe("function not found");
					}
				} else if(secondToken.t!=Token.Type.EQ_SIGN)
				{
					if(this.fnInputTypes.containsKey(root.getToken().s))
					{
						pe("bare identifier. Function calls must be suffixed with $");
					} else {
						pe("bare identifier not used for assignment");
					}
				} else {
					
					SyntaxTree newRoot = new SyntaxTree(new Token("assign",Token.Type.EQ_SIGN,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
					
					newRoot.addChild(root.copyWithDifferentParent(newRoot)).addChild(parseExpr(t,newRoot));
					if(!parent.hasVariable(root.getTokenString()))
						parent.addVariableToScope(secondToken, root.getTokenString(), newRoot.getChild(1).getType());
					return newRoot;
				}
				
				break;
			case IF_GE:
			case IF_GT:
			case IF_EQ:
			case IF_LT:
			case IF_NE:
			case IF_LE:
				root.addChild(parseExpr(t,root)).addChild(parseExpr(t,root)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IF:
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IFNOT:
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root)).addChild(parseBlock(t,root));
				break;
			case IN:
				pe("result from in unused");
				break;
			case INCREMENT_LOC:
				root.addChild(parseExpr(t,root));
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
				// array assignment (index) list = input_val
				// reformat to assign_something$ list (index) input_val
				SyntaxTree indexVal = parseExpr(t,root);
				if(t.remove(0).t!=Token.Type.CLOSE_RANGE_EXCLUSIVE) {
					pe("list assignments must be done using (index)list = ...");
				}
				SyntaxTree listValue = parseExpr(t,root);
				if(t.remove(0).t!=Token.Type.EQ_SIGN) {
					pe("list assignments must be done using (index)list = ...");
				}
				SyntaxTree rightSide = parseExpr(t,root);
				String calledFunction = "assign_";
				if(listValue.getType().assignable().size>1) {
					calledFunction+="word";
				} else {
					calledFunction+="byte";
				}
				if(root.functionIn()==null) {
					root.notifyCalled(calledFunction);
				} else {
					root.addDependent(root.functionIn(), calledFunction);
				}
				// rearrange
				root = new SyntaxTree(new Token(calledFunction,Token.Type.FUNC_CALL_NAME,false,root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				root.addChild(listValue.copyWithDifferentParent(root));
				root.addChild(indexVal.copyWithDifferentParent(root));
				root.addChild(rightSide.copyWithDifferentParent(root));
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
				function = root.functionIn();
				if(function==null)
				{
					try{
						root.addChild(parseExpr(t,root));
					} catch(Exception e){
						//use no return value
					}
				} else {
					DataType retType = fnOutputTypes.get(function).get(0);
					if(retType!=DataType.Void)
					{
						root.addChild(parseExpr(t,root)); //return from this
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
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root));
				break;
			case WHILENOT:
				root.addChild(parseExpr(t,root)).addChild(parseBlock(t,root));
				break;
			case WITH:
				pe("expected with after for");
				break;
		
		}
		return root;
	}
	/**
	 * Parse the token list token for an expected expression
	 * @param t the token list
	 * @param parent the parent syntax tree
	 * @param inAssignment whether this expression is in an assignment (I don't think this is used and I actually forgot its purpose)
	 * @return the syntax tree representing this expression
	 */
	private SyntaxTree parseExpr(ArrayList<Token> t, BaseTree parent) {
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
				root.addChild(parseExpr(t,root)).addChild(parseExpr(t,root));
				break;
			case BYTE_LITERAL:
			case FLOAT_LITERAL:
			case INT_LITERAL:
			case FALSE:
			case TRUE:
			case UBYTE_LITERAL:
			case UINT_LITERAL:
			case STRING_LITERAL:
				break;
			case IDENTIFIER:
				while((!t.isEmpty()) && t.get(0).t==Token.Type.FIELD_ACCESS) {
					SyntaxTree lastRoot = root;
					root = new SyntaxTree(t.remove(0),this,root.getParent());
					root.addChild(lastRoot.copyWithDifferentParent(root));
				}
				
				if((!t.isEmpty())&&t.get(0).t==Token.Type.CLASS_FUNC_CALL) {
					Token secondToken = t.remove(0);
					String classFunc = root.getType()+secondToken.s.substring(0, secondToken.s.length()-1);
					parent.notifyCalled(classFunc);
					SyntaxTree call = new SyntaxTree(new Token(classFunc,Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
					call.addChild(root);
					root = call;
					if(this.fnInputTypes.containsKey(classFunc)) {
						int args = fnInputTypes.get(classFunc).get(0).size() - 1;
						for(int i=0;i<args;i++)
						{
							root.addChild(parseExpr(t,root));
						}
					} else {
						pe("function not found");
					}
				}
				break;
			case POINTER_TO:
				try {
					String removed = tok.s.replaceAll("guard_.*?_.*?_.*?_.*?(_.*)$", "$1");
					DataType cast = DataType.valueOf(Character.toUpperCase(removed.charAt(1))+removed.substring(2));
					root = new SyntaxTree(new Token("as",Token.Type.AS,false,tok.srcFile()).setLineNum(tok.linenum),this,parent);
					root.addChild(parseExpr(t,root));
					root.addChild(new Token(cast.name(),Token.Type.TYPE,false,tok.srcFile()).setLineNum(tok.linenum));
					break;
				} catch(IllegalArgumentException|IndexOutOfBoundsException e) {
					
				}
				
				
				break;
			case CORRECT:
			case COMPLEMENT:
			case SHIFT_LEFT:
			case SHIFT_RIGHT:
				root.addChild(parseExpr(t,root));
				break;
			case NEGATE:
				root.addChild(parseExpr(t,root));
				break;
			case AS:
				root.addChild(parseExpr(t,root));
				if(t.get(0).t==Token.Type.TYPE) {
					root.addChild(t.remove(0));
				} else {
					pe("cast with as [value] [type]");
				}
				break;
			case CONSTRUCTOR_CALL:
			case FUNC_CALL_NAME:
				String callname = tok.s.substring(0, tok.s.length()-1);
				root = new SyntaxTree(new Token(callname,tok.t,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				if(root.functionIn()==null) {
					if(tok.t==Token.Type.CONSTRUCTOR_CALL) {
						root.notifyCalled(callname+".init");
					} else 
						root.notifyCalled(callname);
				}
				if(this.fnInputTypes.containsKey(callname)) {
					int args = fnInputTypes.get(callname).get(0).size();
					for(int i=0;i<args;i++)
					{
						root.addChild(parseExpr(t,root));
					}
				} else {
					try {
						
						DataType customType = DataType.valueOf(callname);
						
						SyntaxTree mallocCall = new SyntaxTree(new Token("malloc",Token.Type.FUNC_CALL_NAME,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),
								this,
								parent);
						mallocCall.addChild(new Token(""+customType.getHeapSizeString(settings, 0),Token.Type.UINT_LITERAL,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum));
						root.addChild(mallocCall);
						if(!fnInputTypes.containsKey(callname+".init"))
							pe("cannot construct type "+callname+" without a .init member function");
						for(int i=0;i<fnInputTypes.get(callname+".init").get(0).size()-1;i++) {
							root.addChild(parseExpr(t,root));
						}
						if(root.functionIn()!=null) {
							if(this.functionNames().contains(callname+".delete"))
								if(this.getFunctionInputTypes(callname+".delete").get(0).size()==1)
								{
									root.addDependent(root.functionIn(), callname+".delete");
								} else {
									throw new RuntimeException("function "+callname+".delete must have no parameters");
								}
						}
					} catch(IllegalArgumentException e) {
						e.printStackTrace();
						pe("function or type not found");
					}
				}
				break;
			case OPEN_RANGE_INCLUSIVE:
			case OPEN_RANGE_EXCLUSIVE:
				
				SyntaxTree possibleRoot = new SyntaxTree(new Token(",",Token.Type.RANGE_COMMA,root.getToken().guarded(),root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
				SyntaxTree openBracket = new SyntaxTree(root.getToken(),this,possibleRoot);
				SyntaxTree result1 = parseExpr(t,possibleRoot);
				if(t.get(0).t==Token.Type.RANGE_COMMA) {
					t.remove(0);
					root = possibleRoot;
					root.addChild(openBracket);
					root.addChild(result1);
					root.addChild(parseExpr(t,root));
					Token endChoice = t.remove(0);
					if(endChoice.t!=Token.Type.CLOSE_RANGE_EXCLUSIVE && endChoice.t!=Token.Type.CLOSE_RANGE_INCLUSIVE)
						pe("not a valid way to end a range");
					root.addChild(endChoice);
				} else {
					if(root.getToken().t==Token.Type.OPEN_RANGE_EXCLUSIVE) {
						// array access (index) list
						// reformat to access_something$ list (index)
						if(t.remove(0).t!=Token.Type.CLOSE_RANGE_EXCLUSIVE) {
							pe("list accesses must be done using (index)list");
						}
						SyntaxTree listValue = parseExpr(t,root);
						String calledFunction = "access_";
						if(listValue.getType().assignable().size>1) {
							calledFunction+="word";
						} else {
							calledFunction+="byte";
						}
						if(root.functionIn()==null) {
							root.notifyCalled(calledFunction);
						} else {
							root.addDependent(root.functionIn(), calledFunction);
						}
						// rearrange
						root = new SyntaxTree(new Token(calledFunction,Token.Type.FUNC_CALL_NAME,false,root.getToken().srcFile()).setLineNum(root.getToken().linenum),this,parent);
						root.addChild(listValue.copyWithDifferentParent(root));
						root.addChild(result1.copyWithDifferentParent(root));
					} else {
						//list literal
						root.addChild(result1.copyWithDifferentParent(root));
						while(t.get(0).t!=Token.Type.CLOSE_RANGE_INCLUSIVE) {
							root.addChild(parseExpr(t,root));
						}
						t.remove(0);
					}
					
				}
			break;
			default:
				throw new RuntimeException(tok.toString()+" not recognized as a valid expression token at line "+tok.linenum);
		}
		
		return root;
	}
	/**
	 * Parse a block of code used in a for loop, if statement, function, etc.
	 * @param t the token list
	 * @param parent this block's parent
	 * @return a syntaxtree representing this block of code
	 */
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
	private ArrayList<String> symbolTable = new ArrayList<>();
	/**
	 * Notify the parser that the given identifier string represents a global constant
	 * @param string the symbol name
	 */
	public void notifySymbol(String string) {
		symbolTable.add(string);
	}
	/**
	 * 
	 * @return Whether or not the given string represents a global constant
	 */
	public boolean isSymbol(String s) {
		return symbolTable.contains(s);
	}
	
	private HashSet<String> externASMFunctions = new HashSet<>();
	
	public Set<String> getExternFunctions() {
		HashSet<String> copy = new HashSet<>();
		externASMFunctions.forEach(x -> copy.add(x));
		return copy;
	}
	
	public void asmExternFunction(List<DataType> asList, DataType i, String name) {
		this.registerLibFunction(asList, i, name);
		externASMFunctions.add(name);
		inlined.add(name);
	}
}
