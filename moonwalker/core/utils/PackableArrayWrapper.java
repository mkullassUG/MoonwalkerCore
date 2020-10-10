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

import java.util.Arrays;

class PackableArrayWrapper extends ArrayWrapper
{
	private byte[] src;
	private int maxAddr;
	
	public PackableArrayWrapper(byte[] src)
	{
		super(src);
		this.src = src;
		maxAddr = 0;
	}
	
	public byte[] getBlock(int ind, int len)
	{
		if (ind + len - 1 > maxAddr)
			maxAddr = ind + len - 1;
		return super.getBlock(ind, len);
	}
	public byte getByte(int ind)
	{
		if (ind > maxAddr)
			maxAddr = ind;
		return super.getByte(ind);
	}
	public short getShort(int ind)
	{
		if (ind + 1 > maxAddr)
			maxAddr = ind + 1;
		return super.getShort(ind);
	}
	public int getInt(int ind)
	{
		if (ind + 3 > maxAddr)
			maxAddr = ind + 3;
		return super.getInt(ind);
	}
	
	public void put(int ind, byte[] arr)
	{
		if (ind + arr.length - 1 > maxAddr)
			maxAddr = ind + arr.length - 1;
		super.put(ind, arr);
	}
	public void putByte(int ind, byte val)
	{
		if (ind > maxAddr)
			maxAddr = ind;
		super.putByte(ind, val);
	}
	public void putShort(int ind, short val)
	{
		if (ind + 1 > maxAddr)
			maxAddr = ind + 1;
		super.putShort(ind, val);
	}
	public void putInt(int ind, int val)
	{
		if (ind + 3 > maxAddr)
			maxAddr = ind + 3;
		super.putInt(ind, val);
	}
	
	public byte[] pack()
	{
		return Arrays.copyOfRange(src, 0, maxAddr + 1);
	}
}