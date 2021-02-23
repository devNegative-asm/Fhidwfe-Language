package assembler;

import java.io.IOException;
import java.io.InputStream;

public class Assembler {
	public static void assemble(String fileasm, String fileout) throws IOException, InterruptedException {
		String command = String.format("tniasm.exe %s %s",fileasm,fileout);
		Process p = Runtime.getRuntime().exec(command);
		InputStream errors = p.getErrorStream();
		int result = p.waitFor();
		if(result!=0) {
			while(errors.available() > 0) {
				byte[] error = new byte[errors.available()];
				errors.read(error);
				System.err.write(error);
			}
		}
	}
}
