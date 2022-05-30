package optimizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import settings.CompilationSettings;

public class X64Optimizer implements Optimizer {
	public static final X64Optimizer INSTANCE = new X64Optimizer();
	
	
	
	private X64Optimizer() {
		
	}
	
	private boolean is32Bit(long l) {
		return l >= 0l && l==(long)(int)l;
	}
	
	private boolean isPowerof2(long l) {
		return Long.highestOneBit(l)==Long.lowestOneBit(l) && l!=0;
	}
	private int log2(long l) {
		return 63- Long.numberOfLeadingZeros(l);
	}
	
	private Pattern compile(String s) {
		return Pattern.compile("^\\s*"+s+"\\s*$");
	}
	
	private ArrayList<String> multiPatternOptimization(List<String> instructions, Function<MatchResult,String> replacer , String...patternStrings) {
		//add ; separator temporarily
		ArrayList<String> patternStringList = new ArrayList<String>(Arrays.asList(patternStrings));
		//mov rax, rbx ;  mov [asd], adad;
		patternStringList.replaceAll(s -> "\\s*"+s+"\\s*");
		String combined = "^"+String.join(";", patternStringList)+"$";
		Pattern combinedPattern = Pattern.compile(combined);
		ArrayList<String> results = new ArrayList<String>();
		for(int i=0;i<instructions.size()+1-patternStrings.length;i++) {
			String combinedInstructions = String.join(";", instructions.subList(i, i+patternStrings.length));
			Matcher m =combinedPattern.matcher(combinedInstructions);
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
	
	private ArrayList<String> singleInstructionOptimizations(List<String> instructions) {
		//strip comments if there are any
		ArrayList<String> results = new ArrayList<String>();
		for(String instruction:instructions) {
			if(instruction.contains(";"))
				results.add(instruction.substring(0,instruction.indexOf(';')));
			else
				results.add(instruction);
		}
		instructions = results;
		
		
		results = new ArrayList<String>();
		for(String in:instructions) {
			
			Matcher m;

			m = compile("(sub|xor) r(..), r\\2").matcher(in);
			if(m.find()) {
				results.add(String.format("\txor e%s, e%s",m.group(2), m.group(2)));
				continue;
			}
			
			
			if(!passMatched) {
				m = compile("mov r(..), (\\d+)").matcher(in);
				if(m.find()) {
					long value = Long.parseLong(m.group(2));
					if(is32Bit(value)) {
						results.add(String.format("\tmov e%s, %s",m.group(1), m.group(2)));
						continue;
					}
				}
				
				m = compile("mov r(..), 255 & (\\d+)").matcher(in);
				if(m.find()) {
					results.add(String.format("\tmov e%s, 255 & %s",m.group(1), m.group(2)));
					continue;
				}
			}
			
			
			
			m = compile("imul rax, (\\d+)").matcher(in);
			if(m.find()) {
				long mulconst = Long.parseLong(m.group(1));
				if(isPowerof2(mulconst)) {
					results.add("\tshl rax, "+log2(mulconst));
					continue;
				}
				if(!passMatched)
					if(mulconst==0) {
						results.add("\txor eax, eax");
						continue;
					}
				if(mulconst==-1l) {
					results.add("\tneg rax");
				}
			}
			
			results.add(in);
		}
		return results;
	}
	
	boolean passMatched;
	
	@Override
	public ArrayList<String> optimize(ArrayList<String> instructions, CompilationSettings settings) {
		ArrayList<String> temp = new ArrayList<>();
		temp.addAll(instructions);
		instructions = temp;
		instructions.removeIf(String::isEmpty);
		int totalLines = instructions.size();
		int passCount = 0;
		do {
			passCount++;
			passMatched = false;
			//optimize literals followed by a shift by a constant
			instructions = multiPatternOptimization(instructions,
					m-> "\tmov r"+m.group(1)+", "+(Long.parseLong(m.group(2))<<Long.parseLong(m.group(3))),
					"mov r(..), (\\d+)",
					"s[ah]l r\\1, (\\d+)"
					);
		
			//optimize math, which looks like
			//push rax
	        //mov rax, 10
	        //pop rcx
	        //mul rcx
			instructions = multiPatternOptimization(instructions,
					m -> "imul rax, $1",
					"push rax",
					"mov rax, (\\S+)",
					"pop rcx",
					"mul rcx"
					);
			
			instructions = multiPatternOptimization(instructions,
					m -> "$2 rax, $1",
					"push rax",
					"mov rax, (\\S+)",
					"pop rcx",
					"(add|and|or|xor) rax, rcx"
					);
			
			instructions = multiPatternOptimization(instructions,
					m -> "sub rax, $1",
					"push rax",
					"mov rax, (\\S+)",
					"pop rcx",
					"xchg rax, rcx",
					"sub rax, rcx"
					);
			
			//moving data
			instructions = multiPatternOptimization(instructions,
					m -> "mov rcx, $1\nmov [rax], rcx",
					"push rax",
					"mov rax, (\\S+)",
					"pop rcx",
					"mov \\[rcx\\], rax"
					);
			
			//unary negate
			instructions = multiPatternOptimization(instructions,
					m -> "mov r$1, "+(m.group(3).equals("not")? ~Long.parseLong(m.group(2)):-Long.parseLong(m.group(2))),
					"mov r(..), (\\d+)",
					"(neg|not) r\\1"
					);
			
			//offset move
			instructions = multiPatternOptimization(instructions,
					m -> "mov rax, [rax+$1]",
					"add rax, (\\d+)",
					"mov rax, \\[rax\\]"
					);
			
			instructions = multiPatternOptimization(instructions,
					m -> {
						if(m.group(4)!=null&&m.group(4).contains("(?<!\\w)rax")) {
							return m.group(0).replace(";","\n");
						} else {
							return "mov rcx, $2\n"
									+ "mov [rax+$1], rcx\n"
									+ "$3";
						}
						
					},
					"add rax, (\\d+)",
					"mov rcx, (\\S+)",
					"mov \\[rax\\], rcx",
					"(mov rax, ([^\n]+)|pop rax)"
					);
			//combined offset
			instructions = multiPatternOptimization(instructions,
					m -> "mov r$1, [$2]",
					"lea r(..), \\[(r..+\\d+)\\]",
					"mov r\\1, \\[r\\1\\]"
					);
			//repeated shift left or right
			instructions = multiPatternOptimization(instructions,
					m -> "s$1 $2, "+Math.min(63l,Long.parseLong(m.group(3))+Long.parseLong(m.group(5))),
					"s([ah][lr]) (\\w+?), (\\d+)",
					"s\\1 (\\2), (\\d+)"
					);
			//redundant function exits
			instructions = multiPatternOptimization(instructions,
					m ->"$1\n$2",
					"(leave)",
					"(ret \\d+)",
					"\\1",
					"\\2"
					);
			
			instructions = multiPatternOptimization(instructions,
					m->{
						return "mov rdx, $1\n"
								+"xchg rax, rdx\n"
								+"$2\n$3";
					},
					"push rax",
					"mov rax, (\\S+)",
					"pop rdx",
					"(sub rdx, rax)",
					"(j(.?.?) \\S+)"
					);
			
			//unnecessary exchanges
			
			instructions = multiPatternOptimization(instructions,
					m->{
						if(("r"+m.group(1)).equals(m.group(4))) {
							//aliasing changes this
							return "mov r$1, $2\nmov r$3, r$1";
						} else {
							return "mov r$3, $2\nmov r$1, $4";
						}},
					"mov r(..), (\\S+)",
					"mov r(..), (\\S+)",
					"(xchg r\\1, r\\3|xchg r\\3, r\\1)"
					);
			instructions = singleInstructionOptimizations(instructions);
		} while(passMatched);
		
		instructions = singleInstructionOptimizations(instructions);
		
		System.out.printf("optimized away %.1f%% of instructions in %d passes\n", (100-100*(instructions.size()/(double)totalLines)), passCount);
		
		return instructions;
	}

}
