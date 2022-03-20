package compiler;
import java.io.File;

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
			s= "_guard_"+srcfile.getName().replaceAll("[^_a-zA-Z0-9]", "_")+("_"+srcfile.hashCode()+"_"+s.replace("@", "").hashCode()).replace('-', 'n')+"_"+s.replace("@", "");
		}
		this.s=s;
		this.t=t;
		g=guard;
		f=srcfile;
	}
	private File f;
	private boolean g;
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
	public Token add(Token t) {
		switch(t.t) {
		
		}
		return this;
	}
	public static enum Type{
		
		ADD,
		SUBTRACT,
		IN,
		IDENTIFIER,
		EQ_SIGN,
		FUNC_CALL_NAME,
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
		ALIAS;
		
	}
}
