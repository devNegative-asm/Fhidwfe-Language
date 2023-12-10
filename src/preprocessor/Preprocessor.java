package preprocessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Preprocessor {
	public static void process(String filename) throws FileNotFoundException
	{
		String name = filename.split("\\.")[0];
		String filenameout = name+".prc";
		int defineLength = "#define ".length();
		Scanner in = new Scanner(new File(filename));
		PrintWriter out = new PrintWriter(new File(filenameout));
		
		ArrayList<String> replacements = new ArrayList<String>();
		ArrayList<Pattern> identifiers = new ArrayList<Pattern>();
		boolean line2=false;
		while(in.hasNext())
		{
			String line = in.nextLine();
			if(line2)
			{
				line2= false;
				replacements.add(line);
			} else {
				String lineEdit = line.trim();
				if(lineEdit.contains(";"))
					lineEdit = lineEdit.substring(0,lineEdit.indexOf(';'));
				if(lineEdit.startsWith("#define "))
				{
					lineEdit = lineEdit.substring(defineLength);
					identifiers.add(Pattern.compile(lineEdit));
					line2 = true;
				} else {
					for(int i=0;i<replacements.size();i++)
					{
						line = identifiers.get(i).matcher(line).replaceAll(replacements.get(i));
					}
					out.println(line);
				}
			}
		}
		
		in.close();
		out.close();
	}
}
