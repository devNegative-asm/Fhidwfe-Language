package interpreter;

import java.io.File;
import java.util.ArrayList;

import compiler.DataType;

public class Ram24Bit implements Ram{
	private byte[] RAM = new byte[1<<24];
	File[] openFiles = new File[64];

	@Override
	public byte access(long location) {
		return RAM[(int) (location&0xffffff)];
	}

	@Override
	public void write(long location, byte b) {
		RAM[(int) (location&0xffffff)] = b;
	}

	@Override
	public File valueToFile(Value file) {
		if(file.unsignedByteValue()>62)
			return null;
		return openFiles[file.unsignedByteValue()&63];
	}

	@Override
	public Value fileToValue(File f) {
		int index = 0;
		for(File testFile:openFiles) {
			if(f.equals(testFile))
				return new Value(DataType.File,index,this);
			index++;
		}
		return new Value(DataType.File,63,this);
	}


	ArrayList<String> functionPointers = new ArrayList<>();
	
	@Override
	public int functionNameToPointer(String functionName) {
		int index = functionPointers.indexOf(functionName);
		if(index==-1) {
			index = functionPointers.size();
			functionPointers.add(functionName);
		}
		return index;
	}

	@Override
	public String functionPointerToName(int ptr) {
		if(ptr >= functionPointers.size())
			return "null";
		return functionPointers.get(ptr);
	}
}
