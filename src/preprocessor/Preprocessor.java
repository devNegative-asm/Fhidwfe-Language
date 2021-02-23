package preprocessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Preprocessor {
	public static void process(String filename) throws FileNotFoundException
	{
		String name = filename.split("\\.")[0];
		String filenameout = name+".prc";
		
		Scanner in = new Scanner(new File(filename));
		PrintWriter out = new PrintWriter(new File(filenameout));
		
		ArrayList<String> identifiers = new ArrayList<String>();
		ArrayList<String> replacements = new ArrayList<String>();
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
					lineEdit = lineEdit.substring("#define ".length());
					identifiers.add(lineEdit);
					line2 = true;
				} else {
					for(int i=0;i<identifiers.size();i++)
					{
						line = line.replaceAll(identifiers.get(i), replacements.get(i));
					}
					out.println(line);
				}
			}
		}
		
		in.close();
		out.close();
	}
}
