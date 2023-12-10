package assembler;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Z80Assembler {
	interface F1<I,O> {
		O apply(I a);
	}
	interface F3<I,O> {
		O apply(O a, I b, O c);
	}
	
	public static void assemble(String fileasm, String fileout) throws IOException, InterruptedException {
		byte[] bytes = new byte[65536];
		int baseAddress = 0;
		List<String> registers = List.of("a","b","c","d","e","f","h","l","i","r","sp", "ix", "iy", "hl", "af", "sp", "de", "bc", "af'", "hl'", "de'", "bc'", "ixl", "iyl", "ixh", "iyh");
		String[] opcodeStrings = new String[256];
		Pattern[] opcodePatterns = new Pattern[256];

		String[] ixStrings = new String[256];
		Pattern[] ixPatterns = new Pattern[256];
		String[] iyStrings = new String[256];
		Pattern[] iyPatterns = new Pattern[256];
		
		String opss[] = ops.split("\n");
		for(int i=0;i<opss.length;i++) {
			String[] operationsRow = opss[i].split("\t+");
			assert operationsRow.length == 16;
			for(int j=0;j<operationsRow.length;j++) {
				String op = operationsRow[j].strip();
				op = op.replace("(", "\\(");
				op = op.replace(")", "\\)");
				op = op.replaceAll(",", ",\\\\s*");
				op = op.replaceAll(" +", "\\\\s+");
				if(op.startsWith("djnz") || op.startsWith("jr")) {
					op = op.replaceAll("(\\\\s[*+])d", "$1(?<imm8>[^\\\\n,()]+)");
				}
				op = op.replaceAll("(\\\\s[*+]|\\()nn", "$1(?<imm16>[^\\\\n,()]+)");
				op = op.replaceAll("(\\\\s[*+])n(?![zc])", "$1(?<imm8>[^\\\\n,()]+)");
				op = op.replace("\\(n\\)", "\\((?<imm8>[^\\\\n,()]+)\\)");
				op = "^" + op + "$";
				opcodeStrings[i * 16 + j] = op;
				opcodePatterns[i * 16 + j] = Pattern.compile(op);

				if(op.contains("hl")) {
					{
						String opcodeIxRegex = op.replace("hl", "ix");
						opcodeIxRegex = opcodeIxRegex.replace("\\(ix\\)", "\\(ix(?<index>[^\\\\n,()]+)\\)");
						ixStrings[i * 16 + j] = opcodeIxRegex;
						ixPatterns[i * 16 + j] = Pattern.compile(opcodeIxRegex);
					}
					{
						String opcodeIyRegex = op.replace("hl", "iy");
						opcodeIyRegex = opcodeIyRegex.replace("\\(iy\\)", "\\(iy(?<index>[^\\\\n,()]+)\\)");
						iyStrings[i * 16 + j] = opcodeIyRegex;
						iyPatterns[i * 16 + j] = Pattern.compile(opcodeIyRegex);
					}
				}
			}
		}
		//System.out.println(opcodeRegexes[0xD3]);
		
		String[] extendedStrings = new String[12*16];
		Pattern[] extendedPatterns = new Pattern[12*16];
		opss = exops.split("\n");
		for(int i=0;i<opss.length;i++) {
			String[] operationsRow = opss[i].split("\t");
			assert operationsRow.length == 16;
			for(int j=0;j<operationsRow.length;j++) {
				String op = operationsRow[j].strip();
				if(op.length()==0)
					continue;
				op = op.replace("(", "\\(");
				op = op.replace(")", "\\)");
				op = op.replaceAll(",", ",\\\\s*");
				op = op.replaceAll(" +", "\\\\s+");
				op = op.replaceAll("(\\\\s[*+]|\\()nn", "$1(?<imm16>[^\\\\n,()]+)");
				op = op.replaceAll("(\\\\s[*+])n(?![zc])", "$1(?<imm8>[^\\\\n,()]+)");
				op = "^" + op + "$";
				extendedStrings[i * 16 + j] = op;
				extendedPatterns[i * 16 + j] = Pattern.compile(op);
			}
		}
		String[] bitRegexes = new String[256];
		Pattern[] bitPatterns = new Pattern[256];
		opss = bitops.split("\n");
		assert opss.length == 16;
		for(int i=0;i<opss.length;i++) {
			String[] operationsRow = opss[i].split("\t+");
			assert operationsRow.length == 16;
			for(int j=0;j<operationsRow.length;j++) {
				String op = operationsRow[j].strip();
				op = op.replace("(", "\\(");
				op = op.replace(")", "\\)");
				op = op.replaceAll(",", ",\\\\s*");
				op = op.replaceAll(" +", "\\\\s+");
				op = "^" + op + "$";
				bitRegexes[i * 16 + j] = op;
				bitPatterns[i * 16 + j] = Pattern.compile(op);
			}
		}
		
		HashMap<Integer, String> symbolWordReferences = new HashMap<>();
		HashMap<Integer, String> symbolByteReferences = new HashMap<>();
		TreeMap<String, Integer> symbolValues = new TreeMap<>((a,b) -> (b.length() - a.length() == 0 ? b.compareTo(a) : b.length() - a.length()));
		int location = 0;
		ArrayList<String> lines = new ArrayList<>(Files.readAllLines(Paths.get(fileasm)));
		Pattern equates = Pattern.compile("^(\\S+):\\s+EQU\\s+(.*)$");
		
		for(int index = 0; index < lines.size(); index++) {
			
			String line = lines.get(index);
			line = line.strip();
			int comment = line.indexOf(';');
			if(comment>=0)
				line = line.substring(0,comment).strip();
			if(line.length()==0) {
				continue;
			}
			if(line.contains("|")) {
				lines.add(index + 1, line.substring(line.indexOf('|') + 1));
				line = line.substring(0, line.indexOf('|')).strip();
			}
			//line is cleaned now
			Matcher eqMatch = equates.matcher(line);
			
			if(line.matches("^INCLUDE\\s+\"(.+)\"\\s*$")) {
				List<String> extraLines = Files.readAllLines(Paths.get(line.replaceAll("^INCLUDE\\s+\"(.+)\"\\s*$","$1")));
				lines.addAll(index + 1, extraLines);
			} else if(line.startsWith("ORG")) {
				location = parseNum(line.split("ORG")[1].strip());
				baseAddress = location;
			} else if(eqMatch.find()) {
				String ident = eqMatch.group(1);
				String evals = eqMatch.group(2);
				symbolValues.put(new String(ident), eval(evals, symbolValues));
			} else if(line.matches("^\\S+:$")){
				String label = line.replaceAll("^(\\S+):$", "$1");
				symbolValues.put(label, location);
			} else {
				//write bytes and/or symbols
				if(line.matches("^DB\\s+\\S.*")) {
					String data = line.substring(2).strip();
					for(String bytesData:data.split(",")) {
						if(isNum(bytesData)) {
							bytes[location++] = (byte) parseNum(bytesData);
						} else {
							symbolByteReferences.put(location, bytesData);
							location++;
						}
					}
				} else if(line.matches("^DW\\s+\\S+")) {
					String data = line.substring(2).strip();
					if(isNum(data)) {
						int num = parseNum(data);
						bytes[location++] = (byte) (num & 255);
						bytes[location++] = (byte) ((num>>>8) & 255); 
					} else {
						symbolWordReferences.put(location, data);
						location+=2;
					}
				} else {
					boolean assigned = false;
					Matcher match = null;
					for(int i=0;i<256;i++) {//O(c), but the constant is huge, lol
						String opcodeString = opcodeStrings[i];
						Pattern opcodePattern = opcodePatterns[i];
						match = opcodePattern.matcher(line);
						if(match.find() &&
									(!opcodeString.contains("imm") ||
										(opcodeString.contains("imm8") && !registers.contains(match.replaceAll("${imm8}"))) ||
										(opcodeString.contains("imm16") && !registers.contains(match.replaceAll("${imm16}"))))) {
							//System.out.println(line +" -> "+Integer.toHexString(i).toUpperCase());
							bytes[location++] = (byte) i;
							assigned = true;
							break;
						}
					}
					if(!assigned) for(int i=0;i<12*16;i++) {
						if(extendedStrings[i] == null || extendedStrings[i].isEmpty())
							continue;
						match = extendedPatterns[i].matcher(line);
						if(match.find()) {
							//System.out.println(line +" -> ED"+Integer.toHexString(i).toUpperCase());
							bytes[location++] = (byte) 0xED;
							bytes[location++] = (byte) i;
							assigned = true;
							break;
						}
					}
					if(!assigned) for(int i=0;i<256;i++) {
						match = bitPatterns[i].matcher(line);
						if(match.find()) {
							//System.out.println(line +" -> CB"+Integer.toHexString(i).toUpperCase());
							bytes[location++] = (byte) 0xCB;
							bytes[location++] = (byte) i;
							assigned = true;
							break;
						}
					}
					if(!assigned) {
						//try ix and iy
						for(int i=0;i<256;i++) {
							if(ixStrings[i] != null && !ixStrings[i].isEmpty()) {
								Pattern ixPattern = ixPatterns[i];
								match = ixPattern.matcher(line);
								String opcodeIxRegex = ixStrings[i];
								if(match.find() &&
									(!opcodeIxRegex.contains("imm") ||
										(opcodeIxRegex.contains("imm8") && !registers.contains(line.replaceAll(opcodeIxRegex, "${imm8}"))) ||
										(opcodeIxRegex.contains("imm16") && !registers.contains(line.replaceAll(opcodeIxRegex, "${imm16}"))))) {
									bytes[location++] = (byte) 0xDD;
									bytes[location++] = (byte) i;
									assigned = true;
									break;
								}
							}
						}
					}
					if(!assigned) {
						for(int i=0;i<256;i++) {
							if(iyStrings[i] != null && !iyStrings[i].isEmpty()) {
								Pattern iyPattern = iyPatterns[i];
								match = iyPattern.matcher(line);
								String opcodeIyRegex = iyStrings[i];
								if(match.find() &&
									(!opcodeIyRegex.contains("imm") ||
										(opcodeIyRegex.contains("imm8") && !registers.contains(line.replaceAll(opcodeIyRegex, "${imm8}"))) ||
										(opcodeIyRegex.contains("imm16") && !registers.contains(line.replaceAll(opcodeIyRegex, "${imm16}"))))) {
									bytes[location++] = (byte) 0xFD;
									bytes[location++] = (byte) i;
									assigned = true;
									break;
								}
							}
						}
					}
					if(!assigned) switch(line) {
						//non-standard instructions in use
						case "ld b,ixh":
							bytes[location++] = (byte) 0xDD;
							bytes[location++] = (byte) 0x44;
							assigned = true;
							match = null;
							break;
						case "ld c,ixl":
							bytes[location++] = (byte) 0xDD;
							bytes[location++] = (byte) 0x4D;
							assigned = true;
							match = null;
							break;
					}
					
					if(!assigned) {
						System.out.println("unknown op "+ line) ;
						System.exit(1);
					} else if(match!=null) {
						String originalPattern = match.pattern().pattern();
						if(originalPattern.contains("<index>")) {
							symbolByteReferences.put(location++, match.replaceAll("${index}"));
						}
						if(originalPattern.contains("<imm8>")) {
							if(originalPattern.startsWith("^jr") || originalPattern.startsWith("^djnz")) {
								//jumps relative to the end of this instruction. We wrote one byte already
								location++;
								try {
									int diff = eval(match.replaceAll("${imm8}"), new TreeMap<>());
									bytes[location-1] = (byte)diff;
								} catch(NumberFormatException e) {
									symbolByteReferences.put(location-1, match.replaceAll("${imm8}") + "-"+location);
								}
							} else {
								symbolByteReferences.put(location++, match.replaceAll("${imm8}"));
							}
						} else if(originalPattern.contains("<imm16>")){
							symbolWordReferences.put(location, match.replaceAll("${imm16}"));
							location+=2;
						}
					}
				}
			}
		}
		symbolWordReferences.forEach((address, expression) -> {
			assert bytes[address] == 0;
			assert bytes[address+1] == 0;
			int result = eval(expression,symbolValues);
			bytes[address] = (byte) (result & 255);
			bytes[address+1] = (byte) ((result>>>8) & 255); 
		});
		symbolByteReferences.forEach((address, expression) -> {
			assert bytes[address] == 0;
			int result = eval(expression,symbolValues);
			bytes[address] = (byte) (result & 255);
		});
		FileOutputStream fos = new FileOutputStream(fileout);
		fos.write(bytes, baseAddress, location - baseAddress);
		fos.close();
	}

	//very simple math parser
	//this is only broken into 2 functions for benchmarking purposes
	private static int eval(String num, TreeMap<String,Integer> symbolValues) {
		RuntimeException ee = null;
		int rett = 0;
		try {
			rett = evalImpl(num,symbolValues);
		} catch(RuntimeException e) {
			ee = e;
		}
		if(ee!=null) {
			throw ee;
		}
		return rett;
	}
	private static int evalImpl(String num, TreeMap<String, Integer> symbolValues) {
		if(num.length()==0) {
			return 0;
		}
		Integer isDirectSymbol = symbolValues.get(num);
		if(isDirectSymbol != null) {
			return isDirectSymbol;
		}
		num = num.replaceAll("\\s", "");
		if(num.contains("<<") || num.contains(">>") || num.contains(">>>")) {
			return stringRejoin((a,b,c) -> {
				if(b.equals(">>")) {
					return a>>c;
				} else if(b.equals("<<")){
					return a<<c;
				} else {
					return a>>>c;
			}}, num, new String[] {"<<",">>>",">>"}, a -> Z80Assembler.evalImpl(a, symbolValues));
		} else if(num.contains("+") || num.contains("-")) {
			return stringRejoin((a,b,c) -> {
				if(b.equals("+")) {
					return a+c;
				} else {
					return a-c;
			}}, num, new String[] {"+","-"}, a -> Z80Assembler.evalImpl(a, symbolValues));
		} else if(num.contains("*") || num.contains("/")) {
			return stringRejoin((a,b,c) -> {
				if(b.equals("*")) {
					return a*c;
				} else {
					return a/c;
			}}, num, new String[] {"*","/"}, a -> Z80Assembler.evalImpl(a, symbolValues));
		} else if(num.contains("AND") || num.contains("OR")) {
			return stringRejoin((a,b,c) -> {
				if(b.equals("AND")) {
					return a&c;
				} else {
					return a|c;
			}}, num, new String[] {"AND","OR"}, a -> Z80Assembler.evalImpl(a, symbolValues));
		} else {
			return parseNum(num);
		}
	}

	private static boolean isNum(String data) {
		return data.matches("\\$[a-fA-F0-9]+|%[01]+|\\d+|[a-fA-F0-9]+h|[01]+b");
	}
	
	private static int parseNum(String data) {
		if(data.startsWith("$")) {
			return (int) Long.parseLong(data.substring(1), 16);
		}
		if(data.startsWith("%")) {
			return (int) Long.parseLong(data.substring(1), 2);
		}
		if(data.endsWith("h")) {
			return (int) Long.parseLong(data.substring(0,data.length()-1), 16);
		}
		if(data.endsWith("b")) {
			return (int) Long.parseLong(data.substring(0,data.length()-1), 2);
		}
		if(data.startsWith("0")) {
			if(data.length()==1)
				return 0;
			return (int) Long.parseLong(data.substring(1), 8);
		}
		return (int) Long.parseLong(data);
	}
	
	private static <O> O stringRejoin(F3<String,O> joiner, String input, String[] seqs, F1<String,O> evaluator) {
		ArrayList<String> data = new ArrayList<>();
		ArrayList<String> separators = new ArrayList<>();
		while(input.length() > 0) {
			 int[] indices = new int[seqs.length];
			 int locOfMin = 0;
			 for(int i=0;i<indices.length;i++) {
				 indices[i] = input.indexOf(seqs[i]);
				 if(indices[i] >= 0 && (indices[i]<indices[locOfMin] || indices[locOfMin] == -1)) {
					 locOfMin = i;
				 }
			 }
			 int step;
			 if(indices[locOfMin] == -1) {
				 data.add(input);
				 break;
			 }
			 step = indices[locOfMin] + seqs[locOfMin].length();
			 data.add(input.substring(0, indices[locOfMin]));
			 separators.add(seqs[locOfMin]);
			 input = input.substring(step);
		}
		O base = evaluator.apply(data.get(0));
		for(int i=0;i<separators.size();i++) {
			base = joiner.apply(base, separators.get(i), evaluator.apply(data.get(i+1)));
		}
		return base;
	}


private final static String ops =
		  "nop 	ld bc,nn 	ld (bc),a 	inc bc 	inc b 	dec b 	ld b,n 	rlca 	ex af,af' 	add hl,bc 	ld a,(bc) 	dec bc 	inc c 	dec c 	ld c,n 	rrca\n"
		+ "djnz d 	ld de,nn 	ld (de),a 	inc de 	inc d 	dec d 	ld d,n 	rla 	jr d 	add hl,de 	ld a,(de) 	dec de 	inc e 	dec e 	ld e,n 	rra\n"
		+ "jr nz,d 	ld hl,nn 	ld (nn),hl 	inc hl 	inc h 	dec h 	ld h,n 	daa 	jr z,d 	add hl,hl 	ld hl,(nn) 	dec hl 	inc l 	dec l 	ld l,n 	cpl\n"
		+ "jr nc,d 	ld sp,nn 	ld (nn),a 	inc sp 	inc (hl) 	dec (hl) 	ld (hl),n 	scf 	jr c,d 	add hl,sp 	ld a,(nn) 	dec sp 	inc a 	dec a 	ld a,n 	ccf\n"
		+ "ld b,b 	ld b,c 	ld b,d 	ld b,e 	ld b,h 	ld b,l 	ld b,(hl) 	ld b,a 	ld c,b 	ld c,c 	ld c,d 	ld c,e 	ld c,h 	ld c,l 	ld c,(hl) 	ld c,a\n"
		+ "ld d,b 	ld d,c 	ld d,d 	ld d,e 	ld d,h 	ld d,l 	ld d,(hl) 	ld d,a 	ld e,b 	ld e,c 	ld e,d 	ld e,e 	ld e,h 	ld e,l 	ld e,(hl) 	ld e,a\n"
		+ "ld h,b 	ld h,c 	ld h,d 	ld h,e 	ld h,h 	ld h,l 	ld h,(hl) 	ld h,a 	ld l,b 	ld l,c 	ld l,d 	ld l,e 	ld l,h 	ld l,l 	ld l,(hl) 	ld l,a\n"
		+ "ld (hl),b 	ld (hl),c 	ld (hl),d 	ld (hl),e 	ld (hl),h 	ld (hl),l 	halt 	ld (hl),a 	ld a,b 	ld a,c 	ld a,d 	ld a,e 	ld a,h 	ld a,l 	ld a,(hl) 	ld a,a\n"
		+ "add a,b 	add a,c 	add a,d 	add a,e 	add a,h 	add a,l 	add a,(hl) 	add a,a 	adc a,b 	adc a,c 	adc a,d 	adc a,e 	adc a,h 	adc a,l 	adc a,(hl) 	adc a,a\n"
		+ "sub b 	sub c 	sub d 	sub e 	sub h 	sub l 	sub (hl) 	sub a 	sbc a,b 	sbc a,c 	sbc a,d 	sbc a,e 	sbc a,h 	sbc a,l 	sbc a,(hl) 	sbc a,a\n"
		+ "and b 	and c 	and d 	and e 	and h 	and l 	and (hl) 	and a 	xor b 	xor c 	xor d 	xor e 	xor h 	xor l 	xor (hl) 	xor a\n"
		+ "or b 	or c 	or d 	or e 	or h 	or l 	or (hl) 	or a 	cp b 	cp c 	cp d 	cp e 	cp h 	cp l 	cp (hl) 	cp a\n"
		+ "ret nz 	pop bc 	jp nz,nn 	jp nn 	call nz,nn 	push bc 	add a,n 	rst 00h 	ret z 	ret 	jp z,nn 	Bit	call z,nn 	call nn 	adc a,n 	rst 08h\n"
		+ "ret nc 	pop de 	jp nc,nn 	out (n),a 	call nc,nn 	push de 	sub n 	rst 10h 	ret c 	exx 	jp c,nn 	in a,(n) 	call c,nn 	IX	sbc a,n 	rst 18h\n"
		+ "ret po 	pop hl 	jp po,nn 	ex (sp),hl 	call po,nn 	push hl 	and n 	rst 20h 	ret pe 	jp (hl) 	jp pe,nn 	ex de,hl 	call pe,nn 	Misc.		xor n 	rst 28h\n"
		+ "ret p 	pop af 	jp p,nn 	di 	call p,nn 	push af 	or n 	rst 30h 	ret m 	ld sp,hl 	jp m,nn 	ei 	call m,nn 		IY		cp n 	rst 38h";

//some of these extended opcodes are ez80 only, so uhh... don't use those or we'll be assembling nonexistent opcodes
private final static String exops =""
		+ "in0 b,(n) 	out0 (n),b 			tst b 				in0 c,(n) 	out0 (n),c 			tst c 			\n"
		+ "in0 d,(n) 	out0 (n),d 			tst d 				in0 e,(n) 	out0 (n),e 			tst e 			\n"
		+ "in0 h,(n) 	out0 (n),h 			tst h 				in0 l,(n) 	out0 (n),l 			tst l 			\n"
		+ "				tst (hl) 				in0 a,(n) 	out0 (n),a 			tst a 			\n"
		+ "in b,(c) 	out (c),b 	sbc hl,bc 	ld (nn),bc 	neg 	retn 	im 0 	ld i,a 	in c,(c) 	out (c),c 	adc hl,bc 	ld bc,(nn) 	mlt bc 	reti 		ld r,a\n"
		+ "in d,(c) 	out (c),d 	sbc hl,de 	ld (nn),de 			im 1 	ld a,i 	in e,(c) 	out (c),e 	adc hl,de 	ld de,(nn) 	mlt de 		im 2 	ld a,r\n"
		+ "in h,(c) 	out (c),h 	sbc hl,hl 	ld (nn),hl 	tst n 			rrd 	in l,(c) 	out (c),l 	adc hl,hl 	ld hl,(nn) 	mlt hl 			rld\n"
		+ "in (c) 	out (c),0 	sbc hl,sp 	ld (nn),sp 	tstio n 		slp 		in a,(c) 	out (c),a 	adc hl,sp 	ld sp,(nn) 	mlt sp 			\n"
		+ "			otim 								otdm 				\n"
		+ "			otimr 								otdmr 				\n"
		+ "ldi 	cpi 	ini 	outi 					ldd 	cpd 	ind 	outd 				\n"
		+ "ldir 	cpir 	inir 	otir 					lddr 	cpdr 	indr 	otdr 				";

private final static String bitops =""
		+ "rlc b 	rlc c 	rlc d 	rlc e 	rlc h 	rlc l 	rlc (hl) 	rlc a 	rrc b 	rrc c 	rrc d 	rrc e 	rrc h 	rrc l 	rrc (hl) 	rrc a\n"
		+ "rl b 	rl c 	rl d 	rl e 	rl h 	rl l 	rl (hl) 	rl a 	rr b 	rr c 	rr d 	rr e 	rr h 	rr l 	rr (hl) 	rr a\n"
		+ "sla b 	sla c 	sla d 	sla e 	sla h 	sla l 	sla (hl) 	sla a 	sra b 	sra c 	sra d 	sra e 	sra h 	sra l 	sra (hl) 	sra a\n"
		+ "sll b 	sll c 	sll d 	sll e 	sll h 	sll l 	sll (hl) 	sll a 	srl b 	srl c 	srl d 	srl e 	srl h 	srl l 	srl (hl) 	srl a\n"
		+ "bit 0,b 	bit 0,c 	bit 0,d 	bit 0,e 	bit 0,h 	bit 0,l 	bit 0,(hl) 	bit 0,a 	bit 1,b 	bit 1,c 	bit 1,d 	bit 1,e 	bit 1,h 	bit 1,l 	bit 1,(hl) 	bit 1,a\n"
		+ "bit 2,b 	bit 2,c 	bit 2,d 	bit 2,e 	bit 2,h 	bit 2,l 	bit 2,(hl) 	bit 2,a 	bit 3,b 	bit 3,c 	bit 3,d 	bit 3,e 	bit 3,h 	bit 3,l 	bit 3,(hl) 	bit 3,a\n"
		+ "bit 4,b 	bit 4,c 	bit 4,d 	bit 4,e 	bit 4,h 	bit 4,l 	bit 4,(hl) 	bit 4,a 	bit 5,b 	bit 5,c 	bit 5,d 	bit 5,e 	bit 5,h 	bit 5,l 	bit 5,(hl) 	bit 5,a\n"
		+ "bit 6,b 	bit 6,c 	bit 6,d 	bit 6,e 	bit 6,h 	bit 6,l 	bit 6,(hl) 	bit 6,a 	bit 7,b 	bit 7,c 	bit 7,d 	bit 7,e 	bit 7,h 	bit 7,l 	bit 7,(hl) 	bit 7,a\n"
		+ "res 0,b 	res 0,c 	res 0,d 	res 0,e 	res 0,h 	res 0,l 	res 0,(hl) 	res 0,a 	res 1,b 	res 1,c 	res 1,d 	res 1,e 	res 1,h 	res 1,l 	res 1,(hl) 	res 1,a\n"
		+ "res 2,b 	res 2,c 	res 2,d 	res 2,e 	res 2,h 	res 2,l 	res 2,(hl) 	res 2,a 	res 3,b 	res 3,c 	res 3,d 	res 3,e 	res 3,h 	res 3,l 	res 3,(hl) 	res 3,a\n"
		+ "res 4,b 	res 4,c 	res 4,d 	res 4,e 	res 4,h 	res 4,l 	res 4,(hl) 	res 4,a 	res 5,b 	res 5,c 	res 5,d 	res 5,e 	res 5,h 	res 5,l 	res 5,(hl) 	res 5,a\n"
		+ "res 6,b 	res 6,c 	res 6,d 	res 6,e 	res 6,h 	res 6,l 	res 6,(hl) 	res 6,a 	res 7,b 	res 7,c 	res 7,d 	res 7,e 	res 7,h 	res 7,l 	res 7,(hl) 	res 7,a\n"
		+ "set 0,b 	set 0,c 	set 0,d 	set 0,e 	set 0,h 	set 0,l 	set 0,(hl) 	set 0,a 	set 1,b 	set 1,c 	set 1,d 	set 1,e 	set 1,h 	set 1,l 	set 1,(hl) 	set 1,a\n"
		+ "set 2,b 	set 2,c 	set 2,d 	set 2,e 	set 2,h 	set 2,l 	set 2,(hl) 	set 2,a 	set 3,b 	set 3,c 	set 3,d 	set 3,e 	set 3,h 	set 3,l 	set 3,(hl) 	set 3,a\n"
		+ "set 4,b 	set 4,c 	set 4,d 	set 4,e 	set 4,h 	set 4,l 	set 4,(hl) 	set 4,a 	set 5,b 	set 5,c 	set 5,d 	set 5,e 	set 5,h 	set 5,l 	set 5,(hl) 	set 5,a\n"
		+ "set 6,b 	set 6,c 	set 6,d 	set 6,e 	set 6,h 	set 6,l 	set 6,(hl) 	set 6,a 	set 7,b 	set 7,c 	set 7,d 	set 7,e 	set 7,h 	set 7,l 	set 7,(hl) 	set 7,a";
}