package interfaceCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import assembler.Assembler;
import compiler.BaseTree;
import compiler.Instruction;
import compiler.IntermediateLang;
import compiler.Lexer;
import compiler.Parser;
import compiler.ReplReentrantLexer;
import compiler.SyntaxTree;
import compiler.Token;
import compiler.Translator;
import interpreter.Eval;
import interpreter.Value;
import preprocessor.Preprocessor;
import settings.CompilationSettings;
import settings.ConstantPropagater;
import ti83packager.Packager;
import z80core.MemIoOps;
import z80core.Z80;
import z80core.NotifyOps;
public class Main {
	private static final byte[] RAM = new byte[0x10000];
	private static Scanner interact = new Scanner(System.in);
	private static final byte[] PORTS = new byte[0x100];
	private static final MemIoOps MEMORY = new MemIoOps(RAM.length,PORTS.length);
	private static RandomAccessFile[] files = new RandomAccessFile[63];
	private static NotifyOps operations = new NotifyOps(){

		@Override
		public int breakpoint(int address, int opcode) {
			//System.out.println(Arrays.toString(PORTS).substring(0,100));
			//System.out.println(Arrays.toString(RAM).substring(0,100));
			interact.nextLine();
			return opcode;
		}

		@Override
		public void execDone() {
			
		}
		
	};
	private static final Z80 PROCESSOR = new Z80(MEMORY,operations);
	
	public final static char getC() {
		Pattern previous = interact.delimiter();
		interact.useDelimiter("");
		String character = interact.next();
		interact.useDelimiter(previous);
		if(character.length()!=1) {
			throw new RuntimeException("scanned "+character.length()+" chars instead of 1");
		}
		return character.toCharArray()[0];
	}

	
	private static HashMap<String,String> arguments(String[] args) {
		HashMap<String,String> map = new HashMap<>();
		map.put("mode", "compile");
		String paramName = null;
		for(int i=0;i<args.length;i++) {
			if(args[i].startsWith("--")) {
				if(paramName==null) {
					paramName = args[i].substring(2);
				} else {
					System.err.println("Do not use two --options back to back");
					System.exit(1);
				}
			} else if(args[i].startsWith("-")) {
				if(paramName==null) {
					paramName = args[i].substring(1);
				} else {
					System.err.println("Do not use two --options back to back");
					System.exit(1);
				}
			} else if(paramName==null) {
				if(args[i].endsWith(".fwf"))
					map.put("source", args[i]);
				else
					map.put("mode", args[i]);
			} else {
				map.put(paramName.toLowerCase(), args[i]);
				paramName = null;
			}
		}
		return map;
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		HashMap<String,String> arguments = arguments(args);
		
		
		if(arguments.get("mode").equalsIgnoreCase("repl")) {
			CompilationSettings.Target target = CompilationSettings.Target.REPL;
			int heapSpace = 2<<22;
			CompilationSettings settings = CompilationSettings.setIntByteSize(target.intsize).setHeapSpace(heapSpace).useTarget(target);
			ReplReentrantLexer lx = new ReplReentrantLexer(null,settings, x -> (byte) x);
			ArrayList<Token> tokens = lx.tokenize(true);
			tokens.replaceAll(Token::unguardedVersion);
			Parser p = new Parser(settings);
			BaseTree tree = p.parse(tokens);
			p.functionNames().forEach(tree::notifyCalled);
			tree.typeCheck(); // check that typing is valid, and register all variables in use
			tree.prepareVariables(settings.target.needsAlignment); // give variables their proper locations, whether that be on the stack or in the global scope
			Eval evaluator = new Eval(lx);
			for(SyntaxTree element:tree.getChildren()) {
				if(element.getToken().t==Token.Type.FUNCTION)
					evaluator.evaluate(element);
			}
			for(SyntaxTree element:tree.getChildren()) {
				if(element.getToken().t==Token.Type.EQ_SIGN && element.getTokenString().equals("assign") && element.getChild(0).getTokenString().contains("heap"))
				{
					try {
						evaluator.evaluate(element);
					} catch(RuntimeException e) {
						System.err.println("was processing");
						System.err.println(element);
						throw e;
					}
				}
			}
			for(SyntaxTree element:tree.getChildren()) {
				try {
					evaluator.evaluate(element);
				} catch(RuntimeException e) {
					System.err.println("was processing");
					System.err.println(element);
					throw e;
				}
			}
			System.out.println("[[Fhidwfe repl]]");
			System.out.println("[[type code, then type #!! to execute it]]");
			System.out.println("[[to undo mistakes, type ?!! to reset]]");
			System.out.println("[[type $!! to exit]]");
			Scanner scanningInput = new Scanner(System.in).useDelimiter("!!");
			while(true) {
				String s = scanningInput.next();
				if(s.length()==0)
					continue;
				switch(s.charAt(s.length()-1)) {
					case '?':
						break;
					case '$':
						scanningInput.close();
						System.exit(0);
						break;
					case '#':
						s = s.substring(0,s.length()-1);
						try {
							ArrayList<SyntaxTree> currentEval = p.parseAdditional(tree,lx.getMoreTokens(s));
							try {
								tree.typeCheck(); // check that typing is valid, and register all variables in use
								tree.prepareVariables(settings.target.needsAlignment);
							}catch(RuntimeException e) {
								tree.getChildren().removeAll(currentEval);
								throw e;
							}
							
							for(SyntaxTree element:currentEval) {
								Value result = evaluator.evaluate(element);
								if(result!=Value.SYNTAX && result!=Value.VOID)
									System.out.println(result.type);
									System.out.println(" -> "+result.toString());
							}
						} catch(RuntimeException e) {
							e.printStackTrace();
						}
						break;
					default:
						System.err.println("!! should only be used for repl directives");
						break;
				}
			}
		} else if(arguments.get("mode").equalsIgnoreCase("z80emu")) {
			if(!arguments.containsKey("binary")) {
				System.err.println("Usage: java -jar compiler.jar z80emu --binary \"runnable.bin\"");
				System.exit(1);
			}
			run(arguments.get("binary"));
			System.exit(0);
		} else if(arguments.get("mode").equalsIgnoreCase("compile")) {
			String compilationTarget = arguments.get("target");
			String outputFile = arguments.get("o");
			String heapSize = arguments.get("heap-size");
			String binFile = outputFile;
			String source = arguments.get("source");
			if(outputFile == null || binFile == null || source == null)
				printUsage();
			
			CompilationSettings.Target target = null;
			try {
				 target = CompilationSettings.Target.valueOf(compilationTarget);
			} catch(Exception e) {
				System.err.println("Unrecognized architecture: "+compilationTarget);
				System.err.println("Must be one of "+Arrays.toString(CompilationSettings.Target.values()));
				System.exit(1);
			}
			int heapspace = 0;
			if(heapSize!=null)
				try {
					heapspace = Integer.parseInt(heapSize);
					if(heapspace < 0)
					{ 
						System.err.println("Invalid heap size: "+heapSize);
						System.exit(1);
					}
				} catch(Exception e) {
					System.err.println("Invalid heap size: "+heapSize);
					System.exit(1);
				}
			
			CompilationSettings settings = CompilationSettings.setIntByteSize(target.intsize).setHeapSpace(heapspace).useTarget(target);
			Lexer lx = new Lexer(new File(source),settings, x -> (byte) x);
			ArrayList<Token> tokens = lx.tokenize(false);
			Parser p = new Parser(settings);
			BaseTree tree = p.parse(tokens);
			
			tree.typeCheck(); // check that typing is valid, and register all variables in use
			
			tree.prepareVariables(settings.target.needsAlignment); // give variables their proper locations, whether that be on the stack or in the global scope
			ArrayList<Instruction> VMCode = new IntermediateLang().generateInstructions(tree,lx);// turn elements of the tree into a lower-level intermediate code
			settings.library.correct(VMCode, p);
			PrintWriter pr1 = new PrintWriter(new File(binFile+".vm"));
			p.verify(VMCode);
			for(Instruction s:VMCode) {
				pr1.println(s);
			}
			pr1.close();
			
			ArrayList<String> assembly = Translator.translate(p, VMCode,false);
			ConstantPropagater.propagateConstants(assembly);
			PrintWriter pr = new PrintWriter(new File(binFile+".asm"));
			for(String ins:assembly) {
				pr.println(ins);
			}
			pr.close();
			
			switch(target) {
			case TI83pz80:
				Preprocessor.process(binFile+".asm");
				Assembler.assemble(binFile+".prc", binFile+".bin");
				Packager.to8xp(binFile+".bin");
				break;
			case LINx64:
				break;
			case WINx64:
			case WINx86:
				break;
			case z80Emulator:
				Preprocessor.process(binFile+".asm");
				Assembler.assemble(binFile+".prc", binFile+".bin");
				run(binFile+".bin");
				break;
			}
		} else
			printUsage();
		
	}
	
	private static void printUsage() {
		System.err.println("usage (pick one): ");
		System.err.println("java -jar compiler.jar repl");
		System.err.println("java -jar compiler.jar z80emu --binary ROM.bin");
		System.err.println("java -jar compiler.jar source.fwf --o outputfile --target (TI83pz80 | LINx64 | WINx64 | z80Emulator) [--heap-size heap_size_bytes]");
		
		System.exit(1);
	}


	public static void run(String infile) throws InterruptedException, IOException
	{
		MEMORY.setPorts(PORTS);
		MEMORY.setRam(RAM);
		int maddr=0;
		File inf = new File(infile);
		if(!inf.exists()) {
			System.err.println("Could not find "+infile+" to run.");
			System.exit(1);
		}
		
		FileInputStream fis = new FileInputStream(inf); //load binary
		while(fis.available()>0) {
			int byteCount = fis.available();
			fis.read(RAM, maddr, byteCount);//put binary into ROM portion of memory. This might be changable if an OS is built into it
			maddr+=byteCount;
		}
		fis.close();
		
		Supplier<Byte> bs = () -> {try {
			
			return (byte) System.in.read();} catch(Exception e) {return 0;}};
		
		IODevice port1 = new IODevice() { //FILE IO port
			IOstate state = IOstate.OTHER;
			String nextFile = "";
			byte descriptor = 0;
			@Override
			public Byte get() {
				RandomAccessFile f = files[Byte.toUnsignedInt(descriptor)];
				switch(state) {
				case FILE_SELECT:
					throw new RuntimeException("Cannot read port 1 when selecting file");
				case OTHER:
					if(f==null)
						throw new RuntimeException("Attempt to read nonexistent file #"+descriptor);
					try {
						return f.readByte();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				case PROVIDE_DESCRIPTOR:
					state = IOstate.OTHER;
					return descriptor;
				case WRITE_BYTE:
					throw new RuntimeException("Cannot read port 1 when writing");
				case AVAILABLE:
					state = IOstate.OTHER;
					if(f==null)
						throw new RuntimeException("Attempt to get size of nonexistent file #"+descriptor);
					try {
						return (byte)Math.min(f.length()-f.getFilePointer(), 255);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				
				}
				return 0;//shouldn't happen
			}

			@Override
			public void accept(Byte t) {
				switch(state) {
				case FILE_SELECT:
					if(t==0) {
						String filename = nextFile;
						nextFile="";
						File inf = new File(filename);
						if(!inf.exists()) {
							try {
								inf.createNewFile();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						descriptor = -1;
						for(int tryy=0;tryy<63;tryy++) {
							if(files[tryy]==null) {
								descriptor = (byte)tryy;
								try {
									files[tryy] = new RandomAccessFile(inf,"rw");
									state=IOstate.PROVIDE_DESCRIPTOR;
									return;
								} catch (FileNotFoundException e) {
									throw new RuntimeException(e);
								}
							}
						}
						state=IOstate.PROVIDE_DESCRIPTOR;
					} else {
						nextFile+=(char)Byte.toUnsignedInt(t);
					}
					break;
				case OTHER:
					switch(Byte.toUnsignedInt(t)) {
					case 0xff:
						//file open failed and we got back an invalid file descriptor
						throw new RuntimeException("Failed to open a file. attempted to use descriptor 0x3f");
					case 0x84:
						state = IOstate.FILE_SELECT;
						break;
					case 0x80:
						state = IOstate.WRITE_BYTE;
						break;
					case 0x81://may not be necessary with RandomAccessFile
						if(files[descriptor]==null)
							throw new RuntimeException("attempt to write to file #"+descriptor+" before opening");
						try {
							files[descriptor].getFD().sync();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						break;
					case 0x82:
						if(files[descriptor]!=null)
							try {
								files[descriptor].close();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						files[descriptor] = null;
						break;
					case 0x83:
						state = IOstate.AVAILABLE;
						break;
					default:
						if((Byte.toUnsignedInt(t)&0xc0)==0xc0)
							descriptor = (byte) (t&0x3f);
						else
							//could optionally allow writes for 0x00 - 0x7f if you want the msb to represent the control bit
							throw new RuntimeException("invalid control byte: 0x"+Integer.toHexString(Byte.toUnsignedInt(t)));
					}
					break;
				case PROVIDE_DESCRIPTOR:
					throw new RuntimeException("Cannot write port 1 when providing descriptor");
				case WRITE_BYTE:
					try {
						files[descriptor].write(t);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					state=IOstate.OTHER;
					break;
				case AVAILABLE:
					if(files[descriptor]!=null)
						try {
							files[descriptor].setLength(Byte.toUnsignedLong(t));
							if(files[descriptor].getFilePointer() > files[descriptor].length()) {
								files[descriptor].seek(files[descriptor].length());
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
				}
			}
			
		};
		MEMORY.addOutPortListener(0, n -> {System.out.write(n);System.out.flush();});
		MEMORY.addInPortSupplier(0, bs);//set port 0 to be the text IO port
		MEMORY.addOutPortListener(1, port1);//set port 1 to be file IO
		MEMORY.addInPortSupplier(1, port1);
		//port 1 is for file operations
		//control bytes:
		// 0x80: write byte
		// 0x81: flush
		// 0x82: close
		// 0x83: available mode
		// 0x84: enter file select mode (read chars until 00, then reading from this port will give the file descriptor number) 
		// 0xC0 | descriptor: select file #descriptor
		
		// reading from port 1
		// in file select mode: [undefined]
		// in available mode: returns min(inputstream.available(),255)
		// other time: read 1 byte from file
		
		// writing to port 1
		// in file select mode: name of file
		// in available mode: set file size (only works in range 0-255, so this is only really useful for deleting the file)
		// in write mode: write 1 byte
		// in other mode: read the control byte and update state machine
		
		
		// we exit when the processor does 2 things: Disable all interrupts and halt

		long startTime = System.currentTimeMillis();
		long inCount = 0;
		while(PROCESSOR.isIFF1() || !PROCESSOR.isHalted())
		{
			PROCESSOR.execute();
			inCount++;
		}
		long endTime = System.currentTimeMillis();
		long actualTime = MEMORY.getTstates();
		System.out.println();
		System.out.println("Program finished in "+(endTime-startTime)+"ms.");
		System.out.println("Processed "+inCount+" instructions in "+actualTime+" cycles.");
		System.out.println("Would run in "+(actualTime/6000)+"ms hardware time.");
		FileOutputStream fos = new FileOutputStream(new File("last_mem.bin"));
		fos.write(RAM);
		fos.close();
		for(RandomAccessFile f:files) {
			if(f!=null)
				f.close();
		}
	}
	private static enum IOstate {
		FILE_SELECT,
		WRITE_BYTE,
		PROVIDE_DESCRIPTOR,
		AVAILABLE,
		OTHER
	}
}
