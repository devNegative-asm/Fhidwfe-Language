package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static compiler.DataType.*;
/**
 * Non-root syntax tree nodes
 *
 */
public class SyntaxTree extends BaseTree{
	private Token myToken;
	private final BaseTree parent;
	public SyntaxTree(Token t, Parser p, BaseTree parent)
	{
		super(p);
		myToken = t;
		this.parent = parent;
	}
	@Override
	/**
	 * Notify this syntax tree that the given function was called or a pointer-to-function was made at some point in the code
	 */
	public void notifyCalled(String fnName) {
		parent.notifyCalled(fnName);
	}
	@Override
	/**
	 * Notify this syntax tree that the given caller function relies on the callee function
	 */
	public void addDependent(String caller, String callee) {
		parent.addDependent(caller, callee);
	}
	public static enum Location{
		GLOBAL,
		ARG,
		NONE,
		LOCAL,
	}
	/**
	 * Whether this syntax tree is a member of a function
	 */
	public boolean inFunction() {
		if(parent.getClass()==BaseTree.class) {
			return false;
		} else {
			SyntaxTree pp = (SyntaxTree)parent;
			return pp.isFunction() || pp.inFunction();
		}
	}
	/**
	 * Whether this syntax tree represents a function
	 */
	public boolean isFunction(){
		return getTokenType()==Token.Type.FUNCTION;
	}
	
	
	
	
	private ArrayList<Variable> localVariables = new ArrayList<>();
	private ArrayList<Variable> localPointers = new ArrayList<>();
	private ArrayList<Variable> functionVariables = new ArrayList<>();
	private ArrayList<Variable> functionPointers = new ArrayList<>();
	private ArrayList<String> argorder = new ArrayList<>();
	
	private int localSpace = 0;
	/**
	 * Calculates the number of bytes necessary to hold variables local to a function, rounded up to align the stack pointer even if alignment is disabled
	 */
	public int getMyFunctionsLocalspace() {
		if(this.isFunction()) {
			return getLocalSpaceNeeded();
		} else {
			return parent.getMyFunctionsLocalspace();
		}
	}
	/**
	 * Returns 0 if this tree does not represent a function, otherwise ajusts localSpace to align the stack pointer and returns the number of bytes needed to allocate local variables
	 */
	public int getLocalSpaceNeeded() {
		if(prepared)
		{
			if(localSpace%theParser.settings.intsize!=0) {
				localSpace = ((localSpace / theParser.settings.intsize) + 1)*theParser.settings.intsize;
			}
			return localSpace;
		} else {
			throw new RuntimeException("@@contact devs attempt to get local space needed on "+this.getTokenString()+" at "+this.getToken().linenum+" before preparing");
		}
	}
	boolean prepared = false;
	
	/**
	 * Calculate the local variable tree and update the parent
	 * @param parent The variable tree to update
	 * @param align whether to align byte variables on int boundaries
	 */
	void getNeededLocals(VariableTree parent, boolean align) {
		if(isFunction()) {
			return;
		}
		VariableTree locals = new VariableTree(parent);
		this.scopeTypings.forEach((string, type) ->{locals.addVariable(string, type,theParser,align);});
		for(SyntaxTree child:children()) {
			child.getNeededLocals(locals, align);
		}
		prepared = true;
	}
	/**
	 * Prepare this tree for variable offset calculation
	 * @param align whether to align byte variables on int boundaries
	 */
	public void prepareVariables(boolean align) {
		if(isFunction()) {
			this.argorder.forEach((name) -> {
				functionVariables.add(new Variable(name,SyntaxTree.Location.ARG,DataType.Uint,theParser));//all args should be of int type in terms of stack operations
			});//do not sort arguments. They come on the stack.
			
			int maxOffset = (1+argorder.size())*theParser.settings.intsize;// +4 gets you the last argument, +6 gets second to last, etc. 
			for(int i=0;i<argorder.size();i++) {
				functionPointers.add(new Variable(argorder.get(i),(byte)(maxOffset-theParser.settings.intsize*i),DataType.Ptr,theParser));
			}

			//look at all sub-blocks that hold local variables.
			//Their names will not conflict with globals or arguments
			VariableTree localsTree = new VariableTree(null);
			children()[children().length-1].getNeededLocals(localsTree,align);
			this.localSpace = localsTree.getMaxSize(theParser,align);
			HashMap<String,Variable> vars = localsTree.getVars();
			vars.forEach((name, var)->{
				localPointers.add(var);
				localVariables.add(new Variable(name.substring(1),Location.LOCAL,var.getType(),theParser));
			});
		}
		else if (this.myToken.t==Token.Type.TYPE_DEFINITION) {
			for(SyntaxTree child:children()) {
				child.prepareVariables(align);
			}
		}
		prepared = true;
		
	}
	@Override
	/**
	 * resolves a local variable name to its pointer
	 * @param varname the variable name to resolve
	 * @return the byte offset from the base pointer this variable can be found at
	 */
	public long resolveLocalOffset(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared or type checked");
		}
		for(Variable v:localPointers) {
			if(v.getName().equals(varname)) {
				long ret = v.getValue();
				switch(theParser.settings.target) {
				case REPL:
					if(ret>=(1L<<12))
						throw new RuntimeException("That's way too many argument variables in function "+functionIn());
					break;
				case TI83pz80:
						if(ret<=-128)
							throw new RuntimeException("Can store up to 63 local variables on 8 bit systems in function "+functionIn());
					break;
				case z80Emulator:
						if(ret<=-128)
							throw new RuntimeException("Can store up to 63 local variables on 8 bit systems in function "+functionIn());
					break;
				case LINx64:
				case WINx64:
						if(ret<=-(1L<<31)) {
							throw new RuntimeException("That's way too many local variables in function "+functionIn());
						}
					break;
				case WINx86:
						if(ret<=-(1L<<15)) {
							throw new RuntimeException("That's way too many local variables in function "+functionIn());
						}
					break;
				}
				return v.getValue();
			}
		}
		if(parent!=null)
			return parent.resolveLocalOffset(varname);
		throw new RuntimeException("@@contact devs. Could not resolve variable");
	}
	@Override
	/**
	 * resolves an argument variable to its pointer. Similar to resolveLocalOffset, but this always returns non-negative results
	 * @param varname the variable to generate a pointer to
	 * @return the byte offset from the base pointer this variable can be found at
	 */
	public long resolveArgOffset(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared");
		}
		for(Variable v:functionPointers) {
			if(v.getName().equals(varname)) {
				long ret = v.getValue();
				switch(theParser.settings.target) {
				case REPL:
					if(ret>=(1L<<12))
						throw new RuntimeException("That's way too many argument variables in function "+functionIn());
					break;
				case TI83pz80:
						if(ret>=128)
							throw new RuntimeException("Can store up to 63 argument variables on 8 bit systems in function "+functionIn());
					break;
				case z80Emulator:
						if(ret>=128)
							throw new RuntimeException("Can store up to 63 argument variables on 8 bit systems in function "+functionIn());
					break;
				case LINx64:
				case WINx64:
						if(ret>=(1L<<31)) {
							throw new RuntimeException("That's way too many argument variables in function "+functionIn());
						}
					break;
				case WINx86:
						if(ret>=(1L<<15)) {
							throw new RuntimeException("That's way too many argument variables in function "+functionIn());
						}
					break;
				}
				
				return v.getValue();
			}
		}
		if(parent!=null)
			return parent.resolveArgOffset(varname);
		throw new RuntimeException("@@contact devs. Could not resolve variable");
	}
	/**
	 * Locates a variable and returns a string descibing its location in the current scope
	 * @param varname the variable name
	 * @return one of LOCAL, ARG, GLOBAL for variables, or NONE for constants, then 1 space, then its offset for local & args, or its name for globals and constants
	 */
	public String resolveVariableLocation(String varname)
	{
		try {
			return Location.LOCAL+" "+resolveLocalOffset(varname);
		}catch(RuntimeException e) {
			try {
				return Location.ARG+" "+resolveArgOffset(varname);
			} catch(RuntimeException e2) {
				try {
					return Location.GLOBAL+" "+resolveGlobalLoopVariables(varname);
				}catch(RuntimeException e3) {
					try {
						return Location.GLOBAL+" "+resolveGlobalToString(varname);
					} catch(RuntimeException e4) {
						return Location.NONE+" "+resolveConstant(varname);
					}
				}
			}
		}
	}
	@Override
	public String resolveConstant(String varname) {
		return parent.resolveConstant(varname);
	}
	@Override
	public String resolveGlobalToString(String varname) {
		return parent.resolveGlobalToString(varname);
	}
	@Override
	public long resolveGlobalLoopVariables(String varname) {
		return parent.resolveGlobalLoopVariables(varname);
	}
	
	
	public SyntaxTree addChild(SyntaxTree t)
	{
		super.addChild(t);
		return this;
	}
	public SyntaxTree addChild(Token t)
	{
		return this.addChild(new SyntaxTree(t,theParser,this));
	}
	/**
	 * Get the original token string used to construct this syntax tree
	 * @return the token string
	 */
	public String getTokenString()
	{
		return myToken.s;
	}
	/**
	 * Get the type of the token used to contstruct this tree
	 * @return the token type
	 */
	public Token.Type getTokenType()
	{
		return myToken.t;
	}
	/**
	 * Get the original token used to construct this syntax tree
	 * @return the token
	 */
	public Token getToken()
	{
		return myToken;
	}
	/**
	 * Get the name function this syntax tree is a part of
	 */
	@Override
	public String functionIn()
	{
		if(getTokenType()==Token.Type.FUNCTION)
		{
			//look at children
			for(SyntaxTree st:this.getChildren())
			{
				if(st.getTokenType()==Token.Type.FUNCTION_NAME)
				{
					return st.getTokenString();
				}
			}
			return null;
		} else {
			return parent.functionIn();
		}
	}
	public String toString(int depth)
	{
		byte[] start = new byte[depth];
		Arrays.fill(start,(byte) '-');
		String myString = new String(start)+" "+myToken+"\n";
		for(SyntaxTree tree:getChildren())
		{
			myString+=tree.toString(1+depth);
		}
		return myString;
	}
	public String toString() {
		return this.toString(0);
	}
	/**
	 * Get an array which contains this tree's children, while asserting that the number of children this node has must be matched by sizeConstraint
	 * @param sizeConstraint the number of children this node must have
	 * @return an array containing this node's children
	 */
	private SyntaxTree[] children(int sizeConstraint)
	{
		if(sizeConstraint == super.getChildren().size()) {
			SyntaxTree[] tree = new SyntaxTree[0];
			return super.getChildren().toArray(tree);
		} else {
			throw new RuntimeException("Unexpected amount of subexpressions for type "+myToken.t+". Expected: "+sizeConstraint+", Got: "+super.getChildren().size()+" at line "+myToken.linenum);
		}
	}
	
	/**
	 * Get an array which contains this tree's children, while asserting that the number of children this node must have be within a certain range
	 * @param minSize the minimum number of children
	 * @param maxSize the maximum number of children
	 * @return an array containing this node's children
	 */
	private SyntaxTree[] children(int minSize, int maxSize) {
		if(minSize<= super.getChildren().size() && super.getChildren().size()<=maxSize) {
			SyntaxTree[] tree = new SyntaxTree[0];
			return super.getChildren().toArray(tree);
		} else {
			throw new RuntimeException("Unexpected amount of subexpressions for type "+myToken.t+". Expected: "+minSize+"<=children<="+maxSize+", Got: "+super.getChildren().size()+" at line "+myToken.linenum);
		}
	}
	/**
	 * Get an array of this syntax tree's children
	 * @return the children
	 */
	private SyntaxTree[] children()
	{
		SyntaxTree[] tree = new SyntaxTree[0];
		return super.getChildren().toArray(tree);
	}
	/**
	 * Throw a TypeMismatchException where one and two are expected to be the same type
	 * @param one type 1
	 * @param two type 2
	 */
	private void mismatch(DataType one, DataType two)
	{
		class TypeMismatchException extends RuntimeException{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public TypeMismatchException(String err)
			{
				super(err);
			}
		}
		throw new TypeMismatchException(myToken.s+" expects matching types. Got instead "+one.name()+" and "+two.name()+" at line "+myToken.linenum);
	}
	/**
	 * Throw an IncorrectTypeException where type one was expected and type 2 was given
	 * @param one type 1
	 * @param two type 2
	 */
	private void unexpected(DataType one, DataType two)
	{
		class IncorrectTypeException extends RuntimeException{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public IncorrectTypeException(String err)
			{
				super(err);
			}
		}
		throw new IncorrectTypeException(myToken.s+" expects data of type "+one.name()+". Instead found "+two.name()+" at line "+myToken.linenum);
	}
	/**
	 * Throw an error stating the given type is not usable for math
	 * @param typ the type
	 */
	private void math(DataType typ)
	{
		class UnusableMathTypeException extends RuntimeException{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public UnusableMathTypeException(String err)
			{
				super(err);
			}
		}
		throw new UnusableMathTypeException(myToken.s+" expects data of a mathematical type. Instead found "+typ.name()+" at line "+myToken.linenum);
	}
	/**
	 * Throw an error stating that the given type does not represent valid data
	 * @param type the type
	 */
	private void impossible(DataType type) {
		class NotDataException extends RuntimeException{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public NotDataException(String err)
			{
				super(err);
			}
		}
		throw new NotDataException(myToken.s+" expects to have a data type. Instead found "+type.name()+" at line "+myToken.linenum);
	}
	private static final List<DataType> mathTypes = Arrays.asList(DataType.Byte,
			DataType.Int,
			DataType.Float,
			DataType.Uint,
			DataType.Ubyte,
			DataType.Ptr);
	private static final List<DataType> impossibleForData = Arrays.asList(DataType.Void, DataType.Flag, DataType.SYNTAX);
	private DataType typeCache = null;
	/**
	 * Assert that the 2 children of this syntaxtree are both mathematical types and that the second can be casted to the first
	 * @return the type of the first child
	 */
	private DataType checkBinaryMath()
	{
		SyntaxTree[] children = children(2);
		DataType type = children[0].getType(); 
		if(type != children[1].getType()) {
			if(!children[1].getType().canCastTo(type))
				mismatch(children[0].getType(),children[1].getType());
			else
			{
				System.err.println("WARNING: Implicit cast from "+children[1].getType()+" to "+type+" at line "+this.getToken().linenum);
				return type;
			}
		} else {
			if(!mathTypes.contains(type))
				math(type);
			return type;
		}
		return null;
	}
	
	private DataType checkEqualityMath()
	{
		SyntaxTree[] children = children(2);
		DataType type1 = children[0].getType();
		DataType type2 = children[0].getType();
		if(type1==Ptr && !type2.builtin())
			return Bool;
		if(type2==Ptr && !type1.builtin())
			return Bool;
		if(type1==type2)
			return Bool;
		return checkBinaryMath();
		
	}
	/**
	 * Check that the one and only child of this syntax tree has a mathematical type
	 * @return the type of the child
	 */
	private DataType checkUnaryMath() {
		SyntaxTree children = children(1)[0];
		if(!mathTypes.contains(children.getType()))
			math(children.getType());
		return children.getType();
	}
	
	/**
	 * Check that the one and only child of this syntax tree has a ptr type
	 */
	private void checkPointer() {
		SyntaxTree children = children(1)[0];
		if(children.getType()!=DataType.Ptr)
			unexpected(DataType.Ptr,children.getType());
	}
	
	private HashMap<String,DataType> scopeTypings = new HashMap<>();
	public Set<String> scopeVars() {
		return scopeTypings.keySet();
	}
	@Override
	public DataType resolveVariableType(String varname, String linenum)
	{
		if(scopeTypings.containsKey(varname)) {
			return scopeTypings.get(varname);
		} else {
			return parent.resolveVariableType(varname, linenum);
		}
	}
	@Override
	public void addVariableToScope(Token assignment, String varname, DataType type)
	{
		if(scopeTypings.containsKey(varname) && scopeTypings.get(varname)!=type)
		{
			throw new RuntimeException("tried to assign from type "+type+" to variable of type "+scopeTypings.get(varname)+" at line "+assignment.linenum);
		} else {
			scopeTypings.put(varname, type);
		}
	}

	/**
	 * Type check the elements of this syntax tree, then find out the type of this tree if it's an expression
	 * @return the type
	 */
	public DataType getType()
	{
		//TODO implement pipelined if type checking
		if(typeCache!=null)
		{
			return typeCache;
		}
		DataType ret = null;
		switch(myToken.t)
		{
		case LOGICAL_AND:
		case LOGICAL_OR:
			SyntaxTree[] children = children(2);
			if(children[0].getType()!=DataType.Bool || children[1].getType()!=DataType.Bool) {
				throw new RuntimeException("cannot use non-boolean types with logical operations at line "+this.getToken().linenum);
			}
			ret = DataType.Bool;
			break;
		case MODULO:
			ret = checkBinaryMath();
			if(ret==DataType.Int)
			{
				if(inFunction())
					this.addDependent(this.functionIn(), "smod");
				else
					notifyCalled("smod");
				
			} else if(ret==DataType.Byte) {
				if(inFunction())
					this.addDependent(this.functionIn(), "sbmod");
				else
					notifyCalled("sbmod");
			}
			break;
		case DIVIDE:
			ret = checkBinaryMath();
			
			if(ret==DataType.Int)
			{
				if(inFunction())
					this.addDependent(this.functionIn(), "sdiv");
				else
					notifyCalled("sdiv");
				
			} else if(ret==DataType.Byte) {
				if(inFunction())
					this.addDependent(this.functionIn(), "sdiv");
				else
					notifyCalled("sbdiv");
			}
			break;
		case ADD:
		case BITWISE_AND:
		case BITWISE_OR:
		case SUBTRACT:
		case TIMES:
			ret = checkBinaryMath();
			break;
		case BITWISE_XOR:
			children = children(2);
			if((ret = children[0].getType())==children[1].getType()) {
				if(mathTypes.contains(children[0].getType())||children[0].getType()==DataType.Bool) {
					
				} else {
					math(ret);
				}
			}
			break;
		case LEQUAL:
		case LTHAN:
		case GEQUAL:
		case GTHAN:
			checkBinaryMath();
			ret = DataType.Bool;
			break;
		case AS:
			children = children(2);
			String targettType = children[1].getTokenString();
			DataType exprType = children[0].getType();
			String rttype = Character.toUpperCase(targettType.charAt(0))+targettType.substring(1);
			DataType targetType = DataType.valueOf(rttype);
			if(impossibleForData.contains(targetType) || impossibleForData.contains(exprType))
			{
				impossible(targetType);
			}
			ret = targetType;
			break;
		case BYTE_LITERAL:
			ret = DataType.Byte;
			break;
		case CORRECT:
			children = children(1);
			ret = new TypeResolver<DataType>(children[0].getType())
				.CASE(Int, DataType.Uint)
				.CASE(Ptr, DataType.Uint)
				.CASE(Uint, DataType.Uint)
				.DEFAULT_THROW(new RuntimeException(children[0].getType()+" is not a valid index type at line "+children[0].getToken().linenum))
				.get();
			break;
		case CLOSE_BRACE:
			ret = DataType.SYNTAX;
			break;
		case CLOSE_RANGE_EXCLUSIVE:
		case CLOSE_RANGE_INCLUSIVE:
		case EMPTY_BLOCK:
		case TEMP:
			ret = DataType.SYNTAX;
			break;
		case COMPLEMENT:
			children=children(1);
			if((ret = children[0].getType())!=DataType.Bool)//you gotta love this line
				ret = checkUnaryMath();
			break;
		case EQ_SIGN:
			if(getTokenString().equals("assign"))
			{
				SyntaxTree child = children(2,3)[1];
				if(impossibleForData.contains(child.getType())) {
					impossible(child.getType());
				}
				boolean typeFlag = false;
				try {
					if(resolveVariableType(children(2,3)[0].getTokenString(), getToken().linenum)==child.getType()) {
						
					} else {
						typeFlag = true;
					}
				} catch(Exception e) {
					parent.addVariableToScope(getToken(), children(2,3)[0].getTokenString(), child.getType());
				}
				if(typeFlag)
					unexpected(resolveVariableType(children(2,3)[0].getTokenString(), getToken().linenum),child.getType());
				ret = DataType.Void;
			} else {
				
				ret = checkEqualityMath();
				ret = DataType.Bool;
			}
			break;
		case FALSE:
		case TRUE:
			ret = DataType.Bool;
			break;
		case FLOAT_LITERAL:
			ret = DataType.Float;
			break;
		case FOR:
			//we have 4 children
			children = children(4);
			//type 1 can be {int, uint, byte, ubyte, ptr}
			//type 2 can either be a range that matches the type (by Data.assignable) or 
			//type 2 is a list that matches the type or
			//type 2 is ptr-to-function which takes no args and whose return value matches the type
			//3 is just an identifier and doesn't have a type yet, but it should be assigned in this scope
			//4 is the actual block
			switch(children[1].getTokenType()) {
			case OPEN_RANGE_INCLUSIVE:
			case OPEN_RANGE_EXCLUSIVE:
				if(inFunction()) {
					this.addDependent(functionIn(), "malloc");
					this.addDependent(functionIn(), "free");
				} else {
					this.notifyCalled("malloc");
					this.notifyCalled("free");
				}
				break;
				
			default:
				break;
			}
			DataType loopType = DataType.fromLowerCase(children[0].getTokenString());
			
			if(!children[1].getType().assignable(loopType))
			{
				throw new RuntimeException(loopType+" must be assignable from "+children[1].getType()+" to iterate at line "+children[1].getToken().linenum);
			}
			boolean except = false;
			try {
				parent.resolveVariableType(children[2].getTokenString(), this.getToken().linenum);
				except = true;
			}catch(RuntimeException Ineedthis) {
				children[3].addVariableToScope(this.getToken(), children[2].getTokenString(), loopType);
			}
			if(except) {
				throw new RuntimeException("For loop cannot shadow "+children[2].getTokenString()+" from a higher scope at line "+this.getToken().linenum);
			}
			ret = DataType.SYNTAX;
			break;
		case FUNCTION:
			children = children();
			
			SyntaxTree code = children[children.length-1];
			for(int i=2;i<children.length-1;i++) {
				this.addVariableToScope(children[i].getToken(), children[i].getTokenString(), 
						DataType.fromLowerCase(children[i].getChildren().get(0).getTokenString()));
				argorder.add(children[i].getTokenString());
			}
			ret = code.getType();//this is necessary to resolve the types of variables in the code before we check the return type
			

			DataType expectingType = DataType.fromLowerCase(children[0].getTokenString());
			this.checkReturnTypes(children[1].getTokenString(), expectingType);
			
			
			if(ret!=DataType.SYNTAX)
				throw new RuntimeException("expected block after function definition. Found instead "+ret+" at line "+this.getToken().linenum);
			break;
		case FUNCTION_ARG:
			ret = DataType.SYNTAX;
			break;
		case FUNCTION_ARG_COLON:
			ret = DataType.SYNTAX;
			break;
		case FUNCTION_ARG_TYPE:
			ret = DataType.SYNTAX;
			break;
		case FUNCTION_COMMA:
			ret = DataType.SYNTAX;
			break;
		case FUNCTION_NAME:
			ret = DataType.SYNTAX;
			break;
		case FUNCTION_PAREN_L:
			ret = DataType.SYNTAX;
			break;
		case FUNCTION_PAREN_R:
			ret = DataType.SYNTAX;
			break;
		case FUNCTION_RETTYPE:
			ret = DataType.SYNTAX;
			break;
		case FUNC_CALL_NAME:
			//type check the arguments
			//then return my return type
			List<DataType> usedTypes = new ArrayList<>();
			
			for(SyntaxTree args:this.getChildren()) {
				DataType type = args.getType();
				usedTypes.add(type);
			}
			int argIndex = theParser.getFunctionInputTypes(getTokenString()).indexOf(usedTypes);
			if(argIndex==-1) {
				
				//attempt implicit cast
				for(int argc=0;argc<usedTypes.size();argc++) {
					if(!usedTypes.get(argc).canCastTo(theParser.getFunctionInputTypes(getTokenString()).get(0).get(argc))) {
						throw new RuntimeException("Could not find function or alias named "+getTokenString()+" with input signature "+usedTypes+" at line "+this.getToken().linenum);
					}
				}
				//if we cast implicitly, we can only do it to the first function definition
				if(getTokenString().isEmpty()) {
					System.err.println("WARNING: Implicit cast as argument to $. Maybe use an alias instead? Converted "+usedTypes+" -> "+theParser.getFunctionInputTypes(getTokenString()).get(0)+" at line "+getToken().linenum);
				} else {
					System.err.println("WARNING: Implicit cast as argument to "+getTokenString()+". Maybe use an alias instead? Converted "+usedTypes+" -> "+theParser.getFunctionInputTypes(getTokenString()).get(0)+" at line "+getToken().linenum);
				}
				ret = theParser.getFunctionOutputType(getTokenString()).get(0);
			
			} else
				ret = theParser.getFunctionOutputType(getTokenString()).get(argIndex);
			
			if(inFunction())
				this.addDependent(this.functionIn(), getTokenString());
			else
				notifyCalled(getTokenString());
			break;
		case IDENTIFIER:
			ret = this.resolveVariableType(this.getTokenString(),this.getToken().linenum);
			if(ret==DataType.Func || ret==DataType.Op)
			{
				parent.notifyCalled(getTokenString().replaceAll("guard_.*?_.*?_.*?_.*?_", ""));
			}
			break;
		case FIELD_ACCESS:
			DataType parentType = this.children(1)[0].getType();
			ret = parentType.typeOfField(getTokenString(), myToken.linenum);
			break;
		case IF:
		case IFNOT:
			this.getChildren().get(2).getType();
		case WHILE:
		case WHILENOT:
			//argument must be a math type or a flag and cannot be a float
			SyntaxTree child = this.getChildren().get(0);
			if(child.getType()==DataType.Float)
			{
				throw new RuntimeException("Conditions cannot be floats at line "+this.getToken().linenum);
			} else if(child.getType()==DataType.Flag)
			{
				ret = DataType.Flag;
				//truthiness allowed for pointer types only
			} else if(child.getType()==DataType.Ptr || child.getType()==DataType.Bool){
				ret = child.getType();
			} else {
				throw new RuntimeException("Conditions cannot have type "+child.getType()+" at line "+this.getToken().linenum);
			}
			this.getChildren().get(1).getType();
			break;
			
		case IN:
			
			children = children(2);
			
			if(inFunction()) {
				this.addDependent(functionIn(), "in"+children[1].getType().name().toLowerCase());
			} else {
				this.notifyCalled("in"+children[1].getType().name().toLowerCase());
			}
			
			if(children[1].getType().assignable(children[0].getType()))
				ret = DataType.Bool;
			else
				throw new RuntimeException("cannot check whether "+children[0].getType()+" is a member of "+children[1].getType()+" at line "+this.getToken().linenum);
			break;
		case INCREMENT_LOC:
		case DECREMENT_LOC:
			checkPointer();
			ret = DataType.Void;
			break;
		case INT_LITERAL:
			ret = DataType.Int;
			break;
		case IS:
			throw new UnsupportedOperationException("'is' is deprecated at line "+this.getToken().linenum);
		case NEGATE:
			if(!children(1)[0].getType().signed())
				throw new RuntimeException("Cannot negate an unsigned type at line "+this.getToken().linenum);
		case SHIFT_LEFT:
		case SHIFT_RIGHT:
			ret = checkUnaryMath();
			break;
		case OPEN_BRACE:
			for(SyntaxTree child2:children())
			{
				child2.getType();//type checks without actually looking at the result
			}
			ret = DataType.SYNTAX;
			break;
		case OPEN_RANGE_EXCLUSIVE:
		case OPEN_RANGE_INCLUSIVE:
			if(inFunction()) {
				this.addDependent(functionIn(), "malloc");
				this.addDependent(functionIn(), "free");
			} else {
				this.notifyCalled("malloc");
				this.notifyCalled("free");
			}
			//this is a list. of what type? let's find out!
			DataType save = null; 
			for(SyntaxTree child1:children())
			{
				if(save==null) {
					save = child1.getType();
				} else {
					if(child1.getType()!=save)
					{
						throw new RuntimeException("Cannot have a list of mixed types at line "+this.getToken().linenum);
					}
				}
			}
			if(save == null)
			{
				save = DataType.Int;//empty lists are all int lists. If you want a different kind of empty list, cast it.
			}
			for(DataType data : DataType.values()) {
				if(data.assignable(save)&&!data.isRange()) {
					ret = data;
				}
			}
			if(ret==null)
			{
				throw new RuntimeException("Unable to identify list type "+this.getToken().linenum);
			}
			break;
		case POINTER_TO:
			ret = DataType.Ptr;
			break;
		case RANGE_COMMA:
			if(inFunction()) {
				this.addDependent(functionIn(), "malloc");
				this.addDependent(functionIn(), "free");
			} else {
				this.notifyCalled("malloc");
				this.notifyCalled("free");
			}
			children = children(4);
			save =children[1].getType();
			if(children[2].getType()!=save)
			{
				unexpected(save,children[2].getType());
			}
			String prefix = "";
			prefix = new TypeResolver<String>(save)
				.CASE(Byte, "b")
				.CASE(Float, "f")
				.CASE(Int, "")
				.CASE(Ubyte, "ub")
				.CASE(Uint, "u")
				.CASE(Ptr, "ptr")
				.DEFAULT_THROW(new RuntimeException("Expected a numeric type. Found "+save+" instead at line "+this.getToken().linenum))
				.get();
			
			String tryit = prefix+"range";
			tryit = Character.toUpperCase(tryit.charAt(0))+tryit.substring(1);
			ret = DataType.valueOf(tryit);
			break;
		case RESET:
		case SET:
			child = children()[0];
			try {
				if(child.getType()!=DataType.Flag)
					unexpected(DataType.Flag,child.getType());
				
			} catch(Exception e) {
				parent.addVariableToScope(getToken(), child.getTokenString(), DataType.Flag);
			}
			ret = DataType.Void;
			break;
		case RETURN:
			//I guess I'm in a function.
			//anything that follows me must type check, regardless of whether it matches the return value.
			if(children().length>0)
				children()[0].getType();
			ret = DataType.Void;
			break;
		case STRING_LITERAL:
			ret = DataType.Ptr;
			break;
		case TYPE:
			ret = DataType.SYNTAX;
			break;
		case UBYTE_LITERAL:
			ret = DataType.Ubyte;
			break;
		case UINT_LITERAL:
			ret = DataType.Uint;
			break;
		case WITH:
			ret = DataType.SYNTAX;
			break;
		case ALIAS:
			ret = DataType.SYNTAX;
			break;
		case IF_EQ:
		case IF_NE:
			if(getChild(0).getType()!=getChild(1).getType()) {
				mismatch(getChild(0).getType(), getChild(1).getType());
			}
			getChild(2).getType();
			getChild(3).getType();
			break;
		case IF_GE:
		case IF_GT:
		case IF_LE:
		case IF_LT:
			if(!mathTypes.contains(getChild(0).getType()))
				math(getChild(0).getType());
			if(getChild(0).getType()!=getChild(1).getType()) {
				mismatch(getChild(0).getType(), getChild(1).getType());
			}
			getChild(2).getType();
			getChild(3).getType();
			break;
		case CLASS_FUNC_CALL:
			throw new RuntimeException("should not be here");
		case TYPE_DEFINITION:
			for(SyntaxTree childTree:this.children()) {
				if(childTree.getTokenType()==Token.Type.FUNCTION) {
					childTree.getType();
				}
			}
			break;
		default:
			break;
		
		}
		//check to make sure everything resolves properly
		switch(myToken.t)
		{
		case EQ_SIGN:
			if(myToken.s.equals("assign")) {
				this.getChild(1).getType();
			}
			break;
		case FOR:
			this.getChild(1).getType();
			this.getChild(3).getType();
			break;
		case TYPE_DEFINITION:
			for(SyntaxTree child:this.getChildren()) {
				if(child.getTokenType()==Token.Type.FUNCTION) {
					child.getType();
				}
			}
			break;
		default:
			typeCheck();
		}
		typeCache  = ret;
		return ret;
	}
	/**
	 * Flatten out the syntax tree into a linear list of statements
	 * @return the list of statements
	 */
	private ArrayList<SyntaxTree> flatten() {
		ArrayList<SyntaxTree> results = new ArrayList<SyntaxTree>();
		ArrayList<SyntaxTree> children = this.getChildren();
		if(this.getTokenType()==Token.Type.RETURN || children.size()==0) {
			results.add(this);
		} else {
			for(SyntaxTree s:children) {
				results.addAll(s.flatten());
			}
		}
		return results;
	}
	/**
	 * Assert that this function has a return as its last statement
	 */
	private void checkEndsWithAReturn()
	{
		//last "token" in the code block must be a return
		if(this.children()[this.children().length-1].children()[this.children()[this.children().length-1].children().length-1].getTokenType()!=Token.Type.RETURN)
			throw new RuntimeException("Function "+this.getChild(1).getTokenString()+" does not end with a return at line "+myToken.linenum);
	}
	/**
	 * Throw an error specifying that the function is returning a value that does not match its return type
	 * @param functionName the function name
	 * @param expected the expected return type
	 * @param returnStatement the return statement
	 * @param actual the type of the return
	 */
	private void unexpectedReturn(String functionName, DataType expected, Token returnStatement, DataType actual) {
		throw new RuntimeException("Function "+functionName+" should return "+expected+". Instead returned "+actual+" at line "+returnStatement.linenum);
	}
	/**
	 * Assert that every return statement in this function matches the required return type
	 * @param functionName the name of the function
	 * @param expected the required return type
	 */
	private void checkReturnTypes(String functionName, DataType expected)
	{
		
		ArrayList<SyntaxTree> elements = flatten();
		if(expected != DataType.Void)
			checkEndsWithAReturn();
		for(SyntaxTree me:elements) {
			if(me.getTokenType()==Token.Type.RETURN) {
				if(expected==DataType.Void&&!me.getChildren().isEmpty()) {
					unexpectedReturn(functionName,DataType.Void,me.getToken(),me.getChildren().get(0).getType());
				}else if(expected!=DataType.Void&&me.getChildren().isEmpty()) {
					unexpectedReturn(functionName,expected,me.getToken(),DataType.Void);
				}else if(expected!=DataType.Void&&!me.getChildren().isEmpty()) {
					if(me.children()[0].getType()!=expected) {
						unexpectedReturn(functionName,expected,me.getToken(),me.children()[0].getType());
					}
				}
			}
		}
	}
	/**
	 * Make a copy of this syntax tree, but tie it to a different parent (used for restructuring the syntax tree)
	 * @param p the different parent
	 * @return the new syntax tree
	 */
	protected SyntaxTree copyWithDifferentParent(BaseTree p)
	{
		SyntaxTree copy = new SyntaxTree(myToken,theParser,p);
		this.getChildren().forEach(child->copy.addChild(child));
		copy.scopeTypings=this.scopeTypings;
		copy.typeCache = this.typeCache;
		
		return copy;
	}
	public BaseTree getParent() {
		return this.parent;
	}
	public Parser getParser() {
		return theParser;
	}
	/**
	 * Scan this syntax tree to find any return statements (currently only used for generating memory leak warnings)
	 * @return any found return statements
	 */
	public SyntaxTree scanReturn() {
		if(this.getTokenType().equals(Token.Type.RETURN)) {
			return this;
		}
		for(SyntaxTree child:this.getChildren()) {
			SyntaxTree attempt = child.scanReturn();
			if(attempt!=null)
				return attempt;
		}
		return null;
	}
	
}
