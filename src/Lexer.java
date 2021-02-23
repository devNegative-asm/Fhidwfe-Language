import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import assembler.Assembler;
import interfaceCore.Main;
import preprocessor.Preprocessor;

public class Lexer {
	private ArrayList<Scanner> scan = new ArrayList<Scanner>();
	private ArrayList<File> files = new ArrayList<File>();
	int lineNumber = 0;
	String fileIn ="";
	private ArrayList<String> stringConstants = new ArrayList<>();
	public ArrayList<String> stringConstants() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.addAll(stringConstants);
		return ret;
	}
	public Lexer(File f) throws FileNotFoundException {
		Scanner x = new Scanner(f);
		x.useDelimiter("(?<=\n)");
		for(File fv:new File("./library/").listFiles()) {
			if(fv.getName().endsWith(".fwf"))
			{
				files.add(fv);
				scan.add(new Scanner(fv).useDelimiter("(?<=\n)"));
			}
		}
		scan.add(x);
		files.add(f);
	}
	private String holding = "";
	private boolean hasNextString()
	{
		return !scan.isEmpty();
	}
	private static String[] lowerStringy(Object[] data)
	{
		String[] retts = new String[data.length];
		for(int i=0;i<data.length;i++)
			retts[i] = data[i].toString().toLowerCase();
		return retts;
	}
	static final String type = String.join("|", lowerStringy(Parser.Data.values()));
	
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
					ajustedSource.append((int)charValue);
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
	private static enum Expecting {
		OUTER,
		INNER,
		EXPR,
		FUNCTION_NAME,
		FUNC_OPEN_PAREN,
		FUNC_DETAILS,
	}
	public ArrayList<Token> tokenize()
	{
		ArrayDeque<Expecting> whereami = new ArrayDeque<>();
		whereami.push(Expecting.OUTER);
		ArrayList<Token> tokens = new ArrayList<>();
		boolean functionContext = false;
		boolean functionImmediate = false;
		boolean fnNameImmediate = false;
		boolean guarded = false;
		File lastFile = null;
		while(hasNextString())
		{
			File fileIn;
			String tok = getNextString();
			if(!files.isEmpty()) {
				fileIn = this.files.get(0);
				if(lastFile!=fileIn)
				{
					guarded = false;
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
				if(fnNameImmediate) {
					tk = new Token(tok,Token.Type.FUNCTION_NAME,guarded,fileIn);
				} else if(functionContext) {
					tk = new Token(tok,Token.Type.FUNCTION_ARG,guarded,fileIn);
				} else {
					tk = new Token(tok,Token.Type.IDENTIFIER,guarded,fileIn);
				}
			} else {
				throw new RuntimeException("unrecognizable token: "+tok+" at line "+this.lineNumber+" in "+files.get(0).getName());
			}
			if(tok.startsWith("__"))
				throw new RuntimeException("__ prefix saved for internal use at line "+this.lineNumber+" in "+files.get(0).getName());
			tk.setLineNum(this.lineNumber+" in "+files.get(0).getName());
			tokens.add(tk);
			functionContext |= fnNameImmediate;
			fnNameImmediate = functionImmediate;
			functionImmediate = tok.equals("function") || tok.equals("alias");
			
		}
		
		
		//each of the token types
		
		/*
		ASSIGN_VAR
		*/
		return tokens;
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		String sourceFile = args[0];
		String binFile = args.length>1?args[1]:null;
		
		Lexer lx = new Lexer(new File(args[0]));
		ArrayList<Token> tokens = lx.tokenize();
		CompilationSettings z80settings = CompilationSettings.setIntByteSize(2).setHeapSpace(256).useTarget(CompilationSettings.Target.TI83pz80);
		//x64 not supported yet
		CompilationSettings x64settings = CompilationSettings.setIntByteSize(8).setHeapSpace(1<<14).useTarget(CompilationSettings.Target.WINx64);
		CompilationSettings emulatorSettings = CompilationSettings.setIntByteSize(2).setHeapSpace(2048).useTarget(CompilationSettings.Target.z80Emulator);
		CompilationSettings settings = emulatorSettings;
		
		
		Parser p = new Parser(settings);
		BaseTree tree = p.parse(tokens);
		
		
		tree.typeCheck(); // check that typing is valid, and register all variables in use
		tree.prepareVariables(); // give variables their proper locations, whether that be on the stack or in the global scope
		ArrayList<IntermediateLang.Instruction> VMCode = new IntermediateLang().generateInstructions(tree,lx);// turn elements of the tree into a lower-level intermediate code
		settings.library.correct(VMCode, p);
		PrintWriter pr1 = new PrintWriter(new File(binFile+".vm"));
		p.verify(VMCode);
		for(IntermediateLang.Instruction s:VMCode) {
			pr1.println(s);
		}
		pr1.close();
		
		ArrayList<String> assembly = new Translator().translate(p, VMCode,true);
		if(binFile!=null) {
			//try to save the assembly file, preprocess it, assemble it
			
			PrintWriter pr = new PrintWriter(new File(binFile+".asm"));
			for(String ins:assembly) {
				pr.println(ins);
			}
			pr.close();
			Preprocessor.process(binFile+".asm");
			Assembler.assemble(binFile+".prc", binFile+".bin");
		} else {
			for(String ins:assembly) {
				System.out.println(ins);
			}
		}
		Main.run(binFile+".bin");
	}
}
