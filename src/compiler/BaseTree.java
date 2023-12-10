package compiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import types.DataType;
/**
 * The base of a syntax tree
 *
 */
public class BaseTree {
	private ArrayList<SyntaxTree> children = new ArrayList<>();
	final Parser theParser;
	private static HashSet<String> calledFunctions = new HashSet<>();
	private static HashMap<String,HashSet<String>> dependencies = new HashMap<>();
	private static HashSet<String> calledCache = null;
	/**
	 * Determines whether a function is ever called in the program so that uncalled functions can be trimmed and reduce compiled program size
	 * @param func The function
	 * @return whether or not the function is ever called, or a pointer-to-function is ever used
	 */
	public boolean functionIsEverCalled(String func) {
		if(calledCache!=null)
			return calledCache.contains(func);//do not explore the call hierarchy every time this is called
		
		
		HashSet<String> exploredFunctions = new HashSet<String>();
		ArrayList<String> unexploredFunctions = new ArrayList<String>();
		
		unexploredFunctions.addAll(calledFunctions);
		
		
		//implementation of graph traversal to determine function dependencies and only compile those that are called by the main program
		while(!unexploredFunctions.isEmpty()) {
			String exampleFunction = unexploredFunctions.remove(0);
			exploredFunctions.add(exampleFunction);
			if(dependencies.containsKey(exampleFunction)) {
				for(String dependent:dependencies.get(exampleFunction)) {
					if((!exploredFunctions.contains(dependent))&&(!unexploredFunctions.contains(dependent))) {
						unexploredFunctions.add(dependent);
					}
				}
			}
		}
		//puts$ is called by error$ so it is always needed
		exploredFunctions.add("puts");
		//puts has dependencies of its own
		exploredFunctions.add("putchar");
		exploredFunctions.add("putln");
		return (calledCache = exploredFunctions).contains(func);
		
	}
	/**
	 * Notify this object that the given function is called somewhere in the main body code
	 * @param fnName the function which is called
	 */
	public void notifyCalled(String fnName) {
		if(calledCache!=null) {
			throw new RuntimeException("call cache poisoned by "+ fnName);
		}
		if(fnName.equals("Arraylist.add")) {
			throw new RuntimeException(fnName);
		}
		calledFunctions.add(fnName);
	}
	/**
	 * Add a dependency relation to the call heirarchy graph
	 * @param caller The function which calls the callee
	 * @param callee
	 */
	public void addDependent(String caller, String callee) {
		if(calledCache!=null) {
			throw new RuntimeException("call cache poisoned by "+ caller);
		}
		
		if(dependencies.containsKey(caller)) {
			dependencies.get(caller).add(callee);
		} else {
			HashSet<String> newset = new HashSet<String>();
			newset.add(callee);
			dependencies.put(caller, newset);
		}
	}
	private HashMap<String,DataType> scopeTypings = new HashMap<>();
	public ArrayList<String> allVarsInScope() {
		ArrayList<String> result = new ArrayList<>();
		result.addAll(scopeTypings.keySet());
		return result;
	}
	/**
	 * Find out the type of a given variable
	 * @param varname the name of the variable
	 * @param linenum the line in the source code which contains the variable name
	 * @return the type of the variable
	 */
	public DataType resolveVariableType(String varname, String linenum)
	{
		String unguarded = varname.replaceAll("guard_.*?_.*?_.*?_.*?_", "");
		if(theParser.hasFunction(unguarded)) {
			if(theParser.getFunctionInputTypes(unguarded).get(0).size()==1) {
				if(!theParser.getFunctionOutputType(unguarded).contains(DataType.Void)) {
					addConstantValue(varname,DataType.Func);
					return DataType.Func;
				} else {
					throw new RuntimeException("single-arg function pointer " + unguarded + " must not return void at line "+linenum);
				}
			} else if(theParser.getFunctionInputTypes(unguarded).get(0).size()==2) {
				if(!theParser.getFunctionOutputType(unguarded).contains(DataType.Void)) {
					addConstantValue(varname,DataType.Op);
					return DataType.Op;
				} else {
					throw new RuntimeException("double-arg function pointer " + unguarded + " must not return void at line "+linenum);
				}
			} else 
				throw new RuntimeException("function pointer " + unguarded + " must have 1 or 2 parameters at line "+linenum);
			
		} else {
			if(scopeTypings.containsKey(varname)) {
				return scopeTypings.get(varname);
			} else {
				if(this.constants.containsKey(varname)) {
					return constants.get(varname);
				}
				throw new RuntimeException("could not find type for variable by name of "+varname+" at line "+linenum);
			}
		}
	}
	/**
	 * Fills in the syntax tree with type information and verifies type integrity
	 */
	public void typeCheck() {
		for(SyntaxTree tree:children) {
			tree.getType();
		}
	}

	
	
	private ArrayList<Variable> globalVariables = new ArrayList<>();
	private ArrayList<Variable> globalPointers = new ArrayList<>();
	
	/**
	 * Creates a tree which represents the space necessary for local & loop control variables
	 * @param align whether to align locals on word boundaries
	 * @return a VariableTree representing the local variables
	 */
	private VariableTree getNeededLocals(boolean align) {
		VariableTree locals = new VariableTree(null);
		for(SyntaxTree child:getChildren()) {
			child.getNeededLocals(locals,align);
		}
		return locals;
	}
	
	boolean prepared = false;
	/**
	 * Generates an arrangement of variables to be used in the compiled program
	 * @param align whether to align variables on word boundaries 
	 */
	public void prepareVariables(boolean align) {
		if(prepared)
			return;
		VariableTree localTree = getNeededLocals(align);//no need to use stack space in a global scope
		localTree.doneEditing();
		scopeTypings.forEach((s, type)->{
			globalVariables.add(new Variable(s,SyntaxTree.Location.GLOBAL,type,theParser));
		});
		
		
		
		globalVariables.sort(Variable::compareTo);
		//pointer constants
		theParser.functionNames().forEach(s -> {
			globalPointers.add(new Variable(s,s,theParser));
		});
		int globalLocation = 0;
		for(Variable s:globalVariables){
			globalPointers.add(new Variable(s.getName(),s.getName(),theParser));
			if(theParser.settings.target.needsAlignment)
				globalLocation+=theParser.settings.intsize;
			else
				globalLocation+=s.getType().getSize(theParser.settings);
		}
		int gotGlobal = globalLocation;
		localTree.getVars().forEach((name, variable)->{
			if(scopeTypings.containsKey(name.substring(1))) {
				throw new RuntimeException("Cannot shadow global "+name.substring(1));
			}
			globalPointers.add(new Variable(variable.name,(gotGlobal-variable.getValue()),variable.getType(),theParser));
		});
		prepared = true;
		for(SyntaxTree child:this.getChildren()) {
			child.prepareVariables(align);
		}
		localSpace = localTree.getMaxSize(theParser,align);
	}
	private int localSpace = 0;
	/**
	 * Creates a IntermediateLang representation of the global variable table
	 * @return the global variable table in the form of ArrayList&lt;Instruction&gt;
	 */
	public ArrayList<Instruction> getGlobalSymbols() {
		ArrayList<Instruction> symbols = new ArrayList<>();
		symbols.add(InstructionType.data_label.cv("__globals"));
		globalVariables.forEach(v->{
			symbols.add(InstructionType.data_label.cv(v.getName()));
			if(theParser.settings.target.needsAlignment)
				symbols.add(InstructionType.rawspaceints.cv("1"));
			else
				symbols.add(InstructionType.rawspace.cv(""+v.getType().getSize(theParser.settings)));
		});
		symbols.add(InstructionType.data_label.cv("__global_loop_vars"));
		symbols.add(InstructionType.rawspace.cv(""+localSpace));
		return symbols;
	}
	
	/**
	 * Verifies whether the given string is a global variable
	 * @param varname the variable to test
	 * @return the The variable's location in memory (unchanged from the variable name for normal globals)
	 */
	public String resolveGlobalToString(String varname) {
		for(Variable v:globalPointers){
			
			if(v.getName().equals(varname)) {
				if(v.isLiteral())
					return ""+v.getValue();
				else
					return varname;//lol
			}
		}
		throw new RuntimeException("unable to resolve global vairable "+varname);
	}
	
	/**
	 * Finds the index of the given loop control variable
	 * @param varname the loop control variable to locate
	 * @return the location of the loop control variable as an offset from __globals
	 */
	public long resolveGlobalLoopVariables(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared");
		}
		for(Variable v:globalPointers){
			if(v.getName().equals(varname) && v.isLiteral())
				return v.getValue();
		}
		throw new RuntimeException("unable to resolve global vairable "+varname);
	}
	
	
	/**
	 * Add a variable to the global scope
	 * @param assignment the original token used to declare the variable
	 * @param varname the name of the variable
	 * @param type the variable's type
	 */
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
	 * Constructs the base of a syntax tree with the given parser
	 * @param p a parser
	 */
	public BaseTree(Parser p)
	{
		theParser = p;
		if(this.getClass().equals(BaseTree.class))
			p.settings.library.loadCompiletimeConstants(this);
	}
	/**
	 * Add a child to this syntax tree
	 * @param t the child
	 * @return this
	 */
	public BaseTree addChild(SyntaxTree t)
	{
		children.add(t);
		return this;
	}
	/**
	 * Add a child to this syntax tree
	 * @param t the token which will be used to generate a child
	 * @return this
	 */
	public BaseTree addChild(Token t)
	{
		return this.addChild(new SyntaxTree(t,theParser,this));
	}
	/**
	 * Get all children of this node in the syntax tree
	 * @return list of all children
	 */
	public ArrayList<SyntaxTree> getChildren()
	{
		return children;
	}
	/**
	 * Get the nth child of this syntax tree
	 * @param n the index
	 * @return the nth child
	 */
	public SyntaxTree getChild(int n)
	{
		return children.get(n);
	}
	public String toString()
	{
		StringBuilder StringCool = new StringBuilder("SYNTAX_TREE\n");
		for(SyntaxTree tree:children)
		{
			StringCool.append(tree.toString(1));
		}
		return StringCool.toString();
	}
	// the global scope has no locals
	public long resolveLocalPointerLiteral(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	// the global scope has no locals
	public long resolveLocalOffset(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	// the global scope has no args
	public long resolveArgPointerLiteral(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	// the global scope has no args
	public long resolveArgOffset(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	// the global scope does not use stack-allocated variables
	public int getMyFunctionsLocalspace() {
		return 0;
	}
	// the program itself has no type
	public DataType getType() {
		return DataType.SYNTAX;
	}
	private HashMap<String, DataType> constants = new HashMap<>();
	/**
	 * Resolve either a pointer-to-function or a symbolic constant to its value
	 * @param var the variable name
	 * @return the actual value of the constant, to be used directly in assembly code
	 */
	public String resolveConstant(String var) {
		if(constants.containsKey(var)) {
			if(constants.get(var)==DataType.Func || constants.get(var)==DataType.Op) {
				return var.replaceAll("guard_.*?_.*?_.*?_.*?_", "");//remove guards if they exist because function names are unaffected by guards
			}
			return var;
		}
		throw new RuntimeException("could not find constant "+var);
	}
	/**
	 * Notifies that the compiler should treat the given string as a symbolic constant. Note that the constant's value isn't defined here. It should be defined in the header provided by CompilationSettings
	 * @param string the symbol's name
	 * @param type the type
	 */
	public void addConstantValue(String string, DataType type) {
		constants.put(string, type);
		theParser.notifySymbol(string);
	}
	// the program is not a function
	public String functionIn() {
		return null;
	}
	public boolean hasVariable(String s) {
		try {
			this.resolveVariableType(s, "");
			return true;
		}catch(Exception e) {
			return false;
		}
	}
}
