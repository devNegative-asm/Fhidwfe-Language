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
	Flag(1,false,null,false,false,false),//flags and bools act similar, but flags have fewer uses and are only accessible with set and reset, or as the direct argument to a loop
	Bool(1,false,null,false,false,false),
	Byte(1,false,null,false,false,false),
	Int(2,false,null,false,false,false),
	Float(2,false,null,false,false,false),
	Uint(2,false,null,false,false,false),
	Ubyte(1,false,null,false,false,false),
	Ptr(2,false,null,false,false,false),
	Void(0,false,null,false,false,false),
	Relptr(1,false,null,false,false,false),
	File(1,false,null,false,false,false),
	Func(2,false,null,false,false,false),
	
	Listbyte(2,false,Byte,false,false,true),
	Listint(2,false,Int,false,false,true),
	Listubyte(2,false,Ubyte,false,false,true),
	Listuint(2,false,Uint,false,false,true),
	Listfloat(2,false,Float,false,false,true),
	Listptr(2,false,Ptr,false,false,true),
	Listfile(2,false,File,false,false,true),
	Listfunc(2,false,Func,false,false,true),
	
	Rangecc(2,true,Int,true,true,false),//int ranges
	Rangeco(2,true,Int,true,false,false),
	Rangeoc(2,true,Int,false,true,false),
	Rangeoo(2,true,Int,false,false,false),
	
	Urangecc(2,true,Uint,true,true,false),//unsigned int ranges
	Urangeco(2,true,Uint,true,false,false),
	Urangeoc(2,true,Uint,false,true,false),
	Urangeoo(2,true,Uint,false,false,false),
	
	Brangecc(2,true,Byte,true,true,false),//byte ranges
	Brangeco(2,true,Byte,true,false,false),
	Brangeoc(2,true,Byte,false,true,false),
	Brangeoo(2,true,Byte,false,false,false),
	
	Ubrangecc(2,true,Ubyte,true,true,false),//unsigned byte ranges
	Ubrangeco(2,true,Ubyte,true,false,false),
	Ubrangeoc(2,true,Ubyte,false,true,false),
	Ubrangeoo(2,true,Ubyte,false,false,false),
	
	Frangecc(2,true,Uint,true,true,false),//float ranges
	Frangeco(2,true,Uint,true,false,false),
	Frangeoc(2,true,Uint,false,true,false),
	Frangeoo(2,true,Uint,false,false,false),
	
	Ptrrangecc(2,true,Ptr,true,true,false),
	Ptrrangeco(2,true,Ptr,true,false,false),
	Ptrrangeoc(2,true,Ptr,false,true,false),
	Ptrrangeoo(2,true,Ptr,false,false,false),
	
	SYNTAX(0,false,null,false,false,false);
	
	
	
	final int size;
	private final boolean range;
	private final DataType assignable;
	
	
	public final boolean closedLow;
	public final boolean closedHigh;
	public final boolean isList;
	
	
	private DataType(int siz, boolean Range, DataType assignable, boolean cllow, boolean clhigh, boolean list)
	{
		size=siz;
		this.range=Range;
		this.assignable=assignable;
		isList = list;
		closedLow = cllow;
		closedHigh=clhigh;
	}
	private static HashMap<DataType,ArrayList<DataType>> implicitlyConvertible = new HashMap<>();
	private static HashSet<DataType> freeable = new HashSet<>();
	public boolean isFreeable() {
		return freeable.contains(this);
	}
	static {
		freeable.addAll(asList(
				Rangecc,
				Rangeco,
				Rangeoc,
				Rangeoo,
				Urangecc,
				Urangeco,
				Urangeoc,
				Urangeoo,
				Brangecc,
				Brangeco,
				Brangeoc,
				Brangeoo,
				Ubrangecc,
				Ubrangeco,
				Ubrangeoc,
				Ubrangeoo,
				Frangecc,
				Frangeco,
				Frangeoc,
				Frangeoo,
				Ptrrangecc,
				Ptrrangeco,
				Ptrrangeoc,
				Ptrrangeoo,
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
				Rangecc,
				Rangeco,
				Rangeoc,
				Rangeoo,
				Urangecc,
				Urangeco,
				Urangeoc,
				Urangeoo,
				Brangecc,
				Brangeco,
				Brangeoc,
				Brangeoo,
				Ubrangecc,
				Ubrangeco,
				Ubrangeoc,
				Ubrangeoo,
				Frangecc,
				Frangeco,
				Frangeoc,
				Frangeoo,
				Ptrrangecc,
				Ptrrangeco,
				Ptrrangeoc,
				Ptrrangeoo,
				Listint,
				Listuint,
				Listbyte,
				Listubyte,
				Listptr,
				Listfile,
				Listfloat,
				Uint,
				Func,
				Listfunc
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