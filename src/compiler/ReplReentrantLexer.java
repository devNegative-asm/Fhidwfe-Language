package compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import settings.Charmap;
import settings.CompilationSettings;

//extends is simply so that this can be casted to lexer type
public class ReplReentrantLexer extends Lexer {

	public ReplReentrantLexer(File f, CompilationSettings settings, Charmap cm) throws FileNotFoundException {
		super(f, settings, cm);
	}
	
	public ArrayList<Token> getMoreTokens(String value) {
		final byte[] bytes = value.getBytes();
		InputStream s = new InputStream() {
			int overallIndex = 0;
			@Override
			public int read() throws IOException {
				if(overallIndex>=bytes.length)
					return -1;
				else
					return bytes[overallIndex++];
			}
		};
		Scanner tempScanner = new Scanner(s);
		tempScanner.useDelimiter("(?<=\n)");
		ArrayList<Token> toks = super.lexMoreTokens(tempScanner, "stdin");
		tempScanner.close();
		return toks;
	}
	
}
