package types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import compiler.Parser;
import settings.CompilationSettings;
/**
 * A type recognized by the compiler
 *
 */
public class DataType{
	private static HashMap<String, DataType> namedTypes = new HashMap<>();
	public static final DataType Flag = new DataType("Flag",1,false,null,false);//flags and bools act similar, but flags have fewer uses and are only accessible with set and reset, or as the direct argument to a loop
	public static final DataType Bool = new DataType("Bool",1,false,null,false);
	public static final DataType Byte = new DataType("Byte",1,false,null,false);
	public static final DataType Int = new DataType("Int",2,false,null,false);
	public static final DataType Float = new DataType("Float",2,false,null,false);
	public static final DataType Uint = new DataType("Uint",2,false,null,false);
	public static final DataType Ubyte = new DataType("Ubyte",1,false,null,false);
	public static final DataType Ptr = new DataType("Ptr",2,false,null,false);
	public static final DataType Void = new DataType("Void",0,false,null,false);
	public static final DataType File = new DataType("File",2,false,null,false);
	public static final DataType Func = new DataType("Func",2,false,null,false);
	public static final DataType Op = new DataType("Op",2,false,null,false);
	
	public static final DataType Listbyte = new DataType("Listbyte",2,false,Byte,true);
	public static final DataType Listint = new DataType("Listint",2,false,Int,true);
	public static final DataType Listubyte = new DataType("Listubyte",2,false,Ubyte,true);
	public static final DataType Listuint = new DataType("Listuint",2,false,Uint,true);
	public static final DataType Listfloat = new DataType("Listfloat",2,false,Float,true);
	public static final DataType Listptr = new DataType("Listptr",2,false,Ptr,true);
	public static final DataType Listfile = new DataType("Listfile",2,false,File,true);
	public static final DataType Listfunc = new DataType("Listfunc",2,false,Func,true);
	public static final DataType Listop = new DataType("Listop",2,false,Op,true);
	
	public static final DataType Range = new DataType("Range",2,true,Int,false);//int range
	public static final DataType Urange = new DataType("Urange",2,true,Uint,false);//unsigned int range
	public static final DataType Brange = new DataType("Brange",2,true,Byte,false);//byte ranges
	public static final DataType Ubrange = new DataType("Ubrange",2,true,Ubyte,false);//unsigned byte range
	public static final DataType Frange = new DataType("Frange",2,true,Uint,false);//float range
	public static final DataType Ptrrange = new DataType("Ptrrange",2,true,Ptr,false);

	
	public static final DataType SYNTAX = new DataType("SYNTAX",0,false,null,false);
	private static HashSet<DataType> builtins = new HashSet<>();
	
	public final int size;
	private final boolean range;
	private final DataType assignable;
	
	public final boolean isList;
	private final String name;
	
	private ArrayList<Field> fields = new ArrayList<>();
	private class Field implements Comparable<Field>{
		private String name;
		private DataType type;
		public Field(String name, DataType type) {
			this.name = name;
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public DataType getType() {
			return type;
		}
		@Override
		public int compareTo(Field o) {
			return -type.size + o.type.size;
		}
	}
	
	public int getFieldOffset(String fieldName, CompilationSettings settings) {
		int offset = 0;
		for(Field f:fields) {
			if(f.getName().equals(fieldName)) {
				return offset;
			} else {
				offset+=f.getType().getSize(settings);
			}
		}
		throw new RuntimeException("Type "+this.name()+" does not have field "+fieldName);
	}
	
	public String[] getFieldNames() {
		fields.sort(Field::compareTo);
		String[] strings = new String[fields.size()];
		for(int i=0;i<fields.size();i++) {
			strings[i] = fields.get(i).name;
		}
		return strings;
	}
	
	public DataType typeOfField(String fieldName, String linenum) {
		for(Field f:fields) {
			if(f.getName().equals(fieldName)) {
				return f.getType();
			}
		}
		throw new RuntimeException("Type "+this.name()+" does not have field "+fieldName+" at line "+linenum);
	}
	
	
	@Override
	public String toString() {
		return name();
	}
	
	public void addField(String name, DataType type) {
		this.fields.forEach(field -> {
			if(field.name.equals(name))
				throw new RuntimeException("Cannot define multiple fields of same type in "+this.name);
		});
		Field newField = new Field(name, type);
		this.fields.add(newField);
	}
	
	public String getHeapSizeString(CompilationSettings sett, int elements) {
		if(this.isList) {
			return ""+(elements * this.assignable.getSize(sett));
		} else if(this.isRange()) {
			return ""+(2 * this.assignable.getSize(sett) + 1);
		} else {
			return "Fwf_internal_sizeof_"+this.name;
		}
	}
	
	public String getHeapSizeString() {
		return "Fwf_internal_sizeof_"+this.name;
	}
	
	public int getActualHeapSize(CompilationSettings sett) {
		this.fields.sort(Field::compareTo);
		int totalSize = 0;
		for(Field f:fields) {
			totalSize+=f.getType().getSize(sett);
		}
		if(sett.target.needsAlignment) {
			int difference = totalSize%sett.intsize;
			if(difference==0)
				return totalSize;
			return totalSize-difference+sett.intsize;
			
		} else
			return totalSize;
	}
	
	private final boolean userType;
	public static DataType makeUserType(String name) {
		DataType.builtins.forEach(dt -> {
			if(dt.name().toLowerCase().equals(name.toLowerCase())) {
				throw new RuntimeException("name "+name+" may clash with builtin type");
			}
		});
		if(DataType.namedTypes.keySet().contains(name)) {
			return DataType.namedTypes.get(name);
		}
		DataType result = new DataType(name);
		implicitlyConvertible.put(result, new ArrayList<DataType>(Arrays.asList(Ptr)));
		return result;
	}
	private DataType(String name) {
		this(name, true);
	}
	private DataType(String name, boolean userMade) {
		this.name = name;
		size=2;
		this.range=false;
		this.assignable=null;
		isList = false;
		DataType.namedTypes.put(name, this);
		userType = userMade;
		if(userMade)
			freeable.add(this);
	}
	private DataType(String name, int siz, boolean Range, DataType assignable, boolean list)
	{
		this.name = name;
		size=siz;
		this.range=Range;
		this.assignable=assignable;
		isList = list;
		DataType.namedTypes.put(name, this);
		userType = false;
	}
	private static HashMap<DataType,ArrayList<DataType>> implicitlyConvertible = new HashMap<>();
	private static HashSet<DataType> freeable = new HashSet<>();
	public boolean isFreeable() {
		return freeable.contains(this);
	}
	static {
		freeable.addAll(asList(
				Range,
				Urange,
				Brange,
				Ubrange,
				Frange,
				Ptrrange,
				Listint,
				Listuint,
				Listbyte,
				Listubyte,
				Listptr,
				Listfile,
				Listfloat,
				Ptr,
				Listfunc

		));
		ArrayList<DataType> ptrtypes =asList(
				Range,
				Urange,
				Brange,
				Ubrange,
				Frange,
				Ptrrange,
				Listint,
				Listuint,
				Listbyte,
				Listubyte,
				Listptr,
				Listfile,
				Listfloat,
				Uint,
				Func,
				Listfunc,
				Op
		);
		implicitlyConvertible.put(Ptr,asList(Uint));
		for(DataType type:ptrtypes) {
			implicitlyConvertible.put(type, asList(Ptr,Uint));
		}
		/*
		 * Flag
			Bool
			Byte
			Int
			Float
			Uint
			Ubyte
			Ptr
			Void
			Relptr
			File
		 */
		implicitlyConvertible.put(Flag, asList(Bool));
		implicitlyConvertible.put(Bool, asList(Byte));
		implicitlyConvertible.put(Flag, asList(Byte));
		implicitlyConvertible.put(Int, asList(Ubyte,Ptr));
		ArrayList<DataType> uintTo = asList(Ptr,Int);
		uintTo.addAll(ptrtypes);
		uintTo.removeIf(x -> x.name().contains("list")||x.name().contains("List"));// don't convert uints to list
		implicitlyConvertible.put(Uint, uintTo);
	}
	/**
	 * Whether this data type can be implicitly casted to the given type
	 * @param x the type to cast to
	 * @return whether the cast can be done
	 */
	public String name() {
		return this.name;
	}
	public boolean canCastTo(DataType x) {
		if(implicitlyConvertible.containsKey(this)) {
			return implicitlyConvertible.get(this).contains(x) || this==x;
		} else
			return this==x;
	}
	// wraper for Arrays.asList
	private static ArrayList<DataType> asList(DataType...datas) {
		return new ArrayList<>(Arrays.asList(datas));
	}
	/**
	 * If this is a container type which contains data, this method returns the type of that data
	 * @return the type of the data inside this container, or null if this type is not a container type
	 */
	public DataType assignable() {
		return assignable;
	}
	/**
	 * Compiletime size of this type. The result of this can always be used for type checking, but may not represent the actual data size if alignment is turned on
	 * @param p the parser from which the int size setting is retrieved
	 * @return the size needed to represent this type
	 */
	public int getSize(Parser p)
	{
		if(size==1)
			return 1;
		if(size==0)
			return 0;
		return p.settings.intsize;
	}
	/**
	 * Compiletime size of this type. The result of this can always be used for type checking, but may not represent the actual data size if alignment is turned on
	 * @param p the settings from which the int size setting is retrieved
	 * @return the size needed to represent this type
	 */
	public int getSize(CompilationSettings p)
	{
		if(size==1)
			return 1;
		if(size==0)
			return 0;
		return p.intsize;
	}
	/**
	 * Returns whether or not the given datatype can be assigned from a member of this container type. <br>Returns false if this type is not a container type, or if the assignment cannot be made 
	 * @param x the type to attempt to assign to
	 * @return whether or not this type's elements have the given type
	 */
	public boolean assignable(DataType x)
	{
		return x==assignable;
	}
	/**
	 * @return Whether or not this is a range type
	 */
	public boolean isRange()
	{
		return range;
	}
	/**
	 * Retrieves the DataType enum which matches the given type as a lowercase string
	 * @param s the type name
	 * @return the type enum
	 */
	public static DataType fromLowerCase(String s)
	{
		return DataType.valueOf(Character.toUpperCase(s.charAt(0))+s.substring(1));
	}
	/**
	 * @return Whether or not this type can be considered signed
	 */
	public boolean signed() {
		return this==Int || this==Byte || this==Float;
	}
	
	public static DataType valueOf(String name) {
		if(Character.isLowerCase(name.charAt(0)))
			name = Character.toUpperCase(name.charAt(0))+name.substring(1);
		DataType type = namedTypes.get(name); 
		if(type == null)
			throw new IllegalArgumentException("Type by name of "+name.toLowerCase()+" not found");
		return type;
	}
	
	public static boolean typeExists(String name) {
		if(Character.isLowerCase(name.charAt(0))) {
			name = Character.toUpperCase(name.charAt(0))+name.substring(1);
			return namedTypes.containsKey(name) && !namedTypes.get(name).userType;
		} else {
			return namedTypes.containsKey(name);
		}
	}
	
	public static DataType[] values() {
		Collection<DataType> values = namedTypes.values();
		DataType[] vals = new DataType[values.size()];
		int i=0;
		for(DataType dt: values) {
			vals[i++] = dt;
		}
		return vals;
	}
	
	public boolean numeric() {
		return this==Int || this==Uint || this==Ptr || this==Byte || this == Ubyte;
	}
	
	public <T> TypeResolver<T> SWITCH() {
		return new TypeResolver<T>(this);
	}
	@Override
	public int hashCode() {
		return this.name().hashCode();
	}
	@Override
	public boolean equals(Object other) {
		return other==this;
	}
	public boolean builtin() {
		return !userType;
	}
	
}