package assembler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Assembler {
	public static void assemble(String fileasm, String fileout) throws IOException, InterruptedException {
		if(!new File("tniasm.exe").exists()) {
			throw new RuntimeException("tniasm is required to assemble "+fileasm);
		}
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
			System.exit(result);
		}
	}
}
