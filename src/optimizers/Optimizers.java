package optimizers;

import java.util.ArrayList;

import settings.CompilationSettings;

public class Optimizers {
	public static Optimizer getOptimizer(CompilationSettings settings) {
		switch(settings.target) {
			case WINx64:
			case LINx64:
				return X64Optimizer.INSTANCE;
			case TI83pz80:
				return TI83Optimizer.INSTANCE;
			default:
				return new Optimizer() {
					@Override
					public ArrayList<String> optimize(ArrayList<String> instructions, CompilationSettings settings) {
						return instructions;
					}
					
				};
		}
	}
}
