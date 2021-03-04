package interfaceCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Supplier;

import assembler.Assembler;
import compiler.BaseTree;
import compiler.CompilationSettings;
import compiler.ConstantPropagater;
import compiler.IntermediateLang;
import compiler.Lexer;
import compiler.Parser;
import compiler.Token;
import compiler.Translator;
import preprocessor.Preprocessor;
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
	
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		if(args.length==1) {
			run(args[0]);
			System.exit(0);
		}
		if(args.length!=4) {
			System.err.println("Usage: java -jar compiler.jar \"main_source.fwf\" \"output_prog\" architecture heap_size_bytes");
			System.err.println("Or: java -jar compiler.jar \"executable.bin\" to run a z80emu binary");
			System.err.println("Possible architectures:");
			System.err.println("\t\"TI83pz80\": Ti83+ program");
			System.err.println("\t\"z80Emulator\": z80 ROM, run in emulator");
			System.err.println("\t\"WINx64\": unsupported");
			System.err.println("\t\"WINx86\": unsupported");
			System.exit(-1);
		}
		String binFile = args[1];
		CompilationSettings.Target target = null;
		try {
			 target = CompilationSettings.Target.valueOf(args[2]);
		} catch(Exception e) {
			System.err.println("Unrecognized architecture: "+args[2]);
			System.exit(1);
		}
		int heapspace =0;
		try {
			heapspace = Integer.parseInt(args[3]);
			if(heapspace < 0)
			{ 
				System.err.println("Invalid heap size: "+args[3]);
				System.exit(1);
			}
		} catch(Exception e) {
			System.err.println("Invalid heap size: "+args[3]);
			System.exit(1);
		}
		CompilationSettings settings = CompilationSettings.setIntByteSize(target.intsize).setHeapSpace(heapspace).useTarget(target);
		
		Lexer lx = new Lexer(new File(args[0]),settings);
		ArrayList<Token> tokens = lx.tokenize();
		
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
		case WINx64:
			break;
		case WINx86:
			break;
		case z80Emulator:
			Preprocessor.process(binFile+".asm");
			Assembler.assemble(binFile+".prc", binFile+".bin");
			run(binFile+".bin");
			break;
		}
		
		if(binFile!=null) {
			//try to save the assembly file, preprocess it, assemble it
			
			
			
		} else {
			for(String ins:assembly) {
				System.out.println(ins);
			}
		}
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
		
		Supplier<Byte> bs = () -> {
			try {
				if(System.in.available()!=0) {
						return (byte)System.in.read();
					
				}
			} catch (Exception e) {
				return 0;
			}
			return (byte)0;
		};
		
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
