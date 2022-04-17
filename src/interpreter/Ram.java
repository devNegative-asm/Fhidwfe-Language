package interpreter;

import java.io.File;

public interface Ram {
	public byte access(long location);
	public void write(long location, byte b);
	public File valueToFile(Value file);
	public Value fileToValue(File f);
	public int functionNameToPointer(String functionName);
	public String functionPointerToName(int ptr);
}
