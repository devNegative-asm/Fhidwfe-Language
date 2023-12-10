package optimizers;

import java.util.ArrayList;

import settings.CompilationSettings;

public class TI83Optimizer extends Optimizer {
	public static final TI83Optimizer INSTANCE = new TI83Optimizer();
	
	
	
	private TI83Optimizer() {
		
	}



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
			//optimize loading constants into arrays. otherwise it just takes too much memory to make an array
			instructions = multiPatternOptimization(instructions,
					m -> "ld (hl), $1 AND 255\ninc hl\nld (hl), $1>>>8\ninc hl",
					    "push hl",
						"push hl",
						"ld hl,(-?\\d+)",
						"ex \\(sp\\),hl",
						"pop bc",
						"ld \\(hl\\),c",
						"inc hl",
						"ld \\(hl\\),b",
						"pop hl",
						"inc hl",
						"inc hl"
					);
			instructions = multiPatternOptimization(instructions,
					m -> "ld (hl), $1\ninc hl",
						"push hl",
						"push hl",
						"ld hl,(.*)",
						"ex \\(sp\\),hl",
						"pop bc",
						"ld \\(hl\\),c",
						"pop hl",
						"inc hl");
		} while(passMatched);
		return instructions;
	}
}