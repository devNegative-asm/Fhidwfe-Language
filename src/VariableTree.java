import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
		if(!doneEditing) {
			ArrayList<String> names = new ArrayList<>();
			checkNameConflicts(names);
		}
		doneEditing = true;
		//verification step
		
	}
	private void checkNameConflicts(ArrayList<String> names){
		for(Variable b:myvars) {
			if(names.contains(b.getName())||names.contains(b.getName()))
				throw new RuntimeException("Variable name "+b.getName()+", clashes with another local variable");
			names.add(b.getName());
		}
		for(VariableTree tree:children) {
			tree.checkNameConflicts(names);
		}
	}
	public void addVariable(String name, Parser.Data type, Parser p){
		if(doneEditing)
			throw new RuntimeException("error in resolving variables: local varlist changed after size retrieved");
		if(parent!=null)
			myvars.add(new Variable(name,(byte)(-getMySize()-parent.getOverallSize()-type.getSize(p)),type,p));
		else
			myvars.add(new Variable(name,(byte)(-getMySize()-type.getSize(p)),type,p));
	}
	private int getMySize() {
		int sizeReq = 0;
		for(Variable b:myvars) {
			sizeReq+=b.getSize();
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
	private int getOverallSize() {
		doneEditing();
		return getMySize() + (parent!=null?parent.getMySize():0);
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
	public int getMaxSize() {
		doneEditing();
		int sizeReq = 0;
		for(Variable b:myvars) {
			sizeReq+=b.getSize();
		}
		int maxSubReq = 0;
		for(VariableTree child:children) {
			int posSubReq = child.getMaxSize();
			if(posSubReq>=maxSubReq)
				maxSubReq=posSubReq;
		}
		return sizeReq+maxSubReq;
	}
}
