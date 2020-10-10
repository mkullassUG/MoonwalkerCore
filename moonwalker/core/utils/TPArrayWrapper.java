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

package moonwalker.core.utils;

public class TPArrayWrapper extends PackableArrayWrapper
{
private IntRangeSet rs;
	
	public TPArrayWrapper(byte[] src)
	{
		super(src);
		rs = IntRangeSet.EMPTY;
	}
	
	public byte[] getBlock(int ind, int len)
	{
		rs = rs.union(new IntRangeSet(ind, ind + len));
		return super.getBlock(ind, len);
	}
	public byte getByte(int ind)
	{
		rs = rs.union(new IntRangeSet(ind, ind + 1));
		return super.getByte(ind);
	}
	public short getShort(int ind)
	{
		rs = rs.union(new IntRangeSet(ind, ind + 2));
		return super.getShort(ind);
	}
	public int getInt(int ind)
	{
		rs = rs.union(new IntRangeSet(ind, ind + 4));
		return super.getInt(ind);
	}
	
	public void put(int ind, byte[] arr)
	{
		rs = rs.union(new IntRangeSet(ind, ind + arr.length));
		super.put(ind, arr);
	}
	public void putByte(int ind, byte val)
	{
		rs = rs.union(new IntRangeSet(ind, ind + 1));
		super.putByte(ind, val);
	}
	public void putShort(int ind, short val)
	{
		rs = rs.union(new IntRangeSet(ind, ind + 2));
		super.putShort(ind, val);
	}
	public void putInt(int ind, int val)
	{
		rs = rs.union(new IntRangeSet(ind, ind + 4));
		super.putInt(ind, val);
	}
	
	public byte[] getSource()
	{
		return super.getSource();
	}
	public IntRangeSet getRangeSet()
	{
		return rs;
	}
}
