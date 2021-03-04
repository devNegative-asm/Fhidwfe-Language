package compiler;

import java.util.ArrayList;

public class ConstantPropagater {
	public static void propagateConstants(ArrayList<String> instructions) {
		for(int i=0;i<instructions.size();i++) {
			String doubleMove = match(instructions,i,
					  "\tmov\\s+([^,]+)\\s*,\\s*(.+)\n"
					+ "\tmov\\s+\\2\\s*,\\s*\\1");
			if(doubleMove!=null) {
				//System.out.println(">>optimized>>\n"+doubleMove+"\n>>removed>>\n"+instructions.get(i+1));
				instructions.remove(i+1);
				i--;
				continue;
			}
			
			String simpleOperation = match(instructions,i,
					  "\tpush rax\n"
					+ "\tmov rax,\\s*(.+?)\n"
					+ "\tpop rbx");
			if(simpleOperation!=null) {
				//System.out.println(">>optimized>>\n"+simpleOperation+"\n>>replaced>>\n"+"\tmov rbx, rax\n"+instructions.get(i+1));
				instructions.set(i,"\tmov rbx, rax");
				//instructions.set(i+1,instructions.get(i+1).replaceAll("mov rax", "mov rbx"));
				instructions.remove(i+2);
			}
			
			String constant = match(instructions,i,
					  "\tmov\\s*(.*?)\\s*,\\s*(.+)\n"
					+ "\t(add|or|xor|and|sub|cmp)\\s*(.*?),\\s*\\1\\s*\n");
			if(constant!=null) {
				String recievedValue = constant.replaceAll("\tmov\\s*(.*?)\\s*,\\s*(.+)\n"+"\t(add|or|xor|and|sub|cmp)\\s*(.*?),\\s*\\1\\s*\n", "$2");
				boolean dothis = true;
				try {
					long l = Long.parseLong(recievedValue);
					if(l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
						//will not work
						dothis=false;
					}
				}catch(NumberFormatException e) {
					
				}
				if(dothis) {
					String replacement = constant.replaceAll("\tmov\\s*(.*?)\\s*,\\s*(.+)\n"+"\t(add|or|xor|and|sub|cmp)\\s*(.*?),\\s*\\1\\s*\n", "\t$3 $4, $2");
					//System.out.println(">>optimized>>\n"+constant+">>replaced>>"+replacement);
					instructions.remove(i);
					instructions.set(i, replacement);
				} else {
					
				}
			}
		}
		/*
		for(String s:instructions) {
			System.out.println(s);
		}
		*/
	}
	private static String match(ArrayList<String> strings, int index, String regex) {
		int read = regex.split("\n").length;//disgusting, splitting a regex with a regex
		if(strings.size() - index > read) {
			StringBuilder finalString = new StringBuilder();
			for(int i=0;i<read;i++) {
				finalString.append(strings.get(i+index));
				if(i<read-1)
					finalString.append("\n");
			}
			String fs = finalString.toString();
			if(fs.matches(regex)) {
				return fs;
			}
		}
		return null;
	}
}
