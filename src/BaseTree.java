import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BaseTree {
	private ArrayList<SyntaxTree> children = new ArrayList<>();
	final Parser theParser;
	private static HashSet<String> calledFunctions = new HashSet<>();
	private static HashMap<String,HashSet<String>> dependencies = new HashMap<>();
	private static HashSet<String> calledCache = null;
	public boolean functionIsEverCalled(String func) {
		if(calledCache!=null)
			return calledCache.contains(func);
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
		return (calledCache = exploredFunctions).contains(func);
		
		
		
	}
	public void notifyCalled(String fnName) {
		calledFunctions.add(fnName);
	}
	public void addDependent(String caller, String callee) {
		if(dependencies.containsKey(caller)) {
			dependencies.get(caller).add(callee);
		} else {
			HashSet<String> newset = new HashSet<String>();
			newset.add(callee);
			dependencies.put(caller, newset);
		}
	}
	private HashMap<String,Parser.Data> scopeTypings = new HashMap<>();
	public Parser.Data resolveVariableType(String varname, String linenum)
	{
		if(theParser.hasFunction(varname)) {
			return Parser.Data.Ptr;
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
	public void typeCheck() {
		for(SyntaxTree tree:children) {
			tree.getType();
		}
	}

	
	
	private ArrayList<Variable> globalVariables = new ArrayList<>();
	private ArrayList<Variable> globalPointers = new ArrayList<>();
	
	private VariableTree getNeededLocals() {
		VariableTree locals = new VariableTree(null);
		for(SyntaxTree child:getChildren()) {
			child.getNeededLocals(locals);
		}
		return locals;
	}
	
	boolean prepared = false;
	public void prepareVariables() {
		if(prepared)
			return;
		VariableTree localTree = getNeededLocals();//no need to use stack space in a global scope
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
			globalLocation+=s.getType().getSize(theParser.settings);
		}
		int gotGlobal = globalLocation;
		localTree.getVars().forEach((name, variable)->{
			if(scopeTypings.containsKey(name.substring(1))) {
				throw new RuntimeException("Cannot shadow global "+name.substring(1));
			}
			globalPointers.add(new Variable(variable.name,(byte)(gotGlobal-variable.getValue()),variable.getType(),theParser));
		});
		prepared = true;
		for(SyntaxTree child:this.getChildren()) {
			child.prepareVariables();
		}
		localSpace = localTree.getMaxSize();
	}
	private int localSpace = 0;
	public ArrayList<IntermediateLang.Instruction> getGlobalSymbols() {
		ArrayList<IntermediateLang.Instruction> symbols = new ArrayList<>();
		symbols.add(IntermediateLang.InstructionType.general_label.cv("__globals"));
		globalVariables.forEach(v->{
			symbols.add(IntermediateLang.InstructionType.general_label.cv(v.getName()));
			symbols.add(IntermediateLang.InstructionType.rawspace.cv(""+v.getType().getSize(theParser.settings)));
		});
		symbols.add(IntermediateLang.InstructionType.general_label.cv("__global_loop_vars"));
		symbols.add(IntermediateLang.InstructionType.rawspace.cv(""+localSpace));
		return symbols;
	}
	
	public String resolveGlobalToString(String varname) {
		for(Variable v:globalPointers){
			
			if(v.getName().equals(varname)) {
				if(v.isLiteral())
					return ""+Byte.toUnsignedInt(v.getValue());
				else
					return varname;//lol
			}
		}
		throw new RuntimeException("unable to resolve global vairable "+varname);
	}
	
	public int resolveGlobalLoopVariables(String varname) {
		if(!prepared) {
			throw new RuntimeException("@@contact devs. attemted to resolve global variables before prepared");
		}
		for(Variable v:globalPointers){
			if(v.getName().equals(varname) && v.isLiteral())
				return Byte.toUnsignedInt(v.getValue());
		}
		throw new RuntimeException("unable to resolve global vairable "+varname);
	}
	
	
	
	
	void addVariableToScope(Token assignment, String varname, Parser.Data type)
	{
		if(scopeTypings.containsKey(varname) && scopeTypings.get(varname)!=type)
		{
			throw new RuntimeException("tried to assign from type "+type+" to variable of type "+scopeTypings.get(varname)+" at line "+assignment.linenum);
		} else {
			scopeTypings.put(varname, type);
		}
	}
	public BaseTree(Parser p)
	{
		theParser = p;
		if(this.getClass().equals(BaseTree.class))
			p.settings.library.loadCompiletimeConstants(this);
	}
	public BaseTree addChild(SyntaxTree t)
	{
		children.add(t);
		return this;
	}
	public BaseTree addChild(Token t)
	{
		return this.addChild(new SyntaxTree(t,theParser,this));
	}
	public ArrayList<SyntaxTree> getChildren()
	{
		return children;
	}
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
	public byte resolveLocalPointerLiteral(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	public byte resolveLocalOffset(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	public byte resolveArgPointerLiteral(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	public byte resolveArgOffset(String varname) {
		throw new RuntimeException("Variable not found "+varname);
	}
	public int getMyFunctionsLocalspace() {
		return 0;
	}
	public Parser.Data getType() {
		return Parser.Data.SYNTAX;
	}
	private HashMap<String, Parser.Data> constants = new HashMap<>();
	public String resolveConstant(String var) {
		if(constants.containsKey(var)) {
			return var;
		}
		throw new RuntimeException("could not find constant "+var);
	}
	public void addConstantValue(String string, Parser.Data type) {
		constants.put(string, type);
	}
	public String functionIn() {
		// TODO Auto-generated method stub
		return null;
	}
}
