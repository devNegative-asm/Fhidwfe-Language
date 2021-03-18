package compiler;
/**
 * A representation of an instruction in the intermediate language
 * Made of 1 instructiontype and a fixed number of args depending on the type
 *
 */
public final class Instruction{
	public final InstructionType in;
	String[] args;
	public Instruction(InstructionType in, String... args) {
		super();
		this.in = in;
		this.args = args;
	}
	public String toString() {
		if(this.in==InstructionType.function_label||this.in==InstructionType.general_label||this.in==InstructionType.define_symbolic_constant)
			return in.toString()+" "+String.join(", ", args);
		else
			return "\t"+in.toString()+" "+String.join(", ", args);
	}
	public String[] getArgs() {
		return args.clone();
	}
}