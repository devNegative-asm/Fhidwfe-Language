package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


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
	public void notifyCalled(String fnName) {
		parent.notifyCalled(fnName);
	}
	@Override
	public void addDependent(String caller, String callee) {
		parent.addDependent(caller, callee);
	}
	public static enum Location{
		GLOBAL,
		ARG,
		NONE,
		LOCAL,
	}
	
	public boolean inFunction() {
		if(parent.getClass()==BaseTree.class) {
			return false;
		} else {
			SyntaxTree pp = (SyntaxTree)parent;
			return pp.isFunction() || pp.inFunction();
		}
	}
	public boolean isFunction(){
		return getTokenType()==Token.Type.FUNCTION;
	}
	
	
	
	
	private ArrayList<Variable> localVariables = new ArrayList<>();
	private ArrayList<Variable> localPointers = new ArrayList<>();
	private ArrayList<Variable> functionVariables = new ArrayList<>();
	private ArrayList<Variable> functionPointers = new ArrayList<>();
	private ArrayList<String> argorder = new ArrayList<>();
	
	private int localSpace = 0;
	public int getMyFunctionsLocalspace() {
		if(this.isFunction()) {
			return getLocalSpaceNeeded();
		} else {
			return parent.getMyFunctionsLocalspace();
		}
	}
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
	
	
	void getNeededLocals(VariableTree parent) {
		if(isFunction()) {
			return;
		}
		VariableTree locals = new VariableTree(parent);
		this.scopeTypings.forEach((string, type) ->{locals.addVariable(string, type,theParser);});
		for(SyntaxTree child:children()) {
			child.getNeededLocals(locals);
		}
		prepared = true;
	}
	public void prepareVariables() {
		if(isFunction()) {
			this.argorder.forEach((name) -> {
				functionVariables.add(new Variable(name,SyntaxTree.Location.ARG,Parser.Data.Uint,theParser));//all args should be of int type in terms of stack operations
			});//do not sort arguments. They come on the stack.
			
			int maxOffset = 2+argorder.size()*2;// +4 gets you the last argument, +6 gets second to last, etc. 
			for(int i=0;i<argorder.size();i++) {
				functionPointers.add(new Variable(argorder.get(i),(byte)(maxOffset-2*i),Parser.Data.Relptr,theParser));
			}

			//look at all sub-blocks that hold local variables.
			//Their names will not conflict with globals or arguments
			VariableTree localsTree = new VariableTree(null);
			children()[children().length-1].getNeededLocals(localsTree);
			this.localSpace = localsTree.getMaxSize();
			HashMap<String,Variable> vars = localsTree.getVars();
			vars.forEach((name, var)->{
				localPointers.add(var);
				localVariables.add(new Variable(name.substring(1),Location.LOCAL,var.getType(),theParser));
			});
		}
		prepared = true;
		
		
		
	}
	@Override
	public byte resolveLocalPointerLiteral(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared");
		}
		for(Variable v:localPointers) {
			if(v.getName().equals(varname)) {
				return v.getValue();
			}
		}
		if(parent!=null)
			return parent.resolveLocalPointerLiteral(varname);
		throw new RuntimeException("@@contact devs. Could not resolve variable");
	}
	@Override
	public byte resolveLocalOffset(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared or type checked");
		}
		String gettingPointer = varname;
		for(Variable v:localPointers) {
			if(v.getName().equals(gettingPointer)) {
				return v.getValue();
			}
		}
		if(parent!=null)
			return parent.resolveLocalOffset(varname);
		throw new RuntimeException("@@contact devs. Could not resolve variable");
	}
	@Override
	public byte resolveArgPointerLiteral(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared");
		}
		for(Variable v:functionPointers) {
			if(v.getName().equals(varname)) {
				return v.getValue();
			}
		}
		if(parent!=null)
			return parent.resolveArgPointerLiteral(varname);
		throw new RuntimeException("@@contact devs. Could not resolve variable");
	}
	@Override
	public byte resolveArgOffset(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared");
		}
		String gettingPointer = varname;
		for(Variable v:functionPointers) {
			if(v.getName().equals(gettingPointer)) {
				return v.getValue();
			}
		}
		if(parent!=null)
			return parent.resolveArgOffset(varname);
		throw new RuntimeException("@@contact devs. Could not resolve variable");
	}
	
	
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
	public int resolveGlobalLoopVariables(String varname) {
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
	public String getTokenString()
	{
		return myToken.s;
	}
	public Token.Type getTokenType()
	{
		return myToken.t;
	}
	public Token getToken()
	{
		return myToken;
	}
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
	private SyntaxTree[] children(int sizeConstraint)
	{
		if(sizeConstraint == super.getChildren().size()) {
			SyntaxTree[] tree = new SyntaxTree[0];
			return super.getChildren().toArray(tree);
		} else {
			throw new RuntimeException("Unexpected amount of subexpressions for type "+myToken.t+". Expected: "+sizeConstraint+", Got: "+super.getChildren().size()+" at line "+myToken.linenum);
		}
	}
	private SyntaxTree[] children()
	{
		SyntaxTree[] tree = new SyntaxTree[0];
		return super.getChildren().toArray(tree);
	}
	private void mismatch(Parser.Data one, Parser.Data two)
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
	private void unexpected(Parser.Data one, Parser.Data two)
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
	private void math(Parser.Data two)
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
		throw new UnusableMathTypeException(myToken.s+" expects data of a mathematical type. Instead found "+two.name()+" at line "+myToken.linenum);
	}
	private void impossible(Parser.Data type) {
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
	private static final List<Parser.Data> mathTypes = Arrays.asList(Parser.Data.Byte,
			Parser.Data.Int,
			Parser.Data.Float,
			Parser.Data.Uint,
			Parser.Data.Ubyte,
			Parser.Data.Ptr);
	private static final List<Parser.Data> impossibleForData = Arrays.asList(Parser.Data.Void, Parser.Data.Flag, Parser.Data.SYNTAX);
	private Parser.Data typeCache = null;
	private Parser.Data checkBinaryMath()
	{
		SyntaxTree[] children = children(2);
		Parser.Data type = children[0].getType(); 
		if(type != children[1].getType()) {
			mismatch(children[0].getType(),children[1].getType());
		} else {
			if(!mathTypes.contains(type))
				math(type);
			return type;
		}
		return null;
	}

	private Parser.Data checkUnaryMath() {
		SyntaxTree children = children(1)[0];
		if(!mathTypes.contains(children.getType()))
			math(children.getType());
		return children.getType();
	}
	
	private void checkPointer() {
		SyntaxTree children = children(1)[0];
		if(children.getType()!=Parser.Data.Ptr)
			unexpected(Parser.Data.Ptr,children.getType());
	}
	
	private HashMap<String,Parser.Data> scopeTypings = new HashMap<>();
	@Override
	public Parser.Data resolveVariableType(String varname, String linenum)
	{
		if(scopeTypings.containsKey(varname)) {
			return scopeTypings.get(varname);
		} else {
			return parent.resolveVariableType(varname, linenum);
		}
	}
	@Override
	void addVariableToScope(Token assignment, String varname, Parser.Data type)
	{
		if(scopeTypings.containsKey(varname) && scopeTypings.get(varname)!=type)
		{
			throw new RuntimeException("tried to assign from type "+type+" to variable of type "+scopeTypings.get(varname)+" at line "+assignment.linenum);
		} else {
			scopeTypings.put(varname, type);
		}
	}

	
	public Parser.Data getType()
	{
		//TODO implement pipelined if type checking
		if(typeCache!=null)
		{
			return typeCache;
		}
		Parser.Data ret = null;
		switch(myToken.t)
		{
		case LOGICAL_AND:
		case LOGICAL_OR:
			SyntaxTree[] children = children(2);
			if(children[0].getType()!=Parser.Data.Bool || children[1].getType()!=Parser.Data.Bool) {
				throw new RuntimeException("cannot use non-boolean types with logical operations at line "+this.getToken().linenum);
			}
			ret = Parser.Data.Bool;
			break;
		case MODULO:
			ret = checkBinaryMath();
			if(ret==Parser.Data.Int)
			{
				if(inFunction())
					this.addDependent(this.functionIn(), "smod");
				else
					notifyCalled("smod");
				
			} else if(ret==Parser.Data.Byte) {
				if(inFunction())
					this.addDependent(this.functionIn(), "sbmod");
				else
					notifyCalled("sbmod");
			}
			break;
		case DIVIDE:
			ret = checkBinaryMath();
			
			if(ret==Parser.Data.Int)
			{
				if(inFunction())
					this.addDependent(this.functionIn(), "sdiv");
				else
					notifyCalled("sdiv");
				
			} else if(ret==Parser.Data.Byte) {
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
				if(mathTypes.contains(children[0].getType())||children[0].getType()==Parser.Data.Bool) {
					
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
			ret = Parser.Data.Bool;
			break;
		case AS:
			children = children(2);
			String targettType = children[1].getTokenString();
			Parser.Data exprType = children[0].getType();
			String rttype = Character.toUpperCase(targettType.charAt(0))+targettType.substring(1);
			Parser.Data targetType = Parser.Data.valueOf(rttype);
			if(impossibleForData.contains(targetType) || impossibleForData.contains(exprType))
			{
				impossible(targetType);
			}
			ret = targetType;
			break;
		case BYTE_LITERAL:
			ret = Parser.Data.Byte;
			break;
		case CORRECT:
			children = children(1);
			switch(children[0].getType()) {
				case Int:
				case Ptr:
				case Uint:
					ret = Parser.Data.Uint;
					break;
				default:
					throw new RuntimeException(children[0].getType()+" is not a valid index type at line "+children[0].getToken().linenum);
			}
			break;
		case CLOSE_BRACE:
			ret = Parser.Data.SYNTAX;
			break;
		case CLOSE_RANGE_EXCLUSIVE:
		case CLOSE_RANGE_INCLUSIVE:
		case EMPTY_BLOCK:
			ret = Parser.Data.SYNTAX;
			break;
		case COMPLEMENT:
			children=children(1);
			if((ret = children[0].getType())!=Parser.Data.Bool)//you gotta love this line
				ret = checkUnaryMath();
			break;
		case EQ_SIGN:
			if(getTokenString().equals("assign"))
			{
				SyntaxTree child = children(2)[1];
				if(impossibleForData.contains(child.getType())) {
					impossible(child.getType());
				}
				boolean typeFlag = false;
				try {
					if(resolveVariableType(children(2)[0].getTokenString(), getToken().linenum)==child.getType()) {
						
					} else {
						typeFlag = true;
					}
				} catch(Exception e) {
					parent.addVariableToScope(getToken(), children(2)[0].getTokenString(), child.getType());
				}
				if(typeFlag)
					unexpected(resolveVariableType(children(2)[0].getTokenString(), getToken().linenum),child.getType());
				ret = Parser.Data.Void;
			} else {
				ret = checkBinaryMath();
				ret = Parser.Data.Bool;
			}
			break;
		case FALSE:
		case TRUE:
			ret = Parser.Data.Bool;
			break;
		case FLOAT_LITERAL:
			ret = Parser.Data.Float;
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
			Parser.Data loopType = Parser.Data.fromLowerCase(children[0].getTokenString());
			switch(loopType) {
			case Int:
			case Uint:
			case Byte:
			case Ubyte:
			case Ptr:
				break;
				default:
					throw new RuntimeException("for loop can only loop over countable data types, not "+loopType+" at line "+children[0].getToken().linenum);
			}
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
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION:
			children = children();
			
			SyntaxTree code = children[children.length-1];
			for(int i=2;i<children.length-1;i++) {
				this.addVariableToScope(children[i].getToken(), children[i].getTokenString(), 
						Parser.Data.fromLowerCase(children[i].getChildren().get(0).getTokenString()));
				argorder.add(children[i].getTokenString());
			}
			
			ret = code.getType();//this is necessary to resolve the types of variables in the code before we check the return type
			

			Parser.Data expectingType = Parser.Data.fromLowerCase(children[0].getTokenString());
			this.checkReturnTypes(children[1].getTokenString(), expectingType);
			
			
			if(ret!=Parser.Data.SYNTAX)
				throw new RuntimeException("expected block after function definition. Found instead "+ret+" at line "+this.getToken().linenum);
			break;
		case FUNCTION_ARG:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION_ARG_COLON:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION_ARG_TYPE:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION_COMMA:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION_NAME:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION_PAREN_L:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION_PAREN_R:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNCTION_RETTYPE:
			ret = Parser.Data.SYNTAX;
			break;
		case FUNC_CALL_NAME:
			//type check the arguments
			//then return my return type
			
			List<Parser.Data> usedTypes = new ArrayList<>();
			
			for(SyntaxTree args:this.getChildren()) {
				Parser.Data type = args.getType();
				usedTypes.add(type);
			}
			int argIndex = theParser.getFunctionInputTypes(getTokenString()).indexOf(usedTypes);
			if(argIndex==-1) {
				throw new RuntimeException("Could not find function or alias named "+getTokenString()+" with input signature "+usedTypes+" at line "+this.getToken().linenum);
			}
			ret = theParser.getFunctionOutputType(getTokenString()).get(argIndex);
			
			if(inFunction())
				this.addDependent(this.functionIn(), getTokenString());
			else
				notifyCalled(getTokenString());
			break;
		case IDENTIFIER:
			ret = this.resolveVariableType(this.getTokenString(),this.getToken().linenum);
			break;
		case IF:
		case IFNOT:
			this.getChildren().get(2).getType();
		case WHILE:
		case WHILENOT:
			//argument must be a math type or a flag and cannot be a float
			SyntaxTree child = this.getChildren().get(0);
			if(child.getType()==Parser.Data.Float)
			{
				throw new RuntimeException("Conditions cannot be floats at line "+this.getToken().linenum);
			} else if(child.getType()==Parser.Data.Flag)
			{
				ret = Parser.Data.Flag;
			} else if(mathTypes.contains(child.getType()) || child.getType()==Parser.Data.Bool){
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
				ret = Parser.Data.Bool;
			else
				throw new RuntimeException("cannot check whether "+children[0].getType()+" is a member of "+children[1].getType()+" at line "+this.getToken().linenum);
			break;
		case INCREMENT_LOC:
		case DECREMENT_LOC:
			checkPointer();
			ret = Parser.Data.Void;
			break;
		case INT_LITERAL:
			ret = Parser.Data.Int;
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
			ret = Parser.Data.SYNTAX;
			break;
		case OPEN_RANGE_EXCLUSIVE:
		case OPEN_RANGE_INCLUSIVE:
			//this is a list. of what type? let's find out!
			Parser.Data save = null; 
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
				save = Parser.Data.Int;//empty lists are all int lists. If you want a different kind of empty list, cast it.
			}
			for(Parser.Data data : Parser.Data.values()) {
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
			ret = Parser.Data.Ptr;
			break;
		case RANGE_COMMA:
			children = children(4);
			save =children[1].getType();
			if(children[2].getType()!=save)
			{
				unexpected(save,children[2].getType());
			}
			String prefix = "";
			switch(save) {
			case Byte:
				prefix="b";
				break;
			case Float:
				prefix="f";
				break;
			case Int:
				break;
			case Ubyte:
				prefix="ub";
				break;
			case Uint:
				prefix="u";
				break;
			default:
				throw new RuntimeException("Expected a numeric type. Found "+save+" instead at line "+this.getToken().linenum);
			
			}
			String postfix1 = children[0].getTokenType()==Token.Type.OPEN_RANGE_EXCLUSIVE?"o":"c";
			String postfix2 = children[3].getTokenType()==Token.Type.CLOSE_RANGE_EXCLUSIVE?"o":"c";
			String tryit = prefix+"range"+postfix1+postfix2;
			tryit = Character.toUpperCase(tryit.charAt(0))+tryit.substring(1);
			ret = Parser.Data.valueOf(tryit);
			break;
		case RESET:
		case SET:
			child = children()[0];
			try {
				if(child.getType()!=Parser.Data.Flag)
					unexpected(Parser.Data.Flag,child.getType());
				
			} catch(Exception e) {
				parent.addVariableToScope(getToken(), child.getTokenString(), Parser.Data.Flag);
			}
			ret = Parser.Data.Void;
			break;
		case RETURN:
			//I guess I'm in a function.
			//anything that follows me must type check, regardless of whether it matches the return value.
			if(children().length>0)
				children()[0].getType();
			ret = Parser.Data.Void;
			break;
		case STRING_LITERAL:
			ret = Parser.Data.Ptr;
			break;
		case TYPE:
			ret = Parser.Data.SYNTAX;
			break;
		case UBYTE_LITERAL:
			ret = Parser.Data.Ubyte;
			break;
		case UINT_LITERAL:
			ret = Parser.Data.Uint;
			break;
		case WITH:
			ret = Parser.Data.SYNTAX;
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
		default:
			typeCheck();
		}
		typeCache  = ret;
		return ret;
	}
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
	private void checkEndsWithAReturn(String functionName, ArrayList<SyntaxTree> elements)
	{
		//last "token" must be a return
		if(elements.get(elements.size()-1).getTokenType()!=Token.Type.RETURN) {
			throw new RuntimeException("Function "+functionName+" does not end with a return at line "+elements.get(elements.size()-1).getToken().linenum);
		}
	}
	private void unexpectedReturn(String functionName, Parser.Data expected, Token returnStatement, Parser.Data actual) {
		throw new RuntimeException("Function "+functionName+" should return "+expected+". Instead returned "+actual+" at line "+returnStatement.linenum);
	}
	private void checkReturnTypes(String functionName, Parser.Data expected)
	{
		
		ArrayList<SyntaxTree> elements = flatten();
		if(expected != Parser.Data.Void)
			checkEndsWithAReturn(functionName,elements);
		for(SyntaxTree me:elements) {
			if(me.getTokenType()==Token.Type.RETURN) {
				if(expected==Parser.Data.Void&&!me.getChildren().isEmpty()) {
					unexpectedReturn(functionName,Parser.Data.Void,me.getToken(),me.getChildren().get(0).getType());
				}else if(expected!=Parser.Data.Void&&me.getChildren().isEmpty()) {
					unexpectedReturn(functionName,expected,me.getToken(),Parser.Data.Void);
				}else if(expected!=Parser.Data.Void&&!me.getChildren().isEmpty()) {
					if(me.children()[0].getType()!=expected) {
						unexpectedReturn(functionName,expected,me.getToken(),me.children()[0].getType());
					}
				}
			}
		}
	}
	protected SyntaxTree copyWithDifferentParent(BaseTree p)
	{
		SyntaxTree copy = new SyntaxTree(myToken,theParser,p);
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
	
}
