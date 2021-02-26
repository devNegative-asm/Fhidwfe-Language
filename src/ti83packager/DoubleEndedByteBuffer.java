package ti83packager;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
public class DoubleEndedByteBuffer{
	private ArrayList<Byte> frontEnd = new ArrayList<>();
	private ArrayList<Byte> backEnd = new ArrayList<>();

	private ArrayList<Boolean> frontFilled = new ArrayList<>();
	private final int base;
	public DoubleEndedByteBuffer(int baseIndex)
	{
		base=baseIndex;
	}
	public DoubleEndedByteBuffer(int baseIndex, byte[] data)
	{
		append(data);
		base=baseIndex;
	}
	public void append(byte[] bytes)
	{
		for(byte a:bytes)
		{
			append(a);
		}
	}
	
	public void appendLittleEndian(short s)
	{
		byte high = (byte) (Short.toUnsignedInt(s)/256);
		byte low = (byte) (Short.toUnsignedInt(s)%256);
		append(low);
		append(high);
	}
	public void appendBigEndian(short s)
	{
		byte high = (byte) (Short.toUnsignedInt(s)/256);
		byte low = (byte) (Short.toUnsignedInt(s)%256);
		append(high);
		append(low);
	}
	public void prependLittleEndian(short s)
	{
		byte high = (byte) (Short.toUnsignedInt(s)/256);
		byte low = (byte) (Short.toUnsignedInt(s)%256);
		prepend(high);
		prepend(low);
	}
	public void prependBigEndian(short s)
	{
		byte high = (byte) (Short.toUnsignedInt(s)/256);
		byte low = (byte) (Short.toUnsignedInt(s)%256);
		prepend(low);
		prepend(high);
	}
	
	boolean warning=false;
	public void put(int index, byte b)
	{
		index-=base;
		if(index<0)
			throw new ArrayIndexOutOfBoundsException("\t0x"+Integer.toHexString(index+base));
		if(index>frontEnd.size()-1)
		{
			frontEnd.ensureCapacity(index);
			for(byte a:new byte[index-frontEnd.size()])
			{
				frontEnd.add(a);
				frontFilled.add(false);
			}
			append(b);
			return;
		}
		if(frontFilled.get(index))
		{
			throw new RuntimeException("Trying to different data to same location: 0x"+Integer.toHexString(index+base)+"\n Try instead 0x"+Integer.toHexString(base+frontEnd.size()));
		} else {
			frontEnd.set(index, b);
			frontFilled.set(index, true);
		}
		
	}
	public void put(int index, byte[] b)
	{
		for(int sub=0;sub<b.length;sub++)
		{
			put(sub+index,b[sub]);
		}
	}
	public void append(String s)
	{
		this.append(s.getBytes());
	}
	public void prepend(String s)
	{
		this.prepend(s.getBytes());
	}
	public void append(byte b)
	{
		frontEnd.add(b);
		frontFilled.add(true);
	}
	public void prepend(byte b)
	{
		backEnd.add(b);
	}
	public void prepend(byte[] bytes)
	{
		for(int a=bytes.length;a>0;--a)
		{
			prepend(bytes[a-1]);
		}
	}
	public int size()
	{
		return frontEnd.size()+backEnd.size();
	}
	public int checkSum()
	{
		int i=0;
		for(byte b:getImmutable())
		{
			i+=Byte.toUnsignedInt(b);
		}
		return i;
	}
	public byte[] getImmutable()
	{
		byte[] data = new byte[size()];
		int i=0;
		ArrayList<Byte> peel;
		for(peel=backEnd;i<peel.size();i++)
		{
			data[i]=peel.get(peel.size()-i-1);
		}
		for(peel=frontEnd;i<size();i++)
		{
			data[i]=peel.get(i-backEnd.size());
		}
		return data;
	}
	public void append(DoubleEndedByteBuffer b)
	{
		this.append(b.getImmutable());
	}
	public void writeSelf(OutputStream s)
	{
		try {
			s.write(getImmutable());
			s.flush();
		} catch (IOException e) {
			throw new RuntimeException("Could not write to output stream");
		}
	}
	public void writeHexWithFront(OutputStream s)
	{
		try {
			int i=0;
			for(byte b:frontEnd)
			{
				s.write((Integer.toHexString(base+i++)+": "+Integer.toHexString(Byte.toUnsignedInt(b))+"\r\n").getBytes());
			}
			s.flush();
		} catch (IOException e) {
			throw new RuntimeException("Could not write to output stream");
		}
	}
}
