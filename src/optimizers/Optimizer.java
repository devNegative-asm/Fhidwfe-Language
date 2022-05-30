package optimizers;
import java.util.ArrayList;
import settings.CompilationSettings;
public interface Optimizer {
	public ArrayList<String> optimize(ArrayList<String> instructions, CompilationSettings settings);
}
