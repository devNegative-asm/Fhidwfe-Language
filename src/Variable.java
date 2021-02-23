
public class Variable implements Comparable<Variable>{
	final String name;
	final SyntaxTree.Location scope;
	final Parser.Data type;
	final CompilationSettings intSize;
	final byte value;
	final boolean constant;
	final String compiletimeIdentifier;
	public Variable(String name, SyntaxTree.Location scope, Parser.Data type, Parser set) {
		if(scope==SyntaxTree.Location.NONE) {
			throw new RuntimeException("@@call the devs. Use other constructor for constant pointers");
		}
		intSize = set.settings;
		this.name=name;
		this.scope=scope;
		this.type=type;
		constant = false;
		value = 0;
		compiletimeIdentifier=null;
	}
	public Variable(String name, byte offsetLocation, Parser.Data type, Parser set) {
		intSize = set.settings;
		this.constant=true;
		this.value=offsetLocation;
		this.type=type;
		this.scope=SyntaxTree.Location.NONE;
		this.name=name;
		compiletimeIdentifier = null;
	}
	
	public Variable(String name, byte offsetLocation, SyntaxTree.Location scope, Parser.Data type, Parser set) {
		this.constant=true;
		this.value=offsetLocation;
		intSize = set.settings;
		this.type=type;
		this.scope=scope;
		this.name=name;
		compiletimeIdentifier = null;
	}
	
	public Variable(String name, String compiletimeIdentifier, Parser set) {
		this.constant=true;
		this.value=0;
		this.compiletimeIdentifier = compiletimeIdentifier;
		this.type=Parser.Data.Ptr;
		intSize = set.settings;
		this.scope=SyntaxTree.Location.NONE;
		this.name=name;
	}

	public String getName() {
		return name;
	}

	public SyntaxTree.Location getScope() {
		return scope;
	}

	public Parser.Data getType() {
		return type;
	}
	public byte getValue() {
		return value;
	}
	public String toString() {
		return this.scope.toString()+" "+this.name+": "+(compiletimeIdentifier==null?value:compiletimeIdentifier);
	}
	public boolean isLiteral() {
		return this.constant && compiletimeIdentifier==null;
	}
	@Override
	public int compareTo(Variable arg0) {
		int result =-type.getSize(intSize)+arg0.type.getSize(intSize);//sort larger sizes first, then alphabetical if that fails 
		if(result ==0)
			return this.name.compareTo(arg0.name);
		return result;
	}
	public int getSize() {
		return this.getType().getSize(intSize);
	}
}
