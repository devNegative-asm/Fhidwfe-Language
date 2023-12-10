package optimizers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import settings.CompilationSettings;
public abstract class Optimizer {
	abstract public ArrayList<String> optimize(ArrayList<String> instructions, CompilationSettings settings);
	protected boolean passMatched = false;
	protected ArrayList<String> multiPatternOptimization(List<String> instructions, Function<MatchResult,String> replacer , String...patternStrings) {
		ArrayList<String> patternStringList = new ArrayList<String>(Arrays.asList(patternStrings));
		patternStringList.replaceAll(s -> "\\s*"+s+"\\s*");
		String combined = "^"+String.join(";", patternStringList)+"$";
		Pattern combinedPattern = Pattern.compile(combined);
		ArrayList<String> results = new ArrayList<String>();
		for(int i=0;i<instructions.size()+1-patternStrings.length;i++) {
			String combinedInstructions = String.join(";", instructions.subList(i, i+patternStrings.length));
			Matcher m =combinedPattern.matcher(combinedInstructions);
			//System.out.println(combinedInstructions+ " matches? " +combined);
			if(m.find()) {
				passMatched = true;
				for(String replacementString:m.replaceFirst(replacer).split("\n"))
					results.add("\t"+replacementString);
				i+=patternStrings.length-1;
			} else {
				results.add(instructions.get(i));
			}
		}
		results.addAll(instructions.subList(instructions.size()-patternStrings.length+1, instructions.size()));
		return results;
	}
}
