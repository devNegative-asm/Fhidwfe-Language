package optimizers;

import settings.CompilationSettings;

public class Optimizers {
	public static Optimizer getOptimizer(CompilationSettings settings) {
		switch(settings.target) {
			case WINx64:
			case LINx64:
				return X64Optimizer.INSTANCE;
			default:
				return (x,y)->x;	
				
		}
	}
}
