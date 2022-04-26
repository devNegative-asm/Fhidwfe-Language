package interpreter;

import static types.DataType.*;

import java.io.File;

import types.DataType;

public class Value implements Comparable<Value>{
	/*
	 * Just for fun, let's pretend this is on a 24 bit processor 
	 * floats will lose the last 8 bits of mantissa
	 */
	public final DataType type;
	private String functionNameString = "null";
	public String getFunctionNameString() {
		return functionNameString;
	}
	
	public static Value createException(String exceptionName) {
		throw new RuntimeException(exceptionName);
	}
	private int value;
	public static final Value VOID = new Value(DataType.Void,0,null);
	public static final Value SYNTAX = new Value(DataType.SYNTAX,0,null);
	public static final Value TRUE(Ram ram) {return new Value(DataType.Bool,0xff,ram);}
	public static final Value FALSE(Ram ram) {return new Value(DataType.Bool,0x00,ram);}
	public static final Value TRUE_FLAG(Ram ram) {return new Value(DataType.Flag,0xff,ram);}
	public static final Value FALSE_FLAG(Ram ram) {return new Value(DataType.Flag,0x00,ram);}
	
	public Value(String functionName, Ram ram) {
		this(DataType.Func,0,ram);
		this.functionNameString = functionName;
		value = ram.functionNameToPointer(functionName);
	}
	
	@Override
	public String toString() {
		String result = type.<String>SWITCH()
			.CASE(Bool, () -> value==0?"false":"true")
			.CASE(Byte, String.valueOf(this.signedByteValue()))
			.CASE(File, () -> {
				File f = ram.valueToFile(this);
				if (f==null)
					return "<null file>";
				else
					return "<file @ "+f.getAbsolutePath()+">";})
			.CASE(Flag, value==0?"reset":"set")
			
			
			.CASE(Float, String.valueOf(java.lang.Float.intBitsToFloat(value<<8)))
			.CASE(Func, () ->  "<func "+this.functionNameString+" (x) -> z>")
			.CASE(Int, String.valueOf(this.signedIntValue()))
			.MULTI_CASE(new DataType[]{
				Listbyte,
				Listfile,
				Listfloat,
				Listfunc,
				Listint,
				Listop,
				Listptr,
				Listubyte,
				Listuint
			}, () -> {
				Value size = dereference(DataType.Uint,-DataType.Uint.getSize(Eval.replSettings));
				int len = size.value;
				String res = "[";
				for(int i=0;i<len/type.assignable().getSize(Eval.replSettings);i++) {
					Value element = dereference(type.assignable(),type.assignable().getSize(Eval.replSettings)*i);
					res+=element.toString();
					res+=" ";
				}
				res+="]";
				return res;
			})
			.CASE(Op, () -> "<func "+this.functionNameString+" (x,y) -> z>")
			.CASE(Ptr, () -> "<@0x"+Integer.toHexString(value)+">")
			.CASE(DataType.SYNTAX, "")
			.MULTI_CASE(new DataType[]{
					Ubrange,
					Brange,
					Urange,
					Range,
					Ptrrange,
					Frange,
			}, () -> {
				String res = "";
				Value low = dereference(type.assignable());
				Value high = dereference(type.assignable(),type.assignable().getSize(Eval.replSettings));
				Value flags = dereference(DataType.Byte,2*type.assignable().getSize(Eval.replSettings));
				if((flags.value&1) != 0) {
					//inclusive low
					res = "[";
				} else {
					res = "(";
				}
				res+=low;
				res+=", ";
				res+=high;
				if((flags.value&2) != 0) {
					res+="]";
				} else {
					res+=")";
				}
				return res;
			})
			.CASE(Ubyte, String.valueOf(this.unsignedByteValue()))
			.CASE(Uint, String.valueOf(this.unsignedIntValue()))
			.CASE(Void, "{{{void}}}")
			.DEFAULT(()->{
				String rr = "type "+type.name()+" {\n";
				for(String field:type.getFieldNames()) {
					if(this.getField(field).type==this.type) {
						rr+=field+": <type "+this.type+" @ "+Integer.toHexString(this.getField(field).unsignedIntValue())+">";
					} else {
						rr+=field+":"+this.getField(field).toString()+"\n";
					}
				}
				return rr+"}";
				}).get();
		
		return result;
	}
	private Value getField(String field) {
		return dereference(this.type.typeOfField(field, ""),this.type.getFieldOffset(field, Eval.replSettings));
	}

	public Value dereference(DataType readType, int offset) {
		return new Value(this.type, value+offset, ram).dereference(readType);
	}
	public Value dereference(DataType readType) {
		int val = 0;
		int multiple = 1;
		int totalDeref = readType.getSize(Eval.replSettings);
		for(int i=0;i<totalDeref;i++) {
			val += multiple* java.lang.Byte.toUnsignedInt(ram.access(this.value+i));
			multiple = multiple<<8;
		}
		if(readType==DataType.Func || readType==DataType.Op) {
			return new Value(ram.functionPointerToName(val),ram);
		} else {
			return new Value(readType, val, ram);
		}
	}
	public void useSelfAsPointerToWriteArgToRAM(Value v) {
		int writes = v.type.getSize(Eval.replSettings);
		int writeValue = v.unsignedIntValue();
		for(int i=0;i<writes;i++) {
			ram.write(this.value+i,(byte) writeValue);
			writeValue>>>=8;
		}
	}
	private final Ram ram;
	public Value(DataType type, int data, Ram context) {
		this.type = type;
		value = data & 0xffffff;
		if(type.getSize(Eval.replSettings)==1)
			value = data&0xff;
		this.ram=context;
	}
	public Value(float data, Ram context) {
		this(DataType.Float, java.lang.Float.floatToRawIntBits(data)>>>8, context);
	}
	public int getNative() {
		if(type==DataType.Byte)
			return this.signedByteValue();
		if(type==DataType.Int)
			return this.signedIntValue();
		if(type==DataType.Uint)
			return this.unsignedIntValue();
		if(type==DataType.Ubyte)
			return this.unsignedByteValue();
		if(type==DataType.Ptr)
			return this.unsignedIntValue();
		throw new RuntimeException("cannot convert nonnumeric type to int "+type);
	}
	public int signedIntValue() {
		if((value&0x800000)==0) {
			return value;
		} else {
			return value |~ 0x7fffff;
		}
	}
	public int unsignedIntValue() {
		return value;
	}
	public int signedByteValue() {
		if((value&0x80)==0) {
			return value&0xff;
		} else {
			return value |~ 0x7f;
		}
	}
	public int unsignedByteValue() {
		return value&0xff;
	}
	public float toFloat() {
		return java.lang.Float.intBitsToFloat(value<<8);
	}
	public Value add(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(v.type==DataType.Float) {
			return new Value(toFloat() + v.toFloat(), ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,v.value+value,ram);
	}
	public Value and(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(type==DataType.Bool) {
			int and = v.value&value;
			if(and==0)
				return FALSE(ram);
			return TRUE(ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,v.value&value,ram);
	}
	public Value or(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(type==DataType.Bool) {
			int and = v.value|value;
			if(and==0)
				return FALSE(ram);
			return TRUE(ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,v.value|value,ram);
	}
	public Value xor(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(type==DataType.Bool) {
			int and = v.value^value;
			if(and==0)
				return FALSE(ram);
			return TRUE(ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,v.value^value,ram);
	}
	public Value sub(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(v.type==DataType.Float) {
			return new Value(toFloat() - v.toFloat(), ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,value-v.value,ram);
	}
	public Value equalTo(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		return new Value(DataType.Bool,v.value==value?0xff:0,ram);
	}
	public Value mul(int n) {
		return mul(new Value(this.type,Eval.replSettings.intsize,ram));
	}
	public Value mul(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(v.type==DataType.Float) {
			return new Value(toFloat() * v.toFloat(), ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,v.value*value,ram);
	}
	public Value div(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(v.type==DataType.Float) {
			return new Value(toFloat() / v.toFloat(), ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		if(v.value==0)
			return this;
		if(type.signed()) {
			return new Value(this.type,this.signedIntValue()/v.signedIntValue(),ram);
		} else {
			return new Value(this.type,this.unsignedIntValue()/v.unsignedIntValue(),ram);
		}
	}
	public Value modulo(Value v) {
		if(v.type!=this.type) {
			return Value.createException("mismaching types "+this.type+" and "+v.type);
		}
		if(v.type==DataType.Float) {
			return new Value(fmod(toFloat() , v.toFloat()), ram);
		}
		if(!v.type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		if(v.value==0)
			return new Value(this.type,0,ram);
		if(type.signed()) {
			return new Value(this.type,imod(this.signedIntValue(),v.signedIntValue()),ram);
		} else {
			return new Value(this.type,imod(this.unsignedIntValue(),v.unsignedIntValue()),ram);
		}
	}
	private float fmod(float a, float b) {
		return ((a%b)+b)%b;
	}
	private int imod(int a, int b) {
		return ((a%b)+b)%b;
	}
	public Value negate() {
		if(type==DataType.Float) {
			return new Value(-toFloat(), ram);
		}
		if(!type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,-value,ram);
	}
	public Value complement() {
		if(DataType.Bool==type)
			if(this.equals(TRUE(ram))) {
				return FALSE(ram);
			} else {
				return TRUE(ram);
			}
		if(!type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,~value,ram);
	}
	public Value shiftLeft() {
		if(!type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		return new Value(this.type,value<<1,ram);
	}
	public Value shiftRight() {
		if(!type.numeric())
			return Value.createException("type "+this.type+" not numeric");
		if(type.signed())
			return new Value(this.type,this.signedIntValue()>>1,ram);
		else
			return new Value(this.type,this.unsignedIntValue()>>>1,ram);
	}
	public int floatValue() {
		if(type==DataType.Float) {
			return (int) java.lang.Float.intBitsToFloat(value<<8);
		} else {
			throw new RuntimeException("cannot interpret "+type+" as float");
		}
	}
	@Override
	public int compareTo(Value other) {
		if(type.numeric())
			return getNative()-other.getNative();
		if(type==DataType.Float) {
			return java.lang.Float.compare(this.toFloat(), other.toFloat());
		}
		throw new RuntimeException("cannot compare non numeric types");
	}
	public static Value resolveConstant(String varname, Ram ram) {
		switch(varname) {
		case "int_size":
			return new Value(DataType.Uint,3,ram);
		}
		throw new UnsupportedOperationException("constants in repl not supported yet");
	}
	@Override
	public boolean equals(Object other) {
		if(other==null || !(other instanceof Value)) {
			return false;
		}
		Value oth = (Value)other;
		return oth.value==value && oth.type==type;
	}
	public boolean isTruthy() {
		return value!=0;
	}
	public boolean isFalsy() {
		return value==0;
	}
	public Value add(int i) {
		return new Value(type,value+i,ram);
	}
	public String derefAsString() {
		StringBuilder builder = new StringBuilder();
		int offset = 0;
		while(this.dereference(DataType.Byte,offset).signedByteValue()!=0) {
			builder.append((char)(byte)this.dereference(DataType.Byte,offset).signedByteValue());
			offset++;
		}
		return builder.toString();
	}
}
