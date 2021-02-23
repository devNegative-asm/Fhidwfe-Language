package interfaceCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Supplier;

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
	private static boolean paused = false;
	public static void run(String infile) throws InterruptedException, IOException
	{
		MEMORY.setPorts(PORTS);
		MEMORY.setRam(RAM);
		int maddr=0;
		File inf = new File(infile);
		FileInputStream fis = new FileInputStream(inf);
		
		while(fis.available()>0) {
			int byteCount = fis.available();
			fis.read(RAM, maddr, byteCount);
			maddr+=byteCount;
		}
		
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
		Supplier<Byte> avail = () -> {
			try {
				if(System.in.available()!=0) {
						return (byte)System.in.read();
					
				}
			} catch (Exception e) {
				return 0;
			}
			return (byte)0;
		};
		
		IODevice port1 = new IODevice() {
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
					default:
						if((Byte.toUnsignedInt(t)&0xc0)==0xc0)
							descriptor = (byte) (t&0x3f);
						else
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
		// 0xff: enter file select mode (read chars until 00, then reading from this port will give the file descriptor number) 
		// 0x80: write byte
		// 0x81: flush
		// 0x82: close
		// 0x83: available mode
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

		while(PROCESSOR.isIFF1() || !PROCESSOR.isHalted())
		{
			PROCESSOR.execute();
		}
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
