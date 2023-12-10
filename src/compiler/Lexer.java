package compiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;
import settings.Charmap;
import settings.CompilationSettings;
import types.DataType;
import preprocessor.StringSet;
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
		File library = new File("./library/");
		
		File[] libFiles;
		if(library.exists())
			libFiles = library.listFiles();
		else
			libFiles = new File[] {};
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
	static Pattern type = null;
	
	
	final static Pattern tokenRegex = Pattern.compile("\\s+|(?<=[\\[\\]()])|(?<=[a-zA-Z0-9_])(?=[^a-zA-Z0-9_.$])|(?<=[^a-zA-Z0-9_.@#](?=[^+|&=<>-]))|(?=[.][a-zA-Z_])");
	/**
	 * @return the next string in the input files which matches the boundary conditions to make a sigle token
	 */
	String[] tokenRegexCache = new String[] {};
	int tokenRegexCacheIndex = 0;
	private String getNextString()
	{
		if(tokenRegexCacheIndex < tokenRegexCache.length) {
			return tokenRegexCache[tokenRegexCacheIndex++];
		}

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

				String ret = tokenRegex.split(holding)[0];
				holding = holding.substring(ret.length()).trim();
				return ret;
			}
			else {
				return "";
			}
		}
		tokenRegexCache = tokenRegex.split(holding); 
		String ret = tokenRegexCache[0];
		tokenRegexCacheIndex = 1;
		holding = "";
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
		String rv = ajustedSource.toString();
		return rv;
	}
	final static Pattern fieldAccess = Pattern.compile("\\.[a-zA-Z_][a-zA-Z_0-9]*");
	final static Pattern identifierRegex = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
	final static Pattern PT_BYTE_LITERAL = Pattern.compile("[0-9]+b");
	final static Pattern PT_UBYTE_LITERAL = Pattern.compile("[0-9]+[uU][bB]|[0-9]+[bB][Uu]");
	final static Pattern PT_INT_LITERAL = Pattern.compile("[0-9]+");
	final static Pattern PT_UINT_LITERAL = Pattern.compile("[0-9]+[uU]");
	final static Pattern PT_FLOAT_LITERAL = Pattern.compile("[0-9]+[fF]|[0-9]*\\.[0-9]+|[0-9]+\\.[0-9]*");
	/**
	 * Takes the input files this object was constructed with and turns them into a token list
	 * @return the token list
	 */
	public ArrayList<Token> tokenize(boolean inRepl)
	{
		if(type==null) {
			type = Pattern.compile(String.join("|", lowerStringy(DataType.values())));
		}
		ArrayList<Token> tokens = new ArrayList<>();
		boolean functionContext = false;
		boolean functionImmediate = false;
		boolean fnNameImmediate = false;
		boolean guarded = false;
		StringSet imported = new StringSet();
		File lastFile = null;
		boolean importNext = false;
		boolean typeNameNext = false;
		nextStringLoop:
		while(hasNextString())
		{
			File fileIn;
			String tok = getNextString().intern();
			if(!files.isEmpty()) {
				fileIn = this.files.get(0);
				if(lastFile!=fileIn)
				{
					guarded = false;
					imported.clear();
				}
				lastFile = fileIn;
			} else {
				fileIn = lastFile;
			}
			

			Token tk;
			switch(tok) {
				case "":
					continue nextStringLoop;
				case "in":
					tk = new Token(tok,Token.Type.IN,guarded,fileIn);
					break;
				case "=":
					tk = new Token(tok,Token.Type.EQ_SIGN,guarded,fileIn);
					break;
				case "temp":
					tk = new Token(tok,Token.Type.TEMP,guarded,fileIn);
					break;
				case "import":
					importNext =true;
					continue nextStringLoop;
				case "is":
					tk = new Token(tok,Token.Type.IS,guarded,fileIn);
					break;
				case "guard":
					guarded = !inRepl;
					continue nextStringLoop;
				case "type":
					tk = new Token(tok,Token.Type.TYPE_DEFINITION,guarded,fileIn);
					break;
				case "as":
					tk = new Token(tok,Token.Type.AS,guarded,fileIn);
					break;
				case "while":
					tk = new Token(tok,Token.Type.WHILE,guarded,fileIn);
					break;
				case "if":
					tk = new Token(tok,Token.Type.IF,guarded,fileIn);
					break;
				case "for":
					tk = new Token(tok,Token.Type.FOR,guarded,fileIn);
					break;
				case "ifnot":
					tk = new Token(tok,Token.Type.IFNOT,guarded,fileIn);
					break;
				case "whilenot":
					tk = new Token(tok,Token.Type.WHILENOT,guarded,fileIn);
					break;
				case "extern":
					tk = new Token(tok,Token.Type.EXTERN,guarded,fileIn);
					break;
				case "[":
					tk = new Token(tok,Token.Type.OPEN_RANGE_INCLUSIVE,guarded,fileIn);
					break;
				case "]":
					tk = new Token(tok,Token.Type.CLOSE_RANGE_INCLUSIVE,guarded,fileIn);
					break;
				case "(":
					if(functionContext) {
						tk = new Token(tok,Token.Type.FUNCTION_PAREN_L,guarded,fileIn);
					} else {
						tk = new Token(tok,Token.Type.OPEN_RANGE_EXCLUSIVE,guarded,fileIn);
					}
					break;
				case ")":
					if(functionContext) {
						tk = new Token(tok,Token.Type.FUNCTION_PAREN_R,guarded,fileIn);
						functionContext = false;
					} else {
						tk = new Token(tok,Token.Type.CLOSE_RANGE_EXCLUSIVE,guarded,fileIn);
					}
					break;
				case ",":
					if(functionContext) {
						tk = new Token(tok,Token.Type.FUNCTION_COMMA,guarded,fileIn);
					} else {
						tk = new Token(tok,Token.Type.RANGE_COMMA,guarded,fileIn);
					}
					break;
				case "?":
					tk = new Token(tok,Token.Type.CORRECT,guarded,fileIn);
					break;
				case "with":
					tk = new Token(tok,Token.Type.WITH,guarded,fileIn);
					break;
				case "set":
					tk = new Token(tok,Token.Type.SET,guarded,fileIn);
					break;
				case "reset":
					tk = new Token(tok,Token.Type.RESET,guarded,fileIn);
					break;
				case "!":
					tk = new Token(tok,Token.Type.NEGATE,guarded,fileIn);
					break;
				case "~":
					tk = new Token(tok,Token.Type.COMPLEMENT,guarded,fileIn);
					break;
				case "*":
					tk = new Token(tok,Token.Type.TIMES,guarded,fileIn);
					break;
				case "%":
					tk = new Token(tok,Token.Type.MODULO,guarded,fileIn);
					break;
				case "/":
					tk = new Token(tok,Token.Type.DIVIDE,guarded,fileIn);
					break;
				case "+":
					tk = new Token(tok,Token.Type.ADD,guarded,fileIn);
					break;
				case "-":
					tk = new Token(tok,Token.Type.SUBTRACT,guarded,fileIn);
					break;
				case "|":
					tk = new Token(tok,Token.Type.BITWISE_OR,guarded,fileIn);
					break;
				case "^":
					tk = new Token(tok,Token.Type.BITWISE_XOR,guarded,fileIn);
					break;
				case "&":
					tk = new Token(tok,Token.Type.BITWISE_AND,guarded,fileIn);
					break;
				case "||":
					tk = new Token(tok,Token.Type.LOGICAL_OR,guarded,fileIn);
					break;
				case "&&":
					tk = new Token(tok,Token.Type.LOGICAL_AND,guarded,fileIn);
					break;
				case "function":
					tk = new Token(tok,Token.Type.FUNCTION,guarded,fileIn);
					break;
				case ":":
					tk = new Token(tok,Token.Type.FUNCTION_ARG_COLON,guarded,fileIn);
					break;
				case "++":
					tk = new Token(tok,Token.Type.INCREMENT_LOC,guarded,fileIn);
					break;
				case "--":
					tk = new Token(tok,Token.Type.DECREMENT_LOC,guarded,fileIn);
					break;
				case "{":
					tk = new Token(tok,Token.Type.OPEN_BRACE,guarded,fileIn);
					break;
				case "}":
					tk = new Token(tok,Token.Type.CLOSE_BRACE,guarded,fileIn);
					break;
				case "return":
					tk = new Token(tok,Token.Type.RETURN,guarded,fileIn);
					break;
				case "true":
					tk = new Token(tok,Token.Type.TRUE,guarded,fileIn);
					break;
				case "false":
					tk = new Token(tok,Token.Type.FALSE,guarded,fileIn);
					break;
				case "alias":
					tk = new Token(tok,Token.Type.ALIAS,guarded,fileIn);
					break;
				case "<":
					tk = new Token(tok,Token.Type.LTHAN,guarded,fileIn);
					break;
				case "<=":
					tk = new Token(tok,Token.Type.LEQUAL,guarded,fileIn);
					break;
				case ">":
					tk = new Token(tok,Token.Type.GTHAN,guarded,fileIn);
					break;
				case ">=":
					tk = new Token(tok,Token.Type.GEQUAL,guarded,fileIn);
					break;
				case ";":
					tk = new Token(tok,Token.Type.EMPTY_BLOCK,guarded,fileIn);
					break;
				case "<<":
					tk = new Token(tok,Token.Type.SHIFT_LEFT,guarded,fileIn);
					break;
				case ">>":
					tk = new Token(tok,Token.Type.SHIFT_RIGHT,guarded,fileIn);
					break;
				default:
					if(type.matcher(tok).matches()) {
						if(functionContext) {
							tk = new Token(tok,Token.Type.FUNCTION_ARG_TYPE,guarded,fileIn);
						} else if(functionImmediate) {
							tk = new Token(tok,Token.Type.FUNCTION_RETTYPE,guarded,fileIn);
						} else
							tk = new Token(tok,Token.Type.TYPE,guarded,fileIn);
					} else if(identifierRegex.matcher(tok).matches()){
						//generic string
						//identifier
						if(importNext) {
							imported.add(tok);
							importNext=false;
							continue nextStringLoop;
						}

						if(fnNameImmediate) {
							tk = new Token(tok,Token.Type.FUNCTION_NAME,guarded,fileIn);
						} else if(functionContext) {
							tk = new Token(tok,Token.Type.FUNCTION_ARG,guarded,fileIn);
						} else if(typeNameNext){
							tk = new Token(tok,Token.Type.TYPE,false,fileIn);
							DataType.makeUserType(tok);
							Lexer.type = Pattern.compile(Lexer.type.pattern()+"|"+tok);
						} else {
							boolean hidden = !imported.contains(tok);
							if((!inRepl) && (!guarded) && tok.length()<4 && !(tok.equals("one") || tok.equals("e") || tok.equals("pi") || tok.equals("NaN"))) {
								throw new RuntimeException("Identifier "+tok+" is too short at line "+this.lineNumber+" in "+files.get(0).getName()+"\nconsider using 'guard' ");
							}
							tk = new Token(tok,Token.Type.IDENTIFIER,guarded && hidden,fileIn);
						}
					} else if(tok.endsWith("$")) {
						if(!tok.startsWith("."))
							tk = new Token(tok,Token.Type.FUNC_CALL_NAME,guarded,fileIn);
						else
							tk = new Token(tok,Token.Type.CLASS_FUNC_CALL,guarded,fileIn);
					} else if(PT_BYTE_LITERAL.matcher(tok).matches()) {
						tk = new Token(tok,Token.Type.BYTE_LITERAL,guarded,fileIn);
					} else if(PT_UBYTE_LITERAL.matcher(tok).matches()) {
						tk = new Token(tok,Token.Type.UBYTE_LITERAL,guarded,fileIn);
					} else if(PT_INT_LITERAL.matcher(tok).matches()) {
						tk = new Token(tok,Token.Type.INT_LITERAL,guarded,fileIn);
					} else if(PT_UINT_LITERAL.matcher(tok).matches()) {
						tk = new Token(tok,Token.Type.UINT_LITERAL,guarded,fileIn);
					} else if(PT_FLOAT_LITERAL.matcher(tok).matches()) {
						tk = new Token(tok,Token.Type.FLOAT_LITERAL,guarded,fileIn);
					} else if(tok.startsWith("@")) {
						tk = new Token(tok,Token.Type.POINTER_TO,guarded,fileIn);
					} else if(fieldAccess.matcher(tok).matches()) {
						tk = new Token(tok.substring(1),Token.Type.FIELD_ACCESS,false,fileIn);
					} else if(tok.startsWith("#")) {
						tk = new Token(tok,Token.Type.STRING_LITERAL,guarded,fileIn);
					} else {
						throw new RuntimeException("unrecognizable token: "+tok+" at line "+this.lineNumber+" in "+files.get(0).getName());
					}
					break;
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
			functionImmediate = tok.equals("function") || tok.equals("alias") || tok.equals("extern");
			typeNameNext = tk.t==Token.Type.TYPE_DEFINITION;
		}
		//correct type tokens which were misidentified as identifiers
		ArrayList<Token> newTokens = new ArrayList<>();
		tokens.forEach(t -> {
			if((t.t==Token.Type.IDENTIFIER||t.t==Token.Type.FUNCTION_ARG) && DataType.typeExists(t.unguardedVersion().s)) {
				newTokens.add(new Token(t.unguardedVersion().tokenString(),Token.Type.TYPE,false,t.srcFile()).setLineNum(t.linenum));
			} else if(t.s.length()!=1&&t.t==Token.Type.FUNC_CALL_NAME && DataType.typeExists(t.s.substring(0,t.s.length()-1))) {
				newTokens.add(new Token(t.unguardedVersion().tokenString(),Token.Type.CONSTRUCTOR_CALL,false,t.srcFile()).setLineNum(t.linenum));
			} else {
				newTokens.add(t.setLineNum(t.linenum));
			}
		});
		return newTokens;
	}
	private class fakeFile extends File {
		private static final long serialVersionUID = 1L;
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
		return this.tokenize(true);
	}
	
}
