/*
    Copyright (C) 2020 Micha³ Kullass

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package moonwalker.core.structures;

import java.awt.Point;
import java.util.Arrays;
import java.util.Objects;

public class MDirectObject implements MoonwalkerObject, Cloneable
{
	//TODO move to metadata
	public static final int REGION_WIDTH = 320;
	public static final int REGION_HEIGHT = 224;
	
	private int regionX;
	private int regionY;
	private int offsetX;
	private int offsetY;
	
	private int allocationAddress;
	private short type;
	private byte[] data;
	
	private Container container;
	
	public MDirectObject(int regionX, int relativeX, int regionY, int relativeY, int addr, short type, byte[] data, Container con)
	{
		if (relativeX < 0)
			throw new IllegalArgumentException("relativeX < 0");
		else if (relativeX > REGION_WIDTH)
			throw new IllegalArgumentException("relativeX > Region width: " + relativeX + ", " + REGION_WIDTH);
		if (relativeY < 0)
			throw new IllegalArgumentException("relativeY < 0");
		else if (relativeY > REGION_HEIGHT)
			throw new IllegalArgumentException("relativeY > Region height: " + relativeY + ", " + REGION_HEIGHT);
		if (relativeX < 0)
			throw new IllegalArgumentException("regionX < 0");
		if (relativeY < 0)
			throw new IllegalArgumentException("regionY < 0");
		Objects.requireNonNull(con);
		
		this.regionX = regionX;
		this.regionY = regionY;
		offsetX = relativeX;
		offsetY = relativeY;
		allocationAddress = addr;
		this.type = type;
		this.data = Arrays.copyOf(data, data.length);
		this.container = con;
	}
	public MDirectObject(int absoluteX, int absoluteY, int addr, short type, byte[] data, Container con)
	{		
		this(absoluteX / REGION_WIDTH, absoluteX % REGION_WIDTH,
				absoluteY / REGION_HEIGHT, absoluteY % REGION_HEIGHT,
				addr, type, data, con);
	}
	
	public int getRegionX()
	{
		return regionX;
	}
	public int getRegionY()
	{
		return regionY;
	}
	public int getRelativeX()
	{
		return offsetX;
	}
	public int getRelativeY()
	{
		return offsetY;
	}
	public int getAbsoluteX()
	{
		return REGION_WIDTH * regionX + offsetX;
	}
	public int getAbsoluteY()
	{
		return REGION_HEIGHT * regionY + offsetY;
	}
	public Point getAbsolutePosition()
	{
		return new Point(getAbsoluteX(), getAbsoluteY());
	}
	
	public int getAllocationAddress()
	{
		return allocationAddress;
	}
	public short getType()
	{
		return type;
	}
	public byte[] getData()
	{
		if (data == null)
			return null;
		return Arrays.copyOf(data, data.length);
	}
	public int getDataLength()
	{
		if (data == null)
			return -1;
		return data.length;
	}
	public Container getContainer()
	{
		return container;
	}
	
	public void setRegionX(int regionX)
	{
		if (regionX < 0)
			throw new IllegalArgumentException("regionX < 0");
		this.regionX = regionX;
	}
	public void setRegionY(int regionY)
	{
		if (regionY < 0)
			throw new IllegalArgumentException("regionY < 0");
		this.regionY = regionY;
	}
	public void setRelativeX(int x)
	{
		if (x < 0)
			throw new IllegalArgumentException("x < 0");
		if (x > REGION_WIDTH)
			throw new IllegalArgumentException("x > Region width: " + x + ", " + REGION_WIDTH);
		this.offsetX = x;
	}
	public void setRelativeY(int y)
	{
		if (y < 0)
			throw new IllegalArgumentException("y < 0");
		if (y > REGION_HEIGHT)
			throw new IllegalArgumentException("y > Region height" + y + ", " + REGION_HEIGHT);
		this.offsetY = y;
	}
	public void setAbsouteX(int x)
	{
		if (x < 0)
			throw new IllegalArgumentException("x < 0");
		regionX = x / REGION_WIDTH;
		offsetX = x % REGION_WIDTH;
	}
	public void setAbsouteY(int y)
	{
		if (y < 0)
			throw new IllegalArgumentException("y < 0");
		regionY = y / REGION_HEIGHT;
		offsetY = y % REGION_HEIGHT;
	}
	public void setAbsolutePosition(int x, int y)
	{
		if (x < 0)
			throw new IllegalArgumentException("x < 0");
		if (y < 0)
			throw new IllegalArgumentException("y < 0");
		regionX = x / REGION_WIDTH;
		offsetX = x % REGION_WIDTH;
		regionY = y / REGION_HEIGHT;
		offsetY = y % REGION_HEIGHT;
	}
	public void setAbsolutePosition(Point p)
	{
		setAbsolutePosition(p.x, p.y);
	}
	public void setAllocationAddress(int allocationAddress)
	{
		this.allocationAddress = allocationAddress;
	}
	public void setType(short type)
	{
		this.type = type;
	}
	public void setData(byte[] data)
	{
		this.data = Arrays.copyOf(data, data.length);
	}
	public void setContainer(Container c)
	{
		if (c == null)
			throw new NullPointerException();
		container = c;
	}
	
	public boolean equalsIgnoreContainer(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MDirectObject other = (MDirectObject) obj;
		if (allocationAddress != other.allocationAddress)
			return false;
		if (!Arrays.equals(data, other.data))
			return false;
		if ((offsetX % REGION_WIDTH) != (other.offsetX % REGION_WIDTH))
			return false;
		if ((offsetY % REGION_HEIGHT) != (other.offsetY % REGION_HEIGHT))
			return false;
		if ((regionX + (offsetX / REGION_WIDTH)) != (other.regionX + (other.offsetX / REGION_WIDTH)))
			return false;
		if ((regionY + (offsetY / REGION_HEIGHT)) != (other.regionY + (other.offsetY / REGION_HEIGHT)))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return equalsIgnoreContainer(obj) && (container == ((MDirectObject) obj).container);
	}
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + allocationAddress;
		result = prime * result + ((container == null) ? 0 : container.hashCode());
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + offsetX;
		result = prime * result + offsetY;
		result = prime * result + regionX;
		result = prime * result + regionY;
		result = prime * result + type;
		return result;
	}
	@Override
	public MDirectObject clone()
	{
		return new MDirectObject(regionX, offsetX, regionY, offsetY, allocationAddress, type, data, container);
	}
	
	@Override
	public String toString()
	{
		String ret = "[MoonwalkerObject: ";
		ret += "Type = 0x" + Integer.toHexString(0xFFFF & type);
		ret += ", Position = [Abs: ("
				+ getAbsoluteX() + ", " + getAbsoluteY()
				+ "), Reg: " + "(" + getRegionX() + ", " + getRegionY() + ")"
				+ "), Rel: " + "(" + getRelativeX() + ", " + getRelativeY() + ")"
				+ "]";
		ret += ", Alloc. address = " + Integer.toHexString(allocationAddress);
		ret += ", Container = " + container;
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
	
	public MPassiveObject toPassive()
	{
		return new MPassiveObject(type, data);
	}

	public enum Container
	{
		REGION_TABLE
		{
			@Override
			public String toString()
			{
				return "Region table";
			}
		},
		INITIAL_TABLE
		{
			@Override
			public String toString()
			{
				return "Initial table";
			}
		},
		ALL_TABLES
		{
			@Override
			public String toString()
			{
				return "All tables";
			}
		}
	}
}
