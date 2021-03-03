package compiler;

import java.util.ArrayList;
import java.util.List;

public class Translator {
	public static ArrayList<String> translate(Parser p, List<IntermediateLang.Instruction> instructions, boolean useDSNotation) {
		switch(p.settings.target) {
		case WINx64:
			return new Winx64Translator().translateWin64(instructions, useDSNotation,0,p);
		case TI83pz80:
			return new TI83PTranslator().translateTI83pz80(instructions, useDSNotation,0,p);
		case z80Emulator:
			return new Z80TranslatorForWindows().translate(instructions, useDSNotation,0,p);
		default:
			throw new UnsupportedOperationException("Architecture "+p.settings.target+" does not have a translator yet.");
		}
	}
}
