package compiler;
import static types.DataType.*;

import java.io.File;

import types.DataType;
import types.TypeResolver;

public class Token {
	public final String s;
	private final String reals;
	public final Type t;
	public String linenum="@unknown failure";
	public String toString()
	{
		return "[text:\""+s+"\", type:"+t+"]";
	}
	public String tokenString() {
		return reals;
	}
	public Token(String s, Type t, boolean guard, File srcfile)
	{
		reals=s;
		if(guard && (t==Type.IDENTIFIER||t==Type.FUNCTION_ARG||t==Type.POINTER_TO)) {
			s= "guard_"+srcfile.getName().replaceAll("[^_a-zA-Z0-9]", "_")+("_"+srcfile.hashCode()+"_"+129).replace('-', 'n')+"_"+s.replace("@", "");
		}
		this.s=s;
		this.t=t;
		g=guard;
		f=srcfile;
	}
	private File f;
	private boolean g;
	public Token unguardedVersion() {
		return new Token(reals, t, false, f).setLineNum(linenum);
	}
	public boolean guarded() {
		return g;
	}
	public File srcFile() {
		return f;
	}
	public Token(Token t) {
		s=t.s;
		reals=t.reals;
		this.t=t.t;
		linenum=t.linenum;
	}
	public Token setLineNum(String string)
	{
		linenum = string;
		return this;
	}
	public InstructionType branchToElseType(DataType inputType) {
		String resultString = "branch_";
		switch(this.t) {
			case IF_LT:
				resultString+="greater_equal_";
				break;
			case IF_LE:
				resultString+="greater_than_";
				break;
			case IF_GT:
				resultString+="less_equal_";
				break;
			case IF_GE:
				resultString+="less_than_";
				break;
			case IF_EQ:
				resultString+="not_equal_";
				break;
			case IF_NE:
				resultString+="equal_to_";
				break;
			default:
				throw new RuntimeException("@@contact devs. non-branching instruction being interpreted as branch");
		}
		resultString += new TypeResolver<String>(inputType)
				.CASE(Int, "i")
				.CASE(Ptr, "ui")
				.CASE(Uint, "ui")
				.CASE(Byte, "b")
				.CASE(Ubyte, "ub")
				.CASE(Float, "f")
				.DEFAULT(() -> {
					if(Token.this.t==Token.Type.IF_EQ || Token.this.t==Token.Type.IF_NE && !inputType.builtin()) {
						return "ui";
					} else
						throw new RuntimeException("Cannot compare type "+inputType+" at line "+Token.this.linenum);
					})
				.get();
		
		return InstructionType.valueOf(resultString);
	}
	public static enum Type{
		
		ADD,
		SUBTRACT,
		IN,
		IDENTIFIER,
		EQ_SIGN,
		FUNC_CALL_NAME,
		CLASS_FUNC_CALL,
		CONSTRUCTOR_CALL,
		TYPE,
		IS,
		AS,
		WHILE,
		IF,
		FOR,
		IFNOT,
		WHILENOT,
		OPEN_RANGE_INCLUSIVE,
		OPEN_RANGE_EXCLUSIVE,
		RANGE_COMMA,
		CLOSE_RANGE_INCLUSIVE,
		CLOSE_RANGE_EXCLUSIVE,
		WITH,
		SET,
		RESET,
		NEGATE,
		COMPLEMENT,
		POINTER_TO,
		TIMES,
		DIVIDE,
		MODULO,
		BITWISE_OR,
		BITWISE_XOR,
		BITWISE_AND,
		LOGICAL_OR,
		LOGICAL_AND,
		FUNCTION_RETTYPE,
		FUNCTION,
		RETURN,
		FUNCTION_NAME,
		FUNCTION_PAREN_L,
		FUNCTION_PAREN_R,
		FUNCTION_COMMA,
		FUNCTION_ARG,
		FUNCTION_ARG_COLON,
		FUNCTION_ARG_TYPE,
		INCREMENT_LOC,
		DECREMENT_LOC,
		OPEN_BRACE,
		CLOSE_BRACE,
		STRING_LITERAL,
		TRUE,
		FALSE,
		BYTE_LITERAL,
		INT_LITERAL,
		FLOAT_LITERAL,
		UINT_LITERAL,
		UBYTE_LITERAL,
		LTHAN,
		LEQUAL,
		GTHAN,
		GEQUAL,
		EMPTY_BLOCK,
		
		IF_GT,
		IF_LT,
		IF_EQ,
		IF_NE,
		IF_GE,
		IF_LE,
		
		SHIFT_RIGHT,
		SHIFT_LEFT,
		CORRECT,
		TEMP,
		ALIAS,
		TYPE_DEFINITION,
		FIELD_ACCESS,
		EXTERN;
		
	}
}
