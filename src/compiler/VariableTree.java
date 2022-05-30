package compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import types.DataType;

public class VariableTree {
	private ArrayList<Variable> myvars = new ArrayList<>();
	private ArrayList<VariableTree> children = new ArrayList<>();
	public final VariableTree parent;
	public VariableTree(VariableTree parent) {
		this.parent=parent;
		if(parent!=null)
			parent.children.add(this);
	}
	public ArrayList<VariableTree> getChildren() {
		return children;
	}
	private boolean doneEditing = false;
	public void doneEditing() {
		ArrayList<String> names = new ArrayList<>();
		checkNameConflicts(names);
		doneEditing = true;
		//verification step
		
	}
	private void checkNameConflicts(ArrayList<String> names){
		for(Variable b:myvars) {
			if(names.contains(b.getName()))
				throw new RuntimeException("Variable name "+b.getName()+", clashes with another local variable\n"+SyntaxTree.currentlyPreparing);
			names.add(b.getName());
		}
		for(VariableTree tree:children) {
			tree.checkNameConflicts(names);
		}
	}
	public void addVariable(String name, DataType type, Parser p, boolean align){
		int gs = p.settings.intsize;
		if(doneEditing)
			throw new RuntimeException("error in resolving variables: local varlist changed after size retrieved");
		if(!align)
			if(parent!=null)
				myvars.add(new Variable(name,(byte)(-getMySize(p,align)-parent.getOverallSize(p,align)-type.getSize(p)),type,p));
			else
				myvars.add(new Variable(name,(byte)(-getMySize(p,align)-type.getSize(p)),type,p));
		else
			if(parent!=null)
				myvars.add(new Variable(name,(byte)(-getMySize(p,align)-parent.getOverallSize(p,align)-gs),type,p));
			else
				myvars.add(new Variable(name,(byte)(-getMySize(p,align)-gs),type,p));
	}
	private int getMySize(Parser p,boolean align) {
		int sizeReq = 0;
		for(Variable b:myvars) {
			if(!align)
				sizeReq+=b.getSize();
			else
				sizeReq+=p.settings.intsize;
		}
		return sizeReq;
	}
	public HashMap<String,Variable> getVars() {
		doneEditing();
		HashMap<String,Variable> results = new HashMap<String,Variable>();
		myvars.forEach(v -> results.put(v.getName(), v));
		for(VariableTree tr:children) {
			results.putAll(tr.getVars());
		}
		return results;
	}
	public int getOverallSize(Parser p,boolean align) {
		doneEditing();
		return getMySize(p,align) + (parent!=null?parent.getOverallSize(p,align):0);
	}
	public String toString() {
		String result = myvars.toString();
		for(VariableTree child:children) {
			result+="\n";
			result+=child.toString(1);
		}
		return result;
	}
	private String toString(int depth) {
		byte[] rrr = new byte[depth];
		Arrays.fill(rrr, (byte)'-');
		String prefix = new String(rrr);
		String result = prefix+myvars.toString();
		for(VariableTree child:children) {
			result+="\n";
			result+=child.toString(depth+1);
		}
		return result;
	}
	public int getMaxSize(Parser p,boolean align) {
		doneEditing();
		int sizeReq = 0;
		for(Variable b:myvars) {
			if(!align)
				sizeReq+=b.getSize();
			else
				sizeReq+=p.settings.intsize;
		}
		int maxSubReq = 0;
		for(VariableTree child:children) {
			int posSubReq = child.getMaxSize(p,align);
			if(posSubReq>=maxSubReq)
				maxSubReq=posSubReq;
		}
		return sizeReq+maxSubReq;
	}
}
