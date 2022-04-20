package compiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import settings.Charmap;
import settings.CompilationSettings;
/**
 * Tokenizes fwf files
 *
 */
public class Lexer {
	private ArrayList<Scanner> scan = new ArrayList<Scanner>();
	private ArrayList<File> files = new ArrayList<File>();
	private final Charmap x;
	int lineNumber = 0;
	String fileIn ="";
	private ArrayList<String> stringConstants = new ArrayList<>();
	/**
	 * Using x as a character mapping tool, turn the strings into native string constants
	 * @return a list of byte[]s representing the strings
	 */
	public ArrayList<Byte[]> stringConstants() {
		ArrayList<Byte[]> ret = new ArrayList<Byte[]>();
		stringConstants.forEach(s -> 
		{
			int i=0;
			Byte[] res = new Byte[s.length()];
			for(char c:s.toCharArray()) {
				res[i++]=x.charToByte(c);
			}
			ret.add(res);
		});
		return ret;
	}
	/**
	 * Create a lexer to parse the given file with the given settings, and a charactermap to map strings to their native representation
	 * @param f the file containing the main program
	 * @param settings the compilation settings
	 * @param cm a character map
	 * @throws FileNotFoundException if the given file, or a library file is missing
	 */
	public Lexer(File f, CompilationSettings settings, Charmap cm) throws FileNotFoundException {
		Scanner x = f==null?null:new Scanner(f);
		this.x=cm;
		if(x!=null)
			x.useDelimiter("(?<=\n)");
		File[] libFiles = new File("./library/").listFiles();
		Arrays.sort(libFiles,File::compareTo);
		for(File fv:libFiles) {
			if(fv.getName().endsWith(".fwf"))
			{
				files.add(fv);
				scan.add(new Scanner(fv).useDelimiter("(?<=\n)"));
			}
		}
		if(new File("./"+settings.target.libLoc+"/").exists()) {
			
			libFiles = new File("./"+settings.target.libLoc+"/").listFiles();
			Arrays.sort(libFiles,File::compareTo);
			for(File fv:libFiles) {
				if(fv.getName().endsWith(".fwf"))
				{
					files.add(fv);
					scan.add(new Scanner(fv).useDelimiter("(?<=\n)"));
				}
			}
		} else {
			System.err.println("Architecture missing lib folder "+settings.target.libLoc);
		}
		if(f!=null) {
			scan.add(x);
			files.add(f);
		}
	}
	private String holding = "";
	private boolean hasNextString()
	{
		return !scan.isEmpty();
	}
	/**
	 * Generates a string[] which is just each input converted to a string, the turned to lowercase. This is used to turn the list of types into a regex which matches those types
	 * @param data the list of objects
	 * @return the list of strings
	 */
	private static String[] lowerStringy(Object[] data)
	{
		String[] retts = new String[data.length];
		for(int i=0;i<data.length;i++)
			retts[i] = data[i].toString().toLowerCase();
		return retts;
	}
	static final String type = String.join("|", lowerStringy(DataType.values()));
	
	/**
	 * @return the next string in the input files which matches the boundary conditions to make a sigle token
	 */
	private String getNextString()
	{
		//cut on spaces or special characters
		//match words, literals, numbers surrounded by u, ub, b, f
		while(holding.isEmpty())
		{
			if(hasNextString())
			{
				try {
					holding = scan.get(0).next().trim();
					lineNumber++;
					holding = parseForStringsAndComments(holding);
				} catch(NoSuchElementException e) {
					scan.remove(0).close();
					files.remove(0);
					lineNumber=0;
					continue;
				}
				String ret = holding.split("\\s+|(?<=[a-zA-Z0-9_])(?=[^a-zA-Z0-9_.$])|(?<=[^a-zA-Z0-9_.@#](?=[^+|&=<>-]))")[0];
				holding = holding.substring(ret.length()).trim();
				return ret;
			}
			else
				return "";
		}
		String ret = holding.split("\\s+|(?<=[a-zA-Z0-9_])(?=[^a-zA-Z0-9_.$])|(?<=[^a-zA-Z0-9_.@#](?=[^+|&=<>-]))")[0];
		holding = holding.substring(ret.length()).trim();
		return ret;
	}
	
	/**
	 * Parse the given line of code for any comments or string literals
	 * @param holding2 the line of code to parse
	 * @return the line of code with string literals replaced by a #n representation, characters replaced with their byte values, and comments removed
	 */
	private String parseForStringsAndComments(String holding2) {
		StringBuilder ajustedSource = new StringBuilder();
		StringBuilder stringLiteral = new StringBuilder();
		boolean inString = false;
		boolean escaped = false;
		int char_count = 0;
		char charValue = 0;
		boolean holdingSlash = false;
		//special parser-only syntax #0 #1 #2 etc. for string variables
		for(char c:holding2.toCharArray())
		{
			if(holdingSlash)
			{
				holdingSlash = false;
				if(c=='/')//comments
					break;
				else
					ajustedSource.append('/');
			}
			switch(char_count)
			{
				case 1:
					if(c=='\\')
						char_count=2;
					else
					{
						char_count=3;
						charValue=c;
					}
					break;
				case 2:
					switch(c)
					{
						case '0':
							charValue=0;
							break;
						case 'n':
							charValue=('\n');
							break;
						case 't':
							charValue=('\t');
							break;
						case 'r':
							charValue=('\r');
							break;
						default:
							charValue=c;
							break;
					}
					char_count=3;
					break;
				case 3:
					ajustedSource.append(Byte.toUnsignedInt(x.charToByte(charValue)));
					ajustedSource.append("ub");
					char_count=0;
					break;
				case 0:
					if(inString)
					{
						if(escaped)
						{
							switch(c)
							{
								case '0':
									stringLiteral.append((char)0);
									break;
								case 'n':
									stringLiteral.append('\n');
									break;
								case 't':
									stringLiteral.append('\t');
									break;
								case 'r':
									stringLiteral.append('\r');
									break;
								default:
									stringLiteral.append(c);
									break;
							}
							escaped=false;
						} else {
							switch(c)
							{
								case '\\':
									escaped=true;
									break;
								case '"':
									inString = false;
									ajustedSource.append("#");
									ajustedSource.append(stringConstants.size());
									stringConstants.add(stringLiteral.toString());
									stringLiteral = new StringBuilder();
									break;
								default:
									stringLiteral.append(c);
							}
						}
					} else {
						switch(c)
						{
							case '"':
								inString = true;
								break;
							case '\'':
								char_count=1;
								break;
							case '/':
								holdingSlash=true;
								break;
							default:
								ajustedSource.append(c);
								break;
						}
					}
					break;
			}
			
		}
		if(inString || char_count>0 || escaped)
		{
			throw new RuntimeException("Incorrect string or char declaration > "+holding2+" < at line"+lineNumber+" in "+files.get(0).getName());
		}
		return ajustedSource.toString();
	}
	/**
	 * Context needed for the parser to parse ambiguous tokens such as ( and identifiers, as a state machine
	 *
	 */
	private static enum Expecting {
		OUTER,
		INNER,
		EXPR,
		FUNCTION_NAME,
		FUNC_OPEN_PAREN,
		FUNC_DETAILS,
	}
	/**
	 * Takes the input files this object was constructed with and turns them into a token list
	 * @return the token list
	 */
	public ArrayList<Token> tokenize()
	{
		ArrayDeque<Expecting> whereami = new ArrayDeque<>();
		whereami.push(Expecting.OUTER);
		ArrayList<Token> tokens = new ArrayList<>();
		boolean functionContext = false;
		boolean functionImmediate = false;
		boolean fnNameImmediate = false;
		boolean guarded = false;
		ArrayList<String> imported = new ArrayList<String>();
		File lastFile = null;
		boolean importNext = false;
		while(hasNextString())
		{
			File fileIn;
			String tok = getNextString();
			if(!files.isEmpty()) {
				fileIn = this.files.get(0);
				if(lastFile!=fileIn)
				{
					guarded = false;
					imported = new ArrayList<String>();
				}
				lastFile = fileIn;
			} else {
				fileIn = lastFile;
			}
			
			
			
			Token tk;
			if(tok.equals(""))
				continue;
			if(tok.equals("in"))
			{
				tk = new Token(tok,Token.Type.IN,guarded,fileIn);
			} else if(tok.equals("="))
			{
				tk = new Token(tok,Token.Type.EQ_SIGN,guarded,fileIn);
			} else if(tok.equals("temp"))
			{
				tk = new Token(tok,Token.Type.TEMP,guarded,fileIn);
			} else if(tok.equals("import")) {
				importNext =true;
				continue;
			} else if(tok.matches(type)) {
				if(functionContext) {
					tk = new Token(tok,Token.Type.FUNCTION_ARG_TYPE,guarded,fileIn);
				} else if(functionImmediate) {
					tk = new Token(tok,Token.Type.FUNCTION_RETTYPE,guarded,fileIn);
				} else
					tk = new Token(tok,Token.Type.TYPE,guarded,fileIn);
			} else if(tok.equals("is")) {
				tk = new Token(tok,Token.Type.IS,guarded,fileIn);
			} else if(tok.equals("guard")) {
				guarded = true;
				continue;
			} else if(tok.equals("as")) {
				tk = new Token(tok,Token.Type.AS,guarded,fileIn);
			} else if(tok.equals("while")) {
				tk = new Token(tok,Token.Type.WHILE,guarded,fileIn);
			} else if(tok.equals("if")) {
				tk = new Token(tok,Token.Type.IF,guarded,fileIn);
			} else if(tok.equals("for")) {
				tk = new Token(tok,Token.Type.FOR,guarded,fileIn);
			} else if(tok.equals("ifnot")) {
				tk = new Token(tok,Token.Type.IFNOT,guarded,fileIn);
			} else if(tok.equals("whilenot")) {
				tk = new Token(tok,Token.Type.WHILENOT,guarded,fileIn);
			} else if(tok.equals("[")) {
				whereami.push(Expecting.EXPR);
				tk = new Token(tok,Token.Type.OPEN_RANGE_INCLUSIVE,guarded,fileIn);
			} else if(tok.equals("]")) {
				whereami.pop();
				tk = new Token(tok,Token.Type.CLOSE_RANGE_INCLUSIVE,guarded,fileIn);
			} else if(tok.equals("(")) {
				if(functionContext) {
					tk = new Token(tok,Token.Type.FUNCTION_PAREN_L,guarded,fileIn);
				} else {
					tk = new Token(tok,Token.Type.OPEN_RANGE_EXCLUSIVE,guarded,fileIn);
				}
			} else if(tok.equals(")")) {
				if(functionContext) {
					tk = new Token(tok,Token.Type.FUNCTION_PAREN_R,guarded,fileIn);
					functionContext = false;
				} else {
					tk = new Token(tok,Token.Type.CLOSE_RANGE_EXCLUSIVE,guarded,fileIn);
				}
			} else if(tok.equals(",")) {
				if(functionContext) {
					tk = new Token(tok,Token.Type.FUNCTION_COMMA,guarded,fileIn);
				} else {
					tk = new Token(tok,Token.Type.RANGE_COMMA,guarded,fileIn);
				}
			} else if(tok.equals("?")) {
				tk = new Token(tok,Token.Type.CORRECT,guarded,fileIn);
			} else if(tok.equals("with")) {
				tk = new Token(tok,Token.Type.WITH,guarded,fileIn);
			} else if(tok.equals("set")) {
				tk = new Token(tok,Token.Type.SET,guarded,fileIn);
			} else if(tok.equals("reset")) {
				tk = new Token(tok,Token.Type.RESET,guarded,fileIn);
			} else if(tok.equals("!")) {
				tk = new Token(tok,Token.Type.NEGATE,guarded,fileIn);
			} else if(tok.equals("~")) {
				tk = new Token(tok,Token.Type.COMPLEMENT,guarded,fileIn);
			} else if(tok.startsWith("@")) {
				tk = new Token(tok,Token.Type.POINTER_TO,guarded,fileIn);
			} else if(tok.equals("*")) {
				tk = new Token(tok,Token.Type.TIMES,guarded,fileIn);
			} else if(tok.equals("%")) {
				tk = new Token(tok,Token.Type.MODULO,guarded,fileIn);
			} else if(tok.equals("/")) {
				tk = new Token(tok,Token.Type.DIVIDE,guarded,fileIn);
			} else if(tok.equals("+")) {
				tk = new Token(tok,Token.Type.ADD,guarded,fileIn);
			} else if(tok.equals("-")) {
				tk = new Token(tok,Token.Type.SUBTRACT,guarded,fileIn);
			} else if(tok.equals("|")) {
				tk = new Token(tok,Token.Type.BITWISE_OR,guarded,fileIn);
			} else if(tok.equals("^")) {
				tk = new Token(tok,Token.Type.BITWISE_XOR,guarded,fileIn);
			} else if(tok.equals("&")) {
				tk = new Token(tok,Token.Type.BITWISE_AND,guarded,fileIn);
			} else if(tok.equals("||")) {
				tk = new Token(tok,Token.Type.LOGICAL_OR,guarded,fileIn);
			} else if(tok.equals("&&")) {
				tk = new Token(tok,Token.Type.LOGICAL_AND,guarded,fileIn);
			} else if(tok.equals("function")) {
				tk = new Token(tok,Token.Type.FUNCTION,guarded,fileIn);
			} else if(tok.equals("return")) {
				tk = new Token(tok,Token.Type.RETURN,guarded,fileIn);
			} else if(tok.equals(":")) {
				tk = new Token(tok,Token.Type.FUNCTION_ARG_COLON,guarded,fileIn);
			} else if(tok.equals("++")) {
				tk = new Token(tok,Token.Type.INCREMENT_LOC,guarded,fileIn);
			} else if(tok.equals("--")) {
				tk = new Token(tok,Token.Type.DECREMENT_LOC,guarded,fileIn);
			} else if(tok.equals("{")) {
				tk = new Token(tok,Token.Type.OPEN_BRACE,guarded,fileIn);
			} else if(tok.equals("}")) {
				tk = new Token(tok,Token.Type.CLOSE_BRACE,guarded,fileIn);
			} else if(tok.equals("return")) {
				tk = new Token(tok,Token.Type.RETURN,guarded,fileIn);
			} else if(tok.startsWith("#")) {
				tk = new Token(tok,Token.Type.STRING_LITERAL,guarded,fileIn);
			} else if(tok.equals("true")) {
				tk = new Token(tok,Token.Type.TRUE,guarded,fileIn);
			} else if(tok.equals("false")) {
				tk = new Token(tok,Token.Type.FALSE,guarded,fileIn);
			} else if(tok.equals("false")) {
				tk = new Token(tok,Token.Type.FALSE,guarded,fileIn);
			} else if(tok.equals("alias")) {
				tk = new Token(tok,Token.Type.ALIAS,guarded,fileIn);
			} else if(tok.matches("[0-9]+b")) {
				tk = new Token(tok,Token.Type.BYTE_LITERAL,guarded,fileIn);
			} else if(tok.matches("[0-9]+[uU][bB]|[0-9]+[bB][Uu]")) {
				tk = new Token(tok,Token.Type.UBYTE_LITERAL,guarded,fileIn);
			} else if(tok.matches("[0-9]+")) {
				tk = new Token(tok,Token.Type.INT_LITERAL,guarded,fileIn);
			} else if(tok.matches("[0-9]+[uU]")) {
				tk = new Token(tok,Token.Type.UINT_LITERAL,guarded,fileIn);
			} else if(tok.matches("[0-9]+[fF]|[0-9]*\\.[0-9]+|[0-9]+\\.[0-9]*")) {
				tk = new Token(tok,Token.Type.FLOAT_LITERAL,guarded,fileIn);
			} else if(tok.equals("<")) {
				tk = new Token(tok,Token.Type.LTHAN,guarded,fileIn);
			} else if(tok.equals("<=")) {
				tk = new Token(tok,Token.Type.LEQUAL,guarded,fileIn);
			} else if(tok.equals(">")) {
				tk = new Token(tok,Token.Type.GTHAN,guarded,fileIn);
			} else if(tok.equals(">=")) {
				tk = new Token(tok,Token.Type.GEQUAL,guarded,fileIn);
			} else if(tok.endsWith("$")) {
				tk = new Token(tok,Token.Type.FUNC_CALL_NAME,guarded,fileIn);
			} else if(tok.equals(";")) {
				tk = new Token(tok,Token.Type.EMPTY_BLOCK,guarded,fileIn);
			} else if(tok.equals("<<")) {
				tk = new Token(tok,Token.Type.SHIFT_LEFT,guarded,fileIn);
			} else if(tok.equals(">>")) {
				tk = new Token(tok,Token.Type.SHIFT_RIGHT,guarded,fileIn);
			} else if(tok.matches("[a-zA-Z_][a-zA-Z_0-9]*")){
				//generic string
				//identifier
				if(importNext) {
					imported.add(tok);
					importNext=false;
					continue;
				}
				if(fnNameImmediate) {
					tk = new Token(tok,Token.Type.FUNCTION_NAME,guarded,fileIn);
				} else if(functionContext) {
					tk = new Token(tok,Token.Type.FUNCTION_ARG,guarded,fileIn);
				} else {
					if((!guarded) && tok.length()<4 && !(tok.equals("one") || tok.equals("e") || tok.equals("pi") || tok.equals("NaN"))) {
						throw new RuntimeException("Identifier "+tok+" is too short at line "+this.lineNumber+" in "+files.get(0).getName()+"\nconsider using 'guard' ");
					}
					tk = new Token(tok,Token.Type.IDENTIFIER,guarded && !imported.contains(tok),fileIn);
				}
			} else {
				throw new RuntimeException("unrecognizable token: "+tok+" at line "+this.lineNumber+" in "+files.get(0).getName());
			}
			if(tok.startsWith("_"))
				throw new RuntimeException("_ prefix saved for internal use at line "+this.lineNumber+" in "+files.get(0).getName());
			if(tok.startsWith("Fwf_"))
				throw new RuntimeException("Fwf_ prefix saved for internal use at line "+this.lineNumber+" in "+files.get(0).getName());
			tk.setLineNum(this.lineNumber+" in "+files.get(0).getName());
			if(importNext)
				throw new RuntimeException("cannot import non-identifier "+tok+" at line "+this.lineNumber+" in "+files.get(0).getName());
			tokens.add(tk);
			functionContext |= fnNameImmediate;
			fnNameImmediate = functionImmediate;
			functionImmediate = tok.equals("function") || tok.equals("alias");
			
		}
		
		
		return tokens;
	}
	private class fakeFile extends File {
		String name;
		public fakeFile(String pathname) {
			super(".");
			this.name=pathname;
		}
		@Override
		public String getName() {
			return name;
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
	ArrayList<Token> lexMoreTokens(Scanner sc, String fileName) {
		this.files.add(new fakeFile(fileName));
		this.scan.add(sc);
		return this.tokenize();
	}
	
}
