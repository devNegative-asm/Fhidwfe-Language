package compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import settings.CompilationSettings;
/**
 * A type recognized by the compiler
 *
 */
public enum DataType{
	Flag(1,false,null,false),//flags and bools act similar, but flags have fewer uses and are only accessible with set and reset, or as the direct argument to a loop
	Bool(1,false,null,false),
	Byte(1,false,null,false),
	Int(2,false,null,false),
	Float(2,false,null,false),
	Uint(2,false,null,false),
	Ubyte(1,false,null,false),
	Ptr(2,false,null,false),
	Void(0,false,null,false),
	Relptr(1,false,null,false),
	File(1,false,null,false),
	Func(2,false,null,false),
	Op(2,false,null,false),
	
	Listbyte(2,false,Byte,true),
	Listint(2,false,Int,true),
	Listubyte(2,false,Ubyte,true),
	Listuint(2,false,Uint,true),
	Listfloat(2,false,Float,true),
	Listptr(2,false,Ptr,true),
	Listfile(2,false,File,true),
	Listfunc(2,false,Func,true),
	Listop(2,false,Op,true),
	
	Range(2,true,Int,false),//int range
	Urange(2,true,Uint,false),//unsigned int range
	Brange(2,true,Byte,false),//byte ranges
	Ubrange(2,true,Ubyte,false),//unsigned byte range
	Frange(2,true,Uint,false),//float range
	Ptrrange(2,true,Ptr,false),
	
	SYNTAX(0,false,null,false);
	
	
	
	final int size;
	private final boolean range;
	private final DataType assignable;
	
	public final boolean isList;
	
	
	private DataType(int siz, boolean Range, DataType assignable, boolean list)
	{
		size=siz;
		this.range=Range;
		this.assignable=assignable;
		isList = list;
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
}