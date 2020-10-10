package moonwalker.core.structures;

import java.awt.Point;
import java.util.Arrays;

public class MPassiveObject implements MoonwalkerObject
{
	private short type;
	private byte[] data;
	
	public MPassiveObject(short type, byte[] data)
	{
		this.type = type;
		this.data = Arrays.copyOf(data, data.length);
	}
	
	public void setType(short type)
	{
		this.type = type;
	}
	public void setData(byte[] data)
	{
		this.data = Arrays.copyOf(data, data.length);
	}
	
	@Override
	public short getType()
	{
		return type;
	}
	@Override
	public byte[] getData()
	{
		if (data == null)
			return null;
		return Arrays.copyOf(data, data.length);
	}
	@Override
	public int getDataLength()
	{
		if (data == null)
			return -1;
		return data.length;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MPassiveObject other = (MPassiveObject) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + type;
		return result;
	}
	
	@Override
	public String toString()
	{
		String ret = "[MoonwalkerPassiveObject: ";
		ret += "Type = 0x" + Integer.toHexString(0xFFFF & type);
		ret += ", Data = " + hexArr(data);
		return ret + "]";
	}
	private String hexArr(byte[] src)
	{
		StringBuilder sb = new StringBuilder(src.length * 4);
		sb.append("[");
		for (byte b: src)
		{
			String s = Integer.toHexString(0xFF & b);
			if (s.length() < 2)
				sb.append("0");
			sb.append(s);
			sb.append(", ");
		}
		int l = sb.length();
		if (l > 1)
			sb.setLength(l - 2);
		sb.append("]");
		return sb.toString();
	}

	public MDirectObject toDirect(int absoluteX, int absoluteY, int addr, MDirectObject.Container con)
	{
		return new MDirectObject(absoluteX, absoluteY, addr, type, data, con);
	}
	public MDirectObject toDirect(Point absolutePos, int addr, MDirectObject.Container con)
	{
		return new MDirectObject(absolutePos.x, absolutePos.y, addr, type, data, con);
	}
}
